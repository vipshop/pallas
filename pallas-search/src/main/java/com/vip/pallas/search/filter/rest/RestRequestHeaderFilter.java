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

		// 默认开启gzip压缩(覆盖PallasRestClient端强制加的lz4压缩)
		request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING,HttpHeaders.Values.GZIP_DEFLATE);


		super.run(filterContext, sessionContext);
	}

}
