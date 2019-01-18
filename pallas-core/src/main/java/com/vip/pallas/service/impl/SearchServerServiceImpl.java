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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vip.pallas.mybatis.entity.SearchServer;
import com.vip.pallas.mybatis.entity.SearchServerExample;
import com.vip.pallas.mybatis.entity.SearchServerExample.Criteria;
import com.vip.pallas.mybatis.repository.SearchServerRepository;
import com.vip.pallas.service.SearchServerService;

@Service
public class SearchServerServiceImpl implements SearchServerService {

	private static Logger logger = LoggerFactory.getLogger(SearchServerServiceImpl.class);
	@Autowired
	private SearchServerRepository searchServerRepository;

	@Override
	public void upsertByIpAndCluster(SearchServer record) {
		SearchServerExample example = new SearchServerExample();
		example.setLimit(1);
		Criteria criteria = example.createCriteria();
		criteria.andIpportEqualTo(record.getIpport());
		criteria.andClusterEqualTo(record.getCluster());
		
		List<SearchServer> list = searchServerRepository.selectByExample(example);
		if (list.isEmpty()) { // insert
			record.setCreateTime(new Date());
			searchServerRepository.insertSelective(record);
		} else {
			SearchServer searchServer = list.get(0);
			searchServer.setInfo(record.getInfo());
			searchServer.setCluster(record.getCluster());
			searchServer.setTakeTraffic(record.getTakeTraffic());
			searchServerRepository.updateByPrimaryKeySelective(searchServer);
		}
	}
	
	@Override
	public List<SearchServer> selectByCluster(String cluster) {
		SearchServerExample example = new SearchServerExample();
		Criteria criteria = example.createCriteria();
		criteria.andClusterEqualTo(cluster);
		List<SearchServer> list = searchServerRepository.selectByExample(example);
		return list;
	}

	@Override
	public long countByExample(SearchServerExample example) {
		return searchServerRepository.countByExample(example);
	}

	@Override
	public List<SearchServer> selectByExampleWithBLOBsAndHealthyInterval(SearchServerExample example) {
		example.setHealthyInterval(SearchServerService.HEALTHY_UPLOAD_INTERVAL_TOLERANCE / 1000);
		return searchServerRepository.selectByExampleWithBLOBsAndHealthyInterval(example);
	}

	@Override
	public List<String> selectDistictCluster() {
		return searchServerRepository.selectDistictCluster();
	}
	
	@Override
	public SearchServer selectByPrimaryKey(Long id) {
		return searchServerRepository.selectByPrimaryKey(id);
	}
	
	@Override
	public List<SearchServer> markUnHealthyServer(List<SearchServer> ssList) {
		if (!ssList.isEmpty()) {
			long currentTimeMillis = System.currentTimeMillis();
			
			for (SearchServer searchServer : ssList) {
				if (currentTimeMillis - searchServer.getUpdateTime().getTime() > SearchServerService.HEALTHY_UPLOAD_INTERVAL_TOLERANCE) {
					logger.info("got one unhealthy server: {}", searchServer);
					searchServer.setHealthy(false);
				} else {
					searchServer.setHealthy(true);
				}
			}
		}
		return ssList;
	}

	@Override
	public List<SearchServer> selectHealthyServers(Long healthyInterval) {
		return searchServerRepository.selectHealthyServers(healthyInterval);
	}
	
	@Override
	public void deleteByPrimaryKey(Long id) {
		searchServerRepository.deleteByPrimaryKey(id);
	}

	@Override
	public List<SearchServer> selectAllHealthyServer() {
		List<SearchServer> searchServerList = searchServerRepository.selectAll();
		return searchServerList != null ? this.markUnHealthyServer(searchServerList).stream().
				filter(server -> server.isHealthy()).collect(Collectors.toList()) : null;
	}

	@Override
	public List<SearchServer> selectHealthyServersByCluster(Long healthyInterval, String cluster) {
		return searchServerRepository.selectHealthyServersByCluster(healthyInterval / 1000, cluster);
	}

	@Override
	public void setTakeTraffic(Long id, Boolean takeTraffic) {
		searchServerRepository.setTakeTraffic(id, takeTraffic);
	}

	@Override
	public void deleteNDaysOldServer(int n) {
		searchServerRepository.deleteNDaysOldServer(n);

	}

}