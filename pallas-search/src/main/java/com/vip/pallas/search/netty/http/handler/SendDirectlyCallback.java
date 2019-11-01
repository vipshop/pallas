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

package com.vip.pallas.search.netty.http.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;

import com.vip.pallas.search.utils.PallasSearchProperties;
import com.vip.pallas.utils.LogUtils;
import io.netty.handler.codec.http.*;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.entity.DecompressingEntity;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.exception.PallasException;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.circuitbreaker.CircuitBreakerService;
import com.vip.pallas.search.filter.common.PallasRunner;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.filter.rest.RestInvokerFilter;
import com.vip.pallas.search.http.HttpCode;
import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.model.ShardGroup;
import com.vip.pallas.search.monitor.GaugeMonitorService;
import com.vip.pallas.search.rampup.RampupHandler;
import com.vip.pallas.search.timeout.TimeoutRetryController;
import com.vip.pallas.search.trace.TraceAspect;

import io.netty.buffer.Unpooled;
import io.netty.util.internal.ThrowableUtil;
import org.springframework.http.HttpHeaders;

public class SendDirectlyCallback implements FutureCallback<HttpResponse> {

	private static final String CONTENT_LENGTH = "Content-Length";
	private static Logger logger = LoggerFactory.getLogger(SendDirectlyCallback.class);
	private static final PallasException REST_INVOKER_ERROR_EXCEPTION = ThrowableUtil.unknownStackTrace(
			new PallasException(HttpCode.HTTP_BAD_GATEWAY_STR), RestInvokerFilter.class, "onError()");
	protected SessionContext sessionContext;
	protected AbstractFilterContext filterContext;
	protected DefaultFullHttpRequest outBoundRequest;
	protected HttpRequestBase httpRequest;
	protected HttpEntity httpEntity;
	protected HttpContext httpContext;
	protected HttpHost targetHost;
	protected String requestUrl;
	protected PallasRequest pallasRequest;
	private ShardGroup shardGroup;

	public SendDirectlyCallback(AbstractFilterContext filterContext, SessionContext sessionContext,
			DefaultFullHttpRequest outBoundRequest, HttpContext httpContext, HttpRequestBase httpRequest,
			HttpEntity httpEntity, HttpHost targetHost, String requestUrl, PallasRequest pallasRequest,
			ShardGroup shardGroup) {
		this.filterContext = filterContext;
		this.sessionContext = sessionContext;
		this.outBoundRequest = outBoundRequest;
		this.httpContext = httpContext;
		this.httpRequest = httpRequest;
		this.httpEntity = httpEntity;
		this.targetHost = targetHost;
		this.requestUrl = requestUrl;
		this.pallasRequest = pallasRequest;
		this.shardGroup = shardGroup;
	}

	public SessionContext getSessionContext() {
		return sessionContext;
	}

	protected void initEndUpstreamTimeInMonitorAccessLog() {
		if (sessionContext.getMonitorAccessLog() != null) {
			sessionContext.getMonitorAccessLog().endUpstreamTime();
		}
	}

	protected void recordMercury(Throwable throwable, int httpCode) {
		TraceAspect aspect = sessionContext.getTraceAspect();
		if (aspect != null) {
			aspect.afterRestEnd(throwable, httpCode);
		}
		//#554 pallas-search调用es全链路时间点跟踪
		if (sessionContext.getTimestampClientResponseRead() == -1) {
			sessionContext.setTimestampClientResponseRead(System.currentTimeMillis());
		}
	}

	protected void setResponseHeader(Header[] headers, FullHttpRequest fullHttpRequest,
			FullHttpResponse fullHttpResponse) {
		for (Header header : headers) {
			fullHttpResponse.headers().set(header.getName(), header.getValue());
		}
	}
	protected void handleCompleted(HttpResponse response) {
		setKeyTimeStamp();

		// #103 #116 默认加gzip头，es返回时需要解压
		autoDecompression(response);

		// in case we got a bad response.
		int responseStatusCode = response.getStatusLine().getStatusCode();
		if (!isSuccessfulResponse(responseStatusCode) && responseStatusCode != HttpCode.HTTP_NOT_FOUND && responseStatusCode != HttpCode.HTTP_BAD_REQUEST) {
			try {
				String res = EntityUtils.toString(response.getEntity(), "UTF-8");
				handleFailed(new HttpResponseException(response.getStatusLine().getStatusCode(), res));
			} catch (ParseException | IOException e) {
				handleFailed(e);
			}
			return;
		}
		try {

			HttpEntity entity = response.getEntity();
			byte[] content = null;
 			//#296 _scroll支持
			if (sessionContext.getRequest().isScrollFirst() || sessionContext.getRequest().isScrollContinue()) {
				content = injectTargetGroupIdIntoScroll(sessionContext.getRequest().getTargetGroupId(), entity);
				response.removeHeaders(CONTENT_LENGTH);
				response.setHeader(CONTENT_LENGTH, Integer.toString(content.length));
			} else {
				content = EntityUtils.toByteArray(entity);
			}

			// 监控 ES Response body的throughput
			GaugeMonitorService.incResponseThroughput(content.length);

			FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
					HttpVersion.valueOf(response.getProtocolVersion().toString()),
					HttpResponseStatus.valueOf(response.getStatusLine().getStatusCode()),
					Unpooled.wrappedBuffer(content));

			setResponseHeader(response.getAllHeaders(), outBoundRequest, fullHttpResponse);

			initEndUpstreamTimeInMonitorAccessLog();
			// mercury接入
			recordMercury(null, HttpCode.HTTP_OK_CODE);
			// 如果是5XX,暂时不做任何处理
			sessionContext.setRestFullHttpResponse(fullHttpResponse);
			filterContext.fireNext(sessionContext);
			//预热其它索引（版本）
			RampupHandler.rampupIfNecessary(targetHost, requestUrl, outBoundRequest, httpRequest, httpEntity, pallasRequest.getIndexName(), pallasRequest.getLogicClusterId());
		} catch (Exception ex) {
			handleFailed(ex);
		}
	}

	private boolean isSuccessfulResponse(int responseStatusCode) {
		return responseStatusCode >= HttpCode.HTTP_OK_CODE && responseStatusCode < HttpCode.HTTP_REDIRECT_CODE;
	}

	private void autoDecompression(HttpResponse response){
		try {
			if (!PallasSearchProperties.SEARCH_GZIP_COMPRESSION) return;
			Header contentEncoding = response.getLastHeader(HttpHeaders.CONTENT_ENCODING);
			if (null == contentEncoding) return;
			// decompression
			if (contentEncoding.getValue().equals(HttpHeaderValues.GZIP.toString()) || contentEncoding.getValue().equals(HttpHeaderValues.X_GZIP.toString())) {
				response.setEntity(new DecompressingEntity(response.getEntity(), GZIPInputStream::new));
			} else if (contentEncoding.getValue().equals(HttpHeaderValues.DEFLATE.toString())) {
				response.setEntity(new DecompressingEntity(response.getEntity(), DeflaterInputStream::new));
			}
		}catch (Exception e){
			// do nothing if sth wrong happened
		}
	}

	private void setKeyTimeStamp() {
		Long connected = (Long) httpContext.getAttribute("connected");
		Long responseReceived = (Long) httpContext.getAttribute("responseReceived");
		if (connected != null) {
			sessionContext.setTimestampClientConnected(connected);
			httpContext.removeAttribute("connected");
		}
		if (responseReceived != null) {
			sessionContext.setTimestampClientResponseReceived(responseReceived);
			httpContext.removeAttribute("responseReceived");
		}
	}

	/**
	 * 针对 Scroll 请求需要把 当时发送命中的targetGroupId 注入到返回的scrollId 去
	 * 该方法是基于 EntityUtils.toByteArray(entity) 的实现来修改，原理是直接读取头4096字节然后来修改，后面的直接字节流拷贝
	 * @param targetGroupId
	 * @param entity
	 * @return
	 */
	private byte[] injectTargetGroupIdIntoScroll(Long targetGroupId, HttpEntity entity) throws IOException {
		Args.notNull(entity, "Entity");
		final InputStream instream = entity.getContent();
		if (instream == null) {
			return null;
		}
		try {
			Args.check(entity.getContentLength() <= Integer.MAX_VALUE,
					"HTTP entity too large to be buffered in memory");
			int i = (int)entity.getContentLength();
			if (i < 0) {
				i = 4096;
			}
			final ByteArrayBuffer buffer = new ByteArrayBuffer(i);
			final byte[] tmp = new byte[4096];
			int l = instream.read(tmp);
			if (l != -1) {
				String first4096 = new String(tmp, 0, l);
				int scrollStartIdx = first4096.indexOf("\"_scroll_id\":");

				int firstQuoIdx = first4096.indexOf('\"', scrollStartIdx + 13);
				int endQuoIdx = first4096.indexOf('\"', firstQuoIdx + 2);
				if (scrollStartIdx != -1 && firstQuoIdx != -1 && endQuoIdx != -1) {
					String scrollId = first4096.substring(firstQuoIdx+1, endQuoIdx);
					first4096 = first4096.replaceFirst(scrollId, scrollId + "[" + targetGroupId + "]");
					byte[] first4096Bytes = first4096.getBytes();
					buffer.append(first4096Bytes, 0, first4096Bytes.length);

				} else {//解析不出scrollId 退化直接把原数组存回去
					buffer.append(tmp, 0, l);
				}
			}

			while((l = instream.read(tmp)) != -1) {
				buffer.append(tmp, 0, l);
			}
			return buffer.toByteArray();
		} finally {
			instream.close();
		}
	}

	protected void doCircuitBreaker() {
		PallasRequest request = sessionContext.getRequest();
		if (shardGroup != null && request.isCircuitBreakerOn()) {
			CircuitBreakerService.getInstance().handleFailedRequestCounter(shardGroup.getId());
		}
	}

	protected void handleFailed(Exception ex, int httpCode) {
		doCircuitBreaker();
		LogUtils.error(logger, sessionContext.getRequest().getTemplateId(),"request failed: " + ((HttpClientContext) httpContext).getTargetHost().toHostString()
				+ " " + ((HttpClientContext) httpContext).getRequest().getRequestLine());
		LogUtils.error(logger, sessionContext.getRequest().getTemplateId(), ex.toString(), ex);
		TimeoutRetryController.notifyGovernor();
		initEndUpstreamTimeInMonitorAccessLog();
		sessionContext.setHttpCode(httpCode);
		//#554 pallas-search调用es全链路时间点跟踪
		if (sessionContext.getTimestampClientResponseRead() == -1) {
			sessionContext.setTimestampClientResponseRead(System.currentTimeMillis());
		}
		// mercury接入
		recordMercury(ex, httpCode);
		REST_INVOKER_ERROR_EXCEPTION.setMessage(ex.toString());
		PallasRunner.errorProcess(sessionContext, REST_INVOKER_ERROR_EXCEPTION);
	}

	protected void handleFailed(Exception ex) {
		handleFailed(ex, HttpCode.HTTP_BAD_GATEWAY);
	}
	
	protected void handleCancelled() {
		initEndUpstreamTimeInMonitorAccessLog();
		sessionContext.setHttpCode(HttpCode.HTTP_BAD_GATEWAY);
		//#554 pallas-search调用es全链路时间点跟踪
		if (sessionContext.getTimestampClientResponseRead() == -1) {
			sessionContext.setTimestampClientResponseRead(System.currentTimeMillis());
		}
		REST_INVOKER_ERROR_EXCEPTION.setMessage("Cancelled");
		PallasRunner.errorProcess(sessionContext, REST_INVOKER_ERROR_EXCEPTION);
		LogUtils.error(logger, sessionContext.getRequest().getTemplateId(), "Request cancelled.");
	}

	@Override
	public void completed(HttpResponse response) {
		handleCompleted(response);
	}

	@Override
	public void failed(Exception e) {
		handleFailed(e);
	}

	@Override
	public void cancelled() {
		handleCancelled();
	}
}
