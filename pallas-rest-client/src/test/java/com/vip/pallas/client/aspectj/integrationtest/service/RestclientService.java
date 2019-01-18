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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collections;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;

import com.vip.pallas.client.PallasRestClient;
import com.vip.pallas.client.PallasRestClientBuilder;

public class RestclientService {

	public static String postContentFileName = "rest.json";

	public static String restMethod = "POST"; 

	public static String endpoint = "/msearch/_search/template";
	
	public PallasRestClient buildClient;

	private long timeout;

	private String token;

	public RestclientService(String token, long timeout) {
		try {
			this.buildClient = PallasRestClientBuilder.buildClient(token, timeout);
			this.timeout = timeout;
			this.token = token;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String queryTemplate(String templateId) throws Exception {
		String query = loadFile(postContentFileName);
		HttpEntity entity = new NStringEntity(query, ContentType.APPLICATION_JSON);
		Header[] headers = new Header[0];
		Response indexResponse = buildClient.performRequest(restMethod, endpoint,
				Collections.<String, String> emptyMap(), templateId, entity, headers);
		return EntityUtils.toString(indexResponse.getEntity());
	}

	public String queryTemplateThenClose(String templateId) throws Exception {
		try {
			return queryTemplate(templateId);
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				PallasRestClientBuilder.closeClientByToken(token);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String loadFile(String path) {
		try {
			URL resource = RestclientService.class.getClassLoader().getResource(path);

			if (resource == null) {
				return null;
			}
			String res = IOUtils.toString(resource);

			if (res != null) {
				res = res.replace("\r\n", "");
				res = res.replace("\n", "");
			}
			return res;
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

}