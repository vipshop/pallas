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

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.vip.pallas.client.util.PallasRestClientProperties;
import com.vip.pallas.utils.LogUtils;
import com.vip.pallas.utils.PallasBasicProperties;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.client.thread.CleanRestClientTask;
import com.vip.pallas.client.thread.QueryConsoleTask;

public class PallasRestClientBuilder {

	private static final Logger log = LoggerFactory.getLogger(PallasRestClientBuilder.class);

	private static final long MAX_TIMEOUT_MILLS = 120L * 1000;

	private static final long DEFAULT_KEEPALIVE_MILLISECOND = 30L * 1000;

	private static final Header[] EMPTY_HEADERS = new Header[0];

	private static final Object lock = new Object();

	public static final Map<String, Long> TIMEOUT_MILLS_MAP = new ConcurrentHashMap<>();

	public static final ConcurrentHashMap<String/** token **/
			, PallasRestClient> CLIENT_MAP = new ConcurrentHashMap<String, PallasRestClient>();

	private static final ConnectionKeepAliveStrategy KEEPALIVESTRATEGY = new ConnectionKeepAliveStrategy() {
		@Override
		public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
			return DEFAULT_KEEPALIVE_MILLISECOND;
		}
	};

	static {
		ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r, "old-restClient-cleaner");
				thread.setDaemon(true);
				return thread;
			}
		});
		executor.submit(new CleanRestClientTask());
		log.info("old-restClient-cleaner started.");
	}

	private PallasRestClientBuilder() {}

	public static PallasRestClient buildClient(String token)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, InterruptedException {
		return buildClient(token, MAX_TIMEOUT_MILLS);
	}

	/**
	 * Create a PallasRestClient instance, which wraps a restClient with maxConnTotal set to 300 and requestTimeout set
	 * to 3000.
	 * @param clientToken the token to access Pallas Search with Index/Cluster privileges
	 * @param maxTimeoutMils timeout in milliseconds used when getting a request result, normally it should be greater
	 * then the socketTimeout.
	 * @return
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InterruptedException
	 */
	public static PallasRestClient buildClient(String clientToken, long maxTimeoutMils)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, InterruptedException {
		CloseableHttpAsyncClient httpClient = createDefaultHttpClient();
		return buildClient(clientToken, httpClient, maxTimeoutMils);
	}

	/**
	 * this is the place to construct the PallasRestClient(restClient) instance for the first time. if
	 * host-list(pallas-search list) changed, the restClient inside the pallasRestClient need to be rebuilt, see
	 * {@link #rebuildInternalRestClient(String)}
	 */
	public static PallasRestClient buildClient(String clientToken, CloseableHttpAsyncClient httpClient,
			long maxTimeoutMils)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, InterruptedException {
		PallasRestClient pallasRestClient = CLIENT_MAP.get(clientToken);
		if (pallasRestClient != null) {
			return pallasRestClient;
		}
		synchronized (lock) {
			pallasRestClient = CLIENT_MAP.get(clientToken);
			if (pallasRestClient != null) {
				return pallasRestClient;
			}
			PallasRestClient.addNewToken(clientToken);
			return createPallasRestclient(clientToken, httpClient, maxTimeoutMils);
		}
	}

	private static PallasRestClient createPallasRestclient(String clientToken, CloseableHttpAsyncClient httpClient,
			long maxTimeoutMils)
			throws InterruptedException, InstantiationException, IllegalAccessException, InvocationTargetException {
		// try 3times to get a valid ps-list
		int retryCount = 3;
		while (QueryConsoleTask.getPsListByToken(clientToken) == null && retryCount-- > 0) {
			TimeUnit.SECONDS.sleep(1);
			LogUtils.error(log, PallasRestClientProperties.PALLAS_CLIENT_FATAL_ERROR_KEY,
					"can't get a valid pallas-search list from {} with token: {}, init pallas-client failed. "
							+ PallasRestClientProperties.PALLAS_CLIENT_FATAL_ERROR_MSG,
					QueryConsoleTask.consoleQueryUrl, clientToken);
		}
		PallasRestClient pallasRestClient;
		List<HttpHost> psHostList = null;
		RestClient restClient = null;
		if (QueryConsoleTask.getPsListByToken(clientToken) != null) {
			psHostList = genPsHostList(clientToken);
			restClient = createRestClient(clientToken, httpClient, maxTimeoutMils, psHostList);
		}
		pallasRestClient = new PallasRestClient(restClient, clientToken, psHostList);
		CLIENT_MAP.put(clientToken, pallasRestClient);
		TIMEOUT_MILLS_MAP.put(clientToken, maxTimeoutMils);
		return pallasRestClient;
	}

	private static RestClient createRestClient(String clientToken, CloseableHttpAsyncClient httpClient,
			long maxTimeoutMils, List<HttpHost> hosts)
			throws InterruptedException, InstantiationException, IllegalAccessException, InvocationTargetException {
		log.info("start to construct a rest client for token:{}, hosts:{}", clientToken, hosts);
		CloseableHttpAsyncClient thisClient = httpClient;
		if (thisClient == null) {
			thisClient = createDefaultHttpClient();
		}
		HttpHost[] hostArray = hosts.toArray(new HttpHost[] {});
		Constructor<?> constructor = RestClient.class.getDeclaredConstructors()[0];
		constructor.setAccessible(true);
		//#450 pallas search对endpoint处理前先判断是否startWith /， 这里把prefixPath 设置成 ""强制让path做 startWith("/") 判断
		//如果PallasRestClientBuilder也开放 setPrefixPath 记得也做类似判断
		RestClient restClient = (RestClient) constructor.newInstance(thisClient, maxTimeoutMils, EMPTY_HEADERS,
				hostArray, "", new RestClient.FailureListener());
		thisClient.start();
		log.info("rest client started, hosts: {}, token: {}, maxTimeoutMils: {}", hosts, clientToken, maxTimeoutMils);
		return restClient;
	}

	private static List<HttpHost> genPsHostList(String clientToken) {
		List<HttpHost> hosts = new ArrayList<>();
		List<String> psList = QueryConsoleTask.getPsListByToken(clientToken);
		if (psList != null) {
			for (String addr : psList) {
				hosts.add(HttpHost.create(addr));
			}
		}
		return hosts;
	}

	public static void rebuildInternalRestClient(String clientToken)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, InterruptedException {
		PallasRestClient pallasRestClient = CLIENT_MAP.get(clientToken);
		if (pallasRestClient != null) {
			RestClient oldRestClient = pallasRestClient.getInternalRestClient();
			Long timeoutMillis = TIMEOUT_MILLS_MAP.get(clientToken);
			CleanRestClientTask.addClient(timeoutMillis, oldRestClient);
			CloseableHttpAsyncClient httpClient = createDefaultHttpClient();
			List<HttpHost> psHostList = genPsHostList(clientToken);
			RestClient newRestClient = createRestClient(clientToken, httpClient, timeoutMillis, psHostList);
			pallasRestClient.updateInternalRestClient(newRestClient, psHostList);
			log.warn("restClient rebuilt for token: {}, hosts: {}", clientToken, psHostList);
		}
	}

	private static CloseableHttpAsyncClient createDefaultHttpClient() {
		RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
				.setConnectTimeout(5000).setSocketTimeout(120_000)
				.setConnectionRequestTimeout(3000);
		HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClientBuilder
				.create().setDefaultRequestConfig(requestConfigBuilder.build())
				.setMaxConnPerRoute(3000).setMaxConnTotal(5000);
		return httpClientBuilder.setKeepAliveStrategy(KEEPALIVESTRATEGY).build();
	}

	public static void closeClientByToken(String token) throws IOException {
		PallasRestClient pallasRestClient = CLIENT_MAP.get(token);
		if (pallasRestClient != null) {
			pallasRestClient.close();
			CLIENT_MAP.remove(token);
		}
		QueryConsoleTask.esDomainMap.remove(token);
		QueryConsoleTask.psListMap.remove(token);
	}

}