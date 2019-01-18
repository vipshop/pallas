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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.vip.pallas.bean.IndexOperationEventName;
import com.vip.pallas.bean.IndexOperationEventType;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.IndexOperation;
import com.vip.pallas.mybatis.entity.IndexOperationExample;
import com.vip.pallas.mybatis.entity.IndexOperationExample.Criteria;
import com.vip.pallas.service.IndexOperationService;
import com.vip.pallas.test.base.BaseSpringEsTest;

public class IndexOperationServiceImplTest extends BaseSpringEsTest {
	
	@Autowired
	private IndexOperationService indexOperationService;
	
	private static final Long INDEX_ID = 1l;
	private static Long recordId = 0l;
	private static Integer versionId = 1;
	
	@Test
	public void testCrud() throws PallasException {
		Date operationTime = new Date();
		IndexOperation record = new IndexOperation();
		record.setEventDetail("ut test.");
		record.setEventName(IndexOperationEventName.DELETE_INDEX);
		record.setEventType(IndexOperationEventType.INDEX_EVENT);
		record.setOperationTime(operationTime);
		record.setVersionId(Long.valueOf(versionId));
		record.setIndexId(INDEX_ID);
		
		// insert
		indexOperationService.insert(record);
		Assert.assertNotNull(record.getId());
		recordId = record.getId();
		
		// find
		IndexOperationExample example = new IndexOperationExample();
		Criteria criteria = example.createCriteria();  
		criteria.andIndexIdEqualTo(INDEX_ID);
		criteria.andEventTypeEqualTo(IndexOperationEventType.INDEX_EVENT);
		criteria.andVersionIdEqualTo(versionId);
		Date start = DateUtils.addSeconds(operationTime, -5);
		Date end = DateUtils.addSeconds(operationTime, 5);
		criteria.andOperationTimeBetween(start, end);
		example.setOffset(0);
		example.setLimit(10);
		example.setOrderByClause(" operation_time desc ");
		
		long total = indexOperationService.countByExample(example);
		Assert.assertEquals(total, 1l);
		
		List<IndexOperation> list = indexOperationService.selectByExampleWithBLOBs(example);
		assertThat(list.size()).isEqualTo(1);
	}
	
	@After
	public void deleteOperation() throws PallasException {
			indexOperationService.deleteByPrimaryKey(recordId);
	}
}