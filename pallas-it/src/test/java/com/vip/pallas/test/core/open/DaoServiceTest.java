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

import com.vip.pallas.mybatis.entity.IndexOperation;
import com.vip.pallas.mybatis.repository.IndexOperationRepository;
import com.vip.pallas.mybatis.repository.SearchServerRepository;
import com.vip.pallas.test.base.BaseSpringEsTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

public class DaoServiceTest extends BaseSpringEsTest {

	@Autowired
    private IndexOperationRepository indexOperationRepository;
    
	@Autowired
    private SearchServerRepository searchServerRepository;

    @Test
    public void testSelectClusterByVersionId() throws Exception{
    	IndexOperation record = new IndexOperation();
    	record.setEventDetail("eventDetail");
    	record.setEventName("eventName");
    	record.setEventType("eventType");
    	record.setOperationTime(new Date());
    	record.setOperator("operator");
		indexOperationRepository.insert(record );
    	System.out.println(record);
    }
    
    @Test
    public void testSS() {
    	searchServerRepository.selectHealthyServers(20l);
    }
}