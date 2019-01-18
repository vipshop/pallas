package com.vip.pallas.search.utils;

import com.vip.pallas.utils.PallasBasicProperties;

public class PallasSearchProperties extends PallasBasicProperties {

	// server端 boss的线程个数默认为min(core/2,2)
	private static final String NAME_BOSS_SIZE = "pallas.boss.size";
	public static final int BOSS_GROUP_SIZE = processor.getInteger(NAME_BOSS_SIZE, Math.min(Runtime.getRuntime().availableProcessors() / 2, 2));

	// worker的个数默认为core的个数
	private static final String NAME_WORKER_SIZE = "pallas.worker.size";
	public static int WORKER_GROUP_SIZE = processor.getInteger(NAME_WORKER_SIZE, Runtime.getRuntime().availableProcessors());

	// TCP包组成HTTP包的最大长度，默认为64M
	private static final String NAME_HTTP_AGGREGATOR_MAXLENGTH = "pallas.http.aggregator.maxlength";
	public static int HTTP_AGGREGATOR_MAXLENGTH = processor.getInteger(NAME_HTTP_AGGREGATOR_MAXLENGTH, 1024 * 1024 * 64);

	// http 单个ip连接池的最大连接数
	private static final String NAME_CONNECTION_MAX_PER_ROUTE = "pallas.connection.max_per_route";
	public static final Integer CONNECTION_MAX_PER_ROUTE = processor.getInteger(NAME_CONNECTION_MAX_PER_ROUTE, 500);

	// http io thread num
	private static final String NAME_CONNECTION_IO_THREAD_NUM = "pallas.connection.io_thread_num";
	public static final Integer CONNECTION_IO_THREAD_NUM = processor.getInteger(NAME_CONNECTION_IO_THREAD_NUM, Runtime.getRuntime().availableProcessors());

	// http连接池的最大连接数
	private static final String NAME_CONNECTION_MAX = "pallas.connection.max";
	public static final Integer PALLAS_CONNECTION_MAX = processor.getInteger(NAME_CONNECTION_MAX, 3000);

	// 连接http 最大超时时间 默认单元是 ms
	private static final String NAME_HTTP_CONNECTION_TIMEOUT = "pallas.http.connection.timeout";
	public static final Integer HTTP_CONNECTION_TIMEOUT = processor.getInteger(NAME_HTTP_CONNECTION_TIMEOUT, 5000);

	//等待结果返回时间
	private static final String NAME_HTTP_SOCKET_TIMEOUT = "pallas.http.socket.timeout";
	public static final Integer HTTP_SOCKET_TIMEOUT = processor.getInteger(NAME_HTTP_SOCKET_TIMEOUT, 120*1000);

	// http connector获取连接池的超时时间 ms
	private static final String NAME_HTTP_POOL_AQUIRE_TIMEOUT = "pallas.http.pool_aquire_timeout";
	public static final Integer HTTP_POOL_AQUIRE_TIMEOUT = processor.getInteger(NAME_HTTP_POOL_AQUIRE_TIMEOUT, 5000);

	// http server的参数:maxInitialLineLength 长度，超过的话抛出 TooLongFrameException
	private static final String NAME_HTTP_SERVER_MAXINITIALLINELENGTH = "pallas.http.server_maxInitialLineLength";
	public static final Integer HTTP_SERVER_MAXINITIALLINELENGTH = processor.getInteger(NAME_HTTP_SERVER_MAXINITIALLINELENGTH, 4096);

	// http server的参数:header 长度，超过的话抛出 TooLongFrameException (处理方式和maxInitialLineLength一致)
	private static final String NAME_HTTP_SERVER_MAXHEADERSIZE = "pallas.http.server_maxHeaderSize";
	public static final Integer HTTP_SERVER_MAXHEADERSIZE = processor.getInteger(NAME_HTTP_SERVER_MAXHEADERSIZE, 8192);

	// http server参数：对header的name和value进行校验。 一般校验特殊字符或者非空 。 是针对set？？还需要调研
	private static final String NAME_HTTP_SERVER_VALIDATEHEADERS = "pallas.http.server_validateHeaders";
	public static final Boolean HTTP_SERVER_VALIDATEHEADERS = processor.getBoolean(NAME_HTTP_SERVER_VALIDATEHEADERS, true);

	// http server：
	private static final String NAME_HTTP_SERVER_KEEPALIVE_TIMEOUT = "pallas.http.server_keepalive_timeout";
	public static final Integer HTTP_SERVER_KEEPALIVE_TIMEOUT = processor.getInteger(NAME_HTTP_SERVER_KEEPALIVE_TIMEOUT, 75 * 1000);
		
	// check and close idle thread interval
	private static final String NAME_HTTP_CHECK_IDLE_INTERVAL_IN_MILS = "pallas.http.check_idle_interval_in_mils";
	public static final Integer HTTP_CHECK_IDLE_INTERVAL_IN_MILS = processor.getInteger(NAME_HTTP_CHECK_IDLE_INTERVAL_IN_MILS, 10 * 1000);

	private static final String NAME_HTTP_SERVER_COMPRESS = "pallas.http.server_compress";
	public static final Boolean HTTP_SERVER_COMPRESS = processor.getBoolean(NAME_HTTP_SERVER_COMPRESS, true);
		
	private static final String NAME_HTTP_SERVER_COMPRESS_TYPE = "pallas.http.server_compress_type";
	public static final String HTTP_SERVER_COMPRESS_TYPE = processor.getString(NAME_HTTP_SERVER_COMPRESS_TYPE, "gzip");

	private static final String NAME_HTTP_SERVER_GZIP_MIN_LENGTH = "pallas.http.server_gzip_min_length";
	public static final Integer HTTP_SERVER_GZIP_MIN_LENGTH = processor.getInteger(NAME_HTTP_SERVER_GZIP_MIN_LENGTH, 1 * 1024);
		
	// 压缩级别
	private static final String NAME_HTTP_SERVER_GZIP_COMP_LEVEL = "pallas.http.server_gzip_comp_level";
	public static final Integer HTTP_SERVER_GZIP_COMP_LEVEL = processor.getInteger(NAME_HTTP_SERVER_GZIP_COMP_LEVEL, 3);

	private static final String NAME_VALID_COOKIE = "pallas.valid_cookie";
	public final static Boolean VALID_COOKIE = processor.getBoolean(NAME_VALID_COOKIE, false);

	private static final String NAME_HEALTH_INVALID_TIME = "pallas.health_invalid_time";
	public static final Integer HEALTH_INVALID_TIME = processor.getInteger(NAME_HEALTH_INVALID_TIME, 10 * 1000);
		
	private static final String NAME_HTTP_RETRY_NUM = "pallas.http.retry_num";
	public static final Integer HTTP_RETRY_NUM = processor.getInteger(NAME_HTTP_RETRY_NUM, 1);
		
	private static final String NAME_RETURN_OK_URL = "pallas.return_ok_url";
	public final static String RETURN_OK_URL = processor.getString(NAME_RETURN_OK_URL, null);

	private static final String NAME_HEALTH_CHECK_URL = "pallas.health_check_url";
	public final static String HEALTH_CHECK_URL = processor.getString(NAME_HEALTH_CHECK_URL, null);

	private static final String NAME_ACCESSLOG_DISABLE = "pallas.accesslog.disable";
	public final static Boolean PALLAS_ACCESSLOG_DISABLE = processor.getBoolean(NAME_ACCESSLOG_DISABLE, false);

	private static final String NAME_ACCESSLOG_PRINT_REQUESTBODY = "pallas.accesslog_print_requestbody";
	public final static Boolean ACCESSLOG_PRINT_REQUESTBODY = processor.getBoolean(NAME_ACCESSLOG_PRINT_REQUESTBODY, false);

	private static final String NAME_ACCESSLOG_PRINT_REQUESTBODY_MAX_SIZE = "pallas.accesslog_print_requestbody_max_size";
	public final static Integer ACCESSLOG_PRINT_REQUESTBODY_MAX_SIZE = processor.getInteger(NAME_ACCESSLOG_PRINT_REQUESTBODY_MAX_SIZE, 4096);

	private static final String NAME_SEARCH_PORT = "pallas.search.port";
	public static final int PALLAS_SEARCH_PORT = processor.getInteger(NAME_SEARCH_PORT, 9201);
		
	private static final String NAME_SEARCH_CLUSTER = "pallas.search.cluster";
	public static final String PALLAS_SEARCH_CLUSTER = processor.getString(NAME_SEARCH_CLUSTER, "SHARED-CLUSTER");
		
	private static final String NAME_CONSOLE_UPLOAD_URL = "pallas.console.upload_url";
	public static final String CONSOLE_UPLOAD_URL = processor.getString(NAME_CONSOLE_UPLOAD_URL, processor.getString(PALLAS_CONSOLE_REST_URL, "http://localhost:8080/pallas") + "/ss/upsert.json");
		
	private static final String NAME_SEARCH_SKIP_ROUTING = "pallas.search.skip.routing";
	public static final Boolean SEARCH_SKIP_ROUTING = processor.getBoolean(NAME_SEARCH_SKIP_ROUTING, false);

	private static final String NAME_SEARCH_AUTHORICATION_DEFAULT = "pallas.search.authorication.default";
	public static final String PALLAS_SEARCH_DEFAULT_AUTHORICATION = processor.getString(NAME_SEARCH_AUTHORICATION_DEFAULT, "ReadOnly");

	private static final String NAME_SEARCH_RETRY_THREADS = "pallas.search.retry.threads";
	public static final int SEARCH_RETRY_THREADS = processor.getInteger(NAME_SEARCH_RETRY_THREADS, 4);
	
	private static final String NAME_HTTP_HEADER_REMOTE_ADDRESS = "pallas.http.header.remote.address";
	public static final String HTTP_HEADER_REMOTE_ADDRESS = processor.getString(NAME_HTTP_HEADER_REMOTE_ADDRESS, "");
}
