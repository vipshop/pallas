package com.vip.pallas.test.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.launch.Startup;
import com.vip.pallas.utils.IPUtils;
import com.vip.pallas.utils.PallasBasicProperties;

/**
 * 基础Search测试类，继承即获得内嵌Search Es Console Api前置启动
 */
public class BaseSearchTest extends BaseSpringEsTest {

	private static final String IT_TEST_CLUSTER = "it-test";
	protected static CloseableHttpClient httpClient = HttpClientBuilder.create().setMaxConnPerRoute(64).build();
	protected static int SERVER_PORT = 9201;
	protected static Thread SERVER_THREAD;
	protected volatile static boolean started  = false;
	protected static AbstractFilterContext backupRouteFilterContext;

	static {
		System.setProperty("pallas.search.port", SERVER_PORT + "");
		System.setProperty("pallas.stdout", "true");
		System.setProperty("pallas.search.cluster", IT_TEST_CLUSTER);
		PallasBasicProperties.REFRESH_AFTER_WRITE_DURATION = 5;
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				BaseSearchTest.shutdown();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
	}

	@Test
	public void testMustExists() {
		Assert.assertEquals(1, 1);
	}

	@Before
	public void setUpPs() throws Exception {
		String port  = System.getProperty("server.port");
		if (StringUtils.isEmpty(port)) {
			waitUntilServerStarted(8080);
		} else {
			waitUntilServerStarted(Integer.valueOf(port));
		}
		if (!started) {
			startPS();
			waitUntilServerStarted(SERVER_PORT);
			started = true;
		}
	}

	public static void shutdown() throws IOException {
		if (SERVER_THREAD != null) {
			System.out.println("start to shutdown the server...");
			SERVER_THREAD.interrupt();
			System.out.println("server interrupted.");
			System.out.println("start to delete all the it-test cluster records in table search_server.");
		}
	}

	private static void waitUntilServerStarted(int port) throws InterruptedException {
		Thread.sleep(500);
		int i = 200;
		while (!serverListening(port) || i > 0) {
			try {
				Thread.sleep(20);
				i--;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (!serverListening(port)) {
			System.out.println("server started at port:" + SERVER_PORT + " failed. exit.");
			System.exit(-1);
		}
		System.out.println("server started at port:" + SERVER_PORT);
	}
	
	public static boolean serverListening(int port) {
		Socket s = null;
		try {
			s = new Socket(IPUtils.localIp4Str(), port);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (s != null) {
				try {
					s.close();
				} catch (Exception e) {
				}
			}
		}
	}

	private synchronized static void startPS() {
		SERVER_THREAD = new Thread(() -> {
			try {
				Startup.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		SERVER_THREAD.start();
	}

	@SuppressWarnings("rawtypes")
	public static Map callRestApi(String url, String requestBody) throws IOException{
		return JSON.parseObject(callRestApiAndReturnString(url, requestBody), Map.class);
	}
	public static CloseableHttpResponse callRestApiAndReturnResponse(String host, int port, String url, String requestBody) throws IOException{
		HttpHost target = new HttpHost(host, port);
		HttpPost request = new HttpPost(url);
		request.setEntity(new StringEntity(requestBody, ContentType.create("application/json", "UTF-8")));
		return httpClient.execute(target, request);
	}
	public static CloseableHttpResponse callRestApiAndReturnResponse(String host, int port, String url, Map<String, String> header, String requestBody) throws IOException{
		HttpHost target = new HttpHost(host, port);
		HttpPost request = new HttpPost(url);
		if(header != null) header.forEach((k, v) -> request.setHeader(k, v));
		request.setEntity(new StringEntity(requestBody, ContentType.create("application/json", "UTF-8")));
		return httpClient.execute(target, request);
	}
	public static String callRestApiAndReturnString(String host, int port, String url, String requestBody) throws IOException{
		return inputStream2String(callRestApiAndReturnResponse(host, port, url, requestBody).getEntity().getContent());
	}
	public static String callRestApiAndReturnString(String host, int port, String url, Map<String, String> header, String requestBody) throws IOException{
		return inputStream2String(callRestApiAndReturnResponse(host, port, url, header, requestBody).getEntity().getContent());
	}

	public static CloseableHttpResponse callRestApiAndReturnResponse(String url, String requestBody) throws IOException{
		return callRestApiAndReturnResponse(IPUtils.localIp4Str(), SERVER_PORT, url, requestBody);
	}
	public static String callRestApiAndReturnString(String url, String requestBody) throws IOException{
		return callRestApiAndReturnString(IPUtils.localIp4Str(), SERVER_PORT, url, requestBody);
	}
	public static String inputStream2String(InputStream is) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		while ((i = is.read()) != -1) {
			baos.write(i);
		}
		is.close();
		return baos.toString();
	}
}
