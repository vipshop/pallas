package com.vip.pallas.search.filter.rest;

import com.vip.pallas.search.filter.AbstractResponseHeaderFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.filter.post.CommonResponseHeaderFilter;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;

public class RestResponseHeaderFilter extends AbstractResponseHeaderFilter {
	public static String DEFAULT_NAME = PRE_FILTER_NAME + RestResponseHeaderFilter.class.getSimpleName().toUpperCase();

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void fireNextFilter(AbstractFilterContext filterContext, SessionContext sessionContext) {
		// 设置返回的http version等
		FullHttpResponse httpResponse = sessionContext.getRestFullHttpResponse();
		sessionContext.setResponseHttpVersion(httpResponse.protocolVersion());
		sessionContext.setResponseBody(httpResponse.content());
		sessionContext.setHttpCode(httpResponse.status().code());
		// 添加默认要加上的header信息

		// Add Default Header Mapping
		addHeader(Names.CONTENT_ENCODING, sessionContext); // 该字段比较重要，用户确定网关层是否要进行gzip压缩
		addHeader(Names.CONTENT_TYPE, sessionContext); // http 默认添加content-type返回
		addHeader(Names.VARY, sessionContext); // nginx默认会加vary字段
		
		filterContext.fireFilter(sessionContext, CommonResponseHeaderFilter.DEFAULT_NAME);
	}

	private void addHeader(String name, SessionContext sessionContext) {
		HttpHeaders responseHeaders = sessionContext.getResponseHttpHeaders();
		HttpHeaders restResponseHeaders = sessionContext.getRestFullHttpResponse().headers();
		String value = restResponseHeaders.get(name);
		if (value != null) {
			responseHeaders.set(name, value);
		}
	}

}
