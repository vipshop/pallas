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

package com.vip.pallas.mybatis.repository;
import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.mybatis.entity.SearchTemplateHistory;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Created by Owen.LI on 4/5/2017.
 */
@Repository
public interface SearchTemplateHistoryRepository {

    int insert(SearchTemplateHistory history);

    int deleteByPrimaryKey(Long id);

    SearchTemplateHistory selectByPrimaryKey(Long id);

    List<SearchTemplateHistory> selectAllByTemplateId(Long tempalteId);

    int getCountByTemplateId (Long templateId);

    //获取模板最后上线模板
    SearchTemplateHistory getLastOnlineById(Long templateId);
}