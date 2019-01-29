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

package com.vip.pallas.console.controller.record;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.service.SearchTemplateHistoryService;
import com.vip.pallas.service.SearchTemplateService;

@RestController
@RequestMapping("/record/index_template")
public class TemplateFindAllController {
	
    @Resource
    private SearchTemplateService templateService;

    @Resource
    private SearchTemplateHistoryService hisService;

    @RequestMapping(path = "/list.json", method = {RequestMethod.GET})
    public List<SearchTemplate> findIndexTemplateLists(@RequestParam Long indexId) {

        List<SearchTemplate> list = templateService.findAllByIndexId(indexId);
        for (SearchTemplate t : list)  {
            t.setHisCount(hisService.count(t.getId()));
        }

        List<SearchTemplate> resultList = new ArrayList<>();
        SearchTemplate template = new SearchTemplate();
        template.setId(-1L);
        template.setTemplateName("全部");
        resultList.add(template);
        resultList.addAll(list);

        return resultList;

    }
}