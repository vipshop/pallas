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

package com.vip.pallas.service.impl;

import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.mybatis.entity.SearchTemplateHistory;
import com.vip.pallas.mybatis.repository.SearchTemplateHistoryRepository;
import com.vip.pallas.service.SearchTemplateHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * Created by owen on 13/6/2017.
 */
@Service
@Transactional(rollbackFor=Exception.class)
public class SearchTemplateHistoryServiceImpl implements SearchTemplateHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(SearchTemplateHistoryServiceImpl.class);

    public static int MAX_STORE_SIZE = 50;

    @Resource
    private SearchTemplateHistoryRepository repository;

    @Override
    public List<SearchTemplateHistory> findAllByTemplateId(Long templateId) {
        return repository.selectAllByTemplateId(templateId);
    }

    @Override
    public SearchTemplateHistory findById(Long id) {
        return repository.selectByPrimaryKey(id);
    }

    @Override
    public int insert(String user, String historyDesc, SearchTemplate template) throws Exception {
        SearchTemplateHistory history = new SearchTemplateHistory();
        history.setContent(template.getContent());
        history.setCreatedTime(new Date());
        history.setCreator(user);
        history.setDescription(historyDesc);
        history.setParams(template.getParams());
        history.setTemplateId(template.getId());
        repository.insert(history);

        if (count(template.getId()) > MAX_STORE_SIZE) {
            logger.info("history count of " + template.getTemplateName() + " exceeds MAX_STORE_SIZE:" + MAX_STORE_SIZE + ", will be truncated");
            List<SearchTemplateHistory> list = findAllByTemplateId(template.getId());
            for (int i = MAX_STORE_SIZE; i < list.size(); i++) {
                delete(list.get(i).getId());
            }
        }

        return 0;
    }

    @Override
    public int count(Long templateId) {
        return repository.getCountByTemplateId(templateId);
    }

    @Override
    public int delete(Long id) {
        return repository.deleteByPrimaryKey(id);
    }

    @Override
    public SearchTemplateHistory findLastOnlineById(Long templateId) {
        return repository.getLastOnlineById(templateId);
    }
}