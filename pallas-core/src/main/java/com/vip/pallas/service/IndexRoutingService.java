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

import com.vip.pallas.mybatis.entity.IndexRouting;
import com.vip.pallas.mybatis.entity.IndexRoutingSecurity;
import com.vip.pallas.mybatis.entity.IndexRoutingTargetGroup;

import java.util.List;

/**
 * Created by owen on 02/11/2017.
 */
public interface IndexRoutingService {

    void addOrUpdateRoutingSecurity(Long indexId, IndexRoutingSecurity indexRoutingSecurity);

    void addOrUpdateRoutingTargetGroup(Long indexId, IndexRoutingTargetGroup group);

    void updateRoutingTargetGroup(IndexRoutingTargetGroup group);

    List<IndexRoutingTargetGroup> getIndexRoutingTargetGroups(Long indexId);

    List<IndexRoutingTargetGroup> getClusterRoutingTargetGroups(Long clusterId);

    void deleteRoutingTargetGroup(Long id);

    void addOrUpdateIndexRouting(Long indexId, IndexRouting routing);

    IndexRouting getIndexRouting(Long ownId, String type);

    IndexRoutingSecurity getRoutingSecurity(Long indexId);

    List<IndexRouting> getAllIndexRouting();

    List<IndexRoutingTargetGroup> getAllIndexRoutingTargetGroup();

    List<IndexRoutingSecurity> getAllIndexRoutingSecurity();

    void updateNodeState(String cluster, String nodeIp, int state);
}