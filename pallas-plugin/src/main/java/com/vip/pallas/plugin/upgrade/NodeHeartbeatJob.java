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

package com.vip.pallas.plugin.upgrade;

import com.vip.pallas.bean.NodeState;
import com.vip.pallas.plugin.utils.PluginInitializer;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

public class NodeHeartbeatJob implements Job {

    private static final org.apache.logging.log4j.Logger LOGGER = ESLoggerFactory.getLogger(NodeHeartbeatJob.class);

    public void execute(JobExecutionContext context) {
        try {
            PluginInitializer.postNodeState(NodeState.HEALTHY);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }
}