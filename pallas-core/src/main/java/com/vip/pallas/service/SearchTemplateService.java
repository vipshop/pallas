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

package com.vip.pallas.service;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.vip.pallas.bean.TemplateImport;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.Approve;
import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.mybatis.entity.TemplateWithTimeoutRetry;

/**
 * Created by Owen.LI on 5/5/2017.
 */
public interface SearchTemplateService {

    List<SearchTemplate> findAllByIndexId(Long indexId);

    List<SearchTemplate> findAllByIndexIdAndTemplateIds(Long indexId, Long[] templateIds);

    List<SearchTemplate> findAllMacroByTemplateId(Long templateId);

    List<SearchTemplate> findAllTemplateByMacroId(Long templateId);

    SearchTemplate findByNameAndIndexId(String templateName, Long indexId);

    int insert(SearchTemplate template) throws Exception;

    int saveTemplate(SearchTemplate template) throws Exception;

    SearchTemplate findById(Long templateId);

    int updateAfterApprove(String user, String historyDesc, SearchTemplate template) throws Exception;

    String submitTemplate(Long templateId, String templateNameSuffix) throws Exception;

    void deleteTemplate(SearchTemplate template, String templateName) throws Exception;

    String inlineDebug(SearchTemplate template, boolean renderOnly, Long clusterId) throws Exception;

    void delateByNameAndIndexId(String templateName, Long indexId) throws Exception;

    void genAPI(SearchTemplate dbEntity, Map<String, Object> apiMap);

    Map<String, Object> genParams(SearchTemplate dbEntity);

    void importTemplates(String user, String updateDesc, List<SearchTemplate> list) throws Exception;

    List<String> importTemplatesFromOtherIndex(String user, TemplateImport templateImport) throws Exception;

    void insertOrUpdateTemplate(SearchTemplate template) throws Exception;

    Approve submitToApprove(String user, String historyDesc, Long templateId) throws Exception;

	List<TemplateWithTimeoutRetry> findAllRetryTimeOutConfig();

	String parseSql(String sql, Long clusterId);

	List<Map<String, Object>> executeSql(Long indexId, String sql, Long dsId)
			throws SQLException, PallasException, InstantiationException, IllegalAccessException;
}