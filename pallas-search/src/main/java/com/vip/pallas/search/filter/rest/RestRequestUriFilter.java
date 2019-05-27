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

package com.vip.pallas.search.filter.rest;

import com.vip.pallas.search.filter.base.AbstractFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.model.ServiceInfo;

import com.vip.pallas.utils.LogUtils;
import com.vip.pallas.search.utils.SearchLogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestRequestUriFilter extends AbstractFilter {
	public static String DEFAULT_NAME = PRE_FILTER_NAME + RestRequestUriFilter.class.getSimpleName().toUpperCase();
	private static final Logger logger = LoggerFactory.getLogger(RestRequestUriFilter.class);

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws Exception {
		PallasRequest req = sessionContext.getRequest();
		ServiceInfo si = sessionContext.getServiceInfo();
		String uri = req.getModifiedUri();
		// 方案是通过检测 Target group 的title， 如果类似 {{index:msearch_rampup}} 这在当期那URI中把当前索引切换成转换的索引
		if (req.isIndexSearch() && si != null && si.getTargetGroupTitle().startsWith("{{") && si.getTargetGroupTitle().endsWith("}}")) {
			try {
				String indexName = req.getIndexName();
				String targetGroupTitle = si.getTargetGroupTitle();
				String rampupIndexName = targetGroupTitle.substring(targetGroupTitle.indexOf("index:")+6, targetGroupTitle.lastIndexOf("}}")).trim();
				uri = uri.replaceFirst("/" + indexName + "/", "/" + rampupIndexName + "/");
			} catch (Exception ignore) {
				LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, ignore.getMessage());
			}
		}
		sessionContext.setRestRequestUri(uri);
		super.run(filterContext, sessionContext);
	}

}
