package com.vip.pallas.search.filter;

import com.vip.pallas.search.filter.base.AbstractFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;

/**
 * Created by fish24k on 16/11/24.
 */
public abstract class AbstractResponseHeaderFilter extends AbstractFilter {
	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws Exception {
		fireNextFilter(filterContext, sessionContext);
	}

	public abstract void fireNextFilter(AbstractFilterContext filterContext, SessionContext sessionContext);

}
