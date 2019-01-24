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

package com.vip.pallas.test.base;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.vip.pallas.console.ConsoleApplication;
import com.vip.pallas.console.vo.ClusterVO;
import com.vip.pallas.console.vo.IndexVO;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.ResultVO;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.test.utils.ObjectJsonUtils;
import com.vip.pallas.utils.JsonUtil;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "classpath:applicationContext-service-test.xml" })
@SpringBootTest(classes =  ConsoleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Ignore
public class BaseSpringEsTest extends BaseEsTest {
	private static final Logger logger = LoggerFactory.getLogger(BaseSpringEsTest.class);
	
	@Autowired
	private TestRestTemplate template;
	
	private static TestRestTemplate restTemplate;

	public static final String INDEX_NAME = "product_comment";
	public static final String CLUSTER_NAME = "pallas-test-cluster";
	public static final Long VERSION_ID = 1l;
	public static final Long INDEX_ID = 1l;
	public static final String CLUSTER_HTTPADDRESS = "127.0.0.1:9200";

	@PostConstruct
	public void onPostConstruct() {
		restTemplate = this.template;
	}
	
	static {
		System.setProperty("pallas.db.type", "h2");
		System.setProperty("spring.profiles.active", "it");
	}

	protected String getResourceContent(String resourcePath) {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
		try {
			return IOUtils.toString(is, Charsets.UTF_8);
		} catch (Exception e) {
			logger.error("BaseSpringTest.getResourceContent,e:" + e);
		} finally {
			IOUtils.closeQuietly(is);
		}
		return null;
	}
	
	public static ResponseEntity<String> callRestApiAndReturn(String url, HttpHeaders headers, String requestBody) {
		headers.setContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_UTF8_VALUE));
		headers.add("Accept", MediaType.APPLICATION_JSON.toString());
		return restTemplate.postForEntity(url, new HttpEntity<>(requestBody, headers), String.class);
	}

	@SuppressWarnings("rawtypes")
	public static Map callRestApi(String url, String requestBody) throws IOException{
		return JSON.parseObject(callRestApiAndReturnString(url, requestBody), Map.class);
	}
	
	public static String callRestApiAndReturnString(String url, String requestBody) throws IOException{
		HttpHeaders headers = new HttpHeaders();
		ResponseEntity<String> responseEntity = callRestApiAndReturn(url, headers, requestBody);
		return responseEntity.getBody();
	}

	public static Map uploadFile(String url, String filePath) throws IOException{
		FileSystemResource resource = new FileSystemResource(new File(filePath));
		MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
		param.add("file", resource);
		return JSON.parseObject(restTemplate.postForObject(url, param, String.class), Map.class);
	}

	@SuppressWarnings("rawtypes")
	public static Map callGetApi(String url) throws IOException{
		return JSON.parseObject(callGetApiAsString(url), Map.class);
	}

	public static <T> ResultVO<T> callGetApi(String url, Class<T> c) throws IOException {

		String bodyJsonString = callGetApiAsString(url);
		if(StringUtils.isEmpty(bodyJsonString)) {
			return new ResultVO<>();
		}

		ResultVO<T> resultVO = new ResultVO<>();
		JSONObject jsonObject = JSONObject.parseObject(bodyJsonString);
		resultVO.setStatus(jsonObject.getInteger("status"));
		resultVO.setMessage(jsonObject.getString("message"));
		if(StringUtils.isNotBlank(jsonObject.getString("data"))) {
			resultVO.setData(JSON.parseObject(jsonObject.getString("data"), c));
		}

		return resultVO;
	}

	public static String callGetApiAsString(String url) throws IOException {
		ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
		return responseEntity.getBody();
	}

	public static String readFile(String filePath){
		StringBuilder result = new StringBuilder();
		try{
			String s = null;
			BufferedReader br = new BufferedReader(new FileReader(BaseSpringEsTest.class.getResource("/").getPath() + filePath));
			while((s = br.readLine())!=null){
				result.append(System.lineSeparator()+s);
			}
			br.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		return result.toString();
	}

	protected ClusterVO insertClusterToTable()  throws Exception{

		ClusterVO clusterVO = ObjectJsonUtils.getClusterVO();
		Cluster cluster = null;

		Assertions.assertThat(callRestApi("/cluster/add.json", JsonUtil.toJson(clusterVO))).isNull();  //首次插入
		//assertThat(callRestApi("/cluster/add.json", JsonUtil.toJson(clusterVO))).containsEntry("status", 500);  //再次插入

		ResultVO resultVO = callGetApi("/cluster/id.json?clusterId=" + clusterVO.getClusterId(), Cluster.class);
		Assertions.assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());
		cluster = (Cluster)resultVO.getData();
 			Assertions.assertThat(cluster.getId()).isNotNull();

		clusterVO.setId(cluster.getId());
		return clusterVO;
	}

	protected IndexVO insertIndexToTable() throws Exception{
		IndexVO indexVO = ObjectJsonUtils.getIndexVO();
		indexVO.setClusterId(EMBEDDED_CLUTER_ID);

		Assertions.assertThat(callRestApi("/index/add.json", JsonUtil.toJson(indexVO))).isNull();  //首次插入

		ResultVO<PageResultVO> resultVO = callGetApi("/index/page.json?indexName=" + indexVO.getIndexName() + "&clusterId=" + EMBEDDED_CLUTER_ID, PageResultVO.class);
		Assertions.assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());

		PageResultVO<JSONObject> pageResultVO = resultVO.getData();
		Assertions.assertThat(pageResultVO.getPageCount()).isEqualTo(1);
		Assertions.assertThat(pageResultVO.getTotal()).isEqualTo(1L);
		Assertions.assertThat(pageResultVO.getList().size()).isEqualTo(1);

		indexVO.setIndexId(pageResultVO.getList().get(0).getLong("id"));

		return indexVO;
	}
}