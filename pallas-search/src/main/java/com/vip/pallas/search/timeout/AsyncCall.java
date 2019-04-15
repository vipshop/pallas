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
	public AtomicInteger failedCount = new AtomicInteger(0);
	public AtomicInteger cancelCount = new AtomicInteger(0);
	private HttpContext httpContext;
	private DefaultFullHttpRequest outBoundRequest;
	private AbstractFilterContext filterContext;
	private SessionContext sessionContext;
	private PallasRequest pallasRequest;

	private static ExecutorService retryExecutor = Executors.newFixedThreadPool(PallasSearchProperties.SEARCH_RETRY_THREADS,
			new PallasThreadFactory("timeout-retry"));
	private HttpEntity entity;

	public AsyncCall(CloseableHttpAsyncClient httpClient, TryPolicy retryPolicy, HttpHost targetHost, String newURL, String templateId,
			AbstractFilterContext filterContext, SessionContext sessionContext, DefaultFullHttpRequest outBoundRequest, HttpContext httpContext, PallasRequest pallasRequest) {
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
		this.filterContext = filterContext;
		this.sessionContext = sessionContext;
		this.pallasRequest = pallasRequest;
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
				String thisRequestUrl = newURL;
				if (retry) { // if it's a retry request, use another shardGroup instead of the failed one.
					ShardGroup shardGroup = sessionContext.getRequest().getShardGroup();
					if (shardGroup != null) {
						List<ShardGroup> shardGroupList = sessionContext.getRequest().getShardGroupList();
						List<ShardGroup> shardGroupListCopy = (ArrayList<ShardGroup>) ((ArrayList) shardGroupList).clone();
						// remove the failed groups and the circuteBreaker-open groups.
						shardGroupListCopy.removeIf(g -> {
							return g.getId().equals(shardGroup.getId())
									|| CircuitBreakerService.getInstance().getOpenGroupsList().contains(g.getId());
						});
						int randomIndex = InternalThreadLocalMap.get().random().nextInt(shardGroupListCopy.size());
						ShardGroup group = shardGroupListCopy.get(randomIndex);
						sessionContext.getRequest().setShardGroup(group); // update shardGroup in request, in case
																			// circuitBreaker counts the wrong server.
						String ip = group.getServerList().get(InternalThreadLocalMap.get().random().nextInt(group.getServerList().size()));
						try {
							targetHost = RestInvokerFilter.constractAtargetHost(ip);
							thisRequestUrl = replaceUriValue(thisRequestUrl, "preference=_prefer_nodes:", group.getPreferNodes());
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

				String urlWithTimeout = replaceUriValue(thisRequestUrl, "timeout=", timeoutMillis + "ms");
				request.setURI(URI.create(urlWithTimeout));
				request.setConfig(RequestConfig.custom()
						.setConnectionRequestTimeout(timeoutMillis)
						.setConnectTimeout(timeoutMillis).setSocketTimeout(timeoutMillis)
						.build());
				// record the start time.
				startCallTime = System.currentTimeMillis();
				LOGGER.info("{}th request routes to: {}, {}", count, targetHost.getHostName(), urlWithTimeout);
				if (count > 1) {
					LOGGER.info("query templateId:{} timeout, now start {}th try with real timeout = {}, request: {}",
							templateId, count, timeoutMillis,
							targetHost.getHostName() + ":" + targetHost.getPort() + urlWithTimeout);
				}
				// if(this.futureCallback == null){
				this.futureCallback = new TimeoutFutureCallback(this, filterContext, sessionContext, outBoundRequest,
						httpContext, null, entity, targetHost, newURL, pallasRequest, pallasRequest.getShardGroup());
				// }
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

	public static String replaceUriValue(String uri, String key, String newValue) {
		int keyIndex = uri.indexOf(key);
		if (keyIndex > 0) { // exist key
			int andIndex = uri.substring(keyIndex).indexOf("&");
			String oldVal = null;
			if (andIndex > 0) {
				oldVal = uri.substring(keyIndex).substring(0, andIndex);
			} else {
				oldVal = uri.substring(keyIndex);
			}
			return uri.replace(oldVal, key + newValue);
		} else if (uri.indexOf("?") > 0) {
			return uri += "&" + key + newValue;
		} else {
			return uri += "?" + key + newValue;
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
				// in case of concurrent modify done
				if (timeLeft < 0 && done.compareAndSet(false, false)) {
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
