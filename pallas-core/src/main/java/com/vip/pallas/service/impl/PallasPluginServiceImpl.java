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

package com.vip.pallas.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.mybatis.entity.PluginCommand;
import com.vip.pallas.mybatis.entity.PluginRuntime;
import com.vip.pallas.mybatis.entity.PluginUpgrade;
import com.vip.pallas.mybatis.repository.PluginCommandRepository;
import com.vip.pallas.mybatis.repository.PluginRuntimeRepository;
import com.vip.pallas.mybatis.repository.PluginUpgradeRepository;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.PallasPluginService;
import com.vip.pallas.utils.ElasticRestClient;

/**
 * Created by owen on 13/9/2017.
 */
@Service
public class PallasPluginServiceImpl implements PallasPluginService {

    private static final Logger logger = LoggerFactory.getLogger(PallasPluginServiceImpl.class);

    @Resource
    private ClusterService clusterService;

    @Resource
    private PluginRuntimeRepository runtimeRepository;

    @Resource
    private PluginUpgradeRepository upgradeRepository;

    @Resource
    private PluginCommandRepository commandRepository;


    @Override
    public int addOrUpdatePluginRuntime(List<PluginRuntime> runtimeList) {
        for (PluginRuntime r : runtimeList) {
            PluginRuntime db = getPluginRuntime(r.getClusterId(), r.getPluginName(), r.getNodeIp());
            if (db == null) {
                runtimeRepository.insert(r);
            } else if (!db.getPluginVersion().equals(r.getPluginVersion()) || !db.getAvailableVersions().equals(r.getAvailableVersions())) {
                db.setPluginVersion(r.getPluginVersion());
                db.setAvailableVersions(r.getAvailableVersions());
                db.setNodeHost(r.getNodeHost());
                runtimeRepository.update(db);
            }
        }
        return 0;
    }

    @Override
    public List<PluginRuntime> getAllPluginRuntimes() {
        return runtimeRepository.selectAll();
    }

    @Override
    public List<PluginRuntime> getPluginRuntimes(String clusterId, String pluginName) {
        return runtimeRepository.findByClusterAndPluginName(clusterId, pluginName);
    }

    @Override
    public List<String> getNodeIPsByCluster(String clusterId) {
        return runtimeRepository.selectDistictNodeIPsByClusterId(clusterId);
    }

    @Override
    public PluginRuntime getPluginRuntime(String clusterId, String pluginName, String nodeIp) {
        return runtimeRepository.findByClusterAndPluginNameAndNodeIp(clusterId, pluginName, nodeIp);
    }

    @Override
    public int deletePluginRuntime(long id) {
        return runtimeRepository.deleteByPrimaryKey(id);
    }

    @Override
    public int addPluginUpgrade(PluginUpgrade upgrade) {
        checkRuntimeListNodesAvailable(upgrade);
        upgrade.setState(PluginUpgrade.UPGRADE_STATUS_NEED_APPROVAL);
        return upgradeRepository.insert(upgrade);
    }

    @Override
    public List<PluginUpgrade> getPluginUpgrade(Page<PluginUpgrade> page) {
        return upgradeRepository.selectPage(page);
    }

    @Override
    public PluginUpgrade getPluginUpgrade(Long id) {
        return upgradeRepository.getById(id);
    }

    @Override
    public PluginUpgrade getLatestUpgrade(String clusterId, String pluginName) {
        return upgradeRepository.findLatestUpgrade(clusterId, pluginName);
    }

    @Override
    public int setUppgradeState(String loginUser, long id, int nextState) {
        PluginUpgrade db = upgradeRepository.getById(id);
        if(db != null) {
            db.setState(nextState);
            if (nextState == PluginUpgrade.UPGRADE_STATUS_DOWNLOAD) {
                db.setApproveUser(loginUser);
                db.setApproveTime(new Date());
            } else if (nextState == PluginUpgrade.UPGRADE_STATUS_UPGRADE) {
                db.setApplyUser(loginUser);
                db.setApplyTime(new Date());
            }
            return upgradeRepository.updateUpgrade(db);
        }
        return -1;

    }

    @Override
    public int addPluginCommand(PluginCommand command) {
        return commandRepository.insert(command);
    }

    @Override
    public int deleteCommand(Long... ids) {
        for (Long id: ids) {
            commandRepository.deleteById(id);
        }

        return ids.length;
    }

    @Override
    public int deleteCommand(String clusterId, String pluginName) {
        return commandRepository.deleteByClusterAndPluginName(clusterId, pluginName);
    }

    @Override
    public List<PluginCommand> getCommands(String clusterId, String pluginName) {
        return commandRepository.selectByClusterAndPluginName(clusterId, pluginName);
    }

    @Override
    public List<PluginCommand> getCommandsByIp(String clusterId, String nodeIp) {
        List<PluginCommand> pluginCommands = commandRepository.selectByNodeIp(clusterId, nodeIp);
        List<Long> list = pluginCommands.stream().map(PluginCommand::getId).collect(Collectors.toList());

        this.deleteCommand(list.toArray(new Long[list.size()]));

        return pluginCommands;
    }

    @Override
    public PluginRuntime getPluginRuntime(String clusterId, String pluginName, String nodeIp, String pluginVersion) {
        return runtimeRepository.findByClusterAndPluginNameAndNodeIpAndPluginVersion(clusterId, pluginName, nodeIp, pluginVersion);
    }

    @Override
    public void deletePluginRuntime(String clusterId, String pluginName, String nodeIp, String pluginVersion) {
        PluginRuntime pluginRuntime;

        if(pluginVersion != null){
            pluginRuntime = getPluginRuntime(clusterId, pluginName, nodeIp, pluginVersion);
        }else{
            pluginRuntime = getPluginRuntime(clusterId, pluginName, nodeIp);
        }

        if(pluginRuntime != null){
            deletePluginRuntime(pluginRuntime.getId());
        }
    }

    private void checkRuntimeListNodesAvailable(PluginUpgrade upgrade) {
        String clusterId = upgrade.getClusterId();
        String pluginName = upgrade.getPluginName();
        int type = upgrade.getPluginType();
        Cluster cluster = clusterService.findByName(clusterId);
        List<String> nodeIps = new LinkedList<>();
        try {
            RestClient client = ElasticRestClient.build(cluster.getHttpAddress());
            Response response = client.performRequest("GET", "/_cat/nodes");
            try(BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    int idx = line.indexOf(' ');
                    idx = idx == -1 ? 0 : idx;
                    String ip = line.substring(0, idx);
                    nodeIps.add(ip);
                }
            }
        } catch(Exception e) {
            logger.error("error", e);
            throw new RuntimeException(e);
        }

        if(nodeIps.isEmpty()) {
            nodeIps.add("");
        }
        List<PluginRuntime> availableNodes = runtimeRepository.findByClusterAndPluginName(clusterId, pluginName);

        //删掉已经过时的旧Runtime数据
        availableNodes
                .stream()
                .filter((PluginRuntime n) -> !nodeIps.contains(n.getNodeIp()))
                .forEach((PluginRuntime n) -> runtimeRepository.deleteByPrimaryKey(n.getId()));

        //插入
        nodeIps.stream()
                .filter((String ip) -> !availableNodes.stream().anyMatch((PluginRuntime n) -> n.getNodeIp().equals(ip)))
                .forEach(
                   (String n) -> {
                       PluginRuntime r = new PluginRuntime();
                       r.setNodeIp(n);
                       r.setClusterId(clusterId);
                       r.setCreateTime(new Date());
                       r.setPluginName(pluginName);
                       r.setPluginType(type);
                       runtimeRepository.insert(r);
                   }
                );
    }
}