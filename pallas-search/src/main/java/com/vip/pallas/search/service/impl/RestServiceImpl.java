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

package com.vip.pallas.search.service.impl;


import com.vip.pallas.search.utils.LogUtils;
import com.vip.pallas.search.utils.SearchLogEvent;
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
		LogUtils.info(logger, SearchLogEvent.NORMAL_EVENT, "received update routing command.");

		try{
			PallasCacheFactory.getCacheService().refreshRouting();
		}catch(Exception e){
			LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, e.toString(), e);
			throw e;
		}

		LogUtils.info(logger, SearchLogEvent.NORMAL_EVENT, "update routing finished!");

		return new DefaultFullHttpResponse(
				HttpVersion.HTTP_1_1,
				HttpResponseStatus.OK,
				Unpooled.wrappedBuffer("rules updated.".getBytes()));
	}
}
