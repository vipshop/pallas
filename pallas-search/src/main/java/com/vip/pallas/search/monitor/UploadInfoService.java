package com.vip.pallas.search.monitor;

import java.net.UnknownHostException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vip.pallas.search.model.SearchServer;
import com.vip.pallas.search.netty.http.server.PallasNettyServer;
import com.vip.pallas.search.utils.HttpClient;
import com.vip.pallas.search.utils.JsonUtil;
import com.vip.pallas.search.utils.PallasSearchProperties;

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
			if (!PallasNettyServer.online) {
				return;
			}
			long time = System.currentTimeMillis()/1000;
			// #474 每 03、13、23、33、43、53 上报统计信息
			if (time % 10 != 3) {
				return;
			}
			String info = serverWatch.buildAllInfo().toJSONString();
			internalUpload(info, true);
		};
	}

	public static void internalUpload(String info, boolean takeTraffic) {
		try {
			if (info == null) {
				logger.warn("server {} 's takeTraffic property is set to {}", "localIpport", takeTraffic);
			}
			String serverInfo = JsonUtil.toJson(new SearchServer(true, info));
			HttpClient.httpPost(upsertUrl, serverInfo);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
}
