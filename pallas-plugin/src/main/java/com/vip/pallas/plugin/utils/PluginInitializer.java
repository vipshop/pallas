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

package com.vip.pallas.plugin.utils;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.logging.ESLoggerFactory;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.vip.pallas.bean.NodeState;
import com.vip.pallas.plugin.PallasPlugin;
import com.vip.pallas.plugin.listen.PluginListener;
import com.vip.pallas.plugin.upgrade.NodeHeartbeatJob;
import com.vip.pallas.plugin.upgrade.PluginKeepaliveJob;
import com.vip.pallas.utils.HttpClient;
import com.vip.pallas.utils.IPUtils;
import com.vip.pallas.utils.JsonUtil;
import com.vip.pallas.utils.PallasBasicProperties;
import com.vip.pallas.utils.PallasConsoleProperties;

public class PluginInitializer {

    private static final org.apache.logging.log4j.Logger LOGGER = ESLoggerFactory.getLogger(PluginInitializer.class);

    private static final String PLUGIN_KEEPALIVE_CRON = System.getProperty("plugin.keepalive.cron", "0/10 * * * * ?");//默认10S
    private static final String NODE_HEARTBEAT_CRON = System.getProperty("node.heartbeat.cron", "0/10 * * * * ?");//默认10S

    public static void init() {
        new PluginListener().monitor();

        try {
            initKeepaliveScheduler();
            initHeartbeatScheduler();
            postNodeState(NodeState.STARTED);
            LOGGER.info("initKeepaliveScheduler successed.");
        } catch (Exception e) {
            LOGGER.error("init keepaliveScheduler error cause by: {}", e.toString(), e);
        }
    }

    public static void postNodeState(NodeState nodeState) throws Exception {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("clusterName", PallasPlugin.clusterName);
        inputMap.put("nodeName", PallasPlugin.nodeName);
        inputMap.put("nodeIp", IPUtils.localIp4Str());
        inputMap.put("state", nodeState.getValue());

        if(NodeState.STARTED.getValue() == nodeState.getValue()){
            inputMap.put("isRestart", System.getProperty("pallas.node.restart"));
        }

        HttpClient.httpPost(PallasBasicProperties.PALLAS_CONSOLE_REST_URL + "/node/state.json", JsonUtil.toJson(inputMap));
    }

    public static void initKeepaliveScheduler()throws Exception
    {
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        JobDetail job = JobBuilder.newJob(PluginKeepaliveJob.class).withIdentity("plugin-state-report-job", "pallas-plugin").build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("plugin-state-report-trigger", "pallas-plugin")
                .withSchedule(CronScheduleBuilder.cronSchedule(PLUGIN_KEEPALIVE_CRON))
                .build();

        sched.scheduleJob(job, trigger);
        sched.start();
    }

    public static void initHeartbeatScheduler()throws Exception
    {
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler sched = sf.getScheduler();

        JobDetail job = JobBuilder.newJob(NodeHeartbeatJob.class).withIdentity("node-heartbeat-job", "pallas-plugin").build();

        CronTrigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("node-heartbeat-trigger", "pallas-plugin")
                .withSchedule(CronScheduleBuilder.cronSchedule(NODE_HEARTBEAT_CRON))
                .build();

        sched.scheduleJob(job, trigger);
        sched.start();
    }
}