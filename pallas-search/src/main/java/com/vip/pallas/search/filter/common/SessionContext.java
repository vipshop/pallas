package com.vip.pallas.search.filter.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vip.pallas.search.http.HttpCode;
import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.model.ServiceInfo;
import com.vip.pallas.search.monitor.MonitorAccessLog;
import com.vip.pallas.search.trace.TraceAop;
import com.vip.pallas.search.trace.TraceAspect;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpVersion;

/**
 * 
 * @author dylan.xue
 *
 */
public class SessionContext {
	public SessionContext() {
		super();
	}

	private Throwable throwable;
	private Boolean printExceStackInfo;
	private String requestCallBack;

	private ServiceInfo serviceInfo;
	private List<ServiceInfo> serviceInfoList;
	private int httpCode = 0;
	private TraceAop traceAop;

	//#554 pallas-search调用es全链路时间点跟踪
	// FLY: CR1-CS2, 网络无延时时，约等于es处理的时间

	private long timestampServerChannelRead = -1L; // SR: netty收到客户端请求
	private long timestampClientStartExecute = -1L; // CS1: httpclient调用execute方法
	private long timestampClientConnected = -1L; // CS2: 获取到连接并成功建立连接(有可能发送了请求)
	private long timestampClientResponseReceived = -1L; // CR1: IO线程获取到请求响应
	private long timestampServerResponseSend = -1L; // SS: netty发送响应给客户端
	private long timestampClientResponseRead = -1L; // CR2: 处理完ES的返回


	private boolean bodySend;
	
	// 如果之前设置过200状态，则不再对mercury的状态进行设置
	public void setHttpCode(int mercuryHttpCode) {
		if (getHttpCode() != HttpCode.HTTP_OK_CODE) {
			this.httpCode = mercuryHttpCode;
		}
	}

	public int getHttpCode() {
		return this.httpCode;
	}

	public String getTraceId() {
		TraceAspect aspect = getTraceAspect();
		if (aspect != null) {
			return aspect.getTraceId();
		}
		return null;
	}


	private MonitorAccessLog monitorAccessLog;
	private Channel inBoundChannel;

	private PallasRequest request;
	private String ospRequestBodyJson;

	private Map<String, String> ospRequestHeader;
	private String restRequestUri;
	


	/**
	 * @return rest服务转发，从该处获取uri数据
	 */
	private ByteBuf restRequestBody;

	// ---------- 返回给访问端
	/**
	 * 获取返回的response对象
	 */
	private HttpHeaders responseHttpHeaders;

	private ByteBuf responseBody;

	// 返回的httpVersion
	private HttpVersion  responseHttpVersion;
	
	public HttpVersion getResponseHttpVersion() {
		if (responseHttpVersion == null) {
			return HttpVersion.HTTP_1_1;
		} else {
			return responseHttpVersion;
		}
	}

	// -------------- rest返回的数据
	private FullHttpResponse restFullHttpResponse;
	private TraceAspect TraceAspect;
	
	public Throwable getThrowable() {
		return throwable;
	}

	public void setThrowable(Throwable throwable) {
		this.throwable = throwable;
	}

	public Boolean getPrintExceStackInfo() {
		return printExceStackInfo;
	}

	public void setPrintExceStackInfo(Boolean printExceStackInfo) {
		this.printExceStackInfo = printExceStackInfo;
	}

	public String getRequestCallBack() {
		return requestCallBack;
	}

	public void setRequestCallBack(String requestCallBack) {
		this.requestCallBack = requestCallBack;
	}

	public ServiceInfo getServiceInfo() {
		return serviceInfo;
	}

	public void setServiceInfo(ServiceInfo serviceInfo) {
		this.serviceInfo = serviceInfo;
	}


	public boolean isBodySend() {
		return bodySend;
	}

	public void setBodySend(boolean bodySend) {
		this.bodySend = bodySend;
	}

	public MonitorAccessLog getMonitorAccessLog() {
		return monitorAccessLog;
	}

	public void setMonitorAccessLog(MonitorAccessLog monitorAccessLog) {
		this.monitorAccessLog = monitorAccessLog;
	}

	public Channel getInBoundChannel() {
		return inBoundChannel;
	}

	public void setInBoundChannel(Channel inBoundChannel) {
		this.inBoundChannel = inBoundChannel;
	}

	public PallasRequest getRequest() {
		return request;
	}

	public void setRequest(PallasRequest request) {
		this.request = request;
	}

	public String getOspRequestBodyJson() {
		return ospRequestBodyJson;
	}

	public void setOspRequestBodyJson(String ospRequestBodyJson) {
		this.ospRequestBodyJson = ospRequestBodyJson;
	}

	public Map<String, String> getOspRequestHeader() {
		if(ospRequestHeader == null){
			ospRequestHeader = new HashMap<String, String>();
		}
		return ospRequestHeader;
	}

	public void setOspRequestHeader(Map<String, String> ospRequestHeader) {
		this.ospRequestHeader = ospRequestHeader;
	}

	public String getRestRequestUri() {
		return restRequestUri;
	}

	public void setRestRequestUri(String restRequestUri) {
		this.restRequestUri = restRequestUri;
	}

	public ByteBuf getRestRequestBody() {
		return restRequestBody;
	}

	public void setRestRequestBody(ByteBuf restRequestBody) {
		this.restRequestBody = restRequestBody;
	}

	public HttpHeaders getResponseHttpHeaders() {
		if(responseHttpHeaders == null){
			responseHttpHeaders =new DefaultHttpHeaders(false);
		}
		return responseHttpHeaders;
	}

	public void setResponseHttpHeaders(HttpHeaders responseHttpHeaders) {
		this.responseHttpHeaders = responseHttpHeaders;
	}

	public ByteBuf getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(ByteBuf responseBody) {
		this.responseBody = responseBody;
	}

	public FullHttpResponse getRestFullHttpResponse() {
		return restFullHttpResponse;
	}

	public void setRestFullHttpResponse(FullHttpResponse restFullHttpResponse) {
		this.restFullHttpResponse = restFullHttpResponse;
	}

	public void setResponseHttpVersion(HttpVersion responseHttpVersion) {
		this.responseHttpVersion = responseHttpVersion;
	}

	
	public List<ServiceInfo> getServiceInfoList() {
		return serviceInfoList;
	}

	public void setServiceInfoList(List<ServiceInfo> serviceInfoList) {
		this.serviceInfoList = serviceInfoList;
	}

	// 需要添加 header,httpVersion,httpMethod,uri,body等数据
	public DefaultFullHttpRequest getOutBoundHttpRequest() {
		PallasRequest httpRequest = getRequest();
		DefaultFullHttpRequest outBoundRequest = new DefaultFullHttpRequest(httpRequest.getHttpVersion(),
				httpRequest.getHttpMethod(), getRestRequestUri(), getRestRequestBody());
		// source header is http request header
		outBoundRequest.headers().add(httpRequest.getHttpHeader());

		return outBoundRequest;
	}

	public long getTimestampServerChannelRead() {
		return timestampServerChannelRead;
	}

	public void setTimestampServerChannelRead(long timestampServerChannelRead) {
		this.timestampServerChannelRead = timestampServerChannelRead;
	}

	public void setTimestampClientStartExecute(long timestampClientStartExecute) {
		this.timestampClientStartExecute = timestampClientStartExecute;
	}

	public long getTimestampClientStartExecute() {
		return timestampClientStartExecute;
	}

	public long getTimestampClientConnected() {
		return timestampClientConnected;
	}

	public void setTimestampClientConnected(long timestampClientConnected) {
		this.timestampClientConnected = timestampClientConnected;
	}

	public long getTimestampClientResponseReceived() {
		return timestampClientResponseReceived;
	}

	public void setTimestampClientResponseReceived(long timestampClientResponseReceived) {
		this.timestampClientResponseReceived = timestampClientResponseReceived;
	}

	public long getTimestampServerResponseSend() {
		return timestampServerResponseSend;
	}

	public void setTimestampServerResponseSend(long timestampServerResponseSend) {
		this.timestampServerResponseSend = timestampServerResponseSend;
	}

	public long getTimestampClientResponseRead() {
		return timestampClientResponseRead;
	}

	public void setTimestampClientResponseRead(long timestampClientResponseRead) {
		this.timestampClientResponseRead = timestampClientResponseRead;
	}

	public String getFormatTimestampStatsStr() {
		return "sr=" + timestampServerChannelRead + ",cs1=" + timestampClientStartExecute + ",cs2=" + timestampClientConnected
				+ ",cr1=" + timestampClientResponseReceived + ",cr2=" + timestampClientResponseRead + ",ss="
				+ timestampServerResponseSend;
	}

	public TraceAspect getTraceAspect() {
		return TraceAspect;
	}

	public void setTraceAspect(TraceAspect TraceAspect) {
		this.TraceAspect = TraceAspect;
	}

}
