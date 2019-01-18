package com.vip.pallas.search.rest;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

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
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class LocalHttpServerService extends LocalServerTestBase {
	 private String startServer(String urlSuffix, HttpRequestHandler handler) throws Exception{
	        this.serverBootstrap.registerHandler(urlSuffix, handler);
	        serverBootstrap.setListenerPort(9400);
	        HttpHost target = start();
	        serverBootstrap.setSocketConfig(SocketConfig.custom().setSoTimeout(61000).build());
	        String serverUrl = "http://localhost:" + target.getPort();
	        System.out.println("server listen at: " + serverUrl);
	        return serverUrl;
	    }

	    @Test
	    public void testCase() throws Exception{
	        String baseURL = startServer("/api", new HttpRequestHandler() {
	            @Override
	            public void handle(HttpRequest request, HttpResponse response,     HttpContext context) throws HttpException, IOException {
	            	response.setEntity(new StringEntity("i'm ok."));
	            	try {
						TimeUnit.MILLISECONDS.sleep(6100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
	                response.setStatusCode(HttpStatus.SC_OK);
	            }
	        });
	        

	        TimeUnit.HOURS.sleep(10);
	        /*HttpClient httpClient;
	        httpClient = HttpClients.custom().build();

	        HttpGet method = new HttpGet(baseURL + "/api");
	        HttpResponse response = httpClient.execute(method);*/
	    }

}
