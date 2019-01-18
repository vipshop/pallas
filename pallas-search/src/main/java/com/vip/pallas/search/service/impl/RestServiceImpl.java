package com.vip.pallas.search.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.netty.http.handler.RequestConfig;
import com.vip.pallas.search.service.PallasCacheFactory;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class RestServiceImpl {
	private static Logger logger = LoggerFactory.getLogger(RestServiceImpl.class);

	@RequestConfig(url="/_py/update_routing")
	public FullHttpResponse updateRouting(PallasRequest request) {
		logger.info("received update routing command.");

		try{
			PallasCacheFactory.getCacheService().refreshRouting();
		}catch(Exception e){
			logger.error(e.toString(), e);
			throw e;
		}

		logger.info("update routing finished!");

		return new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK,
				Unpooled.wrappedBuffer("rules updated.".getBytes()));
	}
}
