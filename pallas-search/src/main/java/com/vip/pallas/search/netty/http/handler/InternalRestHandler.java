/**
 * 
 */
package com.vip.pallas.search.netty.http.handler;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.service.impl.RestServiceImpl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;

public class InternalRestHandler {

	private static Logger logger = LoggerFactory.getLogger(InternalRestHandler.class);
	private final RestServiceImpl restServiceImpl;
	private final static ExecutorService INTERNAL_REST_HANDLER = Executors.newFixedThreadPool(2,
			new ThreadFactoryBuilder().setNameFormat("Pallas-Search-Internal-Rest-Handler").build());

	public InternalRestHandler() {
		AnnotationResolver.parseClass(RestServiceImpl.class);
		restServiceImpl = new RestServiceImpl();
	}
	
	public void invokeRestService(PallasRequest request, ChannelHandlerContext ctx, SessionContext context) {
		doBusiness(request, ctx, context);
	}

	public void doBusiness(final PallasRequest request, final ChannelHandlerContext ctx,
			final SessionContext context) {
		INTERNAL_REST_HANDLER.submit(() -> {
				try {
					String pathInfo = request.getPathInfo();
					Method method = AnnotationResolver.URL_MAPPING.get(pathInfo);
					if (method == null) {
						HttpConnectionHandler.writeBody(context, HttpResponseStatus.NOT_FOUND.code(),
								"No handler found for uri [" + pathInfo + "] and method " + request.getMethod());
						ctx.channel().close();
					} else {
						FullHttpResponse response = (FullHttpResponse) method.invoke(restServiceImpl, request);
						HttpConnectionHandler.writeBody(context, response.status().code(),
								response.content());
						ctx.channel().close();
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					HttpConnectionHandler.writeBody(context, HttpResponseStatus.SERVICE_UNAVAILABLE.code(),
							e.getMessage());
					ctx.channel().close();
				}
		});
	}

}
