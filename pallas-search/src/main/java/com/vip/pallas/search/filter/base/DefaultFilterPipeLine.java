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
