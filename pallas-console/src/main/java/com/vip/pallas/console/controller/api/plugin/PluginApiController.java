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

package com.vip.pallas.console.controller.api.plugin;

import com.vip.pallas.bean.PluginActionType;
import com.vip.pallas.bean.PluginCommands;
import com.vip.pallas.bean.PluginStates;
import com.vip.pallas.bean.PluginType;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.PluginCommand;
import com.vip.pallas.mybatis.entity.PluginRuntime;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.PallasPluginService;
import com.vip.pallas.utils.ElasticRestClient;
import com.vip.pallas.utils.JsonUtil;
import com.vip.pallas.utils.ObjectMapTool;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/rest/plugin")
public class PluginApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginApiController.class);

    @Autowired
    private PallasPluginService pluginService;

    @Autowired
    private ClusterService clusterService;

    private static volatile Map<String, Object> NODE_IP_LOCK_MAP = new ConcurrentHashMap<>();

    @RequestMapping("/keepalive.json")
    public Map<String, Object> execute(@RequestBody Map<String, Object> params) throws Exception { // NOSONAR
        Map<String, Object> resultMap = new HashMap<>();

        String pluginStatesStr = ObjectMapTool.getObject(params, "message", String.class);

        PluginStates pluginStates = JsonUtil.readValue(pluginStatesStr, PluginStates.class);

        String nodeIp = pluginStates.getNodeIp();

        if(isIllegalNode(pluginStates.getClusterId(), nodeIp)){
            return resultMap;
        }

        NODE_IP_LOCK_MAP.putIfAbsent(nodeIp, new Object());

        List<PluginStates.Plugin> pluginList = pluginStates.getPlugins();

        try {
            if(pluginList != null){
                List<PluginRuntime> runtimeList = new LinkedList<>();
                for(PluginStates.Plugin plugin : pluginList) {

                    PluginRuntime pRuntime = new PluginRuntime();
                    pRuntime.setCreateTime(new Date());
                    pRuntime.setClusterId(pluginStates.getClusterId());
                    pRuntime.setNodeIp(nodeIp);
                    pRuntime.setNodeHost(pluginStates.getNodeHost());
                    pRuntime.setPluginName(plugin.getName());
                    pRuntime.setPluginVersion(plugin.getVersion() == null ? "" : plugin.getVersion());
                    pRuntime.setPluginType(plugin.getType().getValue());
                    pRuntime.setAvailableVersions(plugin.getAvailableVersions() == null ? "" : plugin.getAvailableVersions());
                    runtimeList.add(pRuntime);
                }
                synchronized (NODE_IP_LOCK_MAP.get(nodeIp)){
                    pluginService.addOrUpdatePluginRuntime(runtimeList);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            throw new BusinessLevelException(500, "解析Request 错误：" + e.getMessage());
        }

        List<PluginCommand> commandList = pluginService.getCommandsByIp(pluginStates.getClusterId(), nodeIp);

        PluginCommands.Action downloadAction = new PluginCommands.Action();
        downloadAction.setActionType(PluginActionType.DOWNLOAD);

        PluginCommands.Action enableAction = new PluginCommands.Action();
        enableAction.setActionType(PluginActionType.ENABLE);

        PluginCommands.Action removeAction = new PluginCommands.Action();
        removeAction.setActionType(PluginActionType.REMOVE);

        PluginCommands.Action restartAction = new PluginCommands.Action();
        restartAction.setActionType(PluginActionType.RESTART);

        if(commandList != null){
            PluginCommands pluginCommands = new PluginCommands();
            pluginCommands.setClusterId(pluginStates.getClusterId());

            for (PluginCommand pluginCommand : commandList) {
                String command = pluginCommand.getCommand();

                PluginCommands.Plugin plugin = new PluginCommands.Plugin();
                plugin.setName(pluginCommand.getPluginName());
                plugin.setVersion(pluginCommand.getPluginVersion());
                plugin.setType(PluginType.getPluginTypeByValue(pluginCommand.getPluginType()));

                if(PluginActionType.DOWNLOAD.getDesc().equals(command)){
                    downloadAction.addPlugin(plugin);
                }else if(PluginActionType.ENABLE.getDesc().equals(command)){
                    enableAction.addPlugin(plugin);
                }else if(PluginActionType.REMOVE.getDesc().equals(command)){
                    pluginService.deletePluginRuntime(pluginStates.getClusterId(),
                            pluginCommand.getPluginName(), pluginStates.getNodeIp(), pluginCommand.getPluginVersion());
                    removeAction.addPlugin(plugin);
                }else if(PluginActionType.RESTART.getDesc().equals(command)){
                    restartAction.addPlugin(plugin);
                }
            }

            if(downloadAction.getPlugins() != null && downloadAction.getPlugins().size() > 0){
                pluginCommands.addAction(downloadAction);
            }

            if(enableAction.getPlugins() != null && enableAction.getPlugins().size() > 0){
                pluginCommands.addAction(enableAction);
            }

            if(removeAction.getPlugins() != null && removeAction.getPlugins().size() > 0){
                pluginCommands.addAction(removeAction);
            }

            if(restartAction.getPlugins() != null && restartAction.getPlugins().size() > 0){
                pluginCommands.addAction(restartAction);
            }

            resultMap.put("response", pluginCommands);
        }

        resultMap.put("status", 0);

        return resultMap;
    }

    //非法节点返回true，合法返回false
    private boolean isIllegalNode(String clusterId, String nodeIp){
        Cluster cluster = clusterService.findByName(clusterId);
        if(cluster == null){
            return true;
        }
        RestClient client = ElasticRestClient.build(cluster.getHttpAddress());
        Response response = null;
        try {
            response = client.performRequest("GET", "/_cat/nodes");
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
            return true;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.contains(nodeIp)){
                    return false;
                }
            }
        } catch(Exception e) {
            LOGGER.error(e.toString(), e);
        }
        return true;
    }

}