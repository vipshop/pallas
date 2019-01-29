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

package com.vip.pallas.console.controller.plugin;

import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.PluginNodeState;
import com.vip.pallas.console.vo.PluginRuntimeVO;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.PluginRuntime;
import com.vip.pallas.mybatis.entity.PluginUpgrade;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.PallasPluginService;
import com.vip.vjtools.vjkit.collection.ListUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;

@Validated
@RestController
@RequestMapping("/plugin/runtime")
public class PluginRuntimeController {

    @Autowired
    private PallasPluginService pluginService;
    
    @Autowired
    private ClusterService clusterService;
    
    @RequestMapping(path="/list.json")
    public PageResultVO<PluginRuntimeVO> queryRuntimePlugins(
            @RequestParam(required = false, defaultValue = "1") @Min(value = 0, message = "currentPage必须为正数") Integer currentPage,
            @RequestParam(required = false, defaultValue = "10") @Min(value = 0, message = "pageSize必须为正数") Integer pageSize,
            @RequestParam(required = false, defaultValue = "") String pluginName) {
        Map<String, List<PluginRuntime>> pluginsMap = pluginService.getAllPluginRuntimes().stream()
                .filter(p -> pluginName == null || p.getPluginName().contains(pluginName))
                .collect(groupingBy(p -> p.getClusterId() + ":" + p.getPluginName()));

        List<PluginRuntimeVO> resultList = new LinkedList<>();

        pluginsMap.forEach((key, list) -> {
                PluginRuntime pRuntime = list.get(0);
                Cluster cluster = clusterService.findByName(pRuntime.getClusterId());
                PluginRuntimeVO pr = new PluginRuntimeVO();
                pr.setId(pRuntime.getId());
                pr.setClusterDescription(cluster == null ? "" : cluster.getDescription());
                pr.setClusterId(pRuntime.getClusterId());
                pr.setPluginName(pRuntime.getPluginName());
                pr.setPluginType(pRuntime.getPluginType());
                pr.setUpdateTime(pRuntime.getUpdateTime());
                List<PluginNodeState> pns = pr.getNodeStates() == null ? new ArrayList<>() : pr.getNodeStates();
                PluginUpgrade pUpgrade = pluginService.getLatestUpgrade(pRuntime.getClusterId(), pRuntime.getPluginName());
                if (pUpgrade == null) {
                    pr.setState(PluginUpgrade.UPGRADE_STATUS_CREATE);
                    pr.setCreatable(true);
                } else {
                    pr.setState(pUpgrade.getState());
                    pr.setCreatable(pUpgrade.isFinished());
                }

                for(PluginRuntime r : list) {
                    PluginNodeState ns = new PluginNodeState();
                    ns.setNodeIp(r.getNodeIp() + " " + r.getNodeHost());
                    ns.setPluginVersion(r.getPluginVersion());
                    pns.add(ns);
                }
                pr.setNodeStates(pns);
                resultList.add(pr);
            }
        );

        Collections.sort(resultList, Comparator.comparing(PluginRuntimeVO::getUpdateTime).reversed());

        int totalSize = resultList.size();
        int pageCount = totalSize % pageSize == 0 ? totalSize / pageSize : totalSize / pageSize + 1;
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalSize);

        PageResultVO<PluginRuntimeVO> result = new PageResultVO<>();
        List<String> privileges = AuthorizeUtil.loadPrivileges();
		if (ListUtil.isNotEmpty(privileges)
				&& (privileges.contains("plugin.approve") || privileges.contains("plugin.all"))) {
            result.setAllPrivilege(true);
        } else {
            result.setAllPrivilege(false);
        }
        
        if(!resultList.isEmpty() && toIndex >= fromIndex){
            result.setList(resultList.subList(fromIndex, toIndex));
        }
        result.setTotal((long)totalSize);
        result.setPageCount(pageCount);
        
        return result;
    }
    
}