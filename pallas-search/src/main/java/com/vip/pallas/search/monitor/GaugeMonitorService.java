package com.vip.pallas.search.monitor;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * Created by owen on 22/03/2018.
 */
public class GaugeMonitorService {

    /** 最长保存在内存时间 **/
    private static final int HISTORY_MAX_SIZE = 600;

    /** 默认上报大小，默认只上报 60s 数据 **/
    private static final int REPORT_TO_CONSOLE_SIZE = 60;

    private static final int DEFUAL_BUCKETS_SIZE = 10;
    /** 连接数 **/
    private static final LongAdder connectionsGauge = new LongAdder();
    /** 历史连接数 **/
    private static ConcurrentLinkedDeque<Map<String, Long>> connectionsHistory = new ConcurrentLinkedDeque<>();
    /** QPS **/
    private static final List<LongAdder> qpsGauges = new ArrayList<>(DEFUAL_BUCKETS_SIZE);
    /** 历史QPS **/
    private static ConcurrentLinkedDeque<Map<String, Long>> qpsCounterHistory = new ConcurrentLinkedDeque<>();

    /** Request THROUGHPUT **/
    private static final List<LongAdder> reqThroughputGauges = new ArrayList<>(DEFUAL_BUCKETS_SIZE);
    /** 历史 Request THROUGHPUT **/
    private static ConcurrentLinkedDeque<Map<String, Long>> reqThroughputHistory = new ConcurrentLinkedDeque<>();

    /** Response THROUGHPUT **/
    private static final List<LongAdder> respThroughputGauges = new ArrayList<>(DEFUAL_BUCKETS_SIZE);
    /** 历史 Response THROUGHPUT **/
    private static ConcurrentLinkedDeque<Map<String, Long>> respThroughputHistory = new ConcurrentLinkedDeque<>();

    /** 最后搜集的 QPS 的时刻，因为有GC等原因，可能会有未处理的QPS的 bucket 所以要用这个时间来确保 **/
    private static long lastUpdateTime;

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("Pallas-Search-GaugeMonitor-Thread-1").build());

    static {
        for (int i = 0; i < DEFUAL_BUCKETS_SIZE; i++) {
            qpsGauges.add(new LongAdder());
            reqThroughputGauges.add(new LongAdder());
            respThroughputGauges.add(new LongAdder());
        }
        executorService.scheduleWithFixedDelay(collectTask(), 50, 1000, TimeUnit.MILLISECONDS);
        lastUpdateTime = now();
        lastUpdateTime -= lastUpdateTime % 1000;
    }

    /**
     * 增加一条连接
     */
    public static void incConns() {
        connectionsGauge.increment();
    }

    /**
     * 减少一条连接
     */
    public static void decConns() {
        connectionsGauge.decrement();
    }

    /**
     * 增加一条QPS
     */
    public static void incQPS() {
        qpsGauges.get(currentIdx()).increment();
    }

    /**
     * 增加请求Throughput
     * @param delta
     */
    public static void incReqesutThroughput(long delta) {
        reqThroughputGauges.get(currentIdx()).add(delta);
    }

    /**
     * 增加ES Response的Throughput
     * @param delta
     */
    public static void incResponseThroughput(long delta) {
        respThroughputGauges.get(currentIdx()).add(delta);
    }

    private static int currentIdx() {
       return (int)((now()/1000) % DEFUAL_BUCKETS_SIZE);
    }


    private static long now() {
        return System.currentTimeMillis();
    }

    /**
     * 每秒调度一次，主要任务是
     * 1. 获取当前时间戳的上一秒作为记录时刻
     * 2. 对于Connections 统计，直接记录一个点，如果上一秒打了2个点或者GC导致多秒没有记录也不care
     * 3. 对于QPS 统计，由于有GC等因素，会从上一个记录点开始直到（1）的这个记录时刻，收集所有的QPS 入Queue
     * 4. 最后保持各个Queue的大小
     * @return
     */
    private static Runnable collectTask() {

        return () -> {
            long lastSec = now();
            //这次只处理直到上一秒的数据，因为当前这一秒还在累加着
            lastSec = lastSec - (lastSec % 1000) - 1000;

            //把当前的连接数作为上一秒的 data point value
            long currentConns = connectionsGauge.sum();
            Map<String, Long> map = new HashMap<>();
            map.put("" + lastSec, currentConns);
            connectionsHistory.addFirst(map);

            //保存最后更新时间到最后1秒的所有QPS记录，主要逻辑如下:
            //1. 拿来临时存放有一个环数组，默认大小是10，每个QPS或者标的物过来会根据当前时间选中一个下标落坑
            //2. 由于实际运行会转很多手，GUI获取数据也不是实时，所以这里的k/v是带上时间戳
            //3. 默认10 大小的环数组支持最多10s 的延迟没有被拿取并记录到History队列中去（例如GC了好几秒）
            for (long i = lastUpdateTime + 1000; i <= lastSec; i += 1000) {
                int idx = (int)((i/1000) % DEFUAL_BUCKETS_SIZE);
                long count = qpsGauges.get(idx).sumThenReset();
                map = new HashMap<>();
                map.put("" + i, count);
                qpsCounterHistory.addFirst(map);

                count = reqThroughputGauges.get(idx).sumThenReset();
                map = new HashMap<>();
                map.put("" + i, count);
                reqThroughputHistory.addFirst(map);

                count = respThroughputGauges.get(idx).sumThenReset();
                map = new HashMap<>();
                map.put("" + i, count);
                respThroughputHistory.addFirst(map);
            }
            //记录上次处理到第几秒的数据
            lastUpdateTime = lastSec;

            //保持各个Queue的大小，上面的逻辑是每次调度都会往 History 队列的 head 加上data point，同时在下面的逻辑
            //去校验 History Queue的长度，这里不想在达到 HISTORY_MAX_SIZE 的时候每秒都去add head 和remove tail
            //因此这里的策略是让 History Queue 自然增长到 HISTORY_MAX_SIZE*2 再去裁剪，每次裁剪 HISTORY_MAX_SIZE
            int size = connectionsHistory.size();
            int bound = HISTORY_MAX_SIZE * 2;
            if (size > bound) {
                for (int i = 0; i < HISTORY_MAX_SIZE; i++) {
                    connectionsHistory.pollLast();
                }
            }
            size = qpsCounterHistory.size();
            if (size > bound) {
                for (int i = 0; i < HISTORY_MAX_SIZE; i++) {
                    qpsCounterHistory.pollLast();
                }
            }
            size = reqThroughputHistory.size();
            if (size > bound) {
                for (int i = 0; i < HISTORY_MAX_SIZE; i++) {
                    reqThroughputHistory.pollLast();
                }
            }
            size = respThroughputHistory.size();
            if (size > bound) {
                for (int i = 0; i < HISTORY_MAX_SIZE; i++) {
                    respThroughputHistory.pollLast();
                }
            }
        };
    }


    /**
     * 搜集指标 QPS + Connections + Throughput
     * 这个方法由 ServerWatch 调用用于把采集到的信息上报到 Console，这里每次采集都会从 History Queue
     * 的head开始读取 REPORT_TO_CONSOLE_SIZE 个值上报给Console，注意这里**不能把**这些采集的数据从Queue移除
     * @return the collection of guages in terms of Json Format
     */
    public static JSONObject collect() {
        JSONObject jsonObj = new JSONObject();

        int size = qpsCounterHistory.size();
        int idx = REPORT_TO_CONSOLE_SIZE > size ? size : REPORT_TO_CONSOLE_SIZE;
        Iterator<Map<String, Long>> it = qpsCounterHistory.iterator();
        jsonObj.put("qps", fetchFirstIndexItems(it, idx));


        size = reqThroughputHistory.size();
        idx = REPORT_TO_CONSOLE_SIZE > size ? size : REPORT_TO_CONSOLE_SIZE;
        it = reqThroughputHistory.iterator();

        jsonObj.put("reqThroughput", fetchFirstIndexItems(it, idx));

        size = respThroughputHistory.size();
        idx = REPORT_TO_CONSOLE_SIZE > size ? size : REPORT_TO_CONSOLE_SIZE;
        it = respThroughputHistory.iterator();

        jsonObj.put("respThroughput", fetchFirstIndexItems(it, idx));



        size = connectionsHistory.size();
        idx = REPORT_TO_CONSOLE_SIZE > size ? size : REPORT_TO_CONSOLE_SIZE;
        it = connectionsHistory.iterator();
        jsonObj.put("conns", fetchFirstIndexItems(it, idx));
        return jsonObj;
    }

    /**
     * 从各个Gauge Map中收集 Size 个的sample， 为了对齐，这里把 每隔10s 前的几秒的样本skip
     * @param it
     * @param size
     * @return
     */
    private static Map<String, Long> fetchFirstIndexItems(Iterator<Map<String, Long>> it, long size) {
        Map<String, Long> map = new LinkedHashMap<>();
        boolean needSkipFront = true;
        while (it.hasNext() && size > 0) {
            Map<String, Long> item = it.next();
            if (needSkipFront && !item.keySet().iterator().next().endsWith("0000")) {
                // System.out.println("skip:" + item.keySet().iterator().next());
                continue;
            }
            needSkipFront = false;
            map.putAll(item);
            size--;
        }
        return map;
    }



}
