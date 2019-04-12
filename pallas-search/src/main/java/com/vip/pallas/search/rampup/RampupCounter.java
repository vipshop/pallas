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

package com.vip.pallas.search.rampup;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vip.pallas.search.model.IndexRampup;
import com.vip.pallas.search.service.PallasCacheFactory;
import com.vip.pallas.search.utils.HttpClient;
import com.vip.pallas.utils.PallasBasicProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RampupCounter implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(RampupCounter.class);

    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat("pallas-search-rampup-thread").build());

    private volatile static Map<Long, AtomicLong> rampupCounterMap = new ConcurrentHashMap<>();

    public static void start(){
        executorService.scheduleAtFixedRate(new RampupCounter(), 0, 5, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            Map<Long, IndexRampup> rampupMap = PallasCacheFactory.getCacheService().getRampupMap();

            if(rampupMap == null || rampupMap.isEmpty()){
                if(!rampupCounterMap.isEmpty()){
                    rampupCounterMap.clear();
                }
            }else{
                rampupCounterMap.forEach((versionId, rampup) -> {
                    if(rampupMap.containsKey(versionId)){
                        AtomicLong atomicLong = rampupCounterMap.get(versionId);
                        if(atomicLong.get() > 0){
                            postIncrement(versionId, atomicLong.getAndSet(0));
                        }
                    }else{
                        rampupCounterMap.remove(versionId);
                    }
                });
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    /**
     * 上报成功预热条数
     * @param versionId
     * @param increment
     */
    private void postIncrement(Long versionId, Long increment){
        if(increment > 0){
            try{
                String result = HttpClient.httpGet(PallasBasicProperties.PALLAS_CONSOLE_REST_URL + "/version/rampup/increment.json?versionId=" + versionId + "&increment=" + increment);

                if(StringUtils.isNotBlank(result)){
                    throw new Exception("error post rampup increment, cause by : " + result);
                }
            } catch (Exception e){
                LOGGER.error(e.toString(), e);
            }
        }
    }

    public static Map<Long, AtomicLong> getRampupCounterMap() {
        return rampupCounterMap;
    }
}
