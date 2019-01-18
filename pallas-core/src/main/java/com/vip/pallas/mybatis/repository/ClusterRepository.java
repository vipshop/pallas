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

package com.vip.pallas.mybatis.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.Page;

@Repository
public interface ClusterRepository {
    int deleteByClusterId(String clusterId);

    int insert(Cluster record);

    int insertSelective(Cluster record);

    Cluster selectByPrimaryKey(Long id);

    Cluster selectByClusterName(String clusterId);
    
    Cluster selectByVersionId(Long versionId);

    int updateByPrimaryKeySelective(Cluster record);

    int updateByPrimaryKeyWithBLOBs(Cluster record);

    int updateByPrimaryKey(Cluster record);
    
    List<Cluster> selectAll();
    
    List<Cluster> selectAllPhysicalClusters();
    
    List<Cluster> selectPage(Page<Cluster> page);

	List<Cluster> selectPhysicalClustersByIndexId(Long indexId);
}