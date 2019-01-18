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

package com.vip.pallas.utils;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class StopWatch {
//	private static Logger logger = LoggerFactory.getLogger(StopWatch.class);
//
//    private String name;
//    List<Long> timerList = new ArrayList<Long>();
//
//    public StopWatch(String name) {
//        this.name = name;
//    }
//
//    public String getTimerLog() {
//        StringBuilder sb = new StringBuilder(name + " TIME ");
//        if (timerList == null || timerList.size() < 2) {
//        } else if (timerList.size() > 1) {
//            for (int i = 0; i < timerList.size() - 1; i++) {
//                long l0 = timerList.get(i);
//                long l1 = timerList.get(i + 1);
//                sb.append(l1 - l0);
//                sb.append('/');
//            }
//            sb.append(timerList.get(timerList.size() - 1) - timerList.get(0));
//        }
//        return sb.toString();
//    }
//
//    public void stop() {
//        timerList.add(System.currentTimeMillis());
//    }
//
//    public void log(){
//        if (logger.isInfoEnabled()) {
//            logger.info(this.getTimerLog());
//        }
//    }
//
//    public void warn(){
//        logger.warn(this.getTimerLog());
//    }
//
//    public void log(long time){
//        StringBuilder sb = new StringBuilder(name + " TIME ");
//        if (timerList == null || timerList.size() < 2) {
//        } else if (timerList.size() > 1) {
//            for (int i = 0; i < timerList.size() - 1; i++) {
//                long l0 = timerList.get(i);
//                long l1 = timerList.get(i + 1);
//                sb.append(l1 - l0);
//                sb.append('/');
//            }
//            sb.append(time);
//            sb.append('/');
//            sb.append(timerList.get(timerList.size() - 1) - timerList.get(0));
//        }
//
//        logger.info(sb.toString());
//    }
//}