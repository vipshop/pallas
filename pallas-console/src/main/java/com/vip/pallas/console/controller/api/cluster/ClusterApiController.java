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

package com.vip.pallas.console.controller.api.cluster;

import static java.util.stream.Collectors.toList;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.NodeService;
import com.vip.pallas.utils.ObjectMapTool;

@Validated
@RestController
@RequestMapping("/pallas/cluster")
public class ClusterApiController {

    @Autowired
    private NodeService nodeService;
    
    @Autowired
    private ClusterService clusterService;

    @RequestMapping(value = "/abnormal_node/list.json")
    public Map<String, Object> list(@RequestBody Map<String, Object> params) {
        String clusterName = ObjectMapTool.getString(params, "clusterName");

        if (clusterName == null) {
            throw new BusinessLevelException(500, "clusterName不能为空");
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", nodeService.getAbnormalNodeList(clusterName));
        return resultMap;
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
}