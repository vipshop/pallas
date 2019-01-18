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

package com.vip.pallas.client.aspectj.integrationtest.service;

import java.io.IOException;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalServerTestBase;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.Before;

import com.vip.pallas.client.PallasRestClient;
import com.vip.pallas.client.PallasRestClientBuilder;
import com.vip.pallas.client.thread.QueryConsoleTask;

public class MockHttpService extends LocalServerTestBase {

	static HttpHost target;



	@Before
	public void before() throws Exception {

		this.serverBootstrap.registerHandler("/msearch/_search/template", new HttpRequestHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response,     HttpContext context) throws HttpException, IOException {
                response.setStatusCode(HttpStatus.SC_OK);
				String res = "ok;";
				for (Header header : request.getAllHeaders()) {
					String headerName = header.getName();
					String headerValue = header.getValue();
					if (PallasRestClient.DOMAIN_HEADER_NAME.equals(headerName)) {  
						res +=  headerValue;
					}
					if (PallasRestClient.TEMPLATE_ID_HEADER_NAME.equals(headerName)) {
						res += headerValue;
					}
					if (HttpHeaders.CONTENT_TYPE.equals(headerName)) {
						res += headerValue;
					}
				}

				try {
					TimeUnit.SECONDS.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				response.setEntity(new StringEntity(res));
            }
        });
		this.serverBootstrap.registerHandler("/getPsListAndEsDomain", new HttpRequestHandler() {
			boolean switchRes = true;
			@Override
			public void handle(HttpRequest request, HttpResponse response, HttpContext context)
					throws HttpException, IOException {
				response.setStatusCode(HttpStatus.SC_OK);
				String res = null;
				if (switchRes) {
					res = "{\"data\":{" + "  \"domain\":\"" + System.currentTimeMillis() + "\","
							+ "  \"psList\":[\"localhost:" + target.getPort() + "\"]" + "}}";
					switchRes = false;
				} else {
					res = "{\"data\":{" + "  \"domain\":\"" + System.currentTimeMillis() + "\","
							+ "  \"psList\":[\"127.0.0.1:" + target.getPort() + "\"]" + "}}";
					switchRes = true;
				}

				try {
					TimeUnit.MILLISECONDS.sleep(150 + new Random().nextInt(100));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				response.setEntity(new StringEntity("response return:" + request));
			}
		});
		target = start();
        serverBootstrap.setSocketConfig(SocketConfig.custom().setSoTimeout(61000).build());
		QueryConsoleTask.consoleQueryUrl = "http://localhost:" + target.getPort() + "/getPsListAndEsDomain";
        String serverUrl = "http://localhost:" + target.getPort();
        System.out.println("server listen at: " + serverUrl);
	}

	final static RestClient buildClient = RestClient
			.builder(HttpHost.create("localhost:9225"), HttpHost.create("127.0.0.1:9225"))
			.setMaxRetryTimeoutMillis(5000).build();
	final static HttpEntity entity = new NStringEntity("{}", ContentType.APPLICATION_JSON);

	public static void main(String[] args) throws Exception {
		for (int i = 0; i < 1; i++) {
			System.out.println("starting the " + i + " request");
			// System.out.println(HttpClient.httpGet("http://localhost:9225/_health_check"));
			// for (int j = 0; j < 1; j++)
			new Thread(new HttpQuestTask(i)).start();
			// TimeUnit.MILLISECONDS.sleep(100);
		}
		TimeUnit.SECONDS.sleep(60);
		System.exit(1);
	}

	static class HttpQuestTask implements Runnable {

		private int i;

		public HttpQuestTask(int i) {
			this.i = i;
		}

		@Override
		public void run() {
			try {
				// HttpClient.httpGet("http://localhost:9225/getPsListAndEsDomain");

				// Response indexResponse = buildClient.performRequest("GET", "/getPsListAndEsDomain?i=" + i,
				// Collections.<String, String> emptyMap(), entity, new Header[0]);
				// System.out.println("$$$$" + EntityUtils.toString(indexResponse.getEntity(), StandardCharsets.UTF_8));
				PallasRestClient restClient = PallasRestClientBuilder.buildClient("G9NqpEhYjCtU6Ao9aNgYow==");
				HttpEntity entity = new NStringEntity(
						"{\n" + "    \"id\" : \"yy_test_get1id1\",\n" + "    \"params\" : {}\n" + "}",
						ContentType.APPLICATION_JSON);
				Response  r = restClient.performRequest("POST", "/yy_test/_search/template", Collections.EMPTY_MAP,
						"yy_test_get1id1", entity);
				System.out.println(EntityUtils.toString(r.getEntity()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}