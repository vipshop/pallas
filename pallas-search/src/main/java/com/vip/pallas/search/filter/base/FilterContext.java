package com.vip.pallas.search.filter.base;

public class FilterContext extends AbstractFilterContext {
	private Filter filter;

	public FilterContext(Filter filter) {
		this.filter = filter;
	}

	@Override
	public Filter getFilter() {
		return filter;
	}
}
