package com.vip.pallas.search.monitor;

import org.junit.Test;

import junit.framework.TestCase;

/**
 * Created by owen on 09/04/2018.
 */
public class GaugeMonitorServiceTest extends TestCase {

    @Test
    public void test() throws Exception {

        Thread.sleep(1100);
        GaugeMonitorService.incQPS();
        GaugeMonitorService.incQPS();
        //GaugeMonitorService.incConns();
        GaugeMonitorService.incReqesutThroughput(100L);
        GaugeMonitorService.incResponseThroughput(1000L);
        long ts = System.currentTimeMillis();
        ts = ts / 1000;
        String key = "\"" + ts + "000\"";
        boolean pass = false;
        short count = 6;
        while (!pass && count >= 0) {
            count--;
            Thread.sleep(3000);
            String info = GaugeMonitorService.collect().toString();
            //assertTrue(info.contains(key + ":" + 1));
            System.out.println("info.toString = " + info);
            pass = info.contains(key + ":" + 2) && info.contains(key + ":" + 100) && info.contains(key + ":" + 1000);

        }
        assertTrue(pass);
    }
}
