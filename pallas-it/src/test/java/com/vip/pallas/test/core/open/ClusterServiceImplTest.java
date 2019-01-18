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
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.test.base.BaseSpringEsTest;

public class ClusterServiceImplTest extends BaseSpringEsTest {
	
	@Autowired
	private ClusterService clusterService;
	
	private static final String CLUSTER_NAME = "in-" + System.currentTimeMillis();

	@Test
	public void testCrud() throws Exception {
		Cluster c = new Cluster();
		c.setClusterId(CLUSTER_NAME);
		c.setClientAddress("127.0.0.1:9300");
		c.setHttpAddress("http://127.0.0.1:9200");
		
		// insert
		clusterService.insert(c);
		
		// query
		Cluster cFromDb = clusterService.findByName(CLUSTER_NAME);
		assertNotNull(cFromDb);
		
		// update
		cFromDb.setDescription("two");
		clusterService.update(cFromDb);
		
		Cluster cFromDbAfterUpdate = clusterService.findByName(CLUSTER_NAME);
		assertEquals("two", cFromDbAfterUpdate.getDescription());
		
		// findAll
		List<Cluster> findAll = clusterService.findAll();
		assertThat(findAll.size()).isGreaterThan(0);
		
		// findPage
		Page<Cluster> page = new Page<>();
		page.setPageNo(0);
		page.setPageSize(10);
		List<Cluster> findPage = clusterService.findPage(page, CLUSTER_NAME);
		assertThat(findPage.size()).isEqualTo(1);
		
		
		// delete 
		clusterService.deleteByClusterId(CLUSTER_NAME);
		Cluster indexFromDbAfterDelete = clusterService.findByName(CLUSTER_NAME);
		assertNull(indexFromDbAfterDelete);
	}
	
	@After
	public void deleteIndex() throws Exception {
		Cluster cFromDb = clusterService.findByName(CLUSTER_NAME);
		if (cFromDb != null) {
			clusterService.deleteByClusterId(CLUSTER_NAME);
		}
	}

	@Test
	public void testSelectAllPhysicalClusters(){
		List<Cluster> selectAllPhysicalClusters = clusterService.selectAllPhysicalClusters();
		assertThat(selectAllPhysicalClusters.size()).isGreaterThan(0);
	}
	
	@Test
	public void testSelectAllPhysicalClustersByIndexId(){
		List<Cluster> selectAllPhysicalClusters = clusterService.selectPhysicalClustersByIndexId(1L);
		assertThat(selectAllPhysicalClusters.size()).isGreaterThan(0);
	}
	
}