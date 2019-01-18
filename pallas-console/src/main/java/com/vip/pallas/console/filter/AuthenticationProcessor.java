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

package com.vip.pallas.console.filter;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.google.common.collect.Lists;
import com.vip.pallas.console.utils.SessionUtil;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.utils.PallasConsoleProperties;

public class AuthenticationProcessor extends AbstractAuthProcessor {

	private List<String> extensionUrls = Lists.newArrayList(new String[] { "/authorization/login"});
	
	public AuthenticationProcessor() {
		excludeAuthUrls.addAll(extensionUrls);
	}

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response) {
		if (PallasConsoleProperties.PALLAS_SECURITY_ENABLE && !SessionUtil.isAuthorization(request)) {
			String uri = request.getRequestURI();
			if (!excludeAuthUrls.stream().anyMatch(excludeUrl -> uri.indexOf(excludeUrl) >= 0)) {
				throw new BusinessLevelException(HttpStatus.SC_UNAUTHORIZED, PallasConsoleProperties.PALLAS_LOGIN_URL);
			}
		}
	}

}