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

package com.vip.pallas.search.rest;

import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.HttpContext;
import org.junit.Ignore;

import com.vip.pallas.search.utils.PallasSearchProperties;

@Ignore
public class HttpClientTest {
	public static void main(String[] args){
		ConnectionKeepAliveStrategy keepAliveStrategy = new ConnectionKeepAliveStrategy() {

			@Override
			public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
				return PallasSearchProperties.HTTP_SERVER_KEEPALIVE_TIMEOUT;
			}

		};

		ConnectingIOReactor ioReactor = null;
		try {
			ioReactor = new DefaultConnectingIOReactor();
		} catch (IOReactorException e) {
			throw new RuntimeException(e);// Noncompliant
		}

		PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
		cm.setDefaultMaxPerRoute(PallasSearchProperties.CONNECTION_MAX_PER_ROUTE);
		cm.setMaxTotal(PallasSearchProperties.PALLAS_CONNECTION_MAX);

		RequestConfig requestConfig = RequestConfig.custom()
				.setConnectionRequestTimeout(PallasSearchProperties.HTTP_POOL_AQUIRE_TIMEOUT)
				.setConnectTimeout(PallasSearchProperties.HTTP_CONNECTION_TIMEOUT).setSocketTimeout(1)
				.build();

		CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom().setConnectionManager(cm).setKeepAliveStrategy(keepAliveStrategy)
				.setDefaultRequestConfig(requestConfig).build();
		httpClient.start();
		
		HttpGet request = new HttpGet("http://localhost:9200/api");
		request.setConfig(requestConfig);
		
		httpClient.execute(request, new FutureCallback<HttpResponse>(){

			@Override
			public void completed(HttpResponse result) {
				System.out.print("completed");
				System.out.println(result.getEntity().toString());
			}

			@Override
			public void failed(Exception ex) {
				System.out.println(ex instanceof TimeoutException);
				System.out.println(ex instanceof SocketTimeoutException);
				ex.printStackTrace();
				System.out.println(ex.getMessage());
			}

			@Override
			public void cancelled() {
				System.out.println("cancelled");
			}
			
		});
	}
}
