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

import com.vip.pallas.bean.NodeInfo;
import com.vip.pallas.bean.NodeState;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.Node;
import com.vip.pallas.mybatis.entity.PluginCommand;
import com.vip.pallas.mybatis.entity.SearchServer;
import com.vip.pallas.mybatis.repository.NodeRepository;
import com.vip.pallas.service.ElasticSearchService;
import com.vip.pallas.service.NodeService;
import com.vip.pallas.service.PallasPluginService;
import com.vip.pallas.service.SearchServerService;
import com.vip.pallas.thread.ExtendableThreadPoolExecutor;
import com.vip.pallas.thread.PallasThreadFactory;
import com.vip.pallas.thread.TaskQueue;
import com.vip.pallas.utils.HttpClient;
import com.vip.pallas.utils.PallasConsoleProperties;

import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;

import static java.util.stream.Collectors.*;

@Service
@Transactional(rollbackFor=Exception.class)
public class NodeServiceImpl implements NodeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeServiceImpl.class);

    @Resource
    private NodeRepository nodeRepository;

    @Resource
    private ElasticSearchService elasticSearchService;

    @Resource
    private PallasPluginService pluginService;

    @Autowired
    private SearchServerService searchServerService;

    private static final ScheduledExecutorService WAIT_CLUSTER_HEALTH_EXECUTOR = Executors.newScheduledThreadPool(5
            , new PallasThreadFactory("pallas-wait-cluster-health-pool"));

    private static final ExtendableThreadPoolExecutor PUSH_ROUTING_EXECUTOR = new ExtendableThreadPoolExecutor(
            3 , 30, 2L, TimeUnit.MINUTES, new TaskQueue(
            20480), new PallasThreadFactory("pallas-push-routing-pool", Thread.MAX_PRIORITY));

    private static final ExtendableThreadPoolExecutor NODE_RESTART_EXECUTOR = new ExtendableThreadPoolExecutor(
            3 , 10, 2L, TimeUnit.MINUTES, new TaskQueue(
            20480), new PallasThreadFactory("pallas-node-restart-pool", Thread.MAX_PRIORITY));

    private static final String SERVER_UPDATE_ROUTING_URL = "http://%s/_py/update_routing";

    @Override
    public List<NodeInfo> getNodeList(String clusterName) throws Exception {
        List<NodeInfo> nodeInfoList = new ArrayList<>();

        List<String[]> allNodes = elasticSearchService.getNodes(clusterName);

        List<String[]> shards = elasticSearchService.getShards(clusterName);
        Map<String, List<String[]>> indexShardMap = shards.stream().collect(groupingBy((String[] t) -> t[2]));

        List<Node> nodeList = nodeRepository.selectByClusterName(clusterName);
        Map<String, String[]> esNodeMap = allNodes.stream().collect(toMap(array -> array[2], array -> array));

        if(nodeList != null){
            nodeList.stream().forEach(
                node -> {
                    String nodeName = node.getNodeName();
                    NodeInfo nodeInfo = new NodeInfo();
                    nodeInfo.setNodeIp(node.getNodeIp());
                    nodeInfo.setNodeName(nodeName);
                    nodeInfo.setNodeTime(node.getStateTime());
                    nodeInfo.setLastStartupTime(node.getLastStartupTime());
                    nodeInfo.setNodeState(String.valueOf(node.getState()));
                    nodeInfo.setHealthy(node.isHealthy());

                    if(esNodeMap.containsKey(nodeName)){
                        nodeInfo.setOnlyMaster(!esNodeMap.get(nodeName)[1].contains("d"));

                        List<String[]> shardList = indexShardMap.get(esNodeMap.get(nodeName)[2]);
                  
                        if(shardList != null){
                            nodeInfo.setIndexList(shardList.stream().map(e -> e[0]).filter(e -> !e.startsWith(".")).distinct().collect(toList()));

                            if(nodeInfo.getIndexList() != null){
                                nodeInfo.setIndices(nodeInfo.getIndexList().stream().collect(joining(",")));
                            }
                        }
                    }

                    nodeInfoList.add(nodeInfo);
                }
            );
        }

        nodeInfoList.sort(Comparator.comparing(NodeInfo::isOnlyMaster, Comparator.reverseOrder()).thenComparing(NodeInfo::getNodeName));
        return nodeInfoList;
    }

    @Override
    public void restartNode(String clusterName, String nodeIp) {
        NODE_RESTART_EXECUTOR.submit(() -> {
            try{
                //强制pallas-search更新RoutingCache
                waitAndPushRouting2Search();

                Thread.sleep(PallasConsoleProperties.CACHE_LOADDING_CONSUME_TIME);

                //index block write: true
                RestClient restClient = elasticSearchService.getRestClientByClusterName(clusterName);
                List<String> normalIndexList = getNormalIndexOnNode(clusterName, nodeIp);

                for (String actualIndexName : normalIndexList) {
                    //禁止索引写入
                    elasticSearchService.setIndexBlockWrite(restClient, actualIndexName, true);
                }

                //allocation rebalance
                elasticSearchService.setClusterAllocation(restClient, "none");
                elasticSearchService.setClusterRebalance(restClient, "none");

                //设置delay timeout
                elasticSearchService.setClusterDelayedTimeout(restClient, "10m");

                //_flush/synced
                while(true){
                    try{
                        elasticSearchService.setClusterFlushSynced(restClient);
                        break;
                    }catch(Exception e){
                        LOGGER.error(e.toString(), e);
                    }
                    Thread.sleep(3000);
                }

                genRestartCommand(clusterName, nodeIp);
            }catch (Exception e){
                LOGGER.error(e.toString(), e);
            }
        });
    }

    private void genRestartCommand(String clusterName, String nodeIp) {
        List<String> nodeIpList = pluginService.getNodeIPsByCluster(clusterName);
        for(String ip : nodeIpList) {
            if(ip.equals(nodeIp)) {
                PluginCommand cmd = new PluginCommand();
                cmd.setClusterId(clusterName);
                cmd.setCreateTime(new Date());
                cmd.setNodeIp(ip);
                cmd.setCommand(PluginCommand.COMMAND_RESTART);
                pluginService.addPluginCommand(cmd);
            }
        }
    }

    @Override
    public void stateNode(String clusterName, String nodeName, String nodeIp, Byte state, boolean isRestart)throws Exception {
        Node n = nodeRepository.selectByClusterAndNodeIp(clusterName, nodeIp);
        if(n != null){
            Byte s = n.getState();
            if(state == NodeState.HEALTHY.getValue() && (s == NodeState.TO_BE_RESTART.getValue() || s == NodeState.RESTARTING.getValue())){
                return;
            }
        }

        Node node = new Node();
        Date nowDate = new Date();

        NodeState nodeState = NodeState.getNodeStateByValue(state);
        node.setClusterName(clusterName);
        node.setNodeName(nodeName);
        node.setNodeIp(nodeIp);
        node.setState(nodeState.getValue());
        node.setNote(nodeState.getDesc());
        node.setStateTime(nowDate);

        if(node.getState() == NodeState.STARTED.getValue()){
            node.setLastStartupTime(nowDate);
        }

        if(n != null){
            node.setId(n.getId());
            nodeRepository.updateByPrimaryKeySelective(node);
        } else {
            node.setCreateTime(nowDate);
            node.setLastStartupTime(nowDate);
            nodeRepository.insertSelective(node);
        }

        if(isRestart){
            resetClusterDefaultSettings(node.getClusterName());
        }
    }

    private void unBlockWriteIndex(String clusterName) throws Exception {
        Map<String, String> settings = elasticSearchService.getMainClusterSettings(clusterName);
        String blockWriteIndexs = settings.get("index.blocks.write");
        if(blockWriteIndexs != null){
            RestClient restClient = elasticSearchService.getRestClientByClusterName(clusterName);
            for(String index : blockWriteIndexs.split(",")) {
                elasticSearchService.setIndexBlockWrite(restClient, index, false);
            }
        }
    }

    @Override
    public List<String> getNormalIndexOnNode(String clusterName, String nodeIp) {
        List<String[]> shardAndNodeList = elasticSearchService.getIndexAndNodes(elasticSearchService.getHttpAddressByClusterName(clusterName));
        if(shardAndNodeList != null){
            return shardAndNodeList.stream().filter(e -> !e[0].startsWith(".")).filter(e -> e[1].equals(nodeIp)).map(t -> t[0])
                    .distinct().collect(toList());
        }
        return null;
    }

    @Override
    public String getClusterState(String clusterName, String esStatus) throws Exception {
        if ("green".equals(esStatus) && nodeRepository.getCount4Abnormal(clusterName) == 0) {
            return "could_be_restart";
        }
        return "could_not_be_restart";
    }

    @Override
    public String resetClusterDefaultSettings(String clusterName) throws Exception {
        RestClient restClient = elasticSearchService.getRestClientByClusterName(clusterName);

        String result = elasticSearchService.setClusterAllocation(restClient, "all") + elasticSearchService.setClusterDelayedTimeout(restClient, "1m");

        ScheduledFuture<?>[] scheduledFuture = new ScheduledFuture<?>[1];
        scheduledFuture[0] = WAIT_CLUSTER_HEALTH_EXECUTOR.scheduleWithFixedDelay(() -> {
            try {
                if("green".equals(elasticSearchService.getClusterStatus(clusterName))){
                    unBlockWriteIndex(clusterName);
                    if(scheduledFuture[0] != null){
                        scheduledFuture[0].cancel(true);
                    }
                }
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }, 0, 5, TimeUnit.SECONDS);

        return result;
    }

    @Override
    public List<Node> getAbnormalNodeList(String clusterName) {
        return nodeRepository.selectAbnormalNodeList(clusterName);
    }

    @Override
    public void markRestartState(String clusterName, String nodeIp) throws PallasException {
        Node node = nodeRepository.selectByClusterAndNodeIp(clusterName, nodeIp);

        if (node == null) {
            throw new PallasException("没有找到相关节点信息");
        }

        node.setState(NodeState.TO_BE_RESTART.getValue());
        node.setNote(NodeState.TO_BE_RESTART.getDesc());
        nodeRepository.updateByPrimaryKeySelective(node);
    }

    private void waitAndPushRouting2Search() throws InterruptedException {
        List<SearchServer> searchServerList = searchServerService.selectAllHealthyServer();
        if(searchServerList != null){
            CountDownLatch countDownLatch = new CountDownLatch(searchServerList.size());
            searchServerList.stream().forEach(searchServer -> PUSH_ROUTING_EXECUTOR.submit(() -> {
                try {
                    String url = searchServer.getIpport();
                    LOGGER.info("updating pallas-search routing with: {}", url);

                    HttpClient.httpGet(String.format(SERVER_UPDATE_ROUTING_URL, url));
                } catch (Exception e) {
                    LOGGER.info(e.toString(), e);
                } finally {
                    countDownLatch.countDown();
                }
            }));
            countDownLatch.await();
        }
    }
}