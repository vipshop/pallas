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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.vip.pallas.service.ElasticSearchService;
import com.vip.pallas.test.base.BaseSpringEsTest;

public class ElasticSearchServiceImplTest extends BaseSpringEsTest {
	
	@Autowired
	private ElasticSearchService elasticSearchService;
	
	@Test
	public void testInterfaces() throws Exception {
		Long dataCount = elasticSearchService.getDataCount(INDEX_NAME, CLUSTER_HTTPADDRESS, VERSION_ID);
		assertThat(dataCount).isGreaterThanOrEqualTo(0l);
		
		String mapping = elasticSearchService.genMappingJsonByVersionIdAndClusterName(VERSION_ID, CLUSTER_NAME);
		assertThat(mapping).isNotNull();
		System.out.println(mapping);

		boolean result = elasticSearchService.excludeOneNode("127.0.0.1:9200", "127.0.0.1");
		assertTrue(result);

		result = elasticSearchService.includeOneNode("127.0.0.1:9200", "127.0.0.1");
		assertTrue(result);

		List<String> avalableNodeIps = elasticSearchService.getAvalableNodeIps("127.0.0.1:9200");
		assertTrue(avalableNodeIps.size() > 0);

	}
	
	@Test
	public void testDeleteByQuery() {
		System.out.println(elasticSearchService.cancelDeleteByQueryTask(VERSION_ID, "big_desk_103653"));
	}

}