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

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import com.vip.pallas.utils.LogUtils;
import com.vip.pallas.search.utils.SearchLogEvent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.PallasHttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vip.pallas.search.filter.base.AbstractFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.circuitbreaker.CircuitBreakerService;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.model.TemplateWithTimeoutRetry;
import com.vip.pallas.search.netty.http.handler.SendDirectlyCallback;
import com.vip.pallas.search.service.PallasCacheFactory;
import com.vip.pallas.search.timeout.AsyncCall;
import com.vip.pallas.search.timeout.TryPolicy;
import com.vip.pallas.search.trace.TraceAspect;
import com.vip.pallas.search.utils.HttpClientUtil;
import com.vip.pallas.search.utils.PallasSearchProperties;
import com.vip.pallas.utils.PallasBasicProperties;

import io.netty.handler.codec.http.DefaultFullHttpRequest;

/**
 * rest proxy filter，负责http协议代理
 * 
 * @author dylan.xue
 *
 */
public final class RestInvokerFilter extends AbstractFilter {
	private static Logger logger = LoggerFactory.getLogger(RestInvokerFilter.class);

	public static String DEFAULT_NAME = PRE_FILTER_NAME + RestInvokerFilter.class.getSimpleName().toUpperCase();

	public CloseableHttpAsyncClient httpClient;

	private IdleConnectionEvictor connEvictor = null;
	
	public RestInvokerFilter() {
		ConnectionKeepAliveStrategy keepAliveStrategy = (response, context) -> PallasSearchProperties.HTTP_SERVER_KEEPALIVE_TIMEOUT;

		ConnectingIOReactor ioReactor = null;
		try {
			IOReactorConfig config = IOReactorConfig.custom().setSelectInterval(40)
					.setIoThreadCount(PallasSearchProperties.CONNECTION_IO_THREAD_NUM).build();
			ioReactor = new DefaultConnectingIOReactor(config);
		} catch (IOReactorException e) {
			LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, e.getMessage(), e);
			throw new RuntimeException(e);// Noncompliant
		}

		PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
		cm.setDefaultMaxPerRoute(PallasSearchProperties.CONNECTION_MAX_PER_ROUTE);
		cm.setMaxTotal(PallasSearchProperties.PALLAS_CONNECTION_MAX);

		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(PallasSearchProperties.HTTP_POOL_AQUIRE_TIMEOUT)
				.setConnectTimeout(PallasSearchProperties.HTTP_CONNECTION_TIMEOUT)
				.setSocketTimeout(PallasSearchProperties.HTTP_SOCKET_TIMEOUT)
				.build();

		if (httpClient == null) {
			httpClient = PallasHttpAsyncClientBuilder.create().setConnectionManager(cm).setKeepAliveStrategy(keepAliveStrategy)
					.setDefaultRequestConfig(requestConfig).setThreadFactory(
							new ThreadFactoryBuilder().setNameFormat("Pallas-Search-Http-Rest-Client").build())
					.build();
			httpClient.start();
		}

		if (connEvictor == null) {
			connEvictor = new IdleConnectionEvictor(cm);
			connEvictor.start();
		}
	}

	@Override
	public void shutdown() {
		if (connEvictor != null) {
			connEvictor.shutdown();
		}

		if (httpClient != null) {
			try {
				httpClient.close();
			} catch (IOException e) {
				LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, e.getMessage(), e);
			}
		}
	}

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void run(final AbstractFilterContext filterContext, final SessionContext sessionContext) throws Exception {
		// 生成发送的request对象
		DefaultFullHttpRequest outBoundRequest = sessionContext.getOutBoundHttpRequest();

		TraceAspect aspect = sessionContext.getTraceAspect();
		if (aspect != null) {
			aspect.beforeRestStart(sessionContext, outBoundRequest.headers());
		}

		// 添加通用header
		addCommonRequestHeaders(outBoundRequest);

		try {
			HttpHost targetHost = constractAtargetHost(sessionContext.getServiceInfo().getBackendAddress());
			PallasRequest pallasRequest = sessionContext.getRequest();

			String newURL = refactURL(pallasRequest, outBoundRequest.getUri());

			if (sessionContext.getMonitorAccessLog() != null) {
				sessionContext.getMonitorAccessLog().startUpstreamTime();
			}

			//#554 pallas-search调用es全链路时间点跟踪
			sessionContext.setTimestampClientStartExecute(System.currentTimeMillis());


			String templateId = pallasRequest.getTemplateId();
			HttpClientContext httpContext = HttpClientContext.create();


			if (templateId != null && newURL.contains("/_search/template")) { // only search template enables  timeout-retry.
				TemplateWithTimeoutRetry configByIndexNameTemplateName = PallasCacheFactory.getCacheService()
						.getConfigByTemplateIdAndCluster(templateId, pallasRequest.getLogicClusterId(),
								pallasRequest.getIndexName());
				if (configByIndexNameTemplateName == null || configByIndexNameTemplateName.getTimeout() == 0) { // no timeout control
					HttpRequestBase request = HttpClientUtil.getHttpUriRequest(targetHost, outBoundRequest);
					request.setURI(URI.create(newURL));
					httpClient.execute(targetHost, request, httpContext,
							new SendDirectlyCallback(filterContext, sessionContext, outBoundRequest, httpContext,
									request, null, targetHost, newURL, pallasRequest, null));
					// calculate the circuteBreaker TODO 是否在发出请求后再加？因为有可能是连接池满了，根本没发出请求
					if (pallasRequest.isCircuitBreakerOn() && pallasRequest.getShardGroup() != null) {
						CircuitBreakerService.getInstance().increaseServiceRequestCounter(pallasRequest.getShardGroup().getId());
					}
				} else {
					TryPolicy tp = new TryPolicy(configByIndexNameTemplateName.getRetry() + 1,
							configByIndexNameTemplateName.getTimeout());
					AsyncCall asyncCall = new AsyncCall(httpClient, tp, targetHost, newURL, templateId, filterContext,
							sessionContext, outBoundRequest, httpContext, pallasRequest);
					asyncCall.register();
				}
			} else {
				HttpRequestBase request = HttpClientUtil.getHttpUriRequest(targetHost, outBoundRequest);
				request.setURI(URI.create(newURL));
				httpClient.execute(targetHost, request, httpContext,
						new SendDirectlyCallback(filterContext, sessionContext, outBoundRequest, httpContext, request,
								null, targetHost, newURL, pallasRequest, null));
			}
		} catch (Exception ex) {
			LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, ex.getLocalizedMessage(), ex);
			throw ex;
		}
	}

	private void addCommonRequestHeaders(DefaultFullHttpRequest outBoundRequest){
		// #103 默认加Accept-Encoding:gzip,deflate 头
		outBoundRequest.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP_DEFLATE.toString());
	}

	public static HttpHost constractAtargetHost(final String address)
			throws URISyntaxException, UnknownHostException {
		String scheme = HttpHost.DEFAULT_SCHEME_NAME;
		// #1025 通过伪协议转换为URI，避免 IPv6处理不当
		final URI uri = new URI("foo://" + address);

		InetAddress netAddress = InetAddress.getByName(uri.getHost());
		int port = uri.getPort() >= 0 ? uri.getPort() : 80;

		HttpHost targetHost = new HttpHost(netAddress.getHostAddress(), port, scheme);
		return targetHost;
	}

	/**
	 * 针对选择不同类型的节点集，在URL上加上响应的配置
	 * @param pallasRequest
	 * @param uri
	 * @return
	 */
	private String refactURL(PallasRequest pallasRequest, String uri) {
		String newURL = uri;

		if ((uri.endsWith("/_search") || uri.endsWith("/_search/template")) && !uri.contains("scroll=")) {
			String preference = pallasRequest.getPreference();
			if (StringUtils.isNotBlank(preference)) {
				newURL += uri.contains("?") ? "&preference=" + preference : "?preference=" + preference;
			} else if (pallasRequest.isRoutePrimaryFirst()) {
				newURL += uri.contains("?") ? "&preference=_primary_first" : "?preference=_primary_first";
			} else if (pallasRequest.isRouteReplicaFirst()) {
				newURL += uri.contains("?") ? "&preference=_replica_first" : "?preference=_replica_first";
			} else if (newURL.contains("/_search/template")) {
				newURL += uri.contains("?") ? "&preference=_local" : "?preference=_local";
			}
		}
		return newURL;
	}

	public static class IdleConnectionEvictor extends Thread {
		private final NHttpClientConnectionManager connMgr;

		private volatile boolean stopped;

		public IdleConnectionEvictor(NHttpClientConnectionManager connMgr) {
			super("Pallas-Search-Rest-Threadpool-CloseIdle");
			this.connMgr = connMgr;
		}

		@Override
		public void run() {
			try {
				while (!stopped) {
					long a,b;
					synchronized (this) {
						wait(PallasSearchProperties.HTTP_CHECK_IDLE_INTERVAL_IN_MILS);

						a = System.currentTimeMillis();
						// Close expired connections
						connMgr.closeExpiredConnections();

						connMgr.closeIdleConnections(PallasSearchProperties.HTTP_SERVER_KEEPALIVE_TIMEOUT, TimeUnit.MILLISECONDS);
					}
					b = System.currentTimeMillis() - a;
					if (b > PallasBasicProperties.DEFAULT_PS_SIDE_THRESHOLD) {
						LogUtils.warn(logger, SearchLogEvent.NORMAL_EVENT,
								"IdleConnectionEvictor took too much time to close expire connections, took: {}(ms)", b);
					}
				}
			} catch (InterruptedException ex) {
				LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, ex.getMessage(), ex);
				Thread.currentThread().interrupt();
			}
		}

		public void shutdown() {
			stopped = true;
			synchronized (this) {
				notifyAll();
			}
		}

	}
}
