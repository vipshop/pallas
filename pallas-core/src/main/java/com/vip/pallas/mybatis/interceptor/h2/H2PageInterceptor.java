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

package com.vip.pallas.mybatis.interceptor.h2;

import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.mybatis.interceptor.PageInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;

@Intercepts({ @Signature(method = "prepare", type = StatementHandler.class, args = { Connection.class }),
        @Signature(method = "query", type = Executor.class, args = { MappedStatement.class, Object.class,
                RowBounds.class, ResultHandler.class }) })
public class H2PageInterceptor extends PageInterceptor {
    public static final String H2 = "h2";
    @Override
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
            } else if (productName.indexOf(H2) != -1) {
                databaseType = H2;
            } else {
                throw new PageNotSupportException(
                        "Page not support for the type of database, database product name [" + productName + "]");
            }
            if (log.isInfoEnabled()) {
                log.info("自动检测到的数据库类型为: " + databaseType);
            }
        }
    }

    @Override
    protected String buildPageSql(Page<?> page, String sql) {
        if (MYSQL.equalsIgnoreCase(databaseType)) {
            return buildMysqlPageSql(page, sql);
        } else if (ORACLE.equalsIgnoreCase(databaseType)) {
            return buildOraclePageSql(page, sql);
        } else if (H2.equalsIgnoreCase(databaseType)) {
            return buildH2PageSql(page, sql);
        }
        return sql;
    }


    protected String buildH2PageSql(Page<?> page, String sql) {
        return buildMysqlPageSql(page, sql);
    }

    @Override
    protected String buildCountSql(String sql) {
        sql = sql.toLowerCase();
        int index = sql.indexOf("from");
        if(H2.equalsIgnoreCase(databaseType)) {
            sql = sqlRemoveOrderBy(sql);
        }
        return "select count(*) " + sql.substring(index);
    }

    //H2
    protected String sqlRemoveOrderBy(String sql) {
        if(sql.indexOf("order") != -1) {
            sql = sql.substring(0, sql.indexOf("order"));
        }
        return sql;
    }

}