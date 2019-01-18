package com.vip.pallas.search.filter.base;

import com.vip.pallas.search.filter.common.SessionContext;

public abstract class AbstractFilter implements Filter {
	public final static String PRE_FILTER_NAME = "PALLAS_FILTER_";
	protected boolean valid = true;

	@Override
	public void init() {
		valid = true;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws Exception {
		filterContext.fireNext(sessionContext);
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean isValid) {
		this.valid = isValid;
	}

	@Override
	public void shutdown() {
	}
	
}
