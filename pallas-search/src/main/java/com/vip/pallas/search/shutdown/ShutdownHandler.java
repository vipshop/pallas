package com.vip.pallas.search.shutdown;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.filter.base.DefaultFilterPipeLine;
import com.vip.pallas.search.filter.base.Filter;
import com.vip.pallas.search.monitor.ServerStatus;
import com.vip.pallas.search.monitor.UploadInfoService;
import com.vip.pallas.search.netty.http.server.PallasNettyServer;
import com.vip.pallas.search.timeout.TimeoutRetryController;
import com.vip.pallas.search.utils.PallasSearchProperties;

public class ShutdownHandler extends Thread {

	private static Logger logger = LoggerFactory.getLogger(ShutdownHandler.class);
	
	private PallasNettyServer server;

	private boolean working = true;

	public ShutdownHandler(PallasNettyServer server) {
		super("Pallas-Search-Shutdown-handler");
		this.server = server;
	}

	@Override
	public void run() {
	    offLineServer();
		tellConsoleTakeNoTraffic();
		try {
			System.out.println("shutdown now, wait 31s for lb server to discover.");// NOSONAR
			Thread.sleep(PallasSearchProperties.HEALTH_INVALID_TIME);
		} catch (Exception e) {//because it's shuting down
			logger.error(e.toString(), e);
		}
		ServerStatus.offline.set(true);
		shutdownFilters();
		shutdownTimeoutGovernor();
		bossGroupShutdown();
		workerGroupShutdown();	
	}
	
	private static void tellConsoleTakeNoTraffic() {
		UploadInfoService.internalUpload(null, false);
	}

	private static void shutdownTimeoutGovernor() {
		TimeoutRetryController.stop();
	}

	private static void offLineServer() {
	    PallasNettyServer.online = false;
	}

	private void shutdownFilters(){
		List<Filter> filters = DefaultFilterPipeLine.getInstance().getAllFilter();
		for(Filter filter : filters){
			try{
				filter.shutdown();
			}catch(Exception e){
				logger.error(e.getMessage(), e);
			}
		}
	}
	private void workerGroupShutdown() {
		server.shutdownWorkerGroup();
		while (working) {
			if (server.isWorkerGroupShuttingDown()) {
				logger.info("WorkerGroup is shutdown.");
				System.out.println(new Date() + " WorkerGroup is shutdown.");// NOSONAR

				break;
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException ignore) {
				logger.info("Thread is interrupted.");
				Thread.currentThread().interrupt();
				working = false;
			}
		}

	}

	private void bossGroupShutdown() {

		server.shutdownBossGroup();
		while (working) {
			if (server.isBossGroupShuttingDown()) {
				logger.info("BossGroup is shutdown.");
				System.out.println(new Date() + " BossGroup is shutdown.");// NOSONAR
				break;
			}

			try {
				Thread.sleep(500);
			} catch (InterruptedException ignore) {
				Thread.currentThread().interrupt();
				working = false;
			}
		}

	}
}
