package com.vip.pallas.search.filter.rest;

import com.vip.pallas.search.filter.base.AbstractFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;

public class RestRequestBodyFilter extends AbstractFilter {
	public static String DEFAULT_NAME = PRE_FILTER_NAME + RestRequestBodyFilter.class.getSimpleName().toUpperCase();

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws Exception {
		// body的处理
		sessionContext.setRestRequestBody(sessionContext.getRequest().getModifyBodyContent());
		super.run(filterContext, sessionContext);
	}

}
