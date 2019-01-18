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

package com.vip.pallas.test.core.open;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.SearchServer;
import com.vip.pallas.mybatis.entity.SearchServerExample;
import com.vip.pallas.mybatis.entity.SearchServerExample.Criteria;
import com.vip.pallas.service.SearchServerService;
import com.vip.pallas.test.base.BaseSpringEsTest;

public class SearchServerServiceImplTest extends BaseSpringEsTest {
	
	@Autowired
	private SearchServerService searchServerService;
	
	static long ID = System.currentTimeMillis();
	static String CLUSTER = ID + "-cluster";
	
	@Test
	public void testCru() throws PallasException {
		SearchServer ss = new SearchServer();
		ss.setId(ID);
		ss.setCluster(CLUSTER);
		ss.setInfo("{}");
		ss.setIpport(ID + "");
		ss.setUpdateTime(new Date());
		
		assertEquals(ss.isHealthy(), false);
		List<SearchServer> ssList = Arrays.asList(ss);
		ssList = searchServerService.markUnHealthyServer(ssList);
		assertEquals(ss.isHealthy(), true);
		ss.setUpdateTime(DateUtils.addSeconds(new Date(), Integer.valueOf(SearchServerService.HEALTHY_UPLOAD_INTERVAL_TOLERANCE * -2 + "")));
		ssList = searchServerService.markUnHealthyServer(ssList);
		assertEquals(ss.isHealthy(), false);
		
		searchServerService.upsertByIpAndCluster(ss);
		SearchServer selectByPrimaryKey = searchServerService.selectByPrimaryKey(ID);
		assertNotNull(selectByPrimaryKey);
		
		List<SearchServer> selectByCluster = searchServerService.selectByCluster(CLUSTER);
		assertEquals(selectByCluster.size(), 1);
		
		
		SearchServerExample example = new SearchServerExample();
		Criteria createCriteria = example.createCriteria();
		createCriteria.andClusterEqualTo(CLUSTER);
		List<SearchServer> selectByExampleWithBLOBs = searchServerService
				.selectByExampleWithBLOBsAndHealthyInterval(example);
		assertEquals(selectByExampleWithBLOBs.size(), 1);
		
		SearchServer searchServerFromDb = selectByExampleWithBLOBs.get(0);
		searchServerFromDb.setTakeTraffic(false);
		searchServerService.upsertByIpAndCluster(searchServerFromDb);

		SearchServer searchServerFromDbAfterUpdate = searchServerService.selectByPrimaryKey(ID);
		assertThat(searchServerFromDbAfterUpdate.getTakeTraffic()).isEqualTo(false);

		long countByExample = searchServerService.countByExample(example);
		assertEquals(countByExample, 1l);
		
		List<String> selectDistictCluster = searchServerService.selectDistictCluster();
    	assertThat(selectDistictCluster.size()).isGreaterThanOrEqualTo(1);
		
    	List<SearchServer> selectHealthyServers = searchServerService.selectHealthyServers(SearchServerService.HEALTHY_UPLOAD_INTERVAL_TOLERANCE);
    	assertThat(selectHealthyServers.size()).isGreaterThanOrEqualTo(1);
    	
	}
	
	@After
	public void deleteOperation() throws PallasException {
		searchServerService.deleteByPrimaryKey(ID);
	}
}