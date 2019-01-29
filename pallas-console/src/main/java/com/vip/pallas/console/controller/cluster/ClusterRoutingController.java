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

package com.vip.pallas.console.controller.cluster;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotBlank;

import org.codehaus.jackson.node.ArrayNode;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.vo.IndexRoutingVO;
import com.vip.pallas.console.vo.TargetGroupVO;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.IndexRouting;
import com.vip.pallas.mybatis.entity.IndexRoutingTargetGroup;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.ElasticSearchService;
import com.vip.pallas.service.IndexRoutingService;
import com.vip.pallas.utils.ElasticRestClient;
import com.vip.pallas.utils.ObjectMapTool;

@Validated
@RestController
@RequestMapping("/cluster/routing")
public class ClusterRoutingController {
    private static Logger logger = LoggerFactory.getLogger(ClusterRoutingController.class);
    @Resource
    private IndexRoutingService routingService;

    @Resource
    private ElasticSearchService elasticSearchService;

    @Resource
    private ClusterService clusterService;

    @RequestMapping(value = "/rule/list.json", method = RequestMethod.GET)
	public Map<String, Object> list(@RequestParam @NotBlank(message = "clusterId不能为空") String clusterId,
			HttpServletRequest request) throws Exception { // NOSONAR
        Cluster cluster = clusterService.findByName(clusterId);
        if (null == cluster){
        	throw new BusinessLevelException(500, "cluster不存在");
        }
        
        Long id = cluster.getId();

        IndexRouting routing = routingService.getIndexRouting(id, IndexRouting.ROUTE_TYPE_CLUSTER);
        if(routing == null) {
            routing = new IndexRouting();
            routing.setIndexId(id);
            routing.setIndexName(cluster.getClusterId());
        }
        IndexRoutingVO indexRouting = new IndexRoutingVO();
        indexRouting.setIndexId(routing.getIndexId());
        indexRouting.setIndexName(routing.getIndexName());
        indexRouting.setRules(IndexRouting.fromXContent(routing.getRoutingsInfo()));

        Map<Long, TargetGroupVO> nodesInfoMap = new TreeMap<>();
        for(IndexRoutingTargetGroup g : routingService.getClusterRoutingTargetGroups(id)) {
            nodesInfoMap.put(g.getId(),  parseRoutingTarget(g));
        }
        indexRouting.setRoutingTargetGroups(nodesInfoMap);

        Map<String, Object> resultMap = new HashMap<>();
        List<String> privileges = AuthorizeUtil.loadPrivileges();
        if(privileges != null
                && (privileges.contains("cluster.all") || privileges.contains("cluster." + clusterId))){
            resultMap.put("privilege", Boolean.TRUE);
        } else {
            resultMap.put("privilege", Boolean.FALSE);
        }
        resultMap.put("data", indexRouting);
        return resultMap;
    }

    @RequestMapping(value = "/target_group/update.json", method = RequestMethod.POST)
    public void updateTargetGroup(@RequestBody Map<String, Object> params) { // NOSONAR
        Long id = ObjectMapTool.getLong(params, "id");

        String clusterName =  ObjectMapTool.getString(params, "clusterId");
        String name =  ObjectMapTool.getString(params, "name");
        int clusterLevel =  ObjectMapTool.getInteger(params, "clusterLevel");

        if (ObjectUtils.isEmpty(clusterName)){
            throw new BusinessLevelException(500, "clusterId不能为空");
        }

        if (ObjectUtils.isEmpty(name)){
            throw new BusinessLevelException(500, "name不能为空");
        }

        Cluster cluster = clusterService.findByName(clusterName);
        if(cluster == null) {
            throw new BusinessLevelException(500, "cluster 不存在");
        }


        IndexRoutingTargetGroup g = routingService.getClusterRoutingTargetGroups(cluster.getId()).stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseGet(() -> new IndexRoutingTargetGroup());

        if (g.getId() == null) {
            g.setIndexId(cluster.getId());
            g.setIndexName(clusterName);
            g.setState(0);
            g.setCreateTime(new Date());
        }
        g.setType(IndexRouting.ROUTE_TYPE_CLUSTER);
        g.setName(name);
        g.setClusterLevel(clusterLevel);

        String jsonStr = null;

        try {
            ArrayNode nodes = ObjectMapTool.getObject(params,"nodes", ArrayNode.class);
            if (ObjectUtils.isEmpty(nodes)){
                throw new BusinessLevelException(500, "nodes不能为空");
            }

            jsonStr = nodes.toString();
            g.setClustersInfo("[]");
            g.setNodesInfo(IndexRoutingTargetGroup.toXContent(IndexRoutingTargetGroup.fromXContent(jsonStr)));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BusinessLevelException(500, "错误:" + e.getMessage() + "，nodes格式非法：" + jsonStr);
        }

        routingService.addOrUpdateRoutingTargetGroup(cluster.getId(), g);
    }


    @RequestMapping(value = "/rule/update.json", method = RequestMethod.POST)
    public void updateRule(@RequestBody Map<String, Object> params) throws Exception { // NOSONAR
        String clusterName =  ObjectMapTool.getString(params, "clusterId");

        if (ObjectUtils.isEmpty(clusterName)){
            throw new BusinessLevelException(500, "clusterId 不能为空");
        }
        ArrayNode rules = ObjectMapTool.getObject(params, "rules", ArrayNode.class);

        Cluster cluster = clusterService.findByName(clusterName);
        if(cluster == null) {
            throw new BusinessLevelException(500, "cluster 不存在");
        }

        List<String> privileges = AuthorizeUtil.loadPrivileges();
        if(privileges == null ||
                !(privileges.contains("cluster.write")
                        || privileges.contains("cluster.all")
                        || privileges.contains("cluster." + clusterName))){
            throw new BusinessLevelException(403, "无权限操作");
        }

        IndexRouting routing = routingService.getIndexRouting(cluster.getId(), IndexRouting.ROUTE_TYPE_CLUSTER);
        if (routing == null) {
            routing = new IndexRouting();
            routing.setCreateTime(new Date());
            routing.setIndexId(cluster.getId());
            routing.setIndexName(clusterName);
        }
        routing.setType(IndexRouting.ROUTE_TYPE_CLUSTER);
        List<IndexRouting.RoutingCondition> list;
        String rulesStr;
        try {
            list = IndexRouting.fromXContent(rules.toString());
            rulesStr = IndexRouting.toXContent(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BusinessLevelException(500, "rules格式非法：" + rules);
        }

        list.stream()
                .flatMap(cond -> cond.getTargetGroups().stream())
                .filter(g -> g.getWeight() < 0 || g.getWeight() > 100)
                .findAny()
                .ifPresent(t -> {
                    throw new BusinessLevelException(500, "权重有效范围是0~100");
                });

        routing.setRoutingsInfo(rulesStr);
        routingService.addOrUpdateIndexRouting(cluster.getId(), routing);
    }

    @RequestMapping(value = "/target_group/list.json", method = RequestMethod.GET)
    public Map<String, Object> page(@RequestParam String  clusterId) { // NOSONAR
        String clusterName = clusterId;

        Cluster cluster = clusterService.findByName(clusterName);
        if(cluster == null) {
            throw new BusinessLevelException(500, "cluster 不存在");
        }

        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> treeNodes = new LinkedList<>();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("cluster", clusterName);
        map.put("name", clusterName);
        map.put("address", cluster.getHttpAddress());
        map.put("children", getNodeList(clusterName));
        treeNodes.add(map);
        resultMap.put("tree", treeNodes);
        return resultMap;
    }

    List<IndexRoutingTargetGroup.NodeInfo> getNodeList(String clusterName) {
        List<IndexRoutingTargetGroup.NodeInfo> nodeList = new LinkedList<>();
        String clusterHost = clusterService.findByName(clusterName).getHttpAddress();
        List<String> excludeIps = elasticSearchService.getExcludeNodeList(clusterHost);
        RestClient client = ElasticRestClient.build(clusterHost);
        Response response = null;
        try {
            response = client.performRequest("GET", "/_cat/nodes");
        } catch (IOException e) {
            logger.error(e.toString(), e);
            return nodeList;
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));)
        {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.contains("di") || line.contains("mdi")){
                    IndexRoutingTargetGroup.NodeInfo node = new IndexRoutingTargetGroup.NodeInfo();
                    node.setCluster(clusterName);
                    node.setName(line.substring(line.lastIndexOf(' ')+1, line.length()));
                    node.setAddress(line.substring(0, line.indexOf(' ')));
                    node.setWeight(1);
                    node.setState(excludeIps.contains(node.getAddress()) ? 1 : 0);
                    nodeList.add(node);
                }
            }
            Collections.sort(nodeList, Comparator.comparing(IndexRoutingTargetGroup.NodeInfo::getName));
            return nodeList;
        } catch (IOException e){
            logger.error(e.toString(), e);
            return nodeList;
        }
    }

    private TargetGroupVO parseRoutingTarget(IndexRoutingTargetGroup g) throws Exception {
        TargetGroupVO fac = new TargetGroupVO();
        fac.setId(g.getId());
        fac.setName(g.getName());
        fac.setClusterLevel(g.getClusterLevel());
        fac.setState(g.getState());

        if(g.isClusterLevel0() || g.isShardLevel()){
            fac.setClusters(IndexRoutingTargetGroup.fromXContent(g.getClustersInfo())
                    .stream()
                    .map(n -> n.getName())
                    .collect(Collectors.toList()));

            if (g.isShardLevel()){
                fac.setNodes(getShardNodesByIndex(g.getIndexName(), fac.getClusters()));
            }
        } else if (g.isNormalLevel()){
            fac.setNodes(IndexRoutingTargetGroup.fromXContent(g.getNodesInfo())
                    .stream()
                    .map(n -> n.getName())
                    .collect(Collectors.toList()));
        }

        return fac;
    }

    private List<String> getShardNodesByIndex(String indexName, List<String> clusterList){
        Map<String, List<String>> map = elasticSearchService.getShardNodesByAlias(indexName);
        if(map != null){
            List<String> list = map.entrySet().stream()
                    .filter(entry -> clusterList.contains(entry.getKey()))
                    .filter(entry -> entry.getValue().size() > 0)
                    .flatMap(entry -> entry.getValue().stream())
                    .collect(toList()).stream()
                    .collect(toSet()).stream()
                    .collect(toList());
            return list != null && list.size() > 0 ? list : null;
        }else{
            return null;
        }
    }

}