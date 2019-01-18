package com.vip.pallas.search.monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.elasticsearch.monitor.jvm.JvmInfo;
import org.elasticsearch.monitor.jvm.JvmStats;
import org.elasticsearch.monitor.os.OsProbe;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;

public class ServerWatch {
    private ZonedDateTime serverStartTime;
    private MemoryUsage memoryUsageAtStartTime;
    private InetAddress hostAddress;
    private MemoryMXBean memoryMXBean;

	private static final SimplePropertyPreFilter PROPERTY_PRE_FILTER = new SimplePropertyPreFilter();
    static {
		PROPERTY_PRE_FILTER.getExcludes().add("tbFrac");
		PROPERTY_PRE_FILTER.getExcludes().add("tb");
		PROPERTY_PRE_FILTER.getExcludes().add("pbFrac");
		PROPERTY_PRE_FILTER.getExcludes().add("pb");
		PROPERTY_PRE_FILTER.getExcludes().add("mbFrac");
		PROPERTY_PRE_FILTER.getExcludes().add("gbFrac");
		PROPERTY_PRE_FILTER.getExcludes().add("bytes");
		PROPERTY_PRE_FILTER.getExcludes().add("kbFrac");
		PROPERTY_PRE_FILTER.getExcludes().add("kb");
		PROPERTY_PRE_FILTER.getExcludes().add("gb");
		PROPERTY_PRE_FILTER.getExcludes().add("kbFrac");
		PROPERTY_PRE_FILTER.getExcludes().add("days");
		PROPERTY_PRE_FILTER.getExcludes().add("hours");
		PROPERTY_PRE_FILTER.getExcludes().add("daysFrac");
		PROPERTY_PRE_FILTER.getExcludes().add("hoursFrac");
		PROPERTY_PRE_FILTER.getExcludes().add("micros");
		PROPERTY_PRE_FILTER.getExcludes().add("microsFrac");
		PROPERTY_PRE_FILTER.getExcludes().add("millis");
		PROPERTY_PRE_FILTER.getExcludes().add("millisFrac");
		PROPERTY_PRE_FILTER.getExcludes().add("minutesFrac");
		PROPERTY_PRE_FILTER.getExcludes().add("nanos");
		PROPERTY_PRE_FILTER.getExcludes().add("seconds");
		PROPERTY_PRE_FILTER.getExcludes().add("secondsFrac");
		PROPERTY_PRE_FILTER.getExcludes().add("stringRep");
		PROPERTY_PRE_FILTER.getExcludes().add("classPath");
		PROPERTY_PRE_FILTER.getExcludes().add("java.class.path");
		PROPERTY_PRE_FILTER.getExcludes().add("java.library.path");
		PROPERTY_PRE_FILTER.getExcludes().add("bootClassPath");
		PROPERTY_PRE_FILTER.getExcludes().add("systemProperties");
    }
    
    public ServerWatch() throws UnknownHostException {
    	init();
    }
    
    private void init() throws UnknownHostException {
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.serverStartTime = ZonedDateTime.now();
        this.memoryUsageAtStartTime = this.memoryMXBean.getHeapMemoryUsage();
        this.hostAddress = lookupHost();
    }

    private InetAddress lookupHost() throws UnknownHostException {
       return InetAddress.getLocalHost();
    }

    public String getMemoryUsageAtStartTime() {
        return memoryUsageAtStartTime.toString();
    }

    public ZonedDateTime getServerStartTime() {
        return serverStartTime;
    }

//    public double getCurrentMemoryUsageInMb() {
//        final MemoryUsage currentMemoryUsage = this.memoryMXBean.getHeapMemoryUsage();
//        return toMb(currentMemoryUsage.getUsed());
//    }

//    public double getAvailableMemoryInMb() {
//        final MemoryUsage currentMemoryUsage = this.memoryMXBean.getHeapMemoryUsage();
//        final long availableMemory = currentMemoryUsage.getCommitted() - currentMemoryUsage.getUsed();
//        return toMb(availableMemory);
//    }

    public JSONObject getHostInfo() {
    	JSONObject jo = new JSONObject();
        if (hostAddress == null) {
            return jo.fluentPut("Host", "<unknown>");
        }
        return jo.fluentPut("Host", this.hostAddress.getHostName())
                .fluentPut("IP", this.hostAddress.getHostAddress());
    }

    public String getServerUpTimeInHms() {
        return toHms(ChronoUnit.MILLIS.between(this.getServerStartTime(), ZonedDateTime.now()));
    }

    public JSONObject getOsInfo() {
    	return JSONObject.parseObject(JSON.toJSONString(OsProbe.getInstance().osStats(), PROPERTY_PRE_FILTER));
    }

    public JSONObject getVmInfo() {
        return JSONObject.parseObject(JSON.toJSONString(JvmInfo.jvmInfo(), PROPERTY_PRE_FILTER));
    }

    public JSONObject getVmStats() {
        return JSONObject.parseObject(JSON.toJSONString(JvmStats.jvmStats(), PROPERTY_PRE_FILTER));
    }


    private String toHms(long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

//    private double toMb(long bytes) {
//        return bytes / 1024 / 1024;
//    }
    
    public JSONObject buildAllInfo() {
    	return getVmInfo().fluentPutAll(getOsInfo())
                .fluentPutAll(getHostInfo())
                .fluentPut("Up Time", getServerUpTimeInHms())
                .fluentPutAll(getVmStats())
                .fluentPut("gaugesStatistics", GaugeMonitorService.collect());
    }
    
}