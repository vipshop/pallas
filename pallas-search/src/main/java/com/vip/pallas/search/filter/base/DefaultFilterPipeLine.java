package com.vip.pallas.search.filter.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultFilterPipeLine implements FilterPipeLine {
	
	private final Map<String, AbstractFilterContext> name2ctx = new HashMap<String, AbstractFilterContext>(16);

	/**
	 * Singleton instance holder class.(For lazy load)
	 */
	private static class InstanceHolder {
		private volatile static DefaultFilterPipeLine instance = new DefaultFilterPipeLine();

		private static DefaultFilterPipeLine getInstance() {
			return instance;
		}
	}

	private DefaultFilterPipeLine() {
	}

	public static FilterPipeLine getInstance() {
		return InstanceHolder.getInstance();
	}

	@Override
	public void addLastSegment(Filter... filters) {
		if (filters.length == 0 || filters[0] == null) {
			return;
		}
		for (int size = 0; size < filters.length; size++) {
			Filter filter = filters[size];
			checkDuplicateName(filter.name());
			name2ctx.put(filter.name(), new FilterContext(filter));
			if (size == 0) {
				continue;
			} else {
				name2ctx.get(filters[size - 1].name()).next = name2ctx.get(filters[size].name());
			}

		}
	}

	@Override
	public List<Filter> getAllFilter() {
		Collection<AbstractFilterContext> con = name2ctx.values();
		List<Filter> filterList = new ArrayList<Filter>();
		for (AbstractFilterContext context : con) {
			filterList.add(context.getFilter());
		}
		return filterList;
	}

	@Override
	public AbstractFilterContext get(String name) {

		return name2ctx.get(name);
	}

	private void checkDuplicateName(String name) {
		if (name2ctx.containsKey(name)) {
			throw new IllegalArgumentException("Duplicate filter name: " + name);
		}
	}

}
