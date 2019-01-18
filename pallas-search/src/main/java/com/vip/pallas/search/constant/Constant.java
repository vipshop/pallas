package com.vip.pallas.search.constant;

public interface Constant {

    public final static String SYS_ENV_CLUSTER_NAME = "PALLAS_CLUSTER_NAME";

    public final static String HEAD_TRACEID = "X-Traceid";

    // netty bossGroupSize Hermes埋点
    public final static String PALLAS_HERMES_BOSS_GROUP_SIZE = "pallasBossGroupSize";

    // netty WorkerGroupSize Hermes埋点
    public final static String PALLAS_HERMES_WORKER_GROUP_SIZE = "pallasWorkerGroupSize";

    // pallasGzip 状态 Hermes埋点
    public final static String PALLAS_HERMES_GZIP_STATUS = "pallasGzipStatus";

    // gzip_min_length Hermes埋点
    public final static String PALLAS_HERMES_GZIP_MIN_LENGTH = "pallasGzipMinlength";

    // keepalive超时时间 Hermes埋点
    public final static String PALLAS_HERME_KEEP_ALIVE_TIMEOUT = "pallasKeepAliveTimeout";

    // 连接池 MaxConnections Hermes埋点
    public final static String PALLAS_HERMES_MAX_CONNECTIONS = "pallasMaxConnections";

    // 连接池 MaxConnectionPerRoute Hermes埋点
    public final static String PALLAS_HERMES_MAX_CONNECTION_PER_ROUTE = "pallasMaxConnectionPerRoute";

    // 连接空闲时间Hermes埋点
    public final static String PALLAS_HERMES_HTTP_IDLET_IMEOUT = "pallasHttpIdleTimeout";

    // aquire_timeout Hermes埋点
    public final static String PALLAS_HERMES_AQUIRE_POOL_TIMEOUT = "pallasAquirePoolTimeout";

    // Http Connect Timeout Hermes埋点
    public final static String PALLAS_HERMES_HTTP_CONNECT_TIMEOUT = "pallasHttpConnectTimeout";

    // 连接池 MaxPendings Hermes埋点
    public final static String PALLAS_HERMES_MAXPENDINGS = "pallasMaxPendings";

    // ServerInfo中的Ip,端口,holdingTimeout埋点
    public final static String PALLAS_HERMES_SERVERINFO = "pallasServerInfo";

    // RestChannelPool中的acquiredChannelCount埋点
    public final static String PALLAS_HERMES_ACQUIRED_CHANNELCOUNT = "PallasAcquiredChannelCount";

    // pallas对外 当前连接数Hermes埋点
    public final static String PALLAS_HERMES_IN_CONNECT_COUNT = "pallas_in_connect_count";

    // pallas对外 超过最大水平位Hermes埋点
    public final static String PALLAS_HERMES_HIGH_WATER_MARK = "pallas_high_water_mark";

    // pallas对外 新建连接数 Hermes埋点
    public final static String PALLAS_HERMES_NEW_CONNMETER = "pallasNewConnMeter";

    // pallas对外 当前连接数 Hermes埋点
    public final static String PALLAS_HERMES_ACTIVE_CONN_COUNTER = "pallasActiveConnCounter";

    // pallas对外连接时间 Hermes埋点
    public final static String PALLAS_HERMES_CONN_LIVE_TIMER = "pallasConnLiveTimer";

	// pallas对外 pallas主动关闭连接数 Hermes埋点
	public final static String PALLAS_HERMES_CONN_IDLEMETER = "pallasConnIdleCloseMeter";

	// pallas对外 pallas被动关闭连接数 Hermes埋点
	public final static String PALLAS_HERMES_CONN_CLOSE_METER = "pallasconnTotalCloseMeter";

	//jmx netty direct byte buffer object name 
	public final static String JMX_NETTY_DIRECT_BYTE_BUF_OBJECT_NAME = "com.vip.pallas.search:type=ByteBuf,name=direct";

	public final static String PALLAS_MERCURY_TRACEID_NAME = "traceId";
	
	public final static String PALLAS_MERCURY_PARENT_SPAN_ID_NAME = "parentId";

	String PALLAS_MERCURY_PARENT_IS_SAMPLE = "X-B3-Sampled";

	String PALLAS_MERCURY_PARENT_SFQ = "X-B3-Sfq";
}
