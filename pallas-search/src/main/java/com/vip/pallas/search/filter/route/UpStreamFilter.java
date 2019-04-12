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

package com.vip.pallas.search.filter.route;

import com.vip.pallas.search.exception.HttpCodeErrorPallasException;
import com.vip.pallas.search.filter.base.AbstractFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.filter.rest.RestRequestUriFilter;
import com.vip.pallas.search.http.HttpCode;
import com.vip.pallas.search.model.ServiceInfo;

public class UpStreamFilter extends AbstractFilter {

	public static String DEFAULT_NAME = PRE_FILTER_NAME + UpStreamFilter.class.getSimpleName().toUpperCase();

	private static final String X_PALLAS_SEARCH_UP_STREAM_URL = "X_PALLAS_SEARCH_UP_STREAM_URL";

	public static String className = UpStreamFilter.class.getSimpleName();
	public static String classMethod = "run";

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws Exception {
		String upStreamUrl = sessionContext.getRequest().getHeader("X_PALLAS_SEARCH_UP_STREAM_URL");
		if(upStreamUrl == null || upStreamUrl.isEmpty()){
			throw new HttpCodeErrorPallasException("could not found upstream url by header key: " + X_PALLAS_SEARCH_UP_STREAM_URL, HttpCode.HTTP_INTERNAL_SERVER_ERROR, className, classMethod);
		}
		sessionContext.setServiceInfo(new ServiceInfo(upStreamUrl, null, null, null));
		filterContext.fireFilter(sessionContext, RestRequestUriFilter.DEFAULT_NAME);
	}
}
