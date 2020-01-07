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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.mybatis.repository.ClusterRepository;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.IndexVersionService;
import com.vip.pallas.service.PrivilegeService;

@Service
public class ClusterServiceImpl implements ClusterService{
	
	private static final Logger logger = LoggerFactory.getLogger(ClusterServiceImpl.class);
	
	@Resource
    private ClusterRepository clusterRepository;
	
	@Autowired
	private PrivilegeService privilegeService;

	@Autowired
	private IndexVersionService indexVersionService;
	private Cluster cluster;

	@Override
	@Transactional(rollbackFor=Exception.class)
	public void insert(Cluster cluster) throws Exception {
		try {
			clusterRepository.insertSelective(cluster);
		} catch (Exception e) {
			logger.error(e.toString(), e);
			String message = e.getMessage();
			
			if(StringUtils.isNotEmpty(message) && message.indexOf("Duplicate entry") >= 0){
				throw new PallasException("集群" + cluster.getClusterId() + "已存在");
			}else{
				throw new PallasException(message);
			}
		}
		
		privilegeService.createClusterPrivilege(cluster.getClusterId());
	}

	@Override
	@Transactional(rollbackFor=Exception.class)
	public void update(Cluster cluster) {
		clusterRepository.updateByPrimaryKeySelective(cluster);
	}

	@Override
	public Cluster findByName(String name) {
		cluster = clusterRepository.selectByClusterName(name);
		if(null != cluster) {
			cluster.setMonitorLevelModel();
		}
		return cluster;
	}

	@Override
	public List<Cluster> findAll() {
		List<Cluster> result = clusterRepository.selectAll();
		return result;
	}

	@Override
	public List<Cluster> findPage(Page<Cluster> page, String clusterId) {
		Map<String, Object> params = new HashMap<String, Object>();  
		params.put("clusterId", clusterId);
        page.setParams(params);
		List<Cluster> result = clusterRepository.selectPage(page);
		if(null != result && result.size() > 0) {
			result.forEach((cluster) -> {
				cluster.setMonitorLevelModel();
			});

		}
		return result;
	}

	@Override
	@Transactional(rollbackFor=Exception.class)
	public void deleteByClusterId(String clusterId) {
		clusterRepository.deleteByClusterId(clusterId);
		
		try {
			privilegeService.deleteClusterPrivilege(clusterId);
		} catch (Exception e){
			logger.error(e.getClass() + " " + e.getMessage(), e);
		}
	}
	
	@Override
	public List<Cluster> selectAllPhysicalClusters() {
		return clusterRepository.selectAllPhysicalClusters();
	}

	@Override
	public List<Cluster> selectPhysicalClustersByIndexId(Long indexId) {
		return clusterRepository.selectPhysicalClustersByIndexId(indexId);
	}

	@Override
	public Cluster selectByPrimaryKey(Long id) {
		return clusterRepository.selectByPrimaryKey(id);
	}
	
}