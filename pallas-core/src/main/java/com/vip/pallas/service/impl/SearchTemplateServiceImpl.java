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

import static com.vip.pallas.mybatis.entity.SearchTemplate.MACRO_END_FLAG;
import static com.vip.pallas.mybatis.entity.SearchTemplate.MACRO_START_FLAG;
import static com.vip.pallas.mybatis.entity.SearchTemplate.TYPE_MACRO;
import static com.vip.pallas.mybatis.entity.SearchTemplate.TYPE_TEMPLATE;
import static com.vip.pallas.utils.TemplateParamsExtractUtil.getParams;
import static com.vip.pallas.utils.TemplateParamsExtractUtil.renderESTemplateBody;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import com.google.gson.Gson;
import com.vip.pallas.mybatis.entity.*;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.entity.ContentType;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.query.SqlElasticRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.vip.pallas.bean.ApproveState;
import com.vip.pallas.bean.ApproveType;
import com.vip.pallas.bean.TemplateImport;
import com.vip.pallas.bean.TemplateInfo;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.repository.DataSourceRepository;
import com.vip.pallas.mybatis.repository.SearchTemplateHistoryRepository;
import com.vip.pallas.mybatis.repository.SearchTemplateRepository;
import com.vip.pallas.service.ApproveService;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.IndexService;
import com.vip.pallas.service.SearchTemplateHistoryService;
import com.vip.pallas.service.SearchTemplateService;
import com.vip.pallas.utils.ElasticRestClient;
import com.vip.pallas.utils.JdbcUtil;
import com.vip.pallas.utils.TemplateParamsExtractUtil;

/**
 * Created by Owen.li on 5/5/2017.
 */
@Service
@Transactional(rollbackFor=Exception.class)
public class SearchTemplateServiceImpl implements SearchTemplateService {

    private static final Logger logger = LoggerFactory.getLogger(SearchTemplateServiceImpl.class);

    public static final Pattern PATTERN = Pattern.compile(MACRO_START_FLAG + "([\\w]*)" + MACRO_END_FLAG);

    @Resource
    private SearchTemplateRepository repository;

    @Resource
    private SearchTemplateHistoryRepository historyRepository;

	@Resource
	private DataSourceRepository dataSourceRepository;

    @Resource
    private SearchTemplateHistoryService historySerivce;

    @Resource
    private ClusterService clusterService;

    @Resource
    private IndexService indexService;

    @Resource
    private ApproveService approveService;

    @Override
    public List<SearchTemplate> findAllByIndexId(Long indexId) {
        List<SearchTemplate> searchTemplateList = repository.selectAllByIndexId(indexId);
        if(searchTemplateList != null){
            for (SearchTemplate template : searchTemplateList) {
                SearchTemplateHistory templateHistory = historyRepository.getLastOnlineById(template.getId());

                if(templateHistory != null){
                    Date lastUpdateTime = templateHistory.getCreatedTime();
                    Date draftTime = template.getUpdateTime();

                    if(lastUpdateTime != null && draftTime != null){
                        template.setNewer(draftTime.compareTo(lastUpdateTime) > 0);
                    }
                } else {
                    template.setNewer(true);
                }

                if(approveService.isInApprove(template.getId())){
                    template.setApproving(true);
                }
            }
        }
        return searchTemplateList;
    }

    @Override
    public List<SearchTemplate> findAllByIndexIdAndTemplateIds(Long indexId, Long[] templateIds) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("indexId", indexId);
        params.put("templateIds", templateIds);
        return repository.selectByIndexIdAndTemplateIds(params);
    }

    @Override
    public List<SearchTemplate> findAllMacroByTemplateId(Long templateId) {
        SearchTemplate searchTemplate = findById(templateId);

        if(searchTemplate.getType() == TYPE_TEMPLATE){
            String content = searchTemplate.getContent();

            if(content != null){
                Matcher matchers = PATTERN.matcher(content);
                List<SearchTemplate> allFiles = repository.selectAllByIndexId(searchTemplate.getIndexId());

                if(allFiles != null){
                    List<SearchTemplate> searchTemplateList = new ArrayList<>();

                    while (matchers.find()) {
                        String flag = matchers.group(1);
                        searchTemplateList.addAll(allFiles.stream()
                                .filter((SearchTemplate x) -> x.getType() == TYPE_MACRO)
                                .filter((SearchTemplate x) -> x.getTemplateName().equals(flag))
                                .collect(toList()));
                    }

                    return searchTemplateList;
                }
            }
        }

        return null;
    }

    @Override
    public List<SearchTemplate> findAllTemplateByMacroId(Long templateId) {
        SearchTemplate searchTemplate = findById(templateId);

        if(searchTemplate.getType() == TYPE_MACRO){
            String flag = MACRO_START_FLAG + searchTemplate.getTemplateName() + MACRO_END_FLAG;
            List<SearchTemplate> allFiles = repository.selectAllByIndexId(searchTemplate.getIndexId());

            return allFiles.stream()
                    .filter((SearchTemplate x) -> x.getType() == TYPE_TEMPLATE)
                    .filter((SearchTemplate x) -> x.getContent() != null && x.getContent().contains(flag))
                    .collect(toList());

        }

        return null;
    }

    @Override
    public SearchTemplate findByNameAndIndexId(String templateName, Long indexId) {
        return repository.selectByNameAndIndexId(templateName, indexId);
    }

    @Override
    public int insert(SearchTemplate template) throws Exception {
        preCheck(template);

        int result = repository.insert(template);

        Index index = indexService.findById(template.getIndexId());

        if(template.getType() == TYPE_TEMPLATE) {
            List<SearchTemplate> allFiles = findAllByIndexId(template.getIndexId());
            updateESSearchTemplate(index.getIndexName() + "_" + template.getTemplateName(), template, allFiles);
        }
        return result;
    }

    @Override
    public int saveTemplate(SearchTemplate template) throws Exception {
        //检查并更新数据库
        SearchTemplate dbEntity = repository.selectByNameAndIndexId(template.getTemplateName(), template.getIndexId());
        if(dbEntity == null) {
            return 1;
        }
        if(dbEntity.getType() != template.getType()) {
            return 1;
        }

        Date nowDate = new Date();

        if (template.getType() == TYPE_MACRO) {
            //同时更新使用了该宏的所有模板
            String flag = getMacroFlag(template.getTemplateName());
            List<SearchTemplate> allFiles = findAllByIndexId(template.getIndexId());
            List<SearchTemplate> subFiles = allFiles.stream()
                    .filter((SearchTemplate x) -> x.getType() == TYPE_TEMPLATE)
                    .filter((SearchTemplate x) -> x.getContent() != null && x.getContent().contains(flag))
                    .collect(toList());
            for(SearchTemplate _template : subFiles) {
                _template.setUpdateTime(nowDate);
                repository.updateByPrimaryKey(_template);
            }
        }

        template.setUpdateTime(nowDate);
        return repository.updateByPrimaryKey(template);
    }

    @Override
    public SearchTemplate findById(Long templateId) {
        SearchTemplate searchTemplate = repository.selectByPrimaryKey(templateId);
        if(null == searchTemplate) {
            return searchTemplate;
        }
        List<SearchTemplateHistory> histories = historyRepository.selectAllByTemplateId(templateId);
        if(CollectionUtils.isEmpty(histories)) {
            searchTemplate.setLastContent("");
        } else {
            searchTemplate.setLastContent(histories.get(0).getContent());
        }
        return searchTemplate;
    }

    private void preCheck(SearchTemplate t) {
        Index index = indexService.findById(t.getIndexId());
        if(index == null) {
            throw new IllegalArgumentException("索引不存在");
        }
        Cluster cluster = clusterService.findByName(index.getClusterName());
        if (cluster == null) {
            throw new IllegalArgumentException("集群不存在");
        }
    }

    @Override
    public void delateByNameAndIndexId(String templateName, Long indexId) throws Exception {
        SearchTemplate template = repository.selectByNameAndIndexId(templateName, indexId);
        if(template != null) {
            checkAndDeleteESSearchTemplate(template);
            repository.deleteByNameAndIndexId(templateName, indexId);
        }
    }

    @Override
    public int updateAfterApprove(String user, String historyDesc, SearchTemplate template) throws Exception {
        if (template.getType() == TYPE_MACRO) {
            //同时更新使用了该宏的所有模板
            String flag = getMacroFlag(template.getTemplateName());
            List<SearchTemplate> allFiles = findAllByIndexId(template.getIndexId());
            List<SearchTemplate> subFiles = allFiles.stream()
                    .filter((SearchTemplate x) -> x.getType() == TYPE_TEMPLATE)
                    .filter((SearchTemplate x) -> x.getContent() != null && x.getContent().contains(flag))
                    .collect(toList());
            for(SearchTemplate t : subFiles) {
                Index index = indexService.findById(t.getIndexId());
                updateESSearchTemplate(index.getIndexName() + "_" + t.getTemplateName(), t, allFiles);
                historySerivce.insert(user, historyDesc, t);
            }
        } else {
            List<SearchTemplate> allFiles = findAllByIndexId(template.getIndexId());
            Index index = indexService.findById(template.getIndexId());
            updateESSearchTemplate(index.getIndexName() + "_" + template.getTemplateName(), template, allFiles);
        }

        return historySerivce.insert(user, historyDesc, template);
    }

    @Override
    public String submitTemplate(Long templateId, String templateNameSuffix) throws Exception {
        SearchTemplate template = this.findById(templateId);

        Index index = indexService.findById(template.getIndexId());

        String templateName = index.getIndexName() + "_" + template.getTemplateName() + "_" + templateNameSuffix;

        updateESSearchTemplate(templateName, template, findAllByIndexId(template.getIndexId()));

        return templateName;
    }

    @Override
    public void deleteTemplate(SearchTemplate t, String templateName) throws Exception {
        if(t.getType() != TYPE_TEMPLATE) {
            return;
        }

        List<Cluster> allPhysicalClustersByIndexId = clusterService.selectPhysicalClustersByIndexId(t.getIndexId());
        for (Cluster cluster : allPhysicalClustersByIndexId) {
            sendESRequest(cluster,
                    "DELETE",
                    "/_search/template/" + templateName,
                    null, false);
        }
    }

    @Override
    public void genAPI(SearchTemplate dbEntity, Map<String, Object> apiMap) {
        preCheck(dbEntity);
        Index index = indexService.findById(dbEntity.getIndexId());
        Cluster cluster = clusterService.findByName(index.getClusterName());
        String address = cluster.getHttpAddress();
        String port = address.contains(":") ? address.substring(address.lastIndexOf(':')) : ":9200";
        apiMap.put("http_address", cluster.getClusterId() + port);
        apiMap.put("transport_address", cluster.getClientAddress());
        apiMap.put("rest_client", getRestClientDemo(index,dbEntity) );
		apiMap.put("path", "/" + index.getIndexName() + "/_search/template");
        Map<String, Object> conMap = new HashMap<>();

        apiMap.put("content", conMap);
        conMap.put("id", index.getIndexName() + "_" + dbEntity.getTemplateName());
        conMap.put("params", genParams(dbEntity));

    }
    public String getRestClientDemo(Index index,SearchTemplate dbEntity){
        StringBuilder demo=new StringBuilder();
        demo.append("String token=\"replace token here\";\n");
        demo.append("final PallasRestClient buildClient = PallasRestClientBuilder.buildClient(token, 2000);\n");
        demo.append("final HttpEntity entity = new NStringEntity(\"{\\n\" +\n");
        demo.append("    \"    \\\"id\\\" : \\\""+index.getIndexName() + "_" + dbEntity.getTemplateName()+"\\\",\\n\" +\n");

        Map<String,Object> params=genParams(dbEntity);
        if (params.size()>0){
            demo.append("    \"    \\\"params\\\" : {\\n\" +\n");
            String comma="";
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                demo.append(comma+"    \"        \\\""+entry.getKey()+"\\\": "+new Gson().toJson(entry.getValue()).replace("\"","\\\""));
                comma=",\\n\" +\n";
            }
            demo.append("\\n\" +\n");
            demo.append("    \"    }\\n\" +\n");
        }else {
            demo.append("    \"    \\\"params\\\" : {}\" +\n");
        }
        demo.append("    \"}\", ContentType.APPLICATION_JSON);\n");
        demo.append("Response response = buildClient.performRequest(\"POST\",\"/" + index.getIndexName() + "/_search/template\", Collections.EMPTY_MAP, \""+index.getIndexName() + "_" + dbEntity.getTemplateName()+"\",entity);\n");

        return demo.toString();
    }

    @Override
    public Map<String, Object> genParams(SearchTemplate dbEntity) {
        preCheck(dbEntity);
        String content = dbEntity.getContent();
        if (content  == null) {
            content = "";
        }
        return getParams(content, this.findAllByIndexId(dbEntity.getIndexId()));
    }

    @Override
    public String inlineDebug(SearchTemplate t, boolean renderOnly, boolean profile, Long clusterId) throws Exception {
        preCheck(t);
        Index index = indexService.findById(t.getIndexId());

        List<SearchTemplate> allFiles = findAllByIndexId(t.getIndexId());
        String content = "\"" + TemplateParamsExtractUtil.renderMacrosThenFormat(t.getContent(), allFiles) + "\"";

        StringBuilder sb = new StringBuilder()
                .append("{\n" + (profile ? "\"profile\": true,\n" : "") + "\"inline\":")
                .append(content)
                .append(",\n\"params\":")
                .append(t.getParams())
                .append("\n}");
        Cluster cluster = clusterService.selectByPrimaryKey(clusterId);
        if (renderOnly) {
            return sendESRequest(cluster, "POST", "/_render/template", sb.toString(), false);
        } else {
            return sendESRequest(cluster, "POST", "/" + index.getIndexName() + "/_search/template", sb.toString(), false);
        }
    }

    @Override
    public void importTemplates(String user, String updateDesc, List<SearchTemplate> list) throws Exception {
    	for (SearchTemplate t : list) {
            if (t.getType() == TYPE_MACRO) {
                insertOrUpdateTemplate(t);
            }
        }

        for (SearchTemplate t : list) {//NOSONAR 必须先循环完MACRO 再循环TEMPLTE
            if (t.getType() == TYPE_TEMPLATE) {
                insertOrUpdateTemplate(t);
                submitToApprove(user, updateDesc, t.getId());
            }
        }
    }

    @Override
    public List<String> importTemplatesFromOtherIndex(String user, TemplateImport templateImport) throws Exception{
        List<String> result = new ArrayList<>();
        List<SearchTemplate> importSearchTempaltes = new ArrayList<>();

        if(null != templateImport && !CollectionUtils.isEmpty(templateImport.getTemplateInfos())) {
            List<TemplateInfo> templateInfos = templateImport.getTemplateInfos();
            for(TemplateInfo templateInfo : templateInfos) {
                SearchTemplate searchTemplate = repository.selectByPrimaryKey(templateInfo.getId());
                if(null == searchTemplate) {
                    result.add(templateInfo.getTemplateName());
                    continue;
                }
                searchTemplate.setId(null);
                searchTemplate.setIndexId(templateImport.getIndexId());
                searchTemplate.setRetry(0);
                searchTemplate.setTimeout(0);
                importSearchTempaltes.add(searchTemplate);

            }

            if(CollectionUtils.isEmpty(importSearchTempaltes)) {
                return result;
            }

            importTemplates(user, "import from other index", importSearchTempaltes);

        }
        return result;
    }

    @Override
    public void insertOrUpdateTemplate(SearchTemplate template) throws Exception {
        SearchTemplate currentTemplate = findByNameAndIndexId(template.getTemplateName(), template.getIndexId());

        if(currentTemplate != null){
            Long templateId = currentTemplate.getId();

            if(approveService.isInApprove(templateId)){
                throw new PallasException("模板【" + template.getTemplateName() + "】已有变更内容等待审批，请等待审批流程结束再发起变更！");
            }

            template.setId(templateId);
            template.setUpdateTime(new Date());
            repository.updateByPrimaryKey(template);
        }else{
            template.setUpdateTime(new Date());
            repository.insert(template);
        }
    }

    @Override
    public Approve submitToApprove(String user, String historyDesc, Long templateId) throws Exception {
        Approve approve = new Approve();
        approve.setApproveType((byte) ApproveType.TEMPLATE.getValue());
        approve.setApproveState((byte) ApproveState.PENDING_APPROVE.getValue());
        approve.setApplyUser(user);
        Date date = new Date();
        approve.setApplyTime(date);
        approve.setCreateTime(date);
        approve.setRelateId(templateId);
        approve.setTitle(historyDesc);
        approve.setNote(historyDesc);
        approve.setIsDeleted(Boolean.FALSE);

        approve.setApproveUser("");
        approve.setApproveOpinion("");
        approveService.insert(approve);

        return approve;
    }

    private String getMacroFlag(String templateId) {
        return MACRO_START_FLAG + templateId + MACRO_END_FLAG;
    }


    private void updateESSearchTemplate(String templateName, SearchTemplate t, List<SearchTemplate> allFiles) throws IOException {
        if(t.getType() != TYPE_TEMPLATE) {
            return;
        }
        //更新模板
        String content = t.getContent();

        if (!StringUtils.isEmpty(content)) {
            List<Cluster> allPhysicalClustersByIndexId = clusterService.selectPhysicalClustersByIndexId(t.getIndexId());
            for (Cluster cluster : allPhysicalClustersByIndexId) {
	            sendESRequest(cluster,
	                    "POST",
	                    "/_search/template/" + templateName,
	                    renderESTemplateBody(content, allFiles), true);
            }
        }
    }

    private String sendESRequest(Cluster cluster, String method, String reqPath, String reqBody, boolean needThrowESException) throws IOException {
        RestClient client = ElasticRestClient.build(cluster.getHttpAddress());
        Response response;
        try {
            if(reqBody != null) {
                NStringEntity entity = new NStringEntity(reqBody, ContentType.APPLICATION_JSON);
                if (reqBody.startsWith("{\n\"inline\":")) {
                    Header header = new BasicHeader("pallas-debug", "true");
                    response = client.performRequest(method, reqPath, Collections.emptyMap(), entity, header);
                } else {
                    response = client.performRequest(method, reqPath, Collections.emptyMap(), entity);
                }
            } else {
                response = client.performRequest(method, reqPath);
            }
        } catch (ResponseException re) {
            logger.error("error", re);
            response = re.getResponse();
            if(needThrowESException) {
                throw re;
            }
        } catch (IOException e) {
            logger.error("error", e);
            throw e;
        }
        return IOUtils.toString(response.getEntity().getContent());
    }

    private void checkAndDeleteESSearchTemplate(SearchTemplate t) throws IOException {
        if(t != null) {
            if(t.getType() == TYPE_MACRO) {
                List<SearchTemplate> allFiles = findAllByIndexId(t.getIndexId());
                boolean isMacroUsing = allFiles.stream()
                        .filter((SearchTemplate x) -> x.getType() == TYPE_TEMPLATE)
                        .anyMatch((SearchTemplate x) -> x.getContent().contains(getMacroFlag(t.getTemplateName())));
                if(isMacroUsing) {
                    throw new IllegalArgumentException("不允许删除正在使用的宏");
                }
            } else {
                try{
                    Index index = indexService.findById(t.getIndexId());
                    List<Cluster> allPhysicalClustersByIndexId = clusterService.selectPhysicalClustersByIndexId(t.getIndexId());
                    for (Cluster cluster : allPhysicalClustersByIndexId) {
	                    sendESRequest(cluster,
	                            "DELETE",
	                            "/_search/template/" + index.getIndexName() + "_" + t.getTemplateName(),
	                            null, false);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

            }
        }
    }

	@Override
	public List<TemplateWithTimeoutRetry> findAllRetryTimeOutConfig() {
		return repository.findAllRetryTimeOutConfig();
	}

	@Override
	public List<TemplateWithThrottling> findAllThrottlingConfig() {
		return repository.findAllThrottlingConfig();
	}

	@Override
	public String parseSql(String sql, Long clusterId) {
		try {
			Cluster cluster = clusterService.selectByPrimaryKey(clusterId);
			SearchDao searchDao = new SearchDao(ElasticRestClient.buildNative(cluster.getClientAddress()));
			SqlElasticRequestBuilder requestBuilder = searchDao.explain(sql).explain();
			return requestBuilder.explain();
		} catch (Exception e) {
			return e.getMessage();
		}
	}

	@Override
	public List<Map<String, Object>> executeSql(Long indexId, String sql, Long dsId)
			throws SQLException, PallasException, InstantiationException, IllegalAccessException {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		List<DataSource> dataSourceList = dataSourceRepository.selectByIndexId(indexId);

		if (dataSourceList == null || dataSourceList.size() == 0) {
			return Collections.emptyList();
		}
		DataSource dataSource = null;
		for (DataSource ds : dataSourceList) {
			if (ds.getId().equals(dsId)) {
				dataSource = ds;
			}
		}

		try {
			conn = JdbcUtil.getConnection(
					"jdbc:mysql://" + dataSource.getIp() + ":" + dataSource.getPort() + "/" + dataSource.getDbname()
							+ "?useUnicode=true&characterEncoding=utf-8&tinyInt1isBit=false",
					dataSource.getUsername(), indexService.decodePassword(dataSource.getPassword()));
			stmt = conn.createStatement();
			stmt.setQueryTimeout(10);
			rs = stmt.executeQuery(sql);
			return populate(rs);
		} finally {
			JdbcUtil.free(conn, stmt, rs);
		}
	}

	private static List<Map<String, Object>> populate(ResultSet rs) throws SQLException, InstantiationException, IllegalAccessException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int colCount = rsmd.getColumnCount();
		List<Map<String, Object>> list = new ArrayList<>();
		while (rs.next()) {
			Map<String, Object> kv = new HashMap<>();
			for (int i = 1; i <= colCount; i++) {
				Object value = rs.getObject(i);
				kv.put(rsmd.getColumnName(i), value);
			}
			list.add(kv);
		}
		return list;
	}

}