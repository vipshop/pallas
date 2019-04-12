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
