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

import java.util.*;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.bean.NodeInfo;
import com.vip.pallas.console.utils.AuditLogUtil;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.vo.ClusterStateVO;
import com.vip.pallas.console.vo.ClusterVO;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.base.BaseClusterOp;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.ElasticSearchService;
import com.vip.pallas.service.IndexService;
import com.vip.pallas.service.NodeService;

@Validated
@RestController
@RequestMapping("/cluster")
public class ClusterController {

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private IndexService indexService;
    
    @Autowired
    private NodeService nodeService;

    @Autowired
    private ElasticSearchService elasticSearchService;


    @RequestMapping(value = "/page.json", method = RequestMethod.GET)
    public PageResultVO<Cluster> page(@RequestParam(required = false, defaultValue = "1") @Min(value = 0, message = "currentPage必须为正数") Integer currentPage,
                                    @RequestParam(required = false, defaultValue = "10") @Min(value = 0, message = "pageSize必须为正数") Integer pageSize,
                                    @RequestParam(required = false) String clusterId) {

        Page<Cluster> page = new Page<>();
        page.setPageNo(currentPage);
        page.setPageSize(pageSize);

        PageResultVO<Cluster> resultVO = new PageResultVO<>();

        List<Cluster> list = clusterService.findPage(page, clusterId);
        List<String> privileges = AuthorizeUtil.loadPrivileges();

        if (CollectionUtils.isNotEmpty(list) && CollectionUtils.isNotEmpty(privileges)) {
            list.stream()
                    .filter((Cluster cluster) -> privileges.contains("cluster.all")
                            || privileges.contains("cluster." + cluster.getClusterId()))
                    .forEach((Cluster cluster) -> cluster.setHasPrivilege(true));
        }

        if (privileges != null && privileges.contains("cluster.all")) {
            resultVO.setAllPrivilege(true);
        }
        resultVO.setList(list);
        resultVO.setTotal(page.getTotalRecord());
        resultVO.setPageCount(page.getTotalPage());

        return resultVO;
    }

    @RequestMapping(value = "/all.json", method = RequestMethod.GET)
    public List<Cluster> listAll() {
        List<Cluster> result = clusterService.findAll();
        return result;
    }

    @RequestMapping(value = "/all/physicals.json", method = RequestMethod.GET)
    public Map<String, Object> getPhysicalsList() {
        Map<String, Object> resultMap = new HashMap<>();
        List<Cluster> allPhysicalList = new LinkedList<>();
        Map<String, List<String>> logicMap = new HashMap<>();

        for(Cluster c : clusterService.findAll()) {
            if (c.isLogicalCluster()) {
                List<String> list = Stream.of(c.getRealClusters().split(",")).collect(toList());
                List<String> subPhysicals = allPhysicalList.stream().filter((Cluster cluster) -> list.contains("" + cluster.getId())).map(Cluster::getClusterId).collect(toList());
                logicMap.putIfAbsent(c.getClusterId(), subPhysicals);
            } else {
                allPhysicalList.add(c);
            }
        }

        resultMap.put("list", allPhysicalList);
        resultMap.put("logic_physical_map", logicMap);
        return resultMap;
    }

    @RequestMapping(value = "/id.json", method = RequestMethod.GET)
	public Cluster findById(@RequestParam @NotBlank(message = "clusterId不能为空") String clusterId) {
        Cluster cluster = clusterService.findByName(clusterId);
        if (null == cluster) {
        	return new Cluster();
        }
        return cluster;
    }

    @RequestMapping(value = "/add.json", method = RequestMethod.POST)
    public void add(@RequestBody ClusterVO params, HttpServletRequest request) throws Exception {
    	if (!AuthorizeUtil.authorizeClusterPrivilege(request, null)) {
    		throw new BusinessLevelException(403, "无权限操作");
    	}
        Cluster cluster = getCluser(params);
        cluster.setCreateTime(new Date());
        clusterService.insert(cluster);
        AuditLogUtil.log("add cluster: {0}，http address:{1}, transport address:{2}", cluster.getClusterId(), cluster.getHttpAddress(), cluster.getClientAddress());
    }

    @RequestMapping(value = "/update.json", method = RequestMethod.POST)
    public void update(@RequestBody ClusterVO params, HttpServletRequest request) {
    	if (!AuthorizeUtil.authorizeClusterPrivilege(request, params.getClusterId())) {
    		throw new BusinessLevelException(403, "无权限操作");
    	}
        if(params.getId() == null) {
            throw new BusinessLevelException(500,"id不能为空");
        }
        Cluster cluster = getCluser(params);
        cluster.setUpdateTime(new Date());

        clusterService.update(cluster);
        AuditLogUtil.log("update cluster: {0} http address:{1}, transport address:{2}, accessiblePs:{3}",
                cluster.getClusterId(), cluster.getHttpAddress(), cluster.getClientAddress(),
                cluster.getAccessiblePs());

    }

    @RequestMapping(value = "/delete/id.json", method = RequestMethod.POST)
    public void deleteById(@RequestBody BaseClusterOp params, HttpServletRequest request) {
        String clusterId = params.getClusterId();
        Cluster cluster = clusterService.findByName(clusterId);
        if (cluster == null) {
            throw new BusinessLevelException(500, "cluster不存在");
        }
        if (!AuthorizeUtil.authorizeClusterPrivilege(request, cluster.getClusterId())) {
    		throw new BusinessLevelException(403, "无权限操作");
    	}

        Page<Index> page = new Page<>();
        page.setPageNo(1);
        page.setPageSize(10);
        List<Index> indexList = indexService.findPage(page, "", clusterId);
        if (indexList != null && !indexList.isEmpty()) {
            throw new BusinessLevelException(500, "该集群存在" + page.getTotalRecord() + "个索引，不能删除");
        }

        clusterService.deleteByClusterId(clusterId);
        AuditLogUtil.log("deleted cluster: {0}，http address:{1}, transport address:{2}", clusterId, cluster.getHttpAddress(), cluster.getClientAddress());
    }
    
    @RequestMapping(value = "/state.json", method = RequestMethod.GET)
	public ClusterStateVO getClusterStatus(@RequestParam @NotBlank(message = "clusterName不能为空") String clusterName,
			HttpServletRequest request) throws Exception {
        String esStatus = elasticSearchService.getClusterStatus(clusterName);
        String cause  = nodeService.getClusterState(clusterName, esStatus);
        Map<String, String> settings = elasticSearchService.getMainClusterSettings(clusterName);
        List<NodeInfo> nodes = nodeService.getNodeList(clusterName);
        
        ClusterStateVO stateVO = new ClusterStateVO();
        stateVO.setStatus(esStatus);
        stateVO.setCause(cause);
        stateVO.setSettings(settings);
        stateVO.setNodes(nodes);
        
        return stateVO;
    }
    
    @RequestMapping(value = "/settings/default/reset.json")
	public Map<String, Object> resetSettings(@RequestParam @NotBlank(message = "clusterName不能为空") String clusterName,
			HttpServletRequest request) throws Exception {
    	if (!AuthorizeUtil.authorizeClusterPrivilege(request, clusterName)) {
    		throw new BusinessLevelException(403, "无权限操作");
    	}
    	Map<String, Object> resultMap = new HashMap<>();
    	String result = nodeService.resetClusterDefaultSettings(clusterName);
    	resultMap.put("result", result);
    	return resultMap;
    }

    private Cluster getCluser(ClusterVO params) {
        if(StringUtils.isEmpty(params.getRealClusters())) {
            if(StringUtils.isEmpty(params.getHttpAddress())) {
                throw new BusinessLevelException(500, "httpAddress不能为空");
            }
            if(StringUtils.isEmpty(params.getClientAddress())) {
                throw new BusinessLevelException(500, "clientAddress");
            }

        } else {
            params.setHttpAddress("");
            params.setClientAddress("");
        }

        Cluster cluster = new Cluster();
        BeanUtils.copyProperties(params, cluster);
        return cluster;
    }
}