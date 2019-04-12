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

package com.vip.pallas.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalServerTestBase;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockLocalHttpServerService extends LocalServerTestBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(MockLocalHttpServerService.class);
	Map<String, HttpRequestHandler> handlerMap = new HashMap<>();
	public static String MOCKES_HOST_PORT;

	public void registerNewHandler(String urlSuffix, HttpRequestHandler handler){
		this.handlerMap.put(urlSuffix, handler);
	}
	
	public void startServer(){
		registerNewHandler("/api", new HttpRequestHandler() {
			@Override
			public void handle(HttpRequest request, HttpResponse response,     HttpContext context) throws HttpException, IOException {
				response.setEntity(new StringEntity("i'm ok."));
				response.setStatusCode(HttpStatus.SC_OK);
			}
		});

		registerNewHandler("/_cat/*", new HttpRequestHandler() {
			@Override
			public void handle(HttpRequest request, HttpResponse response,     HttpContext context) throws HttpException, IOException {
				response.setHeader("Content-Type", "text/plain;charset utf-8");
				response.setEntity(new StringEntity("127.0.0.1  39  97 1 0.08 0.13 0.09 mdi * 127.0.0.1"));
				response.setStatusCode(HttpStatus.SC_OK);
			}
		});
		AtomicInteger i = new AtomicInteger(0);
		registerNewHandler("/vfeature/*", new HttpRequestHandler() {
			@Override
			public void handle(HttpRequest request, HttpResponse response,     HttpContext context) throws HttpException, IOException {
				response.setHeader("Content-Type", "application/json;charset utf-8");
				response.setEntity(new StringEntity("{\"item\":{\"id\","+i.incrementAndGet()+"}}"));
				try {
					TimeUnit.MILLISECONDS.sleep(160);
				} catch (InterruptedException e) {
					e.printStackTrace();
					response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
					return;
				}
				response.setStatusCode(HttpStatus.SC_OK);
			}
		});


		registerNewHandler("/_search/*", new HttpRequestHandler() {
			@Override
			public void handle(HttpRequest request, HttpResponse response,     HttpContext context) throws HttpException, IOException {
				response.setHeader("Content-Type", "application/json;charset utf-8");
				response.setEntity(new StringEntity("{\"result\":\"ok\"}"));
				response.setStatusCode(HttpStatus.SC_OK);
			}
		});

		registerNewHandler("/_cluster/settings*", new HttpRequestHandler() {
			@Override
			public void handle(HttpRequest request, HttpResponse response,     HttpContext context) throws HttpException, IOException {
				response.setHeader("Content-Type", "application/json;charset utf-8");
				response.setEntity(new StringEntity("{\"persistent\":{\"indices.store.throttle.max_bytes_per_sec\":\"500mb\"}," +
						"\"transient\":{\"cluster.routing.allocation.enable\":\"all\",\"cluster.routing.allocation.exclude._ip\":" +
						"\"127.0.0.1\",\"cluster.routing.allocation.node_concurrent_recoveries\":" +
						"\"2\",\"cluster.routing.rebalance.enable\":\"none\",\"indices.recovery.max_bytes_per_sec\":" +
						"\"200mb\",\"indices.store.throttle.type\":\"none\"}}"));
				response.setStatusCode(HttpStatus.SC_OK);
			}
		});

		try {
			for (String urlSuffix : handlerMap.keySet()) {
				HttpRequestHandler tempHandler = handlerMap.get(urlSuffix);
				this.serverBootstrap.registerHandler(urlSuffix, tempHandler);
			}
			//serverBootstrap.setListenerPort(9200);
			HttpHost target = start();
			serverBootstrap.setSocketConfig(SocketConfig.custom().setSoTimeout(61000).build());
			MOCKES_HOST_PORT = "localhost:" + target.getPort();
			System.out.println("server listen at: " + MOCKES_HOST_PORT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
