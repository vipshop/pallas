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

import java.util.List;
import java.util.concurrent.ExecutionException;

import com.vip.pallas.search.filter.base.AbstractFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.filter.rest.RestRequestUriFilter;
import com.vip.pallas.search.model.ServiceInfo;

import io.netty.util.internal.InternalThreadLocalMap;

public class BalanceFilter extends AbstractFilter {

	public static String DEFAULT_NAME = PRE_FILTER_NAME + BalanceFilter.class.getSimpleName().toUpperCase();

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws Exception {
		ServiceInfo serviceInfo = evaluateBalance(sessionContext.getServiceInfoList());
		sessionContext.setServiceInfo(serviceInfo);

		filterContext.fireFilter(sessionContext, RestRequestUriFilter.DEFAULT_NAME);
	}

	private ServiceInfo evaluateBalance(List<ServiceInfo> serversList) throws ExecutionException {
		return randomLB(serversList);
	}

	private ServiceInfo randomLB(List<ServiceInfo> serversList) {
		ServiceInfo serviceInfo = serversList.get(InternalThreadLocalMap.get().random().nextInt(serversList.size()));
		//LOGGER.info("BalanceFilter print info " + JSON.toJSONString(serviceInfo));
		return serviceInfo;
	}


	public String getLBHashKey(SessionContext sessionContext) {
		return sessionContext.getRequest().getUri();
	}

}
