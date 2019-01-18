package com.vip.pallas.search.netty.http.handler;

import com.vip.pallas.search.filter.base.DefaultFilterPipeLine;
import com.vip.pallas.search.filter.common.PallasRunner;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.filter.error.ErrorFilter;
import com.vip.pallas.search.filter.post.CommonResponseHeaderFilter;
import com.vip.pallas.search.http.HttpCode;
import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.monitor.GaugeMonitorService;
import com.vip.pallas.search.monitor.MonitorAccessLog;
import com.vip.pallas.search.netty.ByteBufManager;
import com.vip.pallas.search.netty.http.NettyPallasRequest;
import com.vip.pallas.search.netty.http.server.PallasNettyServer;
import com.vip.pallas.search.trace.TraceAop;
import com.vip.pallas.search.trace.TraceAspect;
import com.vip.pallas.search.utils.ByteUtils;
import com.vip.pallas.search.utils.ClientIpUtil;
import com.vip.pallas.search.utils.PallasSearchProperties;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Netty HTTP连接处理类
 */
@Sharable
public class HttpConnectionHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(HttpConnectionHandler.class);
	private static Map<String, String> returnOkUrlMap = new HashMap<String, String>();

	private static Map<String,String> healthCheckUrlMap = new HashMap<String,String>();

	static {
		if (StringUtils.isNotEmpty(PallasSearchProperties.RETURN_OK_URL)) {
			String[] returnOkUrl = PallasSearchProperties.RETURN_OK_URL.split(";");
			for (int i = 0; i < returnOkUrl.length; i++) {
				returnOkUrlMap.put(returnOkUrl[i], "ok");
			}
		}
		healthCheckUrlMap.put("/_health_check","HealthCheck");
		if (StringUtils.isNotEmpty(PallasSearchProperties.HEALTH_CHECK_URL)) {
			String[] healthCheckUrl = PallasSearchProperties.HEALTH_CHECK_URL.split(";");
			for(int j = 0; j < healthCheckUrl.length; j++){
				healthCheckUrlMap.put(healthCheckUrl[j],"HealthCheck");
			}
		}
	}

	private InternalRestHandler ownRestHandler;

	public HttpConnectionHandler() {
		ownRestHandler = new InternalRestHandler();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		// DefaultFullHttpRequest(decodeResult: failure(io.netty.handler.codec.TooLongFrameException: An HTTP line is
		// larger than 10 bytes.), version: HTTP/1.0, content: EmptyByteBufBE)
		// GET /bad-request HTTP/1.0
		// 如果http协议报错的处理
		PallasRequest request = null;
		SessionContext context = null;
		FullHttpRequest fullHttpRequest = null;
		try {
			fullHttpRequest = (FullHttpRequest) msg;
			// 检查path,netty中为uri，判断http头，如果是http开头，说明path为： http://mapi.vip.com:80/hello?fields=1。
			// 该处需要做两件事情：1：将path变成/hello?fields=1 2:将http header的host设置为mapi.vip.com:80
			String uri = fullHttpRequest.getUri();
			if (StringUtils.startsWithIgnoreCase(uri, "http")) {
				int pos = StringUtils.indexOf(uri, "://");
				if (pos != -1 && pos < 5) {
					int slashPos = StringUtils.indexOf(uri, "/", pos + 3);
					fullHttpRequest.setUri(StringUtils.substring(uri, slashPos));
					HttpHeaders.setHost(fullHttpRequest, StringUtils.substring(uri, pos + 3, slashPos));
				}
			}
			// 设置channel
			context = new SessionContext();
			context.setInBoundChannel(ctx.channel());

			//#554 pallas-search调用es全链路时间点跟踪
			context.setTimestampServerChannelRead(System.currentTimeMillis());

			// 设置request
			request = new NettyPallasRequest(fullHttpRequest, ctx.channel());
			context.setRequest(request);

			// 统计 Request的 body 的大小
			GaugeMonitorService.incReqesutThroughput(fullHttpRequest.content().writerIndex());

			// netty未对参数的非法性进行校验，这里增加对参数的非法性进行校验，避免错误的参数透传到后端。
			// http协议处理统一在HttpProtocolCheckFilter处理，这里进行获取，实际上是做参数校验的工作。主要校验url和form表单提交的body参数校验
			try {
				request.getPathInfo(); // 如果path存在非法字符：IllegalArgumentException
				request.getParameterMap(); // 如果uri字符非法：IllegalArgumentException 如果body非法字符：ErrorDataDecoderException
			} catch (Exception e) {
				fullHttpRequest.setDecoderResult(DecoderResult.failure(e));
			}
			// 判断只有http协议解析成功，才会进行心跳检查，否则不进行心跳检查
			if (fullHttpRequest.getDecoderResult().isSuccess()) {
				// handle pallas search rest api.
				String pathInfo = request.getPathInfo();
				if (pathInfo.startsWith("/_py/")) {
					try{
						ownRestHandler.invokeRestService(request, ctx, context);
					}finally{
						releaseByteBuf(fullHttpRequest);
					}
					return;
				} // 心跳检查,在mercury之前，所以不会记录mercury的日志
				else if (isHeatBeatUrl(request, context)) {
					ctx.channel().close(); // 健康检查后关闭掉连接
					releaseByteBuf(fullHttpRequest);
					return;
				}
			}

			// 设置Ip
            // context.setClientIp();
            request.setHeader(com.google.common.net.HttpHeaders.X_FORWARDED_FOR, ClientIpUtil.getClientIp(context));

			// 放置healthcheck记录access日志，所在在这里记录
			if (!PallasSearchProperties.PALLAS_ACCESSLOG_DISABLE) {
				MonitorAccessLog accessLog = new MonitorAccessLog();
				if (PallasSearchProperties.ACCESSLOG_PRINT_REQUESTBODY){
					try {
						ByteBuf byteBuf = request.getContent();
						String requestBody = byteBuf.toString(CharsetUtil.UTF_8);
						accessLog.setRequestBody(StringUtils.removeAll(requestBody,"\r|\n|\t"));
					}catch (Exception e){
						logger.error("get request body from bytebuf error",e);
					}
				}
				context.setMonitorAccessLog(accessLog);
				accessLog.start(request); // 记录accessLog
			}

			// trace filterStart
			TraceAspect aopAspect = TraceAop.instance().newTrace();
			context.setTraceAspect(aopAspect);
			aopAspect.beforeFilterStart(context);

			// 业务执行代码
			PallasRunner.run(context);
		} catch (Throwable t) {
			logger.error(t.getMessage(), t);
			releaseByteBuf(fullHttpRequest); //NOSONAR
			if (context != null) {
				// 设置返回的httpcode
				context.setThrowable(t);
				context.setHttpCode(HttpCode.HTTP_INTERNAL_SERVER_ERROR);
				DefaultFilterPipeLine.getInstance().get(ErrorFilter.DEFAULT_NAME).fireSelf(context);
			}
		}
	}

	private void releaseByteBuf(FullHttpRequest request){
		ByteBufManager.deepRelease(request.content());
	}

	private boolean isHeatBeatUrl(PallasRequest request, SessionContext context) {
		String pathInfo = request.getPathInfo();
		if (returnOkUrlMap.size() != 0) {
			if (returnOkUrlMap.get(pathInfo) != null) {
				writeBody(context, HttpCode.HTTP_OK_CODE, returnOkUrlMap.get(pathInfo));
				// request.writeAndFlush(HttpCode.HTTP_OK_CODE, returnOkUrlMap.get(pathInfo));
				return true;
			}
		}
		// 心跳检测
		if(healthCheckUrlMap.get(pathInfo) != null){
			if (PallasNettyServer.online) {
				writeBody(context, HttpCode.HTTP_OK_CODE, "ok");
			} else {
				writeBody(context, HttpCode.HTTP_GATEWAY_SHUTDOWN, "not ok");
			}

			return true;
		}
		return false;
	}

	public static void writeBody(SessionContext context, int httpCode, String body) {
		writeBody(context, httpCode, ByteUtils.toByteBuf(body));
	}
	
	public static void writeBody(SessionContext context, int httpCode, ByteBuf bodyByteBuf) {
		context.setHttpCode(httpCode);
		context.setResponseBody(bodyByteBuf);
		DefaultFilterPipeLine.getInstance().get(CommonResponseHeaderFilter.DEFAULT_NAME).fireSelf(context);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof IOException) { // client is shutdown, log.warn
			//由于现在移动的haproxy是短连接，导致Connection reset by peer 异常特别多，如果是该异常，暂时不做打印。在中文环境下是远程主机强迫关闭了一个现有的连接，暂时不作考虑
			String detailMessage = cause.getMessage();
			if(detailMessage != null){
				if(!detailMessage.contains("reset")){
					logger.warn("Unexpected IOException from downstream.Throwable Message:" + cause.getMessage());
				}
			}

		} else if (cause instanceof TooLongFrameException) {
			// bugfix: body过长不是设置DecoderResult,而是抛异常进入exceptionCaught
			// 导致没有进入pallas流程
			FullHttpRequest httpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
					"/bad-request", Unpooled.EMPTY_BUFFER, false);
			httpRequest.setDecoderResult(DecoderResult.failure(cause));
			channelRead(ctx, httpRequest);
			return;
		} else { // other errors, log.error
			logger.error("[pallasExceptionCaught][" + ctx.channel().remoteAddress() + " -> "
					+ ctx.channel().localAddress() + "]", cause);
		}
		// 错误的上报
		ctx.close();
	}

	@Override
	public void userEventTriggered(final ChannelHandlerContext ctx, final Object evt) throws Exception {
		// 如果是超时,则对连接进行关闭
		if (!(evt instanceof IdleStateEvent)) {
			super.userEventTriggered(ctx, evt);
			return;
		}
		Channel channel = ctx.channel();
		channel.close();
	}

//	private static void traceIdProcess(SessionContext context) {
//
//	}

}