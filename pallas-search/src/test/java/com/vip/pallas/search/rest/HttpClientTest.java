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
