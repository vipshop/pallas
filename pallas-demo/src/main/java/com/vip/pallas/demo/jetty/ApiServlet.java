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

package com.vip.pallas.demo.jetty;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.nio.entity.NStringEntity;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

public class ApiServlet extends HttpServlet {

	private static final long serialVersionUID = 2659185401401243102L;

	private static CloseableHttpClient httpClient = HttpClients.createDefault();

	public ApiServlet() {
		super();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		setRequestAndResponse(request, response);

		String locationUrl = getLocaltionUrl(request);

		HttpGet httpGet = new HttpGet(locationUrl);
		if(locationUrl.contains("/esproxy")){
			httpGet.setHeader("es-cluster", request.getHeader("es-cluster"));
			httpGet.setHeader("es-host", request.getHeader("es-host"));
			httpGet.setHeader("es-request", request.getHeader("es-request"));
		}

		CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
		response.getOutputStream().write(IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8").getBytes());
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		setRequestAndResponse(request, response);

		String locationUrl = getLocaltionUrl(request);

		HttpPost httpPost = new HttpPost(locationUrl);
		httpPost.setHeader("Content-Type", request.getHeader("Content-Type"));
		httpPost.setHeader("es-cluster", request.getHeader("es-cluster"));
		httpPost.setHeader("es-host", request.getHeader("es-host"));
		httpPost.setHeader("es-request", request.getHeader("es-request"));
		httpPost.setEntity(new NStringEntity(IOUtils.toString(request.getInputStream(), "UTF-8")));

		CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
		response.getOutputStream().write(IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8").getBytes());
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setRequestAndResponse(request, response);

		String locationUrl = getLocaltionUrl(request);

		HttpPut httpPut = new HttpPut(locationUrl);
		httpPut.setHeader("Content-Type", request.getHeader("Content-Type"));
		httpPut.setHeader("es-cluster", request.getHeader("es-cluster"));
		httpPut.setHeader("es-host", request.getHeader("es-host"));
		httpPut.setHeader("es-request", request.getHeader("es-request"));
		httpPut.setEntity(new NStringEntity(IOUtils.toString(request.getInputStream(), "UTF-8")));

		CloseableHttpResponse httpResponse = httpClient.execute(httpPut);
		response.getOutputStream().write(IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8").getBytes());
	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		setRequestAndResponse(request, response);

		String locationUrl = getLocaltionUrl(request);

		HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(locationUrl);
		httpDelete.setHeader("Content-Type", request.getHeader("Content-Type"));
		httpDelete.setHeader("es-cluster", request.getHeader("es-cluster"));
		httpDelete.setHeader("es-host", request.getHeader("es-host"));
		httpDelete.setHeader("es-request", request.getHeader("es-request"));
		httpDelete.setEntity(new NStringEntity(IOUtils.toString(request.getInputStream(), "UTF-8")));

		CloseableHttpResponse httpResponse = httpClient.execute(httpDelete);
		response.getOutputStream().write(IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8").getBytes());

	}

	private void setRequestAndResponse(HttpServletRequest request, HttpServletResponse response) throws IOException{
		request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
	}

	private String getLocaltionUrl(HttpServletRequest request) {
		String basePath = request.getScheme() + "://" + "127.0.0.1:8080";
		String locationUrl = basePath + request.getRequestURI();
		String queryValue = request.getQueryString();
		if (StringUtils.isNotBlank(queryValue)) {
			locationUrl += "?" + queryValue;
		}
		return locationUrl;
	}

	class HttpDeleteWithBody extends HttpEntityEnclosingRequestBase {
		public static final String METHOD_NAME = "DELETE";
		public String getMethod() { return METHOD_NAME; }

		public HttpDeleteWithBody(final String uri) {
			super();
			setURI(URI.create(uri));
		}
		public HttpDeleteWithBody(final URI uri) {
			super();
			setURI(uri);
		}
		public HttpDeleteWithBody() { super(); }
	}

}