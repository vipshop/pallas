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

import static com.vip.pallas.mybatis.entity.PluginUpgrade.UPGRADE_STATUS_CANCEL;
import static com.vip.pallas.mybatis.entity.PluginUpgrade.UPGRADE_STATUS_DENY;
import static com.vip.pallas.mybatis.entity.PluginUpgrade.UPGRADE_STATUS_DONE;
import static com.vip.pallas.mybatis.entity.PluginUpgrade.UPGRADE_STATUS_DOWNLOAD;
import static com.vip.pallas.mybatis.entity.PluginUpgrade.UPGRADE_STATUS_NEED_APPROVAL;
import static com.vip.pallas.mybatis.entity.PluginUpgrade.UPGRADE_STATUS_REMOVE;
import static com.vip.pallas.mybatis.entity.PluginUpgrade.UPGRADE_STATUS_UPGRADE;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.console.utils.AuditLogUtil;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.utils.SessionUtil;
import com.vip.pallas.console.vo.PluginAction;
import com.vip.pallas.console.vo.RemovePlugin;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.PluginCommand;
import com.vip.pallas.mybatis.entity.PluginUpgrade;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.PallasPluginService;

@RestController
@RequestMapping("/plugin")
public class PluginCommandActionController {

    @Autowired
    private PallasPluginService pluginService;
    
    @Autowired
    private ClusterService clusterService;

    @RequestMapping(path = "/remove.json")
    public void pluginRemoveAction(@RequestBody @Validated RemovePlugin plugin, HttpServletRequest request) {
    	if (!AuthorizeUtil.authorizePluginApprovePrivilege(request)) {
    		throw new BusinessLevelException(403, "无权限操作");
    	}
    	
        if (null == clusterService.findByName(plugin.getClusterId())) {
            throw new BusinessLevelException(500, "cluster不存在");
        }

        sendCommand(PluginCommand.COMMAND_REMOVE, plugin.getClusterId(), plugin.getPluginName(),
                plugin.getPluginVersion());

        AuditLogUtil.log("post remove plugin: clusterId - {0}, pluginName - {1}, pluginVersion - {2}",
                plugin.getClusterId(), plugin.getPluginName(), plugin.getPluginVersion());
    }
    
    @RequestMapping(path = "/upgrade/action.json")
    public void pluginAction(@RequestBody @Validated PluginAction pluginAction, HttpServletRequest request) {
		if (!"recall".equals(pluginAction.getAction()) && !AuthorizeUtil.authorizePluginApprovePrivilege(request)) {
			throw new BusinessLevelException(500, "cluster不存在");
		}
    	
        PluginUpgrade pUpgrade = pluginService.getPluginUpgrade(pluginAction.getPluginUpgradeId());
        if(pUpgrade == null) {
            throw new BusinessLevelException(500, "PluginUpgrade不存在");
        }

        int nextState = doUpgradeAction(pluginAction.getAction(), pUpgrade);
        if(pUpgrade.getState() != nextState) {
            pluginService.setUppgradeState(SessionUtil.getLoginUser(request), pUpgrade.getId(), nextState);
        }
    }
    
    private int doUpgradeAction(String action, PluginUpgrade pUpgrade) {
        if (!pUpgrade.isFinished()) {
            String actionLowcase = action.toLowerCase();
            int currentStatus = pUpgrade.getState();
            switch (actionLowcase) {
                case "recall":
                    if (currentStatus != UPGRADE_STATUS_NEED_APPROVAL) {
                        break;
                    }
                    return UPGRADE_STATUS_CANCEL;
                case "deny":
                    if (currentStatus != UPGRADE_STATUS_NEED_APPROVAL) {
                        break;
                    }
                    return UPGRADE_STATUS_DENY;
                case "stop":
                    return UPGRADE_STATUS_CANCEL;
                case "done":
                    return UPGRADE_STATUS_DONE;
                case "download":
                sendCommand(actionLowcase, pUpgrade.getClusterId(), pUpgrade.getPluginName(),
                        pUpgrade.getPluginVersion(), pUpgrade.getPluginType());
                    return currentStatus <= UPGRADE_STATUS_DOWNLOAD ? UPGRADE_STATUS_DOWNLOAD : currentStatus;
                case "upgrade":
                    sendCommand(actionLowcase, pUpgrade.getClusterId(), pUpgrade.getPluginName(),
                            pUpgrade.getPluginVersion(), pUpgrade.getPluginType());
                    return currentStatus <= UPGRADE_STATUS_UPGRADE ? UPGRADE_STATUS_UPGRADE : currentStatus;
                case "remove":
                    if (currentStatus != UPGRADE_STATUS_DONE) {
                        break;
                    }
                    sendCommand(actionLowcase, pUpgrade.getClusterId(), pUpgrade.getPluginName(),
                            pUpgrade.getPluginVersion(), pUpgrade.getPluginType());
                    return UPGRADE_STATUS_REMOVE;
                default:
                    break;
            }
        }
        throw new BusinessLevelException(500, "该工单不支持该操作:" + action);
    }
    
    private void sendCommand(String command, String clusterId, String pluginName, String pluginVersion) {
        List<String> nodeIpList = pluginService.getNodeIPsByCluster(clusterId);
        for(String ip : nodeIpList) {
            if("".equals(ip)) { //忽略虚拟Runtime状态
                continue;
            }
            PluginCommand cmd = new PluginCommand();
            cmd.setClusterId(clusterId);
            cmd.setCreateTime(new Date());
            cmd.setNodeIp(ip);
            cmd.setPluginName(pluginName);
            cmd.setPluginVersion(pluginVersion);
            cmd.setCommand(command);
            pluginService.addPluginCommand(cmd);
        }
    }

    private void sendCommand(String action, String clusterId, String pluginName, String pluginVersion, int pluginType) {
        List<String> nodeIpList = pluginService.getNodeIPsByCluster(clusterId);
        for(String ip : nodeIpList) {
            if("".equals(ip)) { //忽略虚拟Runtime状态
                continue;
            }
            PluginCommand cmd = new PluginCommand();
            cmd.setClusterId(clusterId);
            cmd.setCreateTime(new Date());
            cmd.setNodeIp(ip);
            cmd.setPluginName(pluginName);
            cmd.setPluginVersion(pluginVersion);
            cmd.setPluginType(pluginType);
            switch (action){
                case "download":
                    cmd.setCommand(PluginCommand.COMMAND_DOWNLOAD);
                    break;
                case "upgrade":
                    cmd.setCommand(PluginCommand.COMMAND_UPGRADE);
                    break;
                case "remove":
                    cmd.setCommand(PluginCommand.COMMAND_REMOVE);
                    break;
                default:
                    cmd.setCommand(PluginCommand.COMMAND_UNKNOWN);
            }
            pluginService.addPluginCommand(cmd);
        }
    }
}