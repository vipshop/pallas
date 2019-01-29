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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.service.NodeService;
import com.vip.pallas.utils.ObjectMapTool;

@Validated
@RestController
@RequestMapping("/cluster/node")
public class ClusterNodeController {

    @Autowired
    private NodeService nodeService;

    @RequestMapping(value = "/restart.json")
	public Map<String, Object> restart(@RequestBody Map<String, Object> params, HttpServletRequest request)
			throws Exception {
        String clusterName = ObjectMapTool.getString(params, "clusterName");
        String nodeIp = ObjectMapTool.getString(params, "nodeIp");

        if(clusterName == null) {
            throw new BusinessLevelException(500, "clusterName不能为空");
        }

        if(nodeIp == null) {
            throw new BusinessLevelException(500, "nodeIp不能为空");
        }
        
        if (!AuthorizeUtil.authorizeClusterPrivilege(request, clusterName)) {
    		throw new BusinessLevelException(403, "无权限操作");
    	}

        nodeService.markRestartState(clusterName, nodeIp);
        nodeService.restartNode(clusterName, nodeIp);
        return new HashMap<>();
    }
    
}