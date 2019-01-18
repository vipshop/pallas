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

package com.vip.pallas.mybatis.entity;

import com.vip.pallas.utils.JsonUtil;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.type.TypeReference;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Created by owen on 2/11/2017.
 */
public class IndexRoutingTargetGroup {

    public static final int NORMAL_LEVEL = 0;
    public static final int CLUSTER_LEVEL = 1;
    public static final int SHARD_LEVEL = 2;
    public static final int CLUSTER_PRIMARY_FIRST_LEVEL = 3;
    public static final int CLUSTER_REPLICA_FIRST_LEVEL = 4;


    private Long id;

    private String name;

    private Long indexId;

    private String indexName;

    private String type;

    private String nodesInfo;

    private String clustersInfo;

    private int state;

    private int clusterLevel;

    private Date createTime;

    private Date updateTime;

    private List<NodeInfo> nodeInfoList;

    private List<ClusterInfo> clusterInfoList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIndexId() {
        return indexId;
    }

    public void setIndexId(Long indexId) {
        this.indexId = indexId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getNodesInfo() {
        return nodesInfo;
    }

    public void setNodesInfo(String nodesInfo) {
        this.nodesInfo = nodesInfo;
    }

    public String getClustersInfo() {
        return clustersInfo;
    }

    public void setClustersInfo(String clustersInfo) {
        this.clustersInfo = clustersInfo;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @JsonIgnore
    public boolean isClusterLevel0() {
        return clusterLevel == CLUSTER_LEVEL || clusterLevel == CLUSTER_PRIMARY_FIRST_LEVEL || clusterLevel == CLUSTER_REPLICA_FIRST_LEVEL;
    }

    @JsonIgnore
    public boolean isShardLevel() {
        return clusterLevel == SHARD_LEVEL;
    }

    @JsonIgnore
    public boolean isNormalLevel() {
        return clusterLevel == NORMAL_LEVEL;
    }

    @JsonIgnore
    public boolean isClusterPrimaryFirstLevel() {
        return clusterLevel == CLUSTER_PRIMARY_FIRST_LEVEL;
    }

    @JsonIgnore
    public boolean isClusterReplicaFirstLevel() {
        return clusterLevel == CLUSTER_REPLICA_FIRST_LEVEL;
    }

    public int getClusterLevel() {
        return clusterLevel;
    }

    public void setClusterLevel(int clusterLevel) {
        this.clusterLevel = clusterLevel;
    }

    public static List<IndexRoutingTargetGroup> genDefault(Index index, List<Cluster> clusters) throws Exception {
        return clusters.stream().map((Cluster c) -> {
            IndexRoutingTargetGroup group = new IndexRoutingTargetGroup();
            group.setIndexId(index.getId());
            group.setIndexName(index.getIndexName());
            group.setName(clusters.size() == 1 ? "Default" : "Default-" + c.getClusterId());
            group.setClusterLevel(IndexRoutingTargetGroup.SHARD_LEVEL);
            group.setState(0);
            group.setCreateTime(new Date());
            ClusterInfo info = new ClusterInfo();
            info.setAddress(c.getHttpAddress());
            info.setCluster(c.getClusterId());
            info.setName(c.getClusterId());
            try {
                group.setClustersInfo(toXContent(Collections.singletonList(info)));
            } catch (Exception e) {//NOSONAR
                group.setClustersInfo("[]");
            }
            group.setNodesInfo("[]");
            return group;
        }).collect(toList());
    }


    @org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class NodeInfo {

        private String cluster;

        private String name;

        private String address;

        private int weight;

        private int state;

        public String getCluster() {
            return cluster;
        }

        public void setCluster(String cluster) {
            this.cluster = cluster;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }
    }

    @org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClusterInfo {

        private String cluster;

        private String name;

        private String address;

        public String getCluster() {
            return cluster;
        }

        public void setCluster(String cluster) {
            this.cluster = cluster;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }
    }

    public static List<NodeInfo> fromXContent(String json) throws Exception {
        String newJson = json;
        if (newJson == null || "".equals(newJson.trim())) {
            newJson = "[]";
        }
        return JsonUtil.readValue(newJson, new TypeReference<List<NodeInfo>>(){});
    }

    public static List<ClusterInfo> fromClusterContent(String json) throws Exception {
        String newJson = json;
        if (newJson == null || "".equals(newJson.trim())) {
            newJson = "[]";
        }
        return JsonUtil.readValue(newJson, new TypeReference<List<ClusterInfo>>(){});
    }

    public static <T> String toXContent(List<T> nodeInfos) throws Exception {
        if (nodeInfos == null) {
            throw new IllegalArgumentException("nodeInfos can not be null");
        }
        return JsonUtil.toJson(nodeInfos);
    }

    public List<NodeInfo> getNodeInfoList() {
        return nodeInfoList;
    }

    public void setNodeInfoList(List<NodeInfo> nodeInfoList) {
        this.nodeInfoList = nodeInfoList;
    }

    public List<ClusterInfo> getClusterInfoList() {
        return clusterInfoList;
    }

    public void setClusterInfoList(List<ClusterInfo> clusterInfoList) {
        this.clusterInfoList = clusterInfoList;
    }
}