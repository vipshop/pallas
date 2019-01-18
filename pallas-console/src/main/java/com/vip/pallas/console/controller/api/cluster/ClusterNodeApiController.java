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

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.service.NodeService;
import com.vip.pallas.utils.ObjectMapTool;

@Validated
@RestController
@RequestMapping("/node")
public class ClusterNodeApiController {

    @Autowired
    private NodeService nodeService;

    @RequestMapping(value = "/state.json")
    public Map<String, Object> nodeState(@RequestBody Map<String, Object> params) throws Exception {
        String clusterName = ObjectMapTool.getString(params, "clusterName");
        String nodeName = ObjectMapTool.getString(params, "nodeName");
        String nodeIp = ObjectMapTool.getString(params, "nodeIp");
        Byte state = ObjectMapTool.getByte(params, "state");
        boolean isRestart = ObjectMapTool.getBoolean(params, "isRestart");

        if(clusterName == null) {
            throw new BusinessLevelException(500, "clusterName不能为空");
        }

        if(nodeName == null) {
            throw new BusinessLevelException(500, "nodeName不能为空");
        }

        if(nodeIp == null) {
            throw new BusinessLevelException(500, "nodeIp不能为空");
        }

        if(state == null) {
            throw new BusinessLevelException(500, "state不能为空");
        }

        nodeService.stateNode(clusterName, nodeName, nodeIp, state, isRestart);
        return new HashMap<>();
    }

}