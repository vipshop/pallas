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

import com.vip.pallas.search.filter.common.PallasRunner;
import com.vip.pallas.search.filter.common.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFilterContext {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFilterContext.class);

	public AbstractFilterContext next;

	public void fireNext(SessionContext sessionContext) {
		if (next == null) {
			throw new RuntimeException();
		}
		fire0(next, sessionContext);
	}

	public void fireSelf(SessionContext sessionContext) {
		fire0(this, sessionContext);
	}

	public void fireFilter(SessionContext sessionContext, String filterName) {
		AbstractFilterContext filterContext = DefaultFilterPipeLine.getInstance().get(filterName);
		if (filterContext == null) {
			throw new RuntimeException();
		}
		fire0(filterContext, sessionContext);

	}

	private void fire0(AbstractFilterContext filterContext, SessionContext sessionContext) {
		try {
			if (filterContext.getFilter().isValid()) {
				filterContext.getFilter().run(filterContext, sessionContext);
			} else {
				fire0(filterContext.next, sessionContext);
			}

		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
			PallasRunner.errorProcess(sessionContext, e);
		}
	}
	
	public abstract Filter getFilter();
}
