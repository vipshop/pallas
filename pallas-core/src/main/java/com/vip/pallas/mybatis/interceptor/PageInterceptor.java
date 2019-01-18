/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.vip.pallas.mybatis.interceptor;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.RoutingStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.mybatis.entity.Page;

@Intercepts({ @Signature(method = "prepare", type = StatementHandler.class, args = { Connection.class }),
		@Signature(method = "query", type = Executor.class, args = { MappedStatement.class, Object.class,
				RowBounds.class, ResultHandler.class }) })
public class PageInterceptor implements Interceptor {
	protected static final Logger log = LoggerFactory.getLogger(PageInterceptor.class);

	public static final String MYSQL = "mysql";
	public static final String ORACLE = "oracle";

	protected String databaseType;// 数据库类型，不同的数据库有不同的分页方法

	@SuppressWarnings("rawtypes")
	protected ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

	public String getDatabaseType() {
		return databaseType;
	}

	public void setDatabaseType(String databaseType) {
		if (!databaseType.equalsIgnoreCase(MYSQL) && !databaseType.equalsIgnoreCase(ORACLE)) {
			throw new PageNotSupportException(
					"Page not support for the type of database, database type [" + databaseType + "]");
		}
		this.databaseType = databaseType;
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties properties) {
		String databaseTypePro = properties.getProperty("databaseType");
		if (databaseTypePro != null) {
			setDatabaseType(databaseTypePro);
		}
	}

	private Object interceptStatementHandler(Invocation invocation) throws Throwable{
		Page page = pageThreadLocal.get();
		if (page == null) { // 不是分页查询
			return invocation.proceed();
		}

		RoutingStatementHandler handler = (RoutingStatementHandler) invocation.getTarget();
		StatementHandler delegate = (StatementHandler) ReflectUtil.getFieldValue(handler, "delegate");
		BoundSql boundSql = delegate.getBoundSql();

		Connection connection = (Connection) invocation.getArgs()[0];
		prepareAndCheckDatabaseType(connection); // 准备数据库类型

		if (page.getTotalPage() > -1) {
			if (log.isTraceEnabled()) {
				log.trace("已经设置了总页数, 不需要再查询总数.");
			}
		} else {
			Object parameterObj = boundSql.getParameterObject();
			MappedStatement mappedStatement = (MappedStatement) ReflectUtil.getFieldValue(delegate,
					"mappedStatement");
			queryTotalRecord(page, parameterObj, mappedStatement, connection);
		}

		String sql = boundSql.getSql();
		String pageSql = buildPageSql(page, sql);
		if (log.isDebugEnabled()) {
			log.debug("分页时, 生成分页pageSql: " + pageSql);
		}
		ReflectUtil.setFieldValue(boundSql, "sql", pageSql);

		return invocation.proceed();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object intercept(Invocation invocation) throws Throwable {
		if (invocation.getTarget() instanceof StatementHandler) { // 控制SQL和查询总数的地方
			return interceptStatementHandler(invocation);
		}
		// 查询结果的地方

		// 获取是否有分页Page对象
		Page page = findPageObject(invocation.getArgs()[1]);
		if (page == null) {
			if (log.isTraceEnabled()) {
				log.trace("没有Page对象作为参数, 不是分页查询.");
			}
			return invocation.proceed();
		} else {
			if (log.isTraceEnabled()) {
				log.trace("检测到分页Page对象, 使用分页查询.");
			}
		}
		// 设置真正的parameterObj
		invocation.getArgs()[1] = extractRealParameterObject(invocation.getArgs()[1]);

		pageThreadLocal.set(page);
		try {
			Object resultObj = invocation.proceed(); // Executor.query(..)
			if (resultObj instanceof List) {
				/* @SuppressWarnings({ "unchecked", "rawtypes" }) */
				page.setResults((List) resultObj);
			}
			return resultObj;
		} finally {
			pageThreadLocal.remove();
		}
	}

	@SuppressWarnings("rawtypes")
	protected Page findPageObject(Object parameterObj) {
		if (parameterObj instanceof Page) {
			return (Page) parameterObj;
		} else if (parameterObj instanceof Map) {
			for (Object val : ((Map) parameterObj).values()) {
				if (val instanceof Page) {
					return (Page) val;
				}
			}
		}
		return null;
	}

	/**
	 * <pre>
	 *
	 * 把真正的参数对象解析出来
	 * Spring会自动封装对个参数对象为Map<String, Object>对象
	 * 对于通过@Param指定key值参数我们不做处理，因为XML文件需要该KEY值
	 * 而对于没有@Param指定时，Spring会使用0,1作为主键
	 * 对于没有@Param指定名称的参数,一般XML文件会直接对真正的参数对象解析，
	 * 此时解析出真正的参数作为根对象
	 * </pre>
	 *
	 * @param parameterObj
	 * @return
	 */
	protected Object extractRealParameterObject(Object parameterObj) {
		if (!(parameterObj instanceof Map<?, ?>)) {
			return parameterObj;
		}
		Map<?, ?> parameterMap = (Map<?, ?>) parameterObj;
		if (parameterMap.size() != 2) {
			return parameterObj;
		}
		boolean springMapWithNoParamName = true;
		for (Object key : parameterMap.keySet()) {
			if (!(key instanceof String)) {
				springMapWithNoParamName = false;
				break;
			}
			String keyStr = (String) key;
			if (!"0".equals(keyStr) && !"1".equals(keyStr)) {
				springMapWithNoParamName = false;
				break;
			}
		}
		if (springMapWithNoParamName) {
			for (Object value : parameterMap.values()) {
				if (!(value instanceof Page<?>)) {
					return value;
				}
			}
		}
		return parameterObj;
	}

	protected void prepareAndCheckDatabaseType(Connection connection) throws SQLException {
		if (databaseType == null) {
			String productName = connection.getMetaData().getDatabaseProductName();
			if (log.isTraceEnabled()) {
				log.trace("Database productName: " + productName);
			}
			productName = productName.toLowerCase();
			if (productName.indexOf(MYSQL) != -1) {
				databaseType = MYSQL;
			} else if (productName.indexOf(ORACLE) != -1) {
				databaseType = ORACLE;
			} else {
				throw new PageNotSupportException(
						"Page not support for the type of database, database product name [" + productName + "]");
			}
			if (log.isInfoEnabled()) {
				log.info("自动检测到的数据库类型为: " + databaseType);
			}
		}
	}

	/**
	 * <pre>
	 *
	 * 生成分页SQL
	 * </pre>
	 *
	 * @param page
	 * @param sql
	 * @return
	 */
	protected String buildPageSql(Page<?> page, String sql) {
		if (MYSQL.equalsIgnoreCase(databaseType)) {
			return buildMysqlPageSql(page, sql);
		} else if (ORACLE.equalsIgnoreCase(databaseType)) {
			return buildOraclePageSql(page, sql);
		}
		return sql;
	}

	/**
	 * <pre>
	 *
	 * 生成Mysql分页查询SQL
	 * </pre>
	 *
	 * @param page
	 * @param sql
	 * @return
	 */
	protected String buildMysqlPageSql(Page<?> page, String sql) {
		if (sql.indexOf("limit") >= 0) {
			return sql;
		}
		// 计算第一条记录的位置，Mysql中记录的位置是从0开始的。
		int start = (page.getPageNo() - 1) >= 0 ? (page.getPageNo() - 1) : 0;

		int offset = start * page.getPageSize();
		return new StringBuilder(sql).append(" limit ").append(offset).append(",").append(page.getPageSize())
				.toString();
	}

	/**
	 * <pre>
	 *
	 * 生成Oracle分页查询SQL
	 * </pre>
	 *
	 * @param page
	 * @param sql
	 * @return
	 */
	protected String buildOraclePageSql(Page<?> page, String sql) {
		// 计算第一条记录的位置，Oracle分页是通过rownum进行的，而rownum是从1开始的
		int offset = (page.getPageNo() - 1) * page.getPageSize() + 1;
		StringBuilder sb = new StringBuilder(sql);
		sb.insert(0, "select u.*, rownum r from (").append(") u where rownum < ").append(offset + page.getPageSize());
		sb.insert(0, "select * from (").append(") where r >= ").append(offset);
		return sb.toString();
	}

	/**
	 * <pre>
	 *
	 * 查询总数
	 * </pre>
	 *
	 * @param page
	 * @param parameterObject
	 * @param mappedStatement
	 * @param connection
	 * @throws SQLException
	 */
	protected void queryTotalRecord(Page<?> page, Object parameterObject, MappedStatement mappedStatement,
									Connection connection) throws SQLException {
		BoundSql boundSql = mappedStatement.getBoundSql(page);
		String sql = boundSql.getSql();
		String countSql = this.buildCountSql(sql);
		if (log.isDebugEnabled()) {
			log.debug("分页时, 生成countSql: " + countSql);
		}

		List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
		BoundSql countBoundSql = new BoundSql(mappedStatement.getConfiguration(), countSql, parameterMappings,
				parameterObject);
		ParameterHandler parameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject,
				countBoundSql);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = connection.prepareStatement(countSql); // NOSONAR
			parameterHandler.setParameters(pstmt);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				long totalRecord = rs.getLong(1);
				page.setTotalRecord(totalRecord);
			}
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					if (log.isWarnEnabled()) {
						log.warn("关闭ResultSet时异常.", e);
					}
				}
			}
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (Exception e) {
					if (log.isWarnEnabled()) {
						log.warn("关闭PreparedStatement时异常.", e);
					}
				}
			}
		}
	}

	/**
	 * 根据原Sql语句获取对应的查询总记录数的Sql语句
	 *
	 * @param sql
	 * @return
	 */
	protected String buildCountSql(String sql) {
		int index = sql.indexOf("from");
		return "select count(*) " + sql.substring(index);
	}

	/**
	 * 利用反射进行操作的一个工具类
	 *
	 */
	private static class ReflectUtil {
		/**
		 * 利用反射获取指定对象的指定属性
		 *
		 * @param obj 目标对象
		 * @param fieldName 目标属性
		 * @return 目标属性的值
		 */
		public static Object getFieldValue(Object obj, String fieldName) {
			Object result = null;
			Field field = ReflectUtil.getField(obj, fieldName);
			if (field != null) {
				field.setAccessible(true);
				try {
					result = field.get(obj);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.error("error", e);
				}
			}
			return result;
		}

		/**
		 * 利用反射获取指定对象里面的指定属性
		 *
		 * @param obj 目标对象
		 * @param fieldName 目标属性
		 * @return 目标字段
		 */
		private static Field getField(Object obj, String fieldName) {
			Field field = null;
			for (Class<?> clazz = obj.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
				try {
					field = clazz.getDeclaredField(fieldName);
					break;
				} catch (NoSuchFieldException e) { // NOSONAR
					// 这里不用做处理，子类没有该字段可能对应的父类有，都没有就返回null。
				}
			}
			return field;
		}

		/**
		 * 利用反射设置指定对象的指定属性为指定的值
		 *
		 * @param obj 目标对象
		 * @param fieldName 目标属性
		 * @param fieldValue 目标值
		 */
		public static void setFieldValue(Object obj, String fieldName, String fieldValue) {
			Field field = ReflectUtil.getField(obj, fieldName);
			if (field != null) {
				try {
					field.setAccessible(true);
					field.set(obj, fieldValue);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					log.error("error", e);
				}
			}
		}
	}

	public static class PageNotSupportException extends RuntimeException {

		/** serialVersionUID */
		private static final long serialVersionUID = 1L;

		public PageNotSupportException() {
			super();
		}

		public PageNotSupportException(String message, Throwable cause) {
			super(message, cause);
		}

		public PageNotSupportException(String message) {
			super(message);
		}

		public PageNotSupportException(Throwable cause) {
			super(cause);
		}
	}

}