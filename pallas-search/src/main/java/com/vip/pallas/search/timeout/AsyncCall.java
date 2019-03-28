package com.vip.pallas.search.timeout;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.circuitbreaker.CircuitBreakerService;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.filter.rest.RestInvokerFilter;
import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.model.ShardGroup;
import com.vip.pallas.search.utils.HttpClientUtil;
import com.vip.pallas.search.utils.PallasSearchProperties;
import com.vip.pallas.thread.PallasThreadFactory;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.util.internal.InternalThreadLocalMap;

public class AsyncCall {

	private static final Logger LOGGER = LoggerFactory.getLogger(AsyncCall.class);
	final CloseableHttpAsyncClient httpClient;
	public long startCallTime;
	private AtomicBoolean canBeginAnotherRetry = new AtomicBoolean(true);
	public TryPolicy retryPolicy;
	public AtomicInteger retryCount;
	private AtomicBoolean done;
	public final ArrayList<Future<HttpResponse>> futureList;
	public TimeoutFutureCallback futureCallback;
	private String templateId;
	private HttpHost targetHost;
	private String newURL;
	private AtomicInteger executeCount = new AtomicInteger(0);
	private HttpContext httpContext;
	private DefaultFullHttpRequest outBoundRequest;

	private static ExecutorService retryExecutor = Executors.newFixedThreadPool(PallasSearchProperties.SEARCH_RETRY_THREADS,
			new PallasThreadFactory("timeout-retry"));
	private HttpEntity entity;
	private SessionContext sessionContext;
	
	public AsyncCall(CloseableHttpAsyncClient httpClient, TryPolicy retryPolicy, HttpHost targetHost, String newURL, String templateId,
			AbstractFilterContext filterContext, SessionContext sessionContext, DefaultFullHttpRequest outBoundRequest, HttpContext httpContext) {
		this.httpClient = httpClient;
		this.retryPolicy =  retryPolicy;
		this.targetHost = targetHost;
		this.newURL = newURL;
		this.templateId = templateId;
		this.sessionContext = sessionContext;
		this.outBoundRequest = outBoundRequest;
		this.httpContext = httpContext;
		this.retryCount = new AtomicInteger(0);
		this.done = new AtomicBoolean(false);
		this.futureList = new ArrayList<>(retryPolicy.getTotalCountIncludedFirstTime());
		this.futureCallback = new TimeoutFutureCallback(this, filterContext, sessionContext, outBoundRequest, httpContext);
	}
	
	public void register() {
		startCallTime = System.currentTimeMillis();
		executeRequest(false);
		// timeout-controller should do the check after it's begun.
		TimeoutRetryController.addRequest(this);
	}

	// retry indicates whether it is the first time to request the server.
	public void executeRequest(boolean retry) {
		if (!isDone()) {
			int count = retryCount.incrementAndGet();
			if (retryPolicy.allowRetry(count)) { // double check.
				int timeoutMillis =  (retryPolicy.getTotalCountIncludedFirstTime() + 1 - count) * retryPolicy.getTimeoutMillis();

				HttpRequestBase request = null;
				
				if (retry) { // if it's a retry request, use another shardGroup instead of the failed one.
					ShardGroup shardGroup = sessionContext.getRequest().getShardGroup();
					if (shardGroup != null) {
						List<ShardGroup> shardGroupList = sessionContext.getRequest().getShardGroupList();
						// remove the failed groups and the circuteBreaker-open groups.
						shardGroupList.removeIf(g -> {return g.getId().equals(shardGroup.getId()) || CircuitBreakerService.getInstance().getOpenGroupsList().contains(g.getId());});
						int randomIndex = InternalThreadLocalMap.get().random().nextInt(shardGroupList.size());
						ShardGroup group = shardGroupList.get(randomIndex);
						sessionContext.getRequest().setShardGroup(group); // update shardGroup in requst, in case circuitBreaker counts the wrong server.
						String ip = group.getServerList().get(InternalThreadLocalMap.get().random().nextInt(group.getServerList().size()));
						try {
							targetHost = RestInvokerFilter.constractAtargetHost(ip);
						} catch (Exception e) {
							LOGGER.error(e.getMessage(), e);
						}
					}
				}
				
				if (this.entity != null) {
					request = HttpClientUtil.getHttpUriRequest(targetHost, outBoundRequest, entity);
				} else {
					request = HttpClientUtil.getHttpUriRequest(targetHost, outBoundRequest, null);
					this.entity = ((HttpPost) request).getEntity();
				}

				String urlWithTimeout = newURL + "&timeout=" + timeoutMillis + "ms";
				request.setURI(URI.create(urlWithTimeout));
				request.setConfig(RequestConfig.custom()
						.setConnectionRequestTimeout(timeoutMillis)
						.setConnectTimeout(timeoutMillis).setSocketTimeout(timeoutMillis)
						.build());
				// record the start time.
				startCallTime = System.currentTimeMillis();
				if (count > 1) {
					LOGGER.info("query templateId:{} timeout, now start {}th try with real timeout = {}.", templateId,
							count, timeoutMillis);
				}
				Future<HttpResponse> future = httpClient.execute(targetHost, request, httpContext, futureCallback);
				// calculate the circuteBreaker TODO 是否在发出请求后再加？因为有可能是连接池满了，根本没发出请求
				PallasRequest pallasRequest = sessionContext.getRequest();
				if (pallasRequest.isCircuitBreakerOn() && pallasRequest.getShardGroup() != null) {
					CircuitBreakerService.getInstance().increaseServiceRequestCounter(pallasRequest.getShardGroup().getId());
				}
				executeCount.incrementAndGet();
				futureList.add(future);
				canBeginAnotherRetry.set(true);
				TimeoutRetryController.notifyGovernor();
			}
		}
	}

	public boolean setDone() {
		return done.compareAndSet(false, true);
	}
	
	public boolean isDone() {
		return done.get();
	}

	public long retryMaybe() {
		if (!isDone() && canBeginAnotherRetry.get()) {
			if (retryPolicy.allowRetry(retryCount.get() + 1)) {
				long now = System.currentTimeMillis();
				long timeLeft = startCallTime + retryPolicy.getTimeoutMillis() - now;
				if (timeLeft < 0) {
					canBeginAnotherRetry.set(false);
					retryExecutor.submit(new RetryTask(this));
				}
				return timeLeft;
			}
		}
		return Long.MAX_VALUE;
	}


	public void logRetryStatisticsIfNeeded(boolean failed) {
		if (executeCount.get() > 1) {
			if (failed) {
				LOGGER.info("{} retried for {} times but failed.", templateId, executeCount.get());
			} else {
				String finalURI = (String) httpContext.getAttribute("finalURI");
				httpContext.removeAttribute("finalURI");
				LOGGER.info("{} retried for {} times and winner is {}", templateId, executeCount.get(),
						finalURI);
			}
		}
	}

	static class RetryTask implements Runnable {

		private AsyncCall asyncCall;

		public RetryTask(AsyncCall asyncCall) {
			this.asyncCall = asyncCall;
		}

		@Override
		public void run() {
			asyncCall.executeRequest(true);
		}

	}
}
