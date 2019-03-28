package com.vip.pallas.search.http;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vip.pallas.search.model.ShardGroup;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

/**
 * httpRequst接口
 * @author freeman.he
 *
 */
public interface PallasRequest {

	/**
	 * 如果是表单的post，则参数为uri和body里面的融合。 获取到该map，不需做变更等操作，只用于查询
	 * @return 获取所有的参数。
	 * 
	 */
	Map<String, List<String>> getParameterMap();

	boolean isPostFormBody();

	String getBodyStrForPost();

	Map<String, String> getCookieMap();

	String getCookie(String name);

	String getHeader(String name);

	// 获取header的名字
	Iterator<String> getHeaderNames();

	/**
	 * @return 获取由系统添加过的header的名字。
	 */
	Set<String> getNewHeaderNames();
	// public Map<String,String> getHeaderMap();

	/**
	 * Returns the name of the HTTP method with which this request was made, for example, GET, POST, or PUT. Same as the
	 * value of the CGI variable REQUEST_METHOD.
	 *
	 * @return a <code>String</code> specifying the name of the method with which this request was made
	 */
	String getMethod();

	/**
	 * Returns any extra path information associated with the URL the client sent when it made this request. The extra
	 * path information follows the servlet path but precedes the query string and will start with a "/" character.
	 *
	 * <p>
	 * This method returns <code>null</code> if there was no extra path information.
	 *
	 * <p>
	 * Same as the value of the CGI variable PATH_INFO.
	 *
	 * @return a <code>String</code>, decoded by the web container, specifying extra path information that comes after
	 * the servlet path but before the query string in the request URL; or <code>null</code> if the URL does not have
	 * any extra path information
	 */
	public String getPathInfo();

	public String getUri();

	String getContentType();

	/**
	 * 只获取uri上面的所有参数
	 */
	String getParameter(String name);

	List<String> getParameters(String name);

	public String remoteAddress();

	public HttpVersion getHttpVersion();

	public void writeAndFlush(FullHttpResponse fullHttpResponse);

	// void writeAndFlush(HttpResponseStatus status);
	public void closeChannle();

	/**
	 * 在解密后，需要将解密后的参数添加参数的map中。 如果key的来源是url，则将params解析的数据放到url的map中， 如果key的来源是form的body,则将params解析的数据放到from body的map中
	 * 1：会对params进行urlDecoder 2: 对参数进行utf-8编码 3：会对参数进行校验，校验失败，则抛出异常
	 * @param params:添加key，value字符。 比如：a=1&b=3&c=4
	 * @param key : 表示上述params放置的位置
	 */
	void addParamterByUrlAndkey(String params, String key) throws IllegalArgumentException;

	/**
	 * 删除数据，用户数据的清理
	 * @param name key
	 */
	void removeParamter(String name);

	/**
	 * 删除header的数据
	 * @param name key
	 */
	void removeHeader(String name);

	/**
	 * 设置新的header，会覆盖掉老的name对应的数据
	 * @param name key
	 * @param value value
	 */
	void setHeader(String name, String value);

	/**
	 * 如果uri未修改过，则获取原始的数据，如果uri修改过，则提取uri的参数，进行编码
	 * @return uri对应的encode之后的编码数据
	 */
	String getModifiedUri();

	HttpMethod getHttpMethod();

	/**
	 * @return 主要用于获取header数据，不能直接做更新等操作
	 */
	HttpHeaders getHttpHeader();

	DecoderResult getDecoderResult();

	/**
	 * @return 获取发送到rest服务的content数据，如果数据变更过，则需要重新生成content，如果没有变更过，则获取原始的数据。
	 * @throws UnsupportedEncodingException
	 */
	ByteBuf getModifyBodyContent() throws UnsupportedEncodingException;

	/**
	 * @return 获取http请求的body内容，如果是get请求，则为EmptyByteBuf
	 */
	ByteBuf getContent();

	/**
	 * @return 获取索引名
	 */
	String getIndexName();

	/**
	 * @return 获取客户端IP
	 */
	String getClientIp();

	/**
	 * @return 获取模板名
	 */
	String getTemplateId();

	/**
	 * @return 设置是否Scroll查询
	 */
	void setScrollFirst(boolean b);

	/**
	 * @return 是否Scroll查询
	 */
	boolean isScrollFirst();

	void setScrollContinue(boolean scroll);

	boolean isScrollContinue();

	void setTargetGroupId(Long id);

	Long getTargetGroupId();

	void setFixScrollRequestContent(byte[] fixContent);

	byte[] getFixScrollRequestContent();

	void setLogicClusterId(String cluster);

	String getLogicClusterId();

	String getClientToken();

	void setIsIndexSearch(boolean isIndexSearch);

	boolean isIndexSearch();

	void setRoutePrimaryFirst(boolean routePrimaryFirst);

	boolean isRoutePrimaryFirst();

	void setRouteReplicaFirst(boolean routeReplicaFirst);

	boolean isRouteReplicaFirst();

	void setPreference(String preference);

	String getPreference();

	boolean isCircuitBreakerOn();

	void setCircuitBreaker(boolean circuitBreaker);

	void setShardGroup(ShardGroup shardGroup);

	ShardGroup getShardGroup();

	void setShardGroupList(List<ShardGroup> groupList);

	List<ShardGroup> getShardGroupList();
}
