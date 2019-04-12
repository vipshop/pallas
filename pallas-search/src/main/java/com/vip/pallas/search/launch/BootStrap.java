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

package com.vip.pallas.search.launch;

import com.vip.pallas.search.filter.HttpProtocolCheckFilter;
import com.vip.pallas.search.filter.base.DefaultFilterPipeLine;
import com.vip.pallas.search.filter.error.ErrorFilter;
import com.vip.pallas.search.filter.post.CommonResponseHeaderFilter;
import com.vip.pallas.search.filter.post.ResponseSendFilter;
import com.vip.pallas.search.filter.rest.RestInvokerFilter;
import com.vip.pallas.search.filter.rest.RestRequestBodyFilter;
import com.vip.pallas.search.filter.rest.RestRequestHeaderFilter;
import com.vip.pallas.search.filter.rest.RestRequestUriFilter;
import com.vip.pallas.search.filter.rest.RestResponseHeaderFilter;
import com.vip.pallas.search.filter.route.BalanceFilter;
import com.vip.pallas.search.filter.route.FlowRecordFilter;
import com.vip.pallas.search.filter.route.RouteFilter;
import com.vip.pallas.search.filter.route.UpStreamFilter;
import com.vip.pallas.search.utils.PallasSearchProperties;

public class BootStrap {
	
	public static void initZuul() throws Exception {
		initJavaFilters();
	}

	private static void initJavaFilters() {
		// 前置流程
		if (!PallasSearchProperties.SEARCH_SKIP_ROUTING) {
			DefaultFilterPipeLine.getInstance().addLastSegment(new HttpProtocolCheckFilter(), new RouteFilter(),
					new BalanceFilter());
		} else {
			DefaultFilterPipeLine.getInstance().addLastSegment(new HttpProtocolCheckFilter(), new UpStreamFilter());
		}

		// rest流程
		DefaultFilterPipeLine.getInstance().addLastSegment(new RestRequestUriFilter(), new RestRequestBodyFilter(),
				new RestRequestHeaderFilter(), new FlowRecordFilter(), new RestInvokerFilter(), new RestResponseHeaderFilter());

		// 后置流程
		DefaultFilterPipeLine.getInstance().addLastSegment(new CommonResponseHeaderFilter(), new ResponseSendFilter());

		// 错误处理
		DefaultFilterPipeLine.getInstance().addLastSegment(new ErrorFilter());
	}
	
}
