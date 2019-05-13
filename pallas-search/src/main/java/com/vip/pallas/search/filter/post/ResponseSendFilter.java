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

package com.vip.pallas.search.filter.post;

import com.vip.pallas.search.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.filter.base.AbstractFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.http.HttpCode;
import com.vip.pallas.search.monitor.MonitorAccessLog;
import com.vip.pallas.search.trace.TraceAspect;
import com.vip.pallas.search.utils.ByteUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class ResponseSendFilter extends AbstractFilter {
	private static Logger logger = LoggerFactory.getLogger(ResponseSendFilter.class);
	public static String DEFAULT_NAME = PRE_FILTER_NAME + ResponseSendFilter.class.getSimpleName().toUpperCase();

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) {
		// 保证只会发送一次
		if (sessionContext.isBodySend()) {
			LogUtils.error(logger, sessionContext.getRequest().getTemplateId(), "body sent, close channel", sessionContext.getThrowable());
			sessionContext.getRequest().closeChannle();
			return;
		}
		sessionContext.setBodySend(true);

		//对http请求的body进行release，避免body未release
		ByteUtils.deepSafeRelease(sessionContext.getRequest().getContent());

		FullHttpResponse fullResponse = null;
		try {
			HttpHeaders headers = sessionContext.getResponseHttpHeaders();
			ByteBuf body = sessionContext.getResponseBody();
			fullResponse = getFullHttpResponse(sessionContext.getHttpCode(), body,
					sessionContext.getResponseHttpVersion());
			fullResponse.headers().add(headers);
			// 发送信息
			sessionContext.getRequest().writeAndFlush(fullResponse);
		} catch (Exception e) {
			LogUtils.error(logger, sessionContext.getRequest().getTemplateId(), e.getMessage(), e);
			sessionContext.getRequest().closeChannle();
		} finally {
			// 记录mercury日志
			//#554 pallas-search调用es全链路时间点跟踪
			sessionContext.setTimestampServerResponseSend(System.currentTimeMillis());

			TraceAspect aspect = sessionContext.getTraceAspect();
			if (aspect != null) {
				aspect.afterFilterEnd(sessionContext);
			}
			// 记录accesslog
			if (sessionContext.getMonitorAccessLog() != null) {
				int statusCode = sessionContext.getHttpCode();
				if (statusCode == 0) {
					statusCode = HttpCode.HTTP_OK_CODE;
				}
				long bodyBytesSent = fullResponse == null ? 0 : fullResponse.content().readableBytes();
				//若 sessionContext.getUpstreamRespTime() 取出的值为 null ，则默认以 "-" 填充
				sessionContext.getMonitorAccessLog().endAndLog(sessionContext, statusCode, bodyBytesSent);
			}
		}

	}

	public FullHttpResponse getFullHttpResponse(int httpCode, ByteBuf body, HttpVersion httpVersion) {
		HttpResponseStatus status = HttpResponseStatus.valueOf(httpCode);
		return new DefaultFullHttpResponse(httpVersion, status, body == null ? Unpooled.EMPTY_BUFFER : body, false);
	}

	@Override
	public void shutdown() {
		MonitorAccessLog.shutdown();
	}

}
