package com.vip.pallas.search.utils;

import java.net.URI;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.entity.ByteArrayEntity;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpClientUtil {
	public static HttpRequestBase getHttpUriRequest(HttpHost targetHost, DefaultFullHttpRequest standardHttpRequest,
			HttpEntity entity) {
		HttpRequestBase request = null;
		String httpMethod = standardHttpRequest.getMethod().name().toUpperCase();
		
		//Unspecified httpMethod treated as GET
		if(StringUtils.isEmpty(httpMethod)){
			httpMethod = "GET";
		}
		
		String uri = standardHttpRequest.getUri();
		if ("POST".equals(httpMethod)) {
			request = new HttpPost(uri);
			((HttpPost) request).setEntity(entity != null ? entity : new ByteArrayEntity(moveByteIntoHeap(standardHttpRequest)));
		} else if ("GET".equals(httpMethod)) {
			request = new HttpGet(uri);
		} else if ("DELETE".equals(httpMethod)) {
			//#464 修正 Pallas Search 目前不支持带 request body的DELETE 操作
			request = new HttpDeleteWithEntity(uri);
			((HttpDeleteWithEntity) request).setEntity(entity != null ? entity : new ByteArrayEntity(moveByteIntoHeap(standardHttpRequest)));
			request.addHeader("Content-Type", "application/json");
		} else if ("HEAD".equals(httpMethod)) {
			request = new HttpHead(uri);
		} else if ("PUT".equals(httpMethod)) {
			request = new HttpPut(uri);
			((HttpPut) request).setEntity(entity != null ? entity : new ByteArrayEntity(moveByteIntoHeap(standardHttpRequest)));
		} else if ("OPTIONS".equals(httpMethod)) {
			request = new HttpOptions(uri);
		} else if ("PATCH".equals(httpMethod)) {
			request = new HttpPatch(uri);
			((HttpPatch) request).setEntity(entity != null ? entity : new ByteArrayEntity(moveByteIntoHeap(standardHttpRequest)));
		} else if ("TRACE".equals(httpMethod)){
			//A TRACE request MUST NOT include an entity.
			request = new HttpTrace(uri);
		} else{
			//Exception on unknown or non-standard HTTP method
			throw new IllegalArgumentException("pallas doesn't know this HTTP method: " + httpMethod);
		}
		Iterator<Entry<String, String>> headerIter = standardHttpRequest.headers().iterator();
		while (headerIter.hasNext()) {
			Entry<String, String> entry = headerIter.next();
			if (isNotIgnoreHeader(entry)){
				request.setHeader(entry.getKey(), entry.getValue());
			}
		}
		request.setHeader("Host",targetHost.toHostString());
		return request;
	}

	public static HttpRequestBase getHttpUriRequest(HttpHost targetHost, DefaultFullHttpRequest standardHttpRequest) {
		return getHttpUriRequest(targetHost, standardHttpRequest, null);
	}

	public static class HttpDeleteWithEntity extends HttpEntityEnclosingRequestBase {

		public static final String METHOD_NAME = "DELETE";

		public HttpDeleteWithEntity() {}

		public HttpDeleteWithEntity(URI uri) {
			this.setURI(uri);
		}

		public HttpDeleteWithEntity(String uri) {
			this.setURI(URI.create(uri));
		}

		public String getMethod() {
			return "DELETE";
		}
	}

	
	public static byte[] moveByteIntoHeap(FullHttpRequest request) {
		ByteBuf contentByteBuf = request.content();
				
		try{	
			byte[] buffer = new byte[contentByteBuf.readableBytes()];
			contentByteBuf.readBytes(buffer);	
			return buffer;
		} finally {
			contentByteBuf.release();
		}
	}

	private static boolean isNotIgnoreHeader(Entry<String, String> entry) {
		return !("content-length".equals(entry.getKey().toLowerCase()));
	}
}
