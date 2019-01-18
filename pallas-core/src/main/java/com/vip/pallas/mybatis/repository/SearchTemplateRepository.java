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
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.mybatis.entity.TemplateWithTimeoutRetry;

/**
 * Created by Owen.LI on 4/5/2017.
 */
@Repository
public interface SearchTemplateRepository {

    int deleteByPrimaryKey(Long id);

    int deleteByNameAndIndexId(String templateName, Long indexId);

    int insert(SearchTemplate record);

    SearchTemplate selectByPrimaryKey(Long id);

    SearchTemplate selectByNameAndIndexId(String templateName, Long indexId);

    int updateByPrimaryKey(SearchTemplate record);

    List<SearchTemplate> selectAllByIndexId(Long indexId);

    List<SearchTemplate> selectByIndexIdAndTemplateIds(Map<String, Object> params);

	List<TemplateWithTimeoutRetry> findAllRetryTimeOutConfig();
}