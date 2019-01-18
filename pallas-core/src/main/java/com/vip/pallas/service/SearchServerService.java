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

import com.vip.pallas.mybatis.entity.SearchServer;
import com.vip.pallas.mybatis.entity.SearchServerExample;

public interface SearchServerService {
	// 2 times of upload interval.
	public static final long HEALTHY_UPLOAD_INTERVAL_TOLERANCE = 20 * 1000l; 
	
	void upsertByIpAndCluster(SearchServer record);

	List<SearchServer> selectByCluster(String cluster);

	long countByExample(SearchServerExample example);

	List<SearchServer> selectByExampleWithBLOBsAndHealthyInterval(SearchServerExample example);

	List<String> selectDistictCluster();

	SearchServer selectByPrimaryKey(Long id);

	List<SearchServer> markUnHealthyServer(List<SearchServer> ssList);

	List<SearchServer> selectHealthyServers(Long healthyInterval);

	void deleteByPrimaryKey(Long id);

	void deleteNDaysOldServer(int n);

	List<SearchServer> selectAllHealthyServer();

	List<SearchServer> selectHealthyServersByCluster(Long healthyInterval, String cluster);

	void setTakeTraffic(Long id, Boolean taleTraffic);
}