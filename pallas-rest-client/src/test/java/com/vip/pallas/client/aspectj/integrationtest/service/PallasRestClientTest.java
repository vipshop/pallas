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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.localserver.LocalServerTestBase;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.vip.pallas.client.PallasRestClient;
import com.vip.pallas.client.thread.QueryConsoleTask;

public class PallasRestClientTest extends LocalServerTestBase {

	static HttpHost target;

	static {
		try {
			System.setProperty("VIP_PALLAS_QUERY_INTERVAL_SECONDS", "2");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

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
				response.setEntity(new StringEntity(res));
			}
		});
		target = start();
        serverBootstrap.setSocketConfig(SocketConfig.custom().setSoTimeout(61000).build());
		QueryConsoleTask.consoleQueryUrl = "http://localhost:" + target.getPort() + "/getPsListAndEsDomain";
        String serverUrl = "http://localhost:" + target.getPort();
        System.out.println("server listen at: " + serverUrl);
	}
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	/**
	 * 测试超时
	 * @throws Exception
	 */
	@Test
	public void testTimeout() throws Exception {
		RestclientService restclientService = new RestclientService("atoken", 1);
		thrown.expect(IOException.class);
		restclientService.queryTemplateThenClose("myTemplateId");
	}

	@Test
	public void testUpdatePsList() throws Exception {
		RestclientService restclientService = new RestclientService("atoken", 2000);
		String res = restclientService.queryTemplate("myTemplateId");
		assertThat(res).contains("ok");
		String oldDomain = QueryConsoleTask.esDomainMap.get("atoken");
		TimeUnit.SECONDS.sleep(3);
		res = restclientService.queryTemplateThenClose("myTemplateId");
		String newDomain = QueryConsoleTask.esDomainMap.get("atoken");
		assertThat(res).contains("ok");
		assertThat(oldDomain).isNotEqualTo(newDomain);

	}

}