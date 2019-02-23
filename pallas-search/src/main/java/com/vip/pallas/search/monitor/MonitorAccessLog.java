package com.vip.pallas.search.monitor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.model.Index;
import com.vip.pallas.search.model.ServiceInfo;
import com.vip.pallas.search.service.PallasCacheFactory;
import com.vip.pallas.search.utils.PallasSearchProperties;
import com.vip.pallas.utils.PallasBasicProperties;

import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.util.internal.InternalThreadLocalMap;

public class MonitorAccessLog {
	private static Logger logger = LoggerFactory.getLogger(MonitorAccessLog.class);

	private String remote_addr;
	private long upStreamStartTime; // 统计后端服务响应时间
	private long upStreamEndTime;
	private String request; // GET /_health_check HTTP/1.0
	private String http_referer;
	private String http_user_agent; // ?
	private String http_x_forwarded_for;

	private String http_host;
	private String requestBody = null;

	private String templateId;

	private static InetAddress inetAddress = null;
	private static String hostname = "-";

	private static ExecutorService executor = Executors.newSingleThreadExecutor(
			new ThreadFactoryBuilder().setNameFormat("Pallas-Search-Write-Access-Log").build());

	static {
		try {
			inetAddress = InetAddress.getLocalHost();
			if (inetAddress != null) {
				hostname = inetAddress.getHostName();
			}
		} catch (UnknownHostException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public MonitorAccessLog() {

	}

	public static void shutdown() {
		executor.shutdown();
	}

	public void setRequestBody(String requestBody) {
		this.requestBody = requestBody;
	}

	public void startUpstreamTime() {
		upStreamStartTime = System.currentTimeMillis();
	}

	public void endUpstreamTime() {
		upStreamEndTime = System.currentTimeMillis();
	}

	public void start(PallasRequest pallasRequest) {
		remote_addr = pallasRequest.remoteAddress();
		StringBuilder builder = InternalThreadLocalMap.get().stringBuilder();
		builder.append(pallasRequest.getMethod()).append(' ').append(pallasRequest.getUri()).append(' ')
				.append(pallasRequest.getHttpVersion());
		request = builder.toString();
		http_referer = pallasRequest.getHeader(Names.REFERER);
		http_user_agent = pallasRequest.getHeader(Names.USER_AGENT);
		http_x_forwarded_for = pallasRequest.getHeader("X-FORWARDED-FOR");
		http_host = pallasRequest.getHeader(Names.HOST);
		templateId = pallasRequest.getTemplateId();

	}

	// 需要确定的 1：时间的单位 2：是否有空格 3：host如何取值 4：cdn的数据如何获取
	public void endAndLog(SessionContext ctx, int mercuryCode, long body_bytes_sent) {
		ServiceInfo serviceInfo = ctx.getServiceInfo();
		String traceId = ctx.getTraceId();
		int status = ctx.getHttpCode();

		int slowerThan = PallasBasicProperties.DEFAULT_INDEX_SLOWER_THAN;

		if(serviceInfo != null){
			//获取索引配置信息
			Index index = PallasCacheFactory.getCacheService().getIndexByIndexAndCluster(serviceInfo.getIndexName(), serviceInfo.getClusterName());
			if(index != null){
				Integer _slowerThan = index.getSlowerThan();
				if (_slowerThan != null && _slowerThan.intValue() != 0) {
					slowerThan = _slowerThan;
				}
			}
		}

		//非bulk的不落盘
		if (request != null && !request.contains("_bulk")) {
			long client_rtt = ctx.getTimestampServerResponseSend() - ctx.getTimestampServerChannelRead();
			if (client_rtt >= slowerThan) {// 小于该时间阀值的不落盘
				writeAccessLog(status, mercuryCode, body_bytes_sent, serviceInfo, traceId, ctx);
			} else { // if ps_side takes more than 30mils(default), it might be sth. wrong.
				if (ctx.getTimestampClientConnected() != -1l && ctx.getTimestampClientResponseReceived() != -1l) {
					long psSlideTime = ctx.getTimestampClientConnected() - ctx.getTimestampServerChannelRead()
							+ ctx.getTimestampServerResponseSend() - ctx.getTimestampClientResponseReceived();
					if (psSlideTime > PallasBasicProperties.DEFAULT_PS_SIDE_THRESHOLD) {
						writeAccessLog(status, mercuryCode, body_bytes_sent, serviceInfo, traceId, ctx);
					}
				}
			}
		}
	}

	private void writeAccessLog(int status, int mercuryCode, long body_bytes_sent, ServiceInfo serviceInfo, String traceId, SessionContext ctx){
		StringBuilder accessLog = InternalThreadLocalMap.get().stringBuilder();
		String vDomainName = null;

		accessLog.append(wrapValue(remote_addr)).append('\t').append('-').append('\t');

		// 因为我们时间的格式只记录到秒，先通过舍弃千分位及以下数值用来提升DatePatternConverter的性能
		// getNgDateFormat().format((System.currentTimeMillis()/1000)*1000, accessLog);

        requestBody = wrapValue(requestBody);
        if(requestBody.length() > PallasSearchProperties.ACCESSLOG_PRINT_REQUESTBODY_MAX_SIZE){
            requestBody = requestBody.substring(0, PallasSearchProperties.ACCESSLOG_PRINT_REQUESTBODY_MAX_SIZE - 1);
        }

		accessLog.append('\t').append(wrapValue(request))
				.append("\tstatus=").append(status)
				.append("\ts_duration=").append(ctx.getTimestampServerResponseSend()-ctx.getTimestampServerChannelRead())
				.append("\tc_duration=").append(ctx.getTimestampClientResponseRead() - ctx.getTimestampClientStartExecute())
				.append("\tfly=").append(ctx.getTimestampClientResponseReceived() - ctx.getTimestampClientConnected())
				.append("\tps_side=")
				.append(ctx.getTimestampClientConnected() - ctx.getTimestampServerChannelRead()
						+ ctx.getTimestampServerResponseSend() - ctx.getTimestampClientResponseReceived())
				.append("\tcs2-cs1=").append(ctx.getTimestampClientConnected() - ctx.getTimestampClientStartExecute())
				.append("\tcr2-cr1=").append(ctx.getTimestampClientResponseRead() - ctx.getTimestampClientResponseReceived())
				.append("\tcs1=").append(ctx.getTimestampClientStartExecute())
				.append("\tcs2=").append(ctx.getTimestampClientConnected())
				.append("\tcr1=").append(ctx.getTimestampClientResponseReceived())
				.append("\tcr2=").append(ctx.getTimestampClientResponseRead())
				.append("\tsr=").append(ctx.getTimestampServerChannelRead())
				.append("\tss=").append(ctx.getTimestampServerResponseSend())
				.append("\tcontent-length=").append(body_bytes_sent)
				.append("(bytes)\t").append("\ttemplateId=").append(templateId)
				.append('\t').append(wrapValue(http_referer))
				.append('\t').append(wrapValue(http_user_agent))
				.append('\t').append(wrapValue(http_x_forwarded_for))
				.append('\t').append(wrapValue(http_host))
				.append('\t').append(wrapValue(hostname))
				.append('\t').append(wrapValue(serviceInfo != null ? serviceInfo.getBackendAddress() : ""))
				.append('\t').append(wrapValue(requestBody))
				.append('\t').append(mercuryCode)
				.append('\t').append(wrapValue(vDomainName))
				.append('\t').append(wrapValue(traceId));

		executor.submit(() -> logger.info(accessLog.toString()));
	}

	private String wrapValue(String value) {
		if (value == null) {
			return "-";
		} else {
			return value;
		}
	}

}