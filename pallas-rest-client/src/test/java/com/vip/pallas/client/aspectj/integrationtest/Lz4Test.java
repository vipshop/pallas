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

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;

import com.vip.pallas.client.PallasRestClient;
import com.vip.pallas.client.PallasRestClientBuilder;

public class Lz4Test {

	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, InterruptedException {
		final Header[] headers = new Header[2];
		headers[1] = new BasicHeader("accept-encoding", "lz4");
		headers[0] = new BasicHeader("X_PALLAS_SEARCH_UP_STREAM_URL", "127.0.0.1:9200");
		final PallasRestClient buildClient = PallasRestClientBuilder.buildClient("tokenHere");
		final HttpEntity entity = new NStringEntity("{}", ContentType.APPLICATION_JSON);
		Response indexResponse;
		try {
			indexResponse = buildClient.performRequest("GET", "/_stats",
					Collections.<String, String> emptyMap(), null, entity, headers);
			for (Header header : indexResponse.getHeaders()) {
				System.out.println(header.getName() + ": " + header.getValue());
			}
			// GzipDecompressingEntity gzipEntity = new GzipDecompressingEntity(indexResponse.getEntity());
			System.err.println(EntityUtils.toString(indexResponse.getEntity()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}