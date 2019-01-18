package com.vip.pallas.search.utils;

import org.apache.commons.lang3.StringUtils;

import com.google.common.net.HttpHeaders;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.http.PallasRequest;

public class ClientIpUtil {

	public static String getClientIp(SessionContext sessionContext) {
		String ipStr = null;
		if (StringUtils.isNotBlank(PallasSearchProperties.HTTP_HEADER_REMOTE_ADDRESS)) {
			ipStr = sessionContext.getRequest().getHeader(PallasSearchProperties.HTTP_HEADER_REMOTE_ADDRESS);// 默认大小写忽略
		}
		if (StringUtils.isNotBlank(ipStr)) {
			return ipStr;
		} else {
			return getClientIpByDefault(sessionContext);
		}
	}

	private static String getClientIpByDefault(SessionContext sessionContext) {
		PallasRequest request = sessionContext.getRequest();
		String forwardedForIp = request.getHeader(HttpHeaders.X_FORWARDED_FOR);
		String result = null;
		if (StringUtils.isBlank(forwardedForIp)) {
			result = request.remoteAddress();
		} else {
			String ipStr = forwardedForIp.replaceAll(" ", "");
			if (ipStr.contains(",")) {
				String[] ipArr = ipStr.split(",");
				if (ipArr.length > 0) {
					result = ipArr[0];
				} else {
					result = request.remoteAddress();
				}
			} else {
				result = ipStr;
			}

		}
		return result;
	}
}
