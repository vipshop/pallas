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

package com.vip.pallas.client.aspectj.integrationtest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.client.PallasRestClient;
import com.vip.pallas.client.PallasRestClientBuilder;

public class RestTest {
	private static Logger logger = LoggerFactory.getLogger(RestTest.class);

	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InterruptedException, IOException {
		// System.setProperty("VIP_PALLAS_CONSOLE_QUERY_URL", "http://localhost:8080/ss/query_pslist_and_domain.json");
		System.setProperty("VIP_PALLAS_QUERY_INTERVAL_SECONDS", "10");
		final PallasRestClient buildClient = PallasRestClientBuilder.buildClient("cGKJojsqaOFLPMZJHP0Dsg==", 1000);
		final HttpEntity entity = new NStringEntity(
				"{\n" + "    \"id\" : \"yy_test_get1id\",\n" + "    \"params\" : {}\n" + "}",
				ContentType.APPLICATION_JSON);
		ExecutorService pool = Executors.newFixedThreadPool(20);
		for (int i = 0; i < 10000000; i++) {
			if (i % 10 == 0) {
				TimeUnit.MILLISECONDS.sleep(100);
			}
			pool.submit(new Runnable() {

				@Override
				public void run() {
					Response indexResponse;
					try {
						indexResponse = buildClient.performRequest("POST",
								"/yy_test/_search/template?request_cache=true", Collections.EMPTY_MAP, "yy_test_get1id",
								entity);
						// System.err.println(EntityUtils.toString(indexResponse.getEntity()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// for (Header header : indexResponse.getHeaders()) { System.out.println(header.getName() + ": " +
					// header.getValue()); }

				}
			});
		}

	}
}