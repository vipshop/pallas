package com.vip.pallas.search.filter.base;

import java.util.List;

public interface FilterPipeLine {

	void addLastSegment(Filter... filters);

	AbstractFilterContext get(String name);

	List<Filter> getAllFilter();

}
