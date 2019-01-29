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

package com.vip.pallas.console.controller.index.routing;

import static com.vip.pallas.mybatis.entity.IndexRoutingSecurity.fromCriteriaXContent;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.vo.IndexRoutingVO;
import com.vip.pallas.console.vo.TargetGroupVO;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.mybatis.entity.IndexRouting;
import com.vip.pallas.mybatis.entity.IndexRoutingSecurity;
import com.vip.pallas.mybatis.entity.IndexRoutingTargetGroup;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.ElasticSearchService;
import com.vip.pallas.service.IndexRoutingService;
import com.vip.pallas.service.IndexService;
import com.vip.pallas.utils.ElasticRestClient;
import com.vip.pallas.utils.ObjectMapTool;

@RestController
@RequestMapping("/index/routing")
public class IndexRoutingController {
    private static Logger logger = LoggerFactory.getLogger(IndexRoutingController.class);

    @Resource
    private IndexRoutingService routingService;

    @Resource
    private ElasticSearchService elasticSearchService;

    @Resource
    private IndexService indexService;

    @Autowired
    private ClusterService clusterService;

    @RequestMapping(value = "/list.json")
    public Map<String, Object> page(@RequestBody Map<String, Object> params) throws Exception { // NOSONAR
        Long indexId =  ObjectMapTool.getLong(params, "indexId");

        if (ObjectUtils.isEmpty(indexId)){
            throw new BusinessLevelException(500, "indexId不能为空");
        }

        String indexName =  ObjectMapTool.getString(params, "indexName");

        if (ObjectUtils.isEmpty(indexId)){
            throw new BusinessLevelException(500, "indexName不能为空");
        }

        Index index = indexService.findById(indexId);

        if (index == null || !index.getIndexName().equals(indexName)) {
            throw new BusinessLevelException(500, "找不到 index，id:" + indexId + ", indexName:" + indexName);
        }

        IndexRouting routing = routingService.getIndexRouting(indexId, IndexRouting.ROUTE_TYPE_INDEX);

        if(routing == null) {
            routing = insertDefaultRoutingForIndex(index);
        }
        IndexRoutingVO indexRouting = new IndexRoutingVO();
        indexRouting.setIndexId(routing.getIndexId());
        indexRouting.setIndexName(routing.getIndexName());
        indexRouting.setRules(IndexRouting.fromXContent(routing.getRoutingsInfo()));

        Map<Long, TargetGroupVO> nodesInfoMap = new TreeMap<>();
        for(IndexRoutingTargetGroup g : routingService.getIndexRoutingTargetGroups(indexId)) {
            nodesInfoMap.put(g.getId(),  parseRoutingTarget(g));
        }
        indexRouting.setRoutingTargetGroups(nodesInfoMap);

        Map<String, Object> resultMap = new HashMap<>();

        List<String> privileges = AuthorizeUtil.loadPrivileges();
		if (privileges != null && (privileges.contains("index.all")
				|| privileges.contains("index." + index.getId() + "-" + index.getIndexName()))){
            resultMap.put("privilege", Boolean.TRUE);
        } else {
            resultMap.put("privilege", Boolean.FALSE);
        }
        resultMap.put("data", indexRouting);
        return resultMap;
    }


    @RequestMapping(value = "/target_group/list.json")
    public List<Map<String, Object>> list(@RequestBody Map<String, Object> params) { // NOSONAR
        Long indexId =  ObjectMapTool.getLong(params, "indexId");

        if (ObjectUtils.isEmpty(indexId)){
            throw new BusinessLevelException(500, "indexId不能为空");
        }

        String indexName =  ObjectMapTool.getString(params, "indexName");

        if (ObjectUtils.isEmpty(indexName)){
            throw new BusinessLevelException(500, "indexName不能为空");
        }
        Index index = indexService.findById(indexId);
        if (index == null) {
            throw new BusinessLevelException(500, "找不到index, indexId:" + indexId);
        }

        List<Cluster> clusters = clusterService.selectPhysicalClustersByIndexId(index.getId());

        List<Map<String, Object>> treeNodes = new LinkedList<>();
        clusters.stream()
                .map(c -> c.getClusterId())
                .collect(toSet())
                .stream()
                .forEach(
                        c -> {
                            Map<String, Object> map = new LinkedHashMap<>();
                            map.put("cluster", c);
                            map.put("name", c);
                            map.put("address", c);
                            map.put("children", getNodeList(c));
                            treeNodes.add(map);
                        }
                );
        return treeNodes;
    }

    @RequestMapping(value = "/rule/update.json")
    public void updateRule(@RequestBody Map<String, Object> params, HttpServletRequest request) throws Exception { // NOSONAR
        Long indexId =  ObjectMapTool.getLong(params, "indexId");

        if (ObjectUtils.isEmpty(indexId)){
            throw new BusinessLevelException(500, "indexId不能为空");
        }
        String indexName =  ObjectMapTool.getString(params, "indexName");

        if (ObjectUtils.isEmpty(indexName)){
            throw new BusinessLevelException(500, "indexName不能为空");
        }
        
        if (!AuthorizeUtil.authorizeIndexPrivilege(request, indexId, indexName)) {
        	throw new BusinessLevelException(403, "无权限操作");
        }
        
        ArrayNode rules = ObjectMapTool.getObject(params, "rules", ArrayNode.class);

        List<String> privileges = AuthorizeUtil.loadPrivileges();
        if(privileges == null ||
                !(privileges.contains("index.write")
                        || privileges.contains("index.all")
                        || privileges.contains("index." + indexId + "-" + indexName))){
            throw new BusinessLevelException(403, "无权限操作");
        }

        IndexRouting routing = routingService.getIndexRouting(indexId, IndexRouting.ROUTE_TYPE_INDEX);
        if (routing == null) {
            routing = new IndexRouting();
            routing.setCreateTime(new Date());
            routing.setIndexId(indexId);
            routing.setIndexName(indexName);
        }
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
        routingService.addOrUpdateIndexRouting(indexId, routing);
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

    @RequestMapping(value = "/target_group/update.json")
    public void updateTargetGroup(@RequestBody Map<String, Object> params, HttpServletRequest request) { // NOSONAR
        Long id = ObjectMapTool.getLong(params, "id");
        Long indexId =  ObjectMapTool.getLong(params, "indexId");
        String indexName =  ObjectMapTool.getString(params, "indexName");
        String name =  ObjectMapTool.getString(params, "name");
        int clusterLevel =  ObjectMapTool.getInteger(params, "clusterLevel");

        if (ObjectUtils.isEmpty(indexId)){
            throw new BusinessLevelException(500, "indexId不能为空");
        }

        if (ObjectUtils.isEmpty(indexName)){
            throw new BusinessLevelException(500, "indexName不能为空");
        }
        
        if (!AuthorizeUtil.authorizeIndexPrivilege(request, indexId, indexName)) {
        	throw new BusinessLevelException(403, "无权限操作");
        }

        if (ObjectUtils.isEmpty(name)){
            throw new BusinessLevelException(500, "name不能为空");
        }

        IndexRoutingTargetGroup g = routingService.getIndexRoutingTargetGroups(indexId).stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElseGet(() -> new IndexRoutingTargetGroup());

        if (g.getId() == null) {
            g.setIndexId(indexId);
            g.setIndexName(indexName);
            g.setState(0);
            g.setCreateTime(new Date());
        }
        g.setType(IndexRouting.ROUTE_TYPE_INDEX);
        g.setName(name);
        g.setClusterLevel(clusterLevel);

        String jsonStr = null;

        try {
            if (g.isClusterLevel0() || g.isShardLevel()) {
                ArrayNode nodes = ObjectMapTool.getObject(params,"clusters", ArrayNode.class);
                if (ObjectUtils.isEmpty(nodes)){
                    throw new BusinessLevelException(500, "clusters不能为空");
                }

                jsonStr = nodes.toString();
                g.setClustersInfo(IndexRoutingTargetGroup.toXContent(IndexRoutingTargetGroup.fromXContent(jsonStr)));
                g.setNodesInfo("[]");
            } else {
                ArrayNode nodes = ObjectMapTool.getObject(params,"nodes", ArrayNode.class);
                if (ObjectUtils.isEmpty(nodes)){
                    throw new BusinessLevelException(500, "nodes不能为空");
                }

                jsonStr = nodes.toString();
                g.setClustersInfo("[]");
                g.setNodesInfo(IndexRoutingTargetGroup.toXContent(IndexRoutingTargetGroup.fromXContent(jsonStr)));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BusinessLevelException(500, "错误:" + e.getMessage() + "，nodes格式非法：" + jsonStr);
        }

        routingService.addOrUpdateRoutingTargetGroup(indexId, g);
    }

    @RequestMapping(value = "/node_state/update.json")
    public void updateNodeState(@RequestBody Map<String, Object> params) { // NOSONAR
        String cluster = ObjectMapTool.getString(params, "cluster");
        String nodeIp = ObjectMapTool.getString(params, "nodeIp");
        Integer state = ObjectMapTool.getInteger(params, "state");

        if (ObjectUtils.isEmpty(cluster)) {
            throw new BusinessLevelException(500, "cluster不能为空");
        }
        if (ObjectUtils.isEmpty(nodeIp)) {
            throw new BusinessLevelException(500, "nodeIp不能为空");
        }
        if (ObjectUtils.isEmpty(state)) {
            throw new BusinessLevelException(500, "state不能为空");
        }

        routingService.updateNodeState(cluster, nodeIp, state);
    }

    @RequestMapping(value = "/target_group/delete.json")
    public void deleteTargetGroup(@RequestBody Map<String, Object> params) {
        Long groupId = ObjectMapTool.getLong(params, "groupId");

        if (ObjectUtils.isEmpty(groupId)){
            throw new BusinessLevelException(500, "groupId不能为空");
        }

        routingService.deleteRoutingTargetGroup(groupId);
    }

    @RequestMapping(value = "/security/token.json")
    public String genToken() throws NoSuchAlgorithmException {
        return Base64.getEncoder().encodeToString(MessageDigest.getInstance("md5").digest(UUID.randomUUID().toString().getBytes()));
    }

    @RequestMapping(value = "/security/update.json")
    public void updateSecurity(@RequestBody Map<String, Object> params) throws Exception { // NOSONAR
        Long indexId = ObjectMapTool.getLong(params, "indexId");

        if (ObjectUtils.isEmpty(indexId)){
            throw new BusinessLevelException(500, "indexId不能为空");
        }
        String indexName =  ObjectMapTool.getString(params, "indexName");

        if (ObjectUtils.isEmpty(indexName)){
            throw new BusinessLevelException(500, "indexName不能为空");
        }

        ObjectNode cri = ObjectMapTool.getObject(params, "protocolControlCriteria", ObjectNode.class);
        if (ObjectUtils.isEmpty(cri)){
            throw new BusinessLevelException(500, "protocolControlCriteria不能为空");
        }

        ObjectNode protocolControlMap = ObjectMapTool.getObject(params, "protocolControlMap", ObjectNode.class);
        if (ObjectUtils.isEmpty(protocolControlMap)){
            throw new BusinessLevelException(500, "protocolControlMap不能为空");
        }

        IndexRoutingSecurity irs = routingService.getRoutingSecurity(indexId);
        if (irs == null) {
            irs = new IndexRoutingSecurity();
            irs.setCreateTime(new Date());
            irs.setIndexId(indexId);
            irs.setIndexName(indexName);
        }
        try {
            irs.setCriteria(cri.toString());
            fromCriteriaXContent(irs.getCriteria());
            irs.setProtocolControls(protocolControlMap.toString());
            fromCriteriaXContent(irs.getProtocolControls());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new BusinessLevelException(500, "原因:" + e.getMessage() + ",Criteria：" + cri + ",ProtocolControls:" + protocolControlMap);
        }

        routingService.addOrUpdateRoutingSecurity(indexId, irs);
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

    private IndexRouting insertDefaultRoutingForIndex(Index index) throws Exception {
        IndexRouting routing = routingService.getIndexRouting(index.getId(), IndexRouting.ROUTE_TYPE_INDEX);
        if (routing == null) {
            List<Cluster> clusters = clusterService.selectPhysicalClustersByIndexId(index.getId());
            List<IndexRoutingTargetGroup> groups = IndexRoutingTargetGroup.genDefault(index, clusters);
            groups.forEach(group -> routingService.addOrUpdateRoutingTargetGroup(index.getId(), group));
            routing = IndexRouting.genDefault(index, groups);
            routingService.addOrUpdateIndexRouting(index.getId(), routing);
        }
        return routing;
    }

    private List<IndexRoutingTargetGroup.NodeInfo> getNodeList(String clusterName) {

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
}