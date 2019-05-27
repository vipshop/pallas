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

package com.vip.pallas.search.netty.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vip.pallas.search.utils.SearchLogEvent;
import com.vip.pallas.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.exception.HttpCodeErrorPallasException;
import com.vip.pallas.search.http.HttpCode;
import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.model.ShardGroup;
import com.vip.pallas.search.utils.QueryStringEncoder;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * Netty HTTP请求数据封装
 *
 * @author jamin.li
 */
public class NettyPallasRequest implements PallasRequest {

	private static Logger logger = LoggerFactory.getLogger(NettyPallasRequest.class);
	private FullHttpRequest httpRequest;
	private Channel channel;

	// private Map<String, List<String>> paramsMap;
	private QueryStringDecoder queryStringDecoder;
	// url的参数
	private Map<String, List<String>> urlParamsMap = new HashMap<String, List<String>>();
	// form 表单提交
	private Map<String, List<String>> postFormParamsMap = new HashMap<String, List<String>>();
	// 是否对参数进行了解析
	private boolean isParsedParams = false;
	// 是否对cookie进行了处理
	private boolean isParsedCookie = false;

	// 主要用于osp转发header的设置。该字段对于http转发无意义
	private Set<String> newHeaderNames = new HashSet<String>();
	// cookie的值
	private Map<String, String> cookieMap = new HashMap<String, String>();

	// 判断uri的参数是否变动过
	private boolean isUriChanged = false;
	// 判断formbody是否有变更
	private boolean isFormBodyChanged = false;
	//缓存content-type信息
	private String contentType;

	private String indexName;

	private boolean scrollFirst = false;

	private boolean scrollContinue = false;

	private Long targetGroupId;

	private byte[] fixScrollRequestContent;

	private String logicClusterId;

	private String templateId = null;

	private boolean indexSearch = true;

	private boolean routePrimaryFirst = false;

	private boolean routeReplicaFirst = false;

	private String preference = null;

	private boolean circuitBreaker = false;
	private ShardGroup shardGroup;
	private List<ShardGroup> groupList;

	public NettyPallasRequest(FullHttpRequest httpRequest, Channel channel) {
		this.httpRequest = httpRequest;
		this.channel = channel;
	}

	@Override
	public String getHeader(String name) {
		return httpRequest.headers().get(name);
	}

	// 获取header的名字
	@Override
	public Iterator<String> getHeaderNames() {
		return (Iterator<String>) httpRequest.headers().names().iterator();
	}

	// 删除对应的header
	@Override
	public void removeHeader(String name) {
		if (name != null) {
			httpRequest.headers().remove(name);
			newHeaderNames.remove(name);
		}

	}

	@Override
	public void setHeader(String name, String value) {
		if (name != null && value != null) {
			httpRequest.headers().set(name, value);
			newHeaderNames.add(name);
		}
	}

	@Override
	public Set<String> getNewHeaderNames() {
		return newHeaderNames;
	}

	@Override
	public HttpHeaders getHttpHeader() {
		return httpRequest.headers();
	}

	// @Override
	// public Map<String, String> getHeaderMap() {
	// Map<String, String> headerMap = new HashMap<String, String>();
	// Iterator<String> iter = getHeaderNames();
	// while (iter.hasNext()) {
	// String key = (String) iter.next();
	// headerMap.put(key, getHeader(key));
	// }
	// return headerMap;
	// }

	@Override
	public String getContentType() {
		if(contentType == null){
			String allContentType = getHeader(Names.CONTENT_TYPE);
			if(allContentType == null){
				return null;
			}
			String[] strs = allContentType.split(";");
			contentType = strs[0];
		}
		return contentType;
		
	}

	@Override
	public String getMethod() {
		return httpRequest.getMethod().name().toUpperCase();
	}

	@Override
	public HttpMethod getHttpMethod() {
		return httpRequest.getMethod();
	}

	private Map<String, List<String>> mergeMap(Map<String, List<String>> fromMap, Map<String, List<String>> mergeMap) {
		if (fromMap == null || fromMap.size() == 0) {
			return mergeMap;
		}
		Set<String> fromKeys = fromMap.keySet();
		for (String fromKey : fromKeys) {
			List<String> mergeList = mergeMap.get(fromKey);
			// 不存在
			if (mergeList == null || mergeList.size() == 0) {
				mergeMap.put(fromKey, fromMap.get(fromKey));
			} else {
				mergeList.addAll(fromMap.get(fromKey));
			}
		}
		return mergeMap;
	}

	// 只获取get的url上面的参数
	@Override
	public Map<String, List<String>> getParameterMap() {
		// 初始化 url和body参数
		initPrarameter();

		if (postFormParamsMap.size() == 0) {// form数据为空，返回url的参数
			return urlParamsMap;
		} else if (urlParamsMap.size() == 0) {// url为空返回form数据
			return postFormParamsMap;
		} else {
			// 说明两个都不为空，则返回总计数据
			Map<String, List<String>> allMap = new HashMap<String, List<String>>();
			mergeMap(urlParamsMap, allMap);
			mergeMap(postFormParamsMap, allMap);
			return allMap;
		}

	}

	private void initPrarameter() {
		if (!isParsedParams) {
			isParsedParams = true;
			try {
				initQueryStringDecoder();
				urlParamsMap = queryStringDecoder.parameters();
				postFormParamsMap = getParameterMapForPost();
			} catch (Exception e) {
				throw e;
			}

		}
	}

	@Override
	public void addParamterByUrlAndkey(String params, String key) throws IllegalArgumentException {
		if (params == null || params.trim().length() == 0 || key == null) {
			return;
		}
		initPrarameter();
		QueryStringDecoder paramsDecoder = new QueryStringDecoder(params, false);
		Map<String, List<String>> newParamMap = paramsDecoder.parameters();
		if (urlParamsMap.get(key) != null) {
			isUriChanged = true;
			mergeMap(newParamMap, urlParamsMap);
		} else if (postFormParamsMap.get(key) != null) {
			isFormBodyChanged = true;
			mergeMap(newParamMap, postFormParamsMap);
		}

	}

	@Override
	public void removeParamter(String name) {
		initPrarameter();
		// isUriChanged
		Object urlOldValue = urlParamsMap.remove(name);
		if (urlOldValue != null) {
			isUriChanged = true;
		}
		Object formBodyOldValue = postFormParamsMap.remove(name);
		if (formBodyOldValue != null) {
			isFormBodyChanged = true;
		}
	}

	@Override
	public String getParameter(String name) {
		List<String> resultList = getParameters(name);
		if (resultList != null && resultList.size() > 0) {
			return resultList.get(0);
		} else {
			return null;
		}
	}

	@Override
	public List<String> getParameters(String name) {
		initPrarameter();
		List<String> urlValueList = urlParamsMap.get(name);
		List<String> postFormValueList = postFormParamsMap.get(name);
		if (urlValueList == null) {
			return postFormValueList;
		} else if (postFormValueList == null) {
			return urlValueList;
		} else {
			List<String> allValueList = new ArrayList<String>();
			allValueList.addAll(urlValueList);
			allValueList.addAll(postFormValueList);
			return allValueList;
		}
	}

	private QueryStringDecoder initQueryStringDecoder() {
		if (queryStringDecoder == null) {
			queryStringDecoder = new QueryStringDecoder(httpRequest.getUri());
		}

		return queryStringDecoder;

	}

	private Map<String, List<String>> getParameterMapForPost() {
		if (!HttpMethod.POST.name().equalsIgnoreCase(getMethod()) || !Values.APPLICATION_X_WWW_FORM_URLENCODED.equalsIgnoreCase(getContentType())) {
			return postFormParamsMap;
		}
		HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), httpRequest);

		List<InterfaceHttpData> dataList = decoder.getBodyHttpDatas();
		for (InterfaceHttpData data : dataList) {
			if (data == null) {
				continue;
			}
			try {
				if (data.getHttpDataType() == HttpDataType.Attribute) {
					Attribute attribute = (Attribute) data;
					List<String> strList = postFormParamsMap.get(data.getName());
					if (strList == null) {
						strList = new ArrayList<>();
						postFormParamsMap.put(data.getName(), strList);
					}
					strList.add(attribute.getValue());
				}
			} catch (IOException e) {
				LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, e.getMessage(), e);
			}
		}
		return postFormParamsMap;
	}

	@Override
	public String getPathInfo() {
		initQueryStringDecoder();
		return queryStringDecoder.path();
	}

	@Override
	public String getUri() {
		initQueryStringDecoder();
		return queryStringDecoder.uri();
	}

	@Override
	public String getModifiedUri() {
		if (!isUriChanged) {
			return getUri();
		} else {
			// 该处对原生的QueryStringEncoder进行了优化，主要是优化stringbuilder，减少内存拷贝。
			return tranToStringEncode(urlParamsMap, getPathInfo());
		}
	}

	private String tranToStringEncode(Map<String, List<String>> params, String pathInfo) {
		QueryStringEncoder encoder = new QueryStringEncoder(pathInfo);
		Set<Map.Entry<String, List<String>>> set = params.entrySet();
		for (Map.Entry<String, List<String>> entry : set) {
			List<String> valueList = entry.getValue();
			if (valueList != null) {
				for (String value : valueList) {
					encoder.addParam(entry.getKey(), value);
				}
			}

		}
		return encoder.toString();
	}

	@Override
	public ByteBuf getModifyBodyContent() throws UnsupportedEncodingException {
		if (isScrollContinue() && fixScrollRequestContent != null) {
			getHttpHeader().set(HttpHeaders.Names.CONTENT_LENGTH, fixScrollRequestContent.length);
			return Unpooled.wrappedBuffer(fixScrollRequestContent);

		} else if (!isFormBodyChanged) {
			return httpRequest.content();
		} else {
			// 如果更改过，说明是post请求，并且是form表单提交
			// 1：获取map，转换为key value的形式，并且进行encode操作
			String encodeBody = tranToStringEncode(postFormParamsMap, null);
			if (encodeBody == null) {//
				getHttpHeader().set(HttpHeaders.Names.CONTENT_LENGTH, 0);
				return Unpooled.EMPTY_BUFFER;
			} else {
				//
				byte[] bytes = encodeBody.getBytes(CharsetUtil.UTF_8);
				// 对原bytebuf进行释放
				ReferenceCountUtil.safeRelease(httpRequest.content());
				// 写content-length
				getHttpHeader().set(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
				// 申请buffer
				return channel.alloc().ioBuffer(bytes.length).writeBytes(bytes);
			}
		}
	}

	@Override
	public String getBodyStrForPost() {
		return httpRequest.content().toString(CharsetUtil.UTF_8);
	}

	@Override
	public boolean isPostFormBody() {
		return Values.APPLICATION_X_WWW_FORM_URLENCODED.equalsIgnoreCase(getContentType());
	}

	@Override
	public Map<String, String> getCookieMap() {
		initCookieMap();
		return cookieMap;
	}

	@Override
	public String getCookie(String name) {
		if (name == null) {
			return null;
		}
		initCookieMap();
		return cookieMap.get(name);
	}

	private void initCookieMap() {
		if (!isParsedCookie) {
			isParsedCookie = true;
			String cookieHeader = getHeader(Names.COOKIE);
			if (cookieHeader != null) {
				Set<Cookie> cookieSet = ServerCookieDecoder.LAX.decode(cookieHeader);
				for (Cookie cookie : cookieSet) {
					cookieMap.put(cookie.name(), cookie.value());
				}
			}
		}
	}

	@Override
	public String remoteAddress() {
		InetSocketAddress insocket = (InetSocketAddress) channel.remoteAddress();
		if (insocket != null) {
			return insocket.getAddress().getHostAddress();
		} else {
			return null;
		}
	}

	@Override
	public HttpVersion getHttpVersion() {

		return httpRequest.getProtocolVersion();
	}

	@Override
	public DecoderResult getDecoderResult() {
		return httpRequest.getDecoderResult();
	}

	@Override
	public void writeAndFlush(FullHttpResponse fullHttpResponse) {
		try {
			channel.writeAndFlush(fullHttpResponse);
		} catch (Exception e) {
			LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, e.getMessage(), e);
			channel.close();
		}
	}

	@Override
	public void closeChannle() {
		try {
			channel.close();
		} catch (Exception e) {
			LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, e.getMessage(), e);
		}

	}

	@Override
	public String getIndexName() {
		if (indexName == null) {
			String uri = httpRequest.getUri();
			if(!uri.endsWith("/")){
				uri += "/";
			}

			String _indexName = null;

			if(!"/".equals(uri)){
				int endIndex = uri.indexOf('/', 2);
				if(endIndex > 1){
					_indexName = uri.substring(1, endIndex);
				}
			}

			if(_indexName != null){
				indexName = _indexName;
			} else {
				throw new HttpCodeErrorPallasException("could not found index_name by uri: " + uri, HttpCode.HTTP_FORBIDDEN
						, NettyPallasRequest.class.getSimpleName(), "getIndexName");
			}
		}
		return indexName;
	}

	@Override
	public String getClientIp() {
		return ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
	}

	@Override
	public String getTemplateId() {
		if (templateId == null) {
			try {
				templateId = getHeader("X-PALLAS-SEARCH-TEMPLATE-ID");
			} catch (Exception e) {
				LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, e.getMessage(), e);
			}
		}
		return templateId;
	}

	@Override
	public ByteBuf getContent(){
		return httpRequest.content();
	}

	@Override
	public void setScrollFirst(boolean scroll) {
		this.scrollFirst = scroll;
	}

	@Override
	public boolean isScrollFirst() {
		return this.scrollFirst;
	}

	@Override
	public void setScrollContinue(boolean scroll) {
		this.scrollContinue = scroll;
	}

	@Override
	public boolean isScrollContinue() {
		return this.scrollContinue;
	}

	@Override
	public void setTargetGroupId(Long id) {
		this.targetGroupId = id;
	}

	@Override
	public Long getTargetGroupId() {
		return this.targetGroupId;
	}

	@Override
	public void setFixScrollRequestContent(byte[] fixContent) {
		this.fixScrollRequestContent = fixContent;
	}

	@Override
	public byte[] getFixScrollRequestContent() {
		return this.fixScrollRequestContent;
	}

	@Override
	public void setLogicClusterId(String cluster) {
		this.logicClusterId = cluster;
	}

	@Override
	public String getLogicClusterId() {
		if (this.logicClusterId == null) {
			String host = getHeader("X-PALLAS-SEARCH-ES-DOMAIN");
			if (host != null) {
				this.logicClusterId = host;
			} else {
				host = getHeader("Host");
				if (host != null) {
					int idx = host.indexOf(':');
					this.logicClusterId = idx > 0 ? host.substring(0, idx) : host;
				}
			}
		}
		return this.logicClusterId;
	}

	@Override
	public String getClientToken() {
		try {
			return getHeader("X-PALLAS-CLIENT-TOKEN");
		} catch (Exception e) {
			LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, e.getMessage(), e);
			return null;
		}
	}

	@Override
	public void setIsIndexSearch(boolean isIndexSearch) {
		this.indexSearch = isIndexSearch;
	}

	@Override
	public boolean isIndexSearch() {
		return this.indexSearch;
	}

	@Override
	public void setRoutePrimaryFirst(boolean routePrimaryFirst) {
		this.routePrimaryFirst = routePrimaryFirst;
	}

	@Override
	public boolean isRoutePrimaryFirst() {
		return this.routePrimaryFirst;
	}

	@Override
	public void setRouteReplicaFirst(boolean routeReplicaFirst) {
		this.routeReplicaFirst = routeReplicaFirst;
	}

	@Override
	public boolean isRouteReplicaFirst() {
		return this.routeReplicaFirst;
	}

	@Override
	public void setPreference(String preference) {
		this.preference = preference;
	}

	@Override
	public String getPreference() {
		return this.preference;
	}

	@Override
	public boolean isCircuitBreakerOn() {
		return circuitBreaker;
	}

	@Override
	public void setCircuitBreaker(boolean circuitBreaker) {
		this.circuitBreaker = circuitBreaker;
	}

	@Override
	public void setShardGroup(ShardGroup shardGroup) {
		this.shardGroup = shardGroup;
	}

	@Override
	public ShardGroup getShardGroup() {
		return this.shardGroup;
	}

	@Override
	public void setShardGroupList(List<ShardGroup> groupList) {
		this.groupList = groupList;
	}

	@Override
	public List<ShardGroup> getShardGroupList() {
		return groupList;
	}
}
