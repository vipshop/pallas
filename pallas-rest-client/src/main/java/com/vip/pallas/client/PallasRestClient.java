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

package com.vip.pallas.client;

import com.vip.pallas.client.exception.PallasTimeoutException;
import com.vip.pallas.client.lz4.PallasHttpAsyncResponseConsumerFactory;
import com.vip.pallas.client.thread.QueryConsoleTask;
import com.vip.pallas.client.util.PallasRestClientProperties;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.ResponseListener;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class PallasRestClient {

	private static Logger logger = LoggerFactory.getLogger(PallasRestClient.class);
	public static final String TEMPLATE_ID_HEADER_NAME  = "X-PALLAS-SEARCH-TEMPLATE-ID";
	public static final String CLIENT_TOKEN_HEADER_NAME  = "X-PALLAS-CLIENT-TOKEN";
	public static final String DOMAIN_HEADER_NAME = "X-PALLAS-SEARCH-ES-DOMAIN";
	public static final String CLIENT_MAXTIMEOUT_MS_HEADER_NAME = "X-PALLAS-CLIENT-MAXTIMEOUT-MS";
	public static final String HOST_HEADER = "Host";
	public static final Header LZ4_HEADER = new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "lz4");
	public static final Header JSON_HEADER = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
	public static final HashSet<String> TOKEN_SET = new HashSet<>();
	private static final QueryConsoleTask QUERY_CONSOLE_TASK = new QueryConsoleTask(TOKEN_SET);
	private RestClient restClient;
	private String clientToken;
	
	private static final ScheduledExecutorService CONSOLE_VISITOR_EXECUTOR = Executors
			.newSingleThreadScheduledExecutor(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r) {
					Thread thread = new Thread(r, "pallas-console-visitor");
					thread.setDaemon(true);
					return thread;
				}
			});

	static {
		CONSOLE_VISITOR_EXECUTOR.scheduleAtFixedRate(QUERY_CONSOLE_TASK, 1,
				PallasRestClientProperties.PALLAS_QUERY_INTERVAL_SECONDS, TimeUnit.SECONDS);
		logger.info("pallas console visitor started.");
	}

	private volatile Holder holder = new Holder() {

		@Override
		public RestClient getRestClient() {
			return restClient;
		}
	};

	private interface Holder {
		RestClient getRestClient();
	}

	/**
	 * Don't call me. Create me via the PallasRestClientBuilder.
	 */
	protected PallasRestClient(RestClient restClient, String clientToken, List<HttpHost> hosts) {
		this.restClient = restClient;
		if (restClient == null) {
			holder = new Holder() {
				@Override
				public RestClient getRestClient() {
					throw new IllegalArgumentException(
							"pallas rest-client is not ready, pls check your token and pallas console domain. make sure you get a valid pallas-search to connect to.");
				}
			};
		}
		setClientToken(clientToken);
	}

	public static void addNewToken(String token) {
		TOKEN_SET.add(token);
		// explicitly invoke the task to update the esDomain & psList.
		QUERY_CONSOLE_TASK.run();
	}

	public void updateInternalRestClient(RestClient newRestClient, List<HttpHost> hosts) {
		this.restClient = newRestClient;
		holder = new Holder() {
			@Override
			public RestClient getRestClient() {
				return restClient;
			}
		};
	}

	public void setClientToken(String clientToken) {
		if (StringUtils.isEmpty(clientToken)) {
			throw new IllegalArgumentException(clientToken);
		}
		this.clientToken = clientToken;
	}
	
	/**
	 * Add the templateId and the domainName and contentType to the headers and then perform the request by the
	 * restClient.
	 * @param method e.g. POST
	 * @param endpoint the search url, e.g. /msearch/_search/template
	 * @param params e.g. mykey1=1&mykey2=1
	 * @param templateId your templateId, leave it null if you do not intend to search by a template.
	 * @param entity query content
	 * @param headers headers. In particular, we will add templateId, domain and contentType to headers.
	 * @return
	 * @throws IOException
	 */
	public Response performRequest(String method, String endpoint, Map<String, String> params, String templateId, HttpEntity entity,
			Header... headers) throws IOException {
		forceToUseLz4IfExistAcceptEncoding(headers);
		Header[] newHeaders = genNewHeaderWithLz4EncodingAndClientTokenAndContentType(headers, templateId, getClientMaxTimeOutMills());

		Response response = null;
		try {
			response = holder.getRestClient().performRequest(method, endpoint, params, entity,
					new PallasHttpAsyncResponseConsumerFactory(), newHeaders);
		}catch (ResponseException e){ // Return ES exception detail for invokers
			logger.error("pallas restclient performRequest error: {}", e.getMessage(), e);
			throw e;
		}catch (IOException e) {
			logger.error("pallas restclient performRequest error: {}", e.getMessage(), e);
			String message = printPerformTimeoutError(endpoint, templateId, getClientMaxTimeOutMills());
			logger.error(e.getMessage(), e);
			throw new PallasTimeoutException(message, e);
		}

		return response;
	}



	public Response performRequest(String method, String endpoint, Map<String, String> params, String templateId, HttpEntity entity, Long maxTimeoutMils,
								   Header... headers) throws  IOException {

		if(maxTimeoutMils == null || maxTimeoutMils <= 0) {
			return performRequest(method, endpoint, Collections.<String, String>emptyMap(), templateId, entity, new Header[0]);
		}
		logger.info("maxTimeoutMils change to : {}", maxTimeoutMils);
		forceToUseLz4IfExistAcceptEncoding(headers);
		Header[] newHeaders = genNewHeaderWithLz4EncodingAndClientTokenAndContentType(headers, templateId, maxTimeoutMils);

		Response response = null;
		try {
			PallasSyncResponseListener responseListener = new PallasSyncResponseListener(maxTimeoutMils);
			holder.getRestClient().performRequestAsync(method, endpoint, params, entity, new PallasHttpAsyncResponseConsumerFactory(),
					responseListener, newHeaders);
			response = responseListener.get();
		} catch (IOException e) {
			String message = printPerformTimeoutError(endpoint, templateId, maxTimeoutMils);
			logger.error(e.getMessage(), e);
			throw new PallasTimeoutException(message, e);
		}

		return response;

	}

	private static void forceToUseLz4IfExistAcceptEncoding(Header... headers) {
		if (headers != null) {
			for (int i = 0; i < headers.length; i++) {
				Header h = headers[i];
				if (HttpHeaders.ACCEPT_ENCODING.equalsIgnoreCase(h.getName()) && !"lz4".equalsIgnoreCase(h.getValue())) {
					headers[i] = new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "lz4");
				}
			}
		}
	}

	public Response performRequestWithEmptyParamsAndNoHeaders(String method, String endpoint, String templateId, HttpEntity entity) throws IOException {
		return performRequest(method, endpoint, Collections.<String, String>emptyMap(), templateId, entity, new Header[0]);
	}

	public Response performRequestWithEmptyParamsAndNoHeaders(String method, String endpoint, String templateId, HttpEntity entity, Long maxTimeoutMils) throws IOException {
		return performRequest(method, endpoint, Collections.<String, String>emptyMap(), templateId, entity, maxTimeoutMils, new Header[0]);
	}


	private Header[] genNewHeaderWithLz4EncodingAndClientTokenAndContentType(Header[] headers, String templateId, Long maxTimeoutMils) {

		List<Header> headerList = new ArrayList<>();
		headerList.add(LZ4_HEADER);
		headerList.add(JSON_HEADER);
		headerList.add(new BasicHeader(DOMAIN_HEADER_NAME,
				QueryConsoleTask.getEsDomainByToken(clientToken)));
		if(templateId != null) {
			headerList.add(new BasicHeader(TEMPLATE_ID_HEADER_NAME, templateId));
		}
		if(clientToken != null) {
			headerList.add(new BasicHeader(CLIENT_TOKEN_HEADER_NAME, clientToken));
		}

		headerList.add(new BasicHeader(CLIENT_MAXTIMEOUT_MS_HEADER_NAME, String.valueOf(maxTimeoutMils)));

		if(headers != null && headers.length > 0) {
			headerList.addAll(Arrays.asList(headers));
		}

		return headerList.toArray(new Header[headerList.size()]);

	}

	public RestClient getInternalRestClient() {
		return this.restClient;
	}

	public void close() throws IOException {
		if (restClient != null) {
			restClient.close();
			logger.warn("restClient for token: {} closed.", clientToken);
		}
	}

	private String printPerformTimeoutError(String endPoint, String templateId, Long maxTimeoutMils) {
		String messageFormat = null;
		String message = null;
		String[] str = StringUtils.split(endPoint, "/");
		if(str == null || str.length <= 0 ) {
			messageFormat = "perform request timeout. token : {0}, cluster: {1}, endPoint : {2}, templateId : {3}, maxTimeoutMils : {4}";
			message = MessageFormat.format(messageFormat, clientToken, QueryConsoleTask.getEsDomainByToken(clientToken), endPoint, templateId, maxTimeoutMils);
			logger.error(message);
			return message;
		}

		String index = str[0];
		String template = StringUtils.substring(templateId, str[0].length() + 1);
		if(StringUtils.indexOf(templateId, index) == -1) {
			template = templateId;
		}
		messageFormat = "perform request timeout. token : {0}, cluster: {1}, index : {2}, templateId : {3}, maxTimeoutMils : {4}";
		message = MessageFormat.format(messageFormat, clientToken, QueryConsoleTask.getEsDomainByToken(clientToken), index, template, maxTimeoutMils);
		logger.error(message);
		return message;
	}

	private Long getClientMaxTimeOutMills() {
		return PallasRestClientBuilder.TIMEOUT_MILLS_MAP.get(clientToken);
	}

	/**
	 * Listener used in any sync performRequest calls, it waits for a response or an exception back up to a timeout
	 */

	static class PallasSyncResponseListener implements ResponseListener {
		private final CountDownLatch latch = new CountDownLatch(1);
		private final AtomicReference<Response> response = new AtomicReference<>();
		private final AtomicReference<Exception> exception = new AtomicReference<>();

		private final long timeout;

		PallasSyncResponseListener(long timeout) {
			assert timeout > 0;
			this.timeout = timeout;
		}

		@Override
		public void onSuccess(Response response) {
			Objects.requireNonNull(response, "response must not be null");
			boolean wasResponseNull = this.response.compareAndSet(null, response);
			if (wasResponseNull == false) {
				throw new IllegalStateException("response is already set");
			}

			latch.countDown();
		}

		@Override
		public void onFailure(Exception exception) {
			Objects.requireNonNull(exception, "exception must not be null");
			boolean wasExceptionNull = this.exception.compareAndSet(null, exception);
			if (wasExceptionNull == false) {
				throw new IllegalStateException("exception is already set");
			}
			latch.countDown();
		}

		/**
		 * Waits (up to a timeout) for some result of the request: either a response, or an exception.
		 */
		Response get() throws IOException {
			try {
				//providing timeout is just a safety measure to prevent everlasting waits
				//the different client timeouts should already do their jobs
				if (latch.await(timeout, TimeUnit.MILLISECONDS) == false) {
					throw new IOException("listener timeout after waiting for [" + timeout + "] ms");
				}
			} catch (Exception e) {
				throw new RuntimeException("thread waiting for the response was interrupted", e);
			}

			Exception exception = this.exception.get();
			Response response = this.response.get();
			if (exception != null) {
				if (response != null) {
					IllegalStateException e = new IllegalStateException("response and exception are unexpectedly set at the same time");
					e.addSuppressed(exception);
					throw e;
				}
				//try and leave the exception untouched as much as possible but we don't want to just add throws Exception clause everywhere
				if (exception instanceof IOException) {
					throw (IOException) exception;
				}
				if (exception instanceof RuntimeException){
					throw (RuntimeException) exception;
				}
				throw new RuntimeException("error while performing request", exception);
			}

			if (response == null) {
				throw new IllegalStateException("response not set and no exception caught either");
			}
			return response;
		}
	}
}