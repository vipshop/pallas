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

import static com.vip.pallas.mybatis.entity.PluginUpgrade.UPGRADE_STATUS_DOWNLOAD;
import static com.vip.pallas.mybatis.entity.PluginUpgrade.UPGRADE_STATUS_DOWNLOAD_DONE;
import static com.vip.pallas.mybatis.entity.PluginUpgrade.UPGRADE_STATUS_UPGRADE;
import static com.vip.pallas.mybatis.entity.PluginUpgrade.UPGRADE_STATUS_UPGRADE_DONE;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.vo.PluginNodeState;
import com.vip.pallas.console.vo.PluginUpgradeVO;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.mybatis.entity.PluginRuntime;
import com.vip.pallas.mybatis.entity.PluginUpgrade;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.PallasPluginService;
import com.vip.vjtools.vjkit.collection.ListUtil;

@Validated
@RestController
@RequestMapping("/plugin/upgrade")
public class PluginUpgradeController {

    @Autowired
    private PallasPluginService pluginService;

    @Autowired
    private ClusterService clusterService;
    
    @RequestMapping(path = "/list.json")
    public PageResultVO<PluginUpgradeVO> queryPlugins(
            @RequestParam(required = false, defaultValue = "1") @Min(value = 0, message = "currentPage必须为正数") Integer currentPage,
            @RequestParam(required = false, defaultValue = "10") @Min(value = 0, message = "pageSize必须为正数") Integer pageSize,
            @RequestParam(required = false, defaultValue = "") String pluginName) {
        PageResultVO<PluginUpgradeVO> result = new PageResultVO<>();
        List<String> privileges = AuthorizeUtil.loadPrivileges();
        if(ListUtil.isNotEmpty(privileges) && (privileges.contains("plugin.approve") || privileges.contains("plugin.all"))){
            result.setAllPrivilege(true);
        } else {
            result.setAllPrivilege(false);
        }
        
        Page<PluginUpgrade> page = new Page<>();
        page.setPageNo(currentPage);
        page.setPageSize(pageSize);
        page.setParam("pluginName", pluginName);

        List<PluginUpgrade> list = pluginService.getPluginUpgrade(page);

        List<PluginUpgradeVO> resultList = new LinkedList<>();
        for(PluginUpgrade u : list) {
            PluginUpgradeVO pu = new PluginUpgradeVO();
            pu.setId(u.getId());
            pu.setApplyUser(u.getApplyUser());
            pu.setApproveUser(u.getApproveUser());
            pu.setClusterId(u.getClusterId());
            pu.setPluginName(u.getPluginName());
            pu.setPluginVersion(u.getPluginVersion());
            pu.setPluginType(u.getPluginType());
            pu.setPackagePath(u.getPackagePath());
            Cluster cluster = clusterService.findByName(u.getClusterId());
            pu.setClusterDescription(cluster == null ? "" : cluster.getDescription());
            pu.setNote(u.getNote());
            pu.setState(u.getState());
            pu.setUpdateTime(u.getUpdateTime());
            List<PluginNodeState> pns = pu.getNodeStates() == null ? new ArrayList<>() : pu.getNodeStates(); //NOSONAR
            List<PluginRuntime> runtimeList = pluginService.getPluginRuntimes(u.getClusterId(), u.getPluginName());
            for(PluginRuntime r : runtimeList) {
                if (StringUtils.isEmpty(r.getNodeIp())) {
                    continue;
                }
                PluginNodeState ns = new PluginNodeState();
                ns.setNodeIp(r.getNodeIp() + " " + r.getNodeHost());
                ns.setPluginVersion(r.getPluginVersion());
                ns.setAvailableVersions(r.getAvailableVersions());
                pns.add(ns);
            }
            pu.setNodeStates(pns);
            calculateGUIStateDisplay(pu);
            resultList.add(pu);
        }

        result.setList(resultList);
        result.setTotal(page.getTotalRecord());
        result.setPageCount(page.getTotalPage());
        return result;
    }

    private void calculateGUIStateDisplay(PluginUpgradeVO pu) {
        String applyVer = pu.getPluginVersion();
        int currentState = pu.getState();
        int downloadCount = 0;
        int upgradeCount = 0;
        int totalNodesCount = pu.getNodeStates().size();
        for(PluginNodeState node : pu.getNodeStates()) {
            node.setState(currentState);
            if (currentState == UPGRADE_STATUS_DOWNLOAD) {
                for(String avaiVer : node.getAvalibleVersionArray()) {
                    if(applyVer.equals(avaiVer)) {
                        downloadCount++;
                        node.setState(UPGRADE_STATUS_DOWNLOAD_DONE);
                        break;
                    }
                }
                if(downloadCount == totalNodesCount) {
                    pu.setState(UPGRADE_STATUS_DOWNLOAD_DONE);
                }
            } else if (currentState == UPGRADE_STATUS_UPGRADE) {
                node.setState(UPGRADE_STATUS_DOWNLOAD);
                for(String avaiVer : node.getAvalibleVersionArray()) {
                    if(applyVer.equals(avaiVer)) {
                        node.setState(UPGRADE_STATUS_UPGRADE);
                        break;
                    }
                }
                if(applyVer.equals(node.getPluginVersion())) {
                    upgradeCount++;
                    node.setState(UPGRADE_STATUS_UPGRADE_DONE);
                }
                if(upgradeCount == totalNodesCount) {
                    pu.setState(UPGRADE_STATUS_UPGRADE_DONE);
                }
            }
        }
    }
    
    @RequestMapping(path="/add.json", method = {RequestMethod.POST})
    public void createPlugin(@RequestBody PluginUpgrade pluginUpgrade){
        if (null == clusterService.findByName(pluginUpgrade.getClusterId())) {
            throw new BusinessLevelException(500, "cluster不存在");
        }

        PluginUpgrade upgrade = pluginService.getLatestUpgrade(pluginUpgrade.getClusterId(), pluginUpgrade.getPluginName());
        if (upgrade != null && !upgrade.isFinished()) {
            throw new BusinessLevelException(500, "存在未完成的升级流程");
        }

        Date date = new Date();
        pluginUpgrade.setState(PluginUpgrade.UPGRADE_STATUS_NEED_APPROVAL);
        pluginUpgrade.setCreateTime(date);
        pluginUpgrade.setApproveTime(date);
        pluginUpgrade.setApplyTime(date);
        pluginService.addPluginUpgrade(pluginUpgrade);
    }
}