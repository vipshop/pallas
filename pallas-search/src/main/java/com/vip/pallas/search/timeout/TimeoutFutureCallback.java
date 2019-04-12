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

package com.vip.pallas.search.timeout;

import java.net.SocketTimeoutException;
import java.util.concurrent.Future;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.protocol.HttpContext;

import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.http.HttpCode;
import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.model.ShardGroup;
import com.vip.pallas.search.netty.http.handler.SendDirectlyCallback;

import io.netty.handler.codec.http.DefaultFullHttpRequest;

public class TimeoutFutureCallback extends SendDirectlyCallback {

	private AsyncCall asyncCall;

	public TimeoutFutureCallback(AsyncCall asyncCall, AbstractFilterContext filterContext, SessionContext sessionContext,
			DefaultFullHttpRequest outBoundRequest, HttpContext httpContext, HttpRequestBase httpRequest,
			HttpEntity httpEntity, HttpHost targetHost, String requestUrl, PallasRequest pallasRequest,
			ShardGroup shardGroup) {
		super(filterContext, sessionContext, outBoundRequest, httpContext, httpRequest, httpEntity, targetHost,
				requestUrl, pallasRequest, shardGroup);
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
			if (asyncCall.failedCount.incrementAndGet() == asyncCall.retryPolicy.getTotalCountIncludedFirstTime()) {
				if (asyncCall.setDone()) {
					try {
						cancelAllRequests(true);
					} finally {
						handleFailed(ex, HttpCode.HTTP_RETRY_TIMEOUT);
					}
				}
			} else {
				doCircuitBreaker(); // before retry, we count the circuitBreaker of the failed host.
				asyncCall.executeRequest(true);
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
		if (asyncCall.cancelCount.incrementAndGet() == asyncCall.retryPolicy.getTotalCountIncludedFirstTime()) {
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
