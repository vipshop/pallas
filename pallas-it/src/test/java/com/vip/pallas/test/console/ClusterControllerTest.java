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

package com.vip.pallas.test.console;

import com.alibaba.fastjson.JSON;
import com.vip.pallas.console.vo.ClusterStateVO;
import com.vip.pallas.console.vo.ClusterVO;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.ResultVO;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.test.base.BaseSpringEsTest;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClusterControllerTest extends BaseSpringEsTest {

	private static String clusterId = null;
	private static ClusterVO clusterVO = null;

	@Test
	public void test1AddCluster() throws Exception{
		clusterVO = insertClusterToTable();
		clusterId = clusterVO.getClusterId();
	}

	@Test
	public void test2Page() throws Exception{
		ResultVO<PageResultVO> resultVO =  callGetApi("/cluster/page.json", PageResultVO.class);
		assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());

		PageResultVO<Cluster> pageResultVO = resultVO.getData();
		assertThat(pageResultVO.getList()).isNotNull();
		assertThat(pageResultVO.getList().size()).isGreaterThanOrEqualTo(1);
		assertThat(pageResultVO.getTotal()).isGreaterThanOrEqualTo(1L);

		resultVO =  callGetApi("/cluster/page.json?clusterId=" + clusterId, PageResultVO.class);
		assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());

		pageResultVO = resultVO.getData();
		assertThat(pageResultVO.getList()).isNotNull();
		assertThat(pageResultVO.getList().size()).isEqualTo(1);
		assertThat(pageResultVO.getTotal()).isEqualTo(1);
		assertThat(pageResultVO.getPageCount()).isEqualTo(1);
	}

	@Test
	public void test3All() throws Exception {
		ResultVO<List> resultVO =  callGetApi("/cluster/all.json", List.class);
		assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());

		List<Cluster> clusters = resultVO.getData();
		assertThat(clusters).isNotNull();
		assertThat(clusters.size()).isGreaterThanOrEqualTo(1);
	}

	@Test
	public void test4AllPhysicals() throws Exception {
		ResultVO<Map> resultVO =  callGetApi("/cluster/all/physicals.json", Map.class);
		assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());

		Map<String, Object> resultMap = resultVO.getData();
		assertThat(resultMap).isNotNull();
		assertThat(resultMap.get("list")).isNotNull();
		assertThat(((List)resultMap.get("list")).size()).isGreaterThanOrEqualTo(1);
	}


	@Test
	public void test5Update() throws Exception {
		clusterVO.setDescription("update es cluster");
		assertThat(callRestApi("/cluster/update.json", JSON.toJSONString(clusterVO))).isNull();
	}


	@Test
	public void test6Stat() throws Exception {
		ResultVO<ClusterStateVO> resultVO = callGetApi("/cluster/state.json?clusterName=" + clusterId, ClusterStateVO.class);
		assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());

		ClusterStateVO stateVO = resultVO.getData();
		assertThat(stateVO).isNotNull();
	}

	@Test
	public void test7ApiController() throws Exception {
		// test
		assertThat(callRestApi("/cluster/abnormal_node/list.json", "{\"clusterName\": \"" + clusterId + "\"}")).isNotNull();
	}

	@AfterClass
	public static void cleanData() throws IOException {
		//delete
		assertThat(callRestApi("/cluster/delete/id.json", "{\"clusterId\": \"" + clusterId + "\"}")).isNull();
	//	assertThat(callRestApi("/cluster/delete/id.json", "{\"clusterId\": \"" + clusterId + "\"}")).containsEntry("status", 500); //再次删除
	}
}