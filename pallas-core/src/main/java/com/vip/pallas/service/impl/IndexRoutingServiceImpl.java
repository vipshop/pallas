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

package com.vip.pallas.service.impl;

import com.vip.pallas.mybatis.entity.IndexRouting;
import com.vip.pallas.mybatis.entity.IndexRoutingSecurity;
import com.vip.pallas.mybatis.entity.IndexRoutingTargetGroup;
import com.vip.pallas.mybatis.repository.IndexRoutingSecurityRepository;
import com.vip.pallas.mybatis.repository.IndexRoutingRepository;
import com.vip.pallas.mybatis.repository.IndexRoutingTargetGroupRepository;
import com.vip.pallas.service.IndexRoutingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by owen on 06/11/2017.
 */
@Service
public class IndexRoutingServiceImpl implements IndexRoutingService {

    private static final Logger logger = LoggerFactory.getLogger(IndexRoutingServiceImpl.class);


    @Resource
    private IndexRoutingRepository routingRepo;

    @Resource
    private IndexRoutingTargetGroupRepository targetGroupRepo;

    @Resource
    private IndexRoutingSecurityRepository routingSecurityRepo;

    @Override
    public void addOrUpdateRoutingTargetGroup(Long indexId, IndexRoutingTargetGroup group) {
        if (group.getId() == null) {
            targetGroupRepo.insert(group);
        } else {
            targetGroupRepo.update(group);
        }
    }

    @Override
    public void addOrUpdateRoutingSecurity(Long indexId, IndexRoutingSecurity routingSecurity) {
        if (routingSecurity.getId() == null) {
            routingSecurityRepo.insert(routingSecurity);
        } else {
            routingSecurityRepo.update(routingSecurity);
        }
    }

    @Override
    public List<IndexRoutingSecurity> getAllIndexRoutingSecurity() {
        return routingSecurityRepo.selectAll();
    }

    @Override
    public IndexRoutingSecurity getRoutingSecurity(Long indexId) {
        return routingSecurityRepo.selectByIndexId(indexId);
    }

    @Override
    public void updateRoutingTargetGroup(IndexRoutingTargetGroup group) {
        targetGroupRepo.update(group);
    }

    @Override
    public List<IndexRoutingTargetGroup> getIndexRoutingTargetGroups(Long indexId) {
        return targetGroupRepo.selectByIndexId(indexId);
    }

    @Override
    public List<IndexRoutingTargetGroup> getClusterRoutingTargetGroups(Long clusterId) {
        return targetGroupRepo.selectByClusterId(clusterId);
    }

    @Override
    public void addOrUpdateIndexRouting(Long indexId, IndexRouting routing) {
        if (routing.getId() != null) {
            routingRepo.update(routing);
        } else {
            routingRepo.insert(routing);
        }
    }

    @Override
    public void deleteRoutingTargetGroup(Long id) {
        targetGroupRepo.deleteByPrimaryKey(id);
    }

    @Override
    public IndexRouting getIndexRouting(Long ownId, String type) {
        return routingRepo.select(ownId, type);
    }

    @Override
    public List<IndexRouting> getAllIndexRouting() {
        return routingRepo.selectAll();
    }

    @Override
    public List<IndexRoutingTargetGroup> getAllIndexRoutingTargetGroup() {
        return targetGroupRepo.selectAll();
    }

    @Override
    public void updateNodeState(String cluster, String nodeIp, int state) {
        List<IndexRoutingTargetGroup> list = getAllIndexRoutingTargetGroup();
        list.forEach(
                (IndexRoutingTargetGroup g) -> {
                    try {
                        List<IndexRoutingTargetGroup.NodeInfo> nodes = IndexRoutingTargetGroup.fromXContent(g.getNodesInfo());
                        AtomicBoolean isUpdated = new AtomicBoolean(false);

                        nodes.stream()
                                .filter((IndexRoutingTargetGroup.NodeInfo nodeInfo) -> nodeInfo.getCluster().equals(cluster) && nodeInfo.getAddress().equals(nodeIp))
                                .forEach((IndexRoutingTargetGroup.NodeInfo nodeInfo) -> {
                                    nodeInfo.setState(state);
                                    isUpdated.set(true);
                                });

                        if(isUpdated.get()){
                            g.setNodesInfo(IndexRoutingTargetGroup.toXContent(nodes));
                            updateRoutingTargetGroup(g);
                        }
                    } catch (Exception e) {
                        logger.error("error", e);
                    }
                }
        );
    }
}