package com.vip.pallas.search.timeout;

import java.net.SocketTimeoutException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;

import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.http.HttpCode;
import com.vip.pallas.search.netty.http.handler.SendDirectlyCallback;

import io.netty.handler.codec.http.DefaultFullHttpRequest;

public class TimeoutFutureCallback extends SendDirectlyCallback {

	private AsyncCall asyncCall;
	private AtomicInteger failedCount = new AtomicInteger(0);
	private AtomicInteger cancelCount = new AtomicInteger(0);

	public TimeoutFutureCallback(AsyncCall asyncCall, AbstractFilterContext filterContext, SessionContext sessionContext,
			DefaultFullHttpRequest outBoundRequest, HttpContext httpContext) {
		super(filterContext, sessionContext, outBoundRequest, httpContext);
		this.asyncCall = asyncCall;
	}
	
	@Override
	public void completed(HttpResponse response) {
		if (asyncCall.setDone()) {
			// cancel all the requests.
			try {
				cancelAllRequests(false);
			} finally {
				handleCompleted(response);
			}
		}
	}

	@Override
	public void failed(Exception ex) {
		if (ex instanceof SocketTimeoutException) { // retry only it is SocketTimeoutException.
			if (failedCount.incrementAndGet() == asyncCall.retryPolicy.getTotalCountIncludedFirstTime()) {
				if (asyncCall.setDone()) {
					try {
						cancelAllRequests(true);
					} finally {
						handleFailed(ex, HttpCode.HTTP_RETRY_TIMEOUT);
					}
				}
			} else {
				asyncCall.executeRequest();
			}
		} else { // if meets other exceptions, stop retry and return.
			if (asyncCall.setDone()) {
				try {
					cancelAllRequests(true);
				} finally {
					handleFailed(ex);
				}
			}
		}
	}

	@Override
	public void cancelled() {
		if (cancelCount.incrementAndGet() == asyncCall.retryPolicy.getTotalCountIncludedFirstTime()) {
			if (asyncCall.setDone()) {
				TimeoutRetryController.notifyGovernor();
				handleCancelled();
			}
		}
	}
	
	private void cancelAllRequests(boolean failed) {
		try {
			for (Future<HttpResponse> future: asyncCall.futureList) {
				future.cancel(true);
			}
			asyncCall.logRetryStatisticsIfNeeded(failed);
		} finally {
			TimeoutRetryController.notifyGovernor();
		}
	}
}
