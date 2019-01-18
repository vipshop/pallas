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
