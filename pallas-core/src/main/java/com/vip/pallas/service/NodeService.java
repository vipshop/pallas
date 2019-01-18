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

package com.vip.pallas.service;

import java.util.List;

import com.vip.pallas.bean.NodeInfo;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.Node;

public interface NodeService {

    List<NodeInfo> getNodeList(String clusterName) throws Exception;

    void restartNode(String clusterName, String nodeIp) throws Exception;

    void stateNode(String clusterName, String nodeName, String nodeIp, Byte state, boolean isRestart) throws Exception;

    List<String> getNormalIndexOnNode(String clusterName, String nodeIp) throws Exception;

    String getClusterState(String clusterName, String esStatus) throws Exception;

    String resetClusterDefaultSettings(String clusterName) throws Exception;

    List<Node> getAbnormalNodeList(String clusterName);

    void markRestartState(String clusterName, String nodeIp) throws PallasException;
}