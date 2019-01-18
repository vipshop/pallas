package com.vip.pallas.search.launch;

import java.util.Locale;

import org.elasticsearch.common.Booleans;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.monitor.UploadInfoService;
import com.vip.pallas.search.netty.http.server.PallasNettyServer;
import com.vip.pallas.search.service.PallasCacheFactory;
import com.vip.pallas.search.timeout.TimeoutRetryController;
import com.vip.pallas.search.trace.TraceAop;
import com.vip.pallas.search.utils.PallasSearchProperties;
import com.vip.pallas.search.utils.StartCheckUtil;

public class Startup {
	private static Logger logger = LoggerFactory.getLogger(Startup.class);

	public static void main(String[] args) {
		try {
			start();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			System.exit(1);
		}
	}

	public static void start() throws Exception {
		//TODO 临时设置es netty参数, 后续版本移除
		if(!Booleans.parseBoolean(System.getProperty("es.set.netty.runtime.available.processors"), false)){
			System.setProperty("es.set.netty.runtime.available.processors", "false");
		}

		StartCheckUtil.add2CheckList(StartCheckUtil.StartCheckItem.PORT);
		prepareSysProps();
		
		logger.info("Gateway Application Start...");
		logger.info("init zuul ...");
		BootStrap.initZuul();

		logger.info("init trace ...");
		TraceAop.instance().start();

		logger.info("start upload info timmer ...");
		new UploadInfoService().startUploadTimmer();

		logger.info("start timeout retry checker ...");
		TimeoutRetryController.start();

		logger.info("init cache service ...");
		try{
			PallasCacheFactory.getCacheService().initCache();
		}catch(Exception ex){
			logger.error(ex.getMessage(), ex);
		}
		
		logger.info("start netty ...");
		final PallasNettyServer gatewayServer = new PallasNettyServer();
		gatewayServer.startServer(PallasSearchProperties.PALLAS_SEARCH_PORT); // 启动HTTP容器
	}
	
	/**
	 *  System properties default reset
	 */
	private static void prepareSysProps(){
		logger.info("Set default locale language[en] ...");
		Locale.setDefault(Locale.ENGLISH);
	}

	public static void fail(String message) {
		logger.info("Error: " + message);
		System.exit(-1);
	}
    
}