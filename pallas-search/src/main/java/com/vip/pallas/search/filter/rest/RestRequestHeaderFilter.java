package com.vip.pallas.search.filter.rest;

import com.vip.pallas.search.filter.base.AbstractFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.http.PallasRequest;

import io.netty.handler.codec.http.HttpHeaders;

public class RestRequestHeaderFilter extends AbstractFilter {
	public static String DEFAULT_NAME = PRE_FILTER_NAME + RestRequestHeaderFilter.class.getSimpleName().toUpperCase();

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws Exception {
		PallasRequest request = sessionContext.getRequest();

		// 通用header的设置
		request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);

		// 用于http转发
		request.setHeader(HttpHeaders.Names.HOST, sessionContext.getServiceInfo().getBackendAddress());
		super.run(filterContext, sessionContext);
	}

}
