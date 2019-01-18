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

import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.SearchTemplateService;
import com.vip.pallas.test.base.BaseSpringEsTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by owen on 6/6/2017.
 */
public class SearchTemplateServiceImplTest extends BaseSpringEsTest {

    private static final String TEST_TEMPLATE_NAME = System.currentTimeMillis() + "_test_template_name";

    private static final Long TEST_INDEX = 1l;
    @Resource
    private SearchTemplateService service;
    
    @Resource
    private ClusterService clusterService;

    @Before
    public void init () throws Exception {
        service.delateByNameAndIndexId(TEST_TEMPLATE_NAME, TEST_INDEX);
        SearchTemplate t = new SearchTemplate();
        t.setTemplateName(TEST_TEMPLATE_NAME);
        t.setIndexId(TEST_INDEX);
        t.setType(SearchTemplate.TYPE_TEMPLATE);
        t.setContent("{\r\n" + 
        		"    \"query\": {\r\n" + 
        		"        \"bool\": {\r\n" + 
        		"            \"filter\": [\r\n" + 
        		"                {\"match_all\":{}}    \r\n" + 
        		"            ]\r\n" + 
        		"        }\r\n" + 
        		"        \r\n" + 
        		"    }\r\n" + 
        		"    \r\n" + 
        		"}");
        service.insert(t);
    }

    @After
    public void close () throws  Exception {
        service.delateByNameAndIndexId(TEST_TEMPLATE_NAME, TEST_INDEX);
    }

    @Test
    public void testFindAllByIndexId () {

        List<SearchTemplate> list = service.findAllByIndexId(TEST_INDEX);
        assertTrue(list.size() > 0);
    }

    @Test
    public void testFindByNameAndIndexId () {
        SearchTemplate t = service.findByNameAndIndexId(TEST_TEMPLATE_NAME, TEST_INDEX);
        assertNotNull(t);
    }

    @Test
    public void testUpdate () throws Exception {
        SearchTemplate t = service.findByNameAndIndexId(TEST_TEMPLATE_NAME, TEST_INDEX);
        String newC = "{\n" +
                "    \"size\":100," +
                "    \"query\": {\n" +
                "        \"match_all\": {}\n" +
                "    }\n" +
                "}";
        t.setContent(newC);
        service.saveTemplate(t);
        service.updateAfterApprove("testUser", "for UT", t);
        t = service.findByNameAndIndexId(TEST_TEMPLATE_NAME, TEST_INDEX);
        assertEquals(newC, t.getContent());

    }

    @Test
    public void testGenAPI () throws Exception {
        SearchTemplate t = service.findByNameAndIndexId(TEST_TEMPLATE_NAME, TEST_INDEX);
        Map<String ,Object> map = new LinkedHashMap<>();
        service.genAPI(t, map);
        assertTrue(map.size() > 0);

    }

    @Test
    public void testMacros () throws Exception {
        String macro = "test_macro";
        service.delateByNameAndIndexId(macro, TEST_INDEX);
        SearchTemplate t = new SearchTemplate();
        t.setTemplateName(macro);
        t.setContent("");
        t.setType(SearchTemplate.TYPE_MACRO);
        t.setIndexId(TEST_INDEX);
        service.insert(t);
        t = service.findByNameAndIndexId(macro, TEST_INDEX);
        t.setContent("{}");
        service.saveTemplate(t);
        service.updateAfterApprove("", "", t);
        t = service.findByNameAndIndexId(macro, TEST_INDEX);
        assertEquals("{}", t.getContent());
        service.delateByNameAndIndexId(macro, TEST_INDEX);
    }

    @Test
    public void testImport () throws Exception {
        SearchTemplate t1 = new SearchTemplate();
        t1.setTemplateName("t1");
        t1.setContent("");
        t1.setType(SearchTemplate.TYPE_TEMPLATE);
        t1.setIndexId(TEST_INDEX);

        SearchTemplate t2 = new SearchTemplate();
        t2.setTemplateName("t2");
        t2.setContent("");
        t2.setType(SearchTemplate.TYPE_MACRO);
        t2.setIndexId(TEST_INDEX);

        List<SearchTemplate> list = new LinkedList<>();
        list.add(t1);
        list.add(t2);

        service.importTemplates("Unknown", "test import template", list);

        t1 = service.findByNameAndIndexId("t1", TEST_INDEX);
        assertEquals("", t1.getContent());

        service.delateByNameAndIndexId("t1", TEST_INDEX);
        service.delateByNameAndIndexId("t2", TEST_INDEX);

    }
    
    @Test
    public void testRetryTimeoutConfig () throws Exception {
    	assertNotNull(service.findAllRetryTimeOutConfig());
    }

}