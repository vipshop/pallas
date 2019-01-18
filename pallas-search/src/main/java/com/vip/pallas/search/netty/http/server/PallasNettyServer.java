package com.vip.pallas.search.netty.http.server;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.monitor.GaugeMonitorHandler;
import com.vip.pallas.search.monitor.UploadInfoService;
import com.vip.pallas.search.netty.ByteBufManager;
import com.vip.pallas.search.netty.http.handler.HttpConnectionHandler;
import com.vip.pallas.search.netty.http.handler.PallasHttpContentCompressor;
import com.vip.pallas.search.shutdown.ShutdownHandler;
import com.vip.pallas.search.utils.PallasSearchProperties;
import com.vip.pallas.search.utils.StartCheckUtil;
import com.vip.pallas.search.utils.StartCheckUtil.StartCheckItem;
import com.vip.pallas.utils.IPUtils;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * 基于Netty的HTTP Proxy服务器
 */
public class PallasNettyServer {

	private static Logger logger = LoggerFactory.getLogger(PallasNettyServer.class);

	private NioEventLoopGroup bossGroup = new NioEventLoopGroup(PallasSearchProperties.BOSS_GROUP_SIZE,
			new DefaultThreadFactory("pallas-Http-Boss", true));
	public static NioEventLoopGroup workerGroup = new NioEventLoopGroup(PallasSearchProperties.WORKER_GROUP_SIZE,
			new DefaultThreadFactory("pallas-Http-Worker", true));

	public volatile static boolean online = true;
	
	private ChannelFuture channelFuture;

	public PallasNettyServer() {
		// registerNettyServerArguments(); // useless, mercury has turned it off.
	}

	public void startServer(int noSSLPort) throws InterruptedException {
		// Configure the server.
		// http接口
		ServerBootstrap insecure = new ServerBootstrap();
		insecure.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
				.option(ChannelOption.ALLOCATOR, ByteBufManager.byteBufAllocator)
				.childOption(ChannelOption.SO_KEEPALIVE, Boolean.TRUE)
				.childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
				.childOption(ChannelOption.ALLOCATOR, ByteBufManager.byteBufAllocator)
				.childHandler(initChildHandler());

		channelFuture = insecure.bind(noSSLPort).sync();
		
		StartCheckUtil.setOk(StartCheckItem.PORT);
		
		addSingalHook();

		UploadInfoService.internalUpload(null, true);
		logger.info("inform console to take traffic.");

		logger.info("[listen HTTP NoSSL][" + IPUtils.localIp4Str() + ":" + noSSLPort + "]");
		// Wait until the server socket is closed.
		channelFuture.channel().closeFuture().sync();
		logger.info("[stop HTTP NoSSL success]");

	}

	@SuppressWarnings("rawtypes")
	private ChannelInitializer initChildHandler(){
		final HttpConnectionHandler httpConnectionHandler = new HttpConnectionHandler();
		return new ChannelInitializer<Channel>() {
			@Override
			public void initChannel(Channel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();
				// 对channel监控的支持
				// keepalive_timeout 的支持
				pipeline.addLast(
						new IdleStateHandler(PallasSearchProperties.HTTP_SERVER_KEEPALIVE_TIMEOUT, 0, 0, TimeUnit.MILLISECONDS));
				pipeline.addLast(new GaugeMonitorHandler());
//				pipeline.addLast(new PallasHermesHandler());
				pipeline.addLast(new HttpResponseEncoder());
				// 经过HttpRequestDecoder会得到N个对象HttpRequest,first HttpChunk,second HttpChunk,....HttpChunkTrailer
				pipeline.addLast(new HttpRequestDecoder(PallasSearchProperties.HTTP_SERVER_MAXINITIALLINELENGTH,
						PallasSearchProperties.HTTP_SERVER_MAXHEADERSIZE, 8192, PallasSearchProperties.HTTP_SERVER_VALIDATEHEADERS));
				// 把HttpRequestDecoder得到的N个对象合并为一个完整的http请求对象
				pipeline.addLast(new HttpObjectAggregator(PallasSearchProperties.HTTP_AGGREGATOR_MAXLENGTH));

				// gzip的支持
				if (PallasSearchProperties.HTTP_SERVER_COMPRESS) {
					pipeline.addLast(new PallasHttpContentCompressor(PallasSearchProperties.HTTP_SERVER_GZIP_COMP_LEVEL,
							PallasSearchProperties.HTTP_SERVER_GZIP_MIN_LENGTH));
				}

				pipeline.addLast(httpConnectionHandler);
			}
		};
	}
	
	private void addSingalHook() {
		Runtime.getRuntime().addShutdownHook(new ShutdownHandler(this));
	}

	
	public ChannelFuture getChannelFuture() {
		return channelFuture;
	}

	public void shutdownBossGroup() {
		bossGroup.shutdownGracefully();
	}

	public boolean isBossGroupShuttingDown() {
		return bossGroup.isShuttingDown();
	}

	public void shutdownWorkerGroup() {
		workerGroup.shutdownGracefully();
	}

	public boolean isWorkerGroupShuttingDown() {
		return workerGroup.isShuttingDown();
	}

}
