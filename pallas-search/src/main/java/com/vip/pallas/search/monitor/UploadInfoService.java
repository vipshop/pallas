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

package com.vip.pallas.search.monitor;

import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.vip.pallas.search.utils.*;
import com.vip.pallas.utils.LogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vip.pallas.search.model.SearchServer;
import com.vip.pallas.search.netty.http.server.PallasNettyServer;

public class UploadInfoService {

	private static Logger logger = LoggerFactory.getLogger(UploadInfoService.class);
	private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
			new ThreadFactoryBuilder().setNameFormat("Pallas-Search-upload-info").build());
	private static String upsertUrl = PallasSearchProperties.CONSOLE_UPLOAD_URL;
	private ServerWatch serverWatch;

	public UploadInfoService() {
	}
	
	public void startUploadTimmer() throws UnknownHostException {
		serverWatch = new ServerWatch();
		executorService.scheduleAtFixedRate(uploadInfo(), 5, 1, TimeUnit.SECONDS);
	}

	private Runnable uploadInfo() {
		return () -> {
			try {
				if (!PallasNettyServer.online) {
					return;
				}
				long time = System.currentTimeMillis()/1000;
				// #474 每 03、13、23、33、43、53 上报统计信息
				if (time % 10 != 3) {
					return;
				}
				JSONObject info = serverWatch.buildAllInfo();
				internalUpload(info, null);
			}catch (Exception e){
				logger.error("sth wrong when upload pallas-search info,msg:{}",e.getMessage());
			}
		};
	}

	public static void internalUpload(Object info, Boolean takeTraffic) {
		try {
			SearchServer server = new SearchServer(takeTraffic, info);
			if (server.getInfo() == null) {
				LogUtils.warn(logger, SearchLogEvent.NORMAL_EVENT, "server {} 's takeTraffic property is set to {}", server.getIpport(), server.getTakeTraffic());
			}
			String serverInfo = JsonUtil.toJson(server);
			HttpClient.httpPost(upsertUrl, serverInfo);
		} catch (Exception e) {
			LogUtils.error(logger, SearchLogEvent.NORMAL_EVENT, e.getMessage(), e);
		}
	}
	
}
