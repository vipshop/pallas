package com.vip.pallas.search.netty.http.handler;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.exception.PallasException;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.PallasRunner;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.filter.rest.RestInvokerFilter;
import com.vip.pallas.search.http.HttpCode;
import com.vip.pallas.search.monitor.GaugeMonitorService;
import com.vip.pallas.search.timeout.TimeoutRetryController;
import com.vip.pallas.search.trace.TraceAspect;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.internal.ThrowableUtil;

public class SendDirectlyCallback implements FutureCallback<HttpResponse> {

	private static final String CONTENT_LENGTH = "Content-Length";
	private static Logger logger = LoggerFactory.getLogger(SendDirectlyCallback.class);
	private static final PallasException REST_INVOKER_ERROR_EXCEPTION = ThrowableUtil.unknownStackTrace(
			new PallasException(HttpCode.HTTP_BAD_GATEWAY_STR), RestInvokerFilter.class, "onError()");
	SessionContext sessionContext;
	AbstractFilterContext filterContext;
	DefaultFullHttpRequest outBoundRequest;
	HttpContext httpContext;

	public SendDirectlyCallback(AbstractFilterContext filterContext, SessionContext sessionContext,
			DefaultFullHttpRequest outBoundRequest, HttpContext httpContext) {
		this.filterContext = filterContext;
		this.sessionContext = sessionContext;
		this.outBoundRequest = outBoundRequest;
		this.httpContext = httpContext;

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
		} catch (Exception ex) {
			handleFailed(ex);
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

	protected void handleFailed(Exception ex, int httpCode) {
		TimeoutRetryController.notifyGovernor();
		initEndUpstreamTimeInMonitorAccessLog();
		sessionContext.setHttpCode(httpCode);
		//#554 pallas-search调用es全链路时间点跟踪
		if (sessionContext.getTimestampClientResponseRead() == -1) {
			sessionContext.setTimestampClientResponseRead(System.currentTimeMillis());
		}
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
		logger.error("Request cancelled.");
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
