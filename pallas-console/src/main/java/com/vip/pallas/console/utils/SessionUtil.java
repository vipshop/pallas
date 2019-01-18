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

package com.vip.pallas.console.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.google.common.base.Strings;
import com.vip.vjtools.vjkit.base.annotation.NotNull;
import com.vip.vjtools.vjkit.base.annotation.Nullable;

public class SessionUtil {

	private static final String UNKNOWN = "Unknown";
	private static final String UNKNOWN_REAL = "Unkown Real Name";
	public static final String SESSION_USERNAME = "sessionUsername"; // --> oa
	public static final String SESSION_REAL_USERNAME = "sessionRealUsername"; // -->usernmae
	
	private static HttpServletRequest getRequest() {
		RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
		if(requestAttributes == null){
			return null;
		}
		
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}
	
	private static String getNameFromSession(HttpServletRequest request, String sessionKey, String unkownName) {
		request = null != request ? request : getRequest();
		if (null == request ) {
			return unkownName;
		}

		String userName = (String) request.getSession().getAttribute(sessionKey);
		return Strings.isNullOrEmpty(userName) ? unkownName : userName;
	}
	
	public static String getLoginUser() {
		HttpServletRequest request = getRequest();
		if (request == null) {
			return null;
		}

		String loguser = (String) request.getSession().getAttribute(SESSION_USERNAME);
		if (Strings.isNullOrEmpty(loguser)) {
			return null;
		} else {
			return loguser;
		}
	}
	
	public static String getLoginUser(@NotNull HttpServletRequest request) {
		return getNameFromSession(request, SESSION_USERNAME, UNKNOWN);
	}
	
	public static String getLoginRealName(@NotNull HttpServletRequest request) {
		return getNameFromSession(request, SESSION_REAL_USERNAME, UNKNOWN_REAL);
	}
	
	public static boolean isAuthorization(@Nullable HttpServletRequest request) {
		String loguser = null;
		if (null == request) {
			loguser =getLoginUser(request);
		} else {
			loguser =getLoginUser();
		}
		if (null == loguser || UNKNOWN.equals(loguser)) {
			return false;
		}
		return true;
	}
	
	public static void invalidateSession(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.removeAttribute(SESSION_USERNAME);
		session.removeAttribute(SESSION_REAL_USERNAME);
		session.invalidate();
	}
	
	public static String getRemoteAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null) {
			return request.getRemoteAddr();
		} else {
			return ip.split("， ")[0];
		}
	}
}