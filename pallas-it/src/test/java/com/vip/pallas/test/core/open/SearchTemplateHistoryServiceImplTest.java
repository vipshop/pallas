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
import com.vip.pallas.mybatis.entity.SearchTemplateHistory;
import com.vip.pallas.service.SearchTemplateHistoryService;
import com.vip.pallas.service.SearchTemplateService;
import com.vip.pallas.service.impl.SearchTemplateHistoryServiceImpl;
import com.vip.pallas.test.base.BaseSpringEsTest;
import org.junit.After;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by owen on 6/6/2017.
 */
public class SearchTemplateHistoryServiceImplTest extends BaseSpringEsTest {

    private static final String TEST_TEMPLATE_NAME = System.currentTimeMillis() + "_template_history_name";

    private static final Long TEST_INDEX = 1l;
    @Resource
    private SearchTemplateService service;

    @Resource
    private SearchTemplateHistoryService hisService;

    public SearchTemplate init () throws Exception {

        SearchTemplateHistoryServiceImpl.MAX_STORE_SIZE = 1;

        service.delateByNameAndIndexId(TEST_TEMPLATE_NAME, TEST_INDEX);
        SearchTemplate t = new SearchTemplate();
        t.setTemplateName(TEST_TEMPLATE_NAME);
        t.setIndexId(TEST_INDEX);
        t.setType(SearchTemplate.TYPE_TEMPLATE);
        t.setContent("{\n" +
                "    \"from\": {{from}}{{^from}}0{{/from}},\n" +
                "    \"size\": {{size}}{{^size}}100{{/size}},\n" +
                "    {{#sort}}\n" +
                "    \"sort\": {{#toJson}}sort.list{{/toJson}},\n" +
                "    {{/sort}}\n" +
                "    {{#search_after}}\n" +
                "    \"search_after\":[\"{{search_after}}\"],\n" +
                "    {{/search_after}}\n" +
                "    \"query\": {\n" +
                "      \"bool\": {\n" +
                "        \"filter\": [\n" +
                "          {\n" +
                "            \"match_all\":{}\n" +
                "          }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  }");
        service.insert(t);

        return service.findByNameAndIndexId(TEST_TEMPLATE_NAME, TEST_INDEX);

    }

    @After
    public void close() throws Exception {
        SearchTemplate t = service.findByNameAndIndexId(TEST_TEMPLATE_NAME, TEST_INDEX);
        if (t != null) {
            service.delateByNameAndIndexId(TEST_TEMPLATE_NAME, TEST_INDEX);
            hisService.findAllByTemplateId(t.getId()).forEach(
                    h -> hisService.delete(h.getId())
            );
        }
    }

    @Test
    public void testDeleteHistoryAfterInsert () throws Exception {

        SearchTemplate t = init();

        hisService.insert("UT_USER", "for UT 1", t);
        hisService.insert("UT_USER", "for UT 2", t);

        List<SearchTemplateHistory> list = hisService.findAllByTemplateId(t.getId());
        assertEquals(1, list.size());
        assertEquals("for UT 2", hisService.findById(list.get(0).getId()).getDescription());

    }


}