package com.vip.pallas.search.filter.base;

import com.vip.pallas.search.filter.common.SessionContext;

public interface Filter {
	public String name();

	public void init();

	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws Exception;

	public boolean isValid();

	public void shutdown();
}
