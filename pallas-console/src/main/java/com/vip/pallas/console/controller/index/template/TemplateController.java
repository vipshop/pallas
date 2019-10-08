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

package com.vip.pallas.console.controller.index.template;

import static com.vip.pallas.mybatis.entity.SearchTemplate.TYPE_MACRO;
import static com.vip.pallas.mybatis.entity.SearchTemplate.TYPE_TEMPLATE;
import static java.util.stream.Collectors.toList;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.vip.pallas.console.vo.BatchSubmitVO;
import com.vip.pallas.entity.BusinessLevelExceptionCode;
import com.vip.pallas.utils.TemplateParamsExtractUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vip.pallas.bean.IndexOperationEventName;
import com.vip.pallas.bean.IndexOperationEventType;
import com.vip.pallas.bean.TemplateImport;
import com.vip.pallas.bean.TemplateInfo;
import com.vip.pallas.console.utils.AuditLogUtil;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.utils.SessionUtil;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.TemplateOp;
import com.vip.pallas.console.vo.TemplateVO;
import com.vip.pallas.console.vo.base.BaseTemplateOp;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.mybatis.entity.Approve;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.mybatis.entity.IndexOperation;
import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.mybatis.entity.SearchTemplateHistory;
import com.vip.pallas.service.ApproveService;
import com.vip.pallas.service.IndexOperationService;
import com.vip.pallas.service.IndexService;
import com.vip.pallas.service.SearchTemplateHistoryService;
import com.vip.pallas.service.SearchTemplateService;

@Validated
@RestController
@RequestMapping("/index_template")
public class TemplateController {
    private static Logger logger = LoggerFactory.getLogger(TemplateController.class);

    @Resource
    private SearchTemplateService templateService;

    @Autowired
    private IndexOperationService indexOperationService;

    @Resource
    private IndexService indexService;

    @Resource
    private ApproveService approveService;

    @Resource
    private SearchTemplateHistoryService hisService;

    @RequestMapping(value = "/add.json", method = RequestMethod.POST)
    public void add(@Validated @RequestBody TemplateVO params, HttpServletRequest request) throws Exception {
        if(params.getType() == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "Type不能为空");
        }
        String templateName = params.getTemplateName();
        Long indexId =  params.getIndexId();
        
        Index index = indexService.findById(indexId);
        if(index == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "该索引不存在");
        }

        if (!AuthorizeUtil.authorizeTemplatePrivilege(request, indexId, index.getIndexName())) {
        	throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_FORBIDDEN, "无权限操作");
        }

        SearchTemplate t = templateService.findByNameAndIndexId(templateName, indexId);
        if(t != null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "该创建templateName已存在");
        }

        SearchTemplate entity = new SearchTemplate();
        BeanUtils.copyProperties(params, entity);
        templateService.insert(entity);
        try {
            AuditLogUtil.log("add search template: name - {0}, indexId - {1}, indexName - {2}", templateName, indexId, index.getIndexName());
            IndexOperation record = new IndexOperation();
            record.setEventDetail(entity.toString());
            record.setEventName(IndexOperationEventName.CREATE_TEMPLATE);
            record.setEventType(IndexOperationEventType.TEMPLATE_EVENT);
            record.setIndexId(index.getId());
            record.setOperationTime(new Date());
            record.setOperator(SessionUtil.getLoginUser(request));
            indexOperationService.insert(record);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @RequestMapping(value = "/debug.json", method = RequestMethod.POST)
    public String debug(@RequestBody TemplateOp params, HttpServletRequest request) throws Exception {
    	Long indexId =  params.getIndexId();
        if (ObjectUtils.isEmpty(indexId)){
			throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "indexId不能为空");
		}
        
        Index index = indexService.findById(indexId);
        if(index == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "该索引不存在");
        }
        
        if (!AuthorizeUtil.authorizeTemplatePrivilege(request, indexId, index.getIndexName())) {
        	throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_FORBIDDEN, "无权限操作");
        }
    	
        SearchTemplate dbEntity = checkAndGetSearchTemplate(params);
        Long clusterId = params.getClusterId();

        return templateService.inlineDebug(dbEntity, false, params.isProfile(), clusterId);
    }

    @RequestMapping(value = "/render.json", method = RequestMethod.POST)
    public String render(@RequestBody TemplateOp params) throws Exception {
        SearchTemplate dbEntity = checkAndGetSearchTemplate(params);
        Long clusterId = params.getClusterId();

        String result = templateService.inlineDebug(dbEntity, true, params.isProfile(), clusterId);

        if (result != null && result.startsWith("{\"template_output\":")) {
            result = result.substring("{\"template_output\":".length(), result.length()-1);
        }
        return result;
    }

	@RequestMapping(value = "/explain.json", method = RequestMethod.POST)
	public String explain(@RequestBody TemplateOp params) throws Exception {
		if (StringUtils.isBlank(params.getSql())) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "SQL不能为空");
        }
        params.setSql(params.getSql().replaceAll(";", ""));
		return templateService.parseSql(params.getSql(), params.getClusterId());
	}

	@RequestMapping(value = "/execute.json", method = RequestMethod.POST)
	public List<Map<String, Object>> execute(@RequestBody TemplateOp params, HttpServletRequest request) throws Exception {
		String sql = params.getSql();
		Long indexId = params.getIndexId();
		Long dsId = params.getDatasourceId();
		if (ObjectUtils.isEmpty(indexId)){
			throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "indexId不能为空");
		}
		if (ObjectUtils.isEmpty(dsId)) {
			throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "数据源不能为空");
		}
		
		Index index = indexService.findById(indexId);
        if(index == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "该索引不存在");
        }

        if (!AuthorizeUtil.authorizeTemplatePrivilege(request, indexId, index.getIndexName())) {
        	throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_FORBIDDEN, "无权限操作");
        }
		
		if (StringUtils.isBlank(sql)) {
			throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "SQL不能为空");
		}
		if (!sql.startsWith("select")) {
			throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "只允许select");
		}
		params.setSql(sql.replaceAll(";", ""));
		if (params.getSql().indexOf("where") < 0 && params.getSql().indexOf("limit") < 0) {
			params.setSql(params.getSql() + " limit 10");
		}
		return templateService.executeSql(params.getIndexId(), params.getSql(), dsId);
	}

    @RequestMapping(value = "/delete.json", method = RequestMethod.POST)
    public void delete(@Validated @RequestBody TemplateOp params, HttpServletRequest request) throws Exception {
        String templateName = params.getTemplateName();
        Long indexId =  params.getIndexId();
        String indexName = params.getIndexName();
        Long templateId =  params.getTemplateId();
        
        if (ObjectUtils.isEmpty(indexName)){
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "indexName不能为空");
        }
        
        if (!AuthorizeUtil.authorizeTemplatePrivilege(request, indexId, indexName)) {
        	throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_FORBIDDEN, "无权限操作");
        }

        if (ObjectUtils.isEmpty(templateId)){
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "templateId不能为空");
        }
        SearchTemplate t = templateService.findById(templateId);
        if (t == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "template 不存在");
        }

        templateService.delateByNameAndIndexId(templateName, indexId);
        Index index = indexService.findById(indexId);
        if(index == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "该索引不存在");
        }

        try {
            AuditLogUtil.log("delete search template: name - {0}, indexId - {1}, indexName  - {2}", templateName, indexId, index.getIndexName());
            IndexOperation record = new IndexOperation();
            record.setEventDetail("delete search template: name - " + templateName + ", indexId - " + indexId + ", indexName  - " + index.getIndexName());
            record.setEventName(IndexOperationEventName.DELETE_TEMPLATE);
            record.setEventType(IndexOperationEventType.TEMPLATE_EVENT);
            record.setIndexId(indexId);
            record.setOperationTime(new Date());
            record.setOperator(SessionUtil.getLoginUser(request));
            indexOperationService.insert(record);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    @RequestMapping(value = "/export.json", method = RequestMethod.GET)
    public void export(@RequestParam Long indexId, String templateIds, HttpServletResponse response) throws Exception {
        Index index = indexService.findById(indexId);
        if(index == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "该索引不存在");
        }

        if(templateIds != null){
            String[] arrTemplateId = templateIds.split(",");
            if(arrTemplateId != null && arrTemplateId.length > 0){
                int len = arrTemplateId.length;
                Long[] longArrTemplateId = new Long[len];
                for(int i = 0; i < len; i++){
                    longArrTemplateId[i] = Long.valueOf(arrTemplateId[i]);
                }

                List<SearchTemplate> allFiles = templateService.findAllByIndexIdAndTemplateIds(indexId, longArrTemplateId);
                response.setStatus(200);
                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=\"templates_" + indexId + "_" + index.getIndexName() + ".zip\"");
                zipFiles(allFiles, response);
            }
        }
    }

    @RequestMapping(value = "/update.json", method = RequestMethod.POST)
    public void page(@Validated @RequestBody TemplateVO params, HttpServletRequest request) throws Exception {
        String templateName = params.getTemplateName();
        Long indexId =  params.getIndexId();
        String content = params.getContent();
        String parameters = params.getParams();

        Index index = indexService.findById(indexId);
        if(index == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "该索引不存在");
        }
        
        if (!AuthorizeUtil.authorizeTemplatePrivilege(request, indexId, index.getIndexName())) {
        	throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_FORBIDDEN, "无权限操作");
        }

        SearchTemplate dbEntity = templateService.findByNameAndIndexId(templateName, indexId);
        if (dbEntity == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "模板不存在");
        }

        Integer retry =  params.getRetry();
        if (!ObjectUtils.isEmpty(retry)) {
            dbEntity.setRetry(retry);
        }

        Integer timeout =  params.getTimeout();
        if (!ObjectUtils.isEmpty(timeout)) {
            dbEntity.setTimeout(timeout);
        }

		Integer threshold =  params.getThreshold();
		if (!ObjectUtils.isEmpty(threshold)) {
			dbEntity.setThreshold(threshold);
		}

		Integer maxBurstSecs =  params.getMaxBurstSecs();
		if (!ObjectUtils.isEmpty(maxBurstSecs)) {
			dbEntity.setMaxBurstSecs(maxBurstSecs);
		}

        dbEntity.setContent(content);
        dbEntity.setParams(parameters);

        //仅保存模板
        templateService.saveTemplate(dbEntity);

    }

    //udpate and approve
    @RequestMapping(value = "/approve.json", method = RequestMethod.POST)
    public Long approve(HttpServletRequest req, @Validated @RequestBody TemplateOp params) throws Exception {
        String templateName = params.getTemplateName();
        Long indexId =  params.getIndexId();
        String content = params.getContent();
        String parameters = params.getParams();
        String historyDesc = params.getHistoryDesc();
        String currentUser = SessionUtil.getLoginUser(req);

        Index index = indexService.findById(indexId);
        if(index == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "该索引不存在");
        }
        
        if (!AuthorizeUtil.authorizeTemplatePrivilege(req, indexId, index.getIndexName())) {
        	throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_FORBIDDEN, "无权限操作");
        }

        SearchTemplate dbEntity = templateService.findByNameAndIndexId(templateName, indexId);
        if (dbEntity == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "模板不存在");
        }

        dbEntity.setContent(content);
        dbEntity.setParams(parameters);

        Long templateId = dbEntity.getId();

        if(approveService.isInApprove(templateId)){
            throw new PallasException("该模板已有变更内容等待审批，请等待审批流程结束再发起变更！");
        }

        //仅保存模板
        templateService.saveTemplate(dbEntity);

        //增加审批流程
        Approve approve = templateService.submitToApprove(currentUser, historyDesc, templateId);

        return approve.getId();

    }


    //batch approve
    @RequestMapping(value = "/batch/approve.json", method = RequestMethod.POST)
    public int batchApprove(HttpServletRequest req, @Validated @RequestBody BatchSubmitVO params) throws Exception {
        Long indexId =  params.getIndexId();
        String historyDesc = params.getHistoryDesc();
        String currentUser = SessionUtil.getLoginUser(req);
        Index index = indexService.findById(indexId);
        if(index == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "该索引不存在");
        }
        if (!AuthorizeUtil.authorizeTemplatePrivilege(req, indexId, index.getIndexName())) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_FORBIDDEN, "无权限操作");
        }
        Long[] templateIds = Arrays.stream(params.getTemplateIds().split(",")).map(id->Long.valueOf(id)).toArray(Long[]::new);
        if (templateIds.length<=0){
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "id数组为空");
        }
        List<SearchTemplate> templates=templateService.findAllByIndexIdAndTemplateIds(indexId,templateIds);
        int validApprove = 0;
        for (SearchTemplate dbEntity:templates){
            if (dbEntity == null) {
                logger.error("batch approve error, submit illegal template id");
                continue;
            }
            if(approveService.isInApprove(dbEntity.getId())){
                logger.error("batch approve error, submit a approving template, template id: " + dbEntity.getId());
                continue;
            }
            //增加审批流程
            templateService.submitToApprove(currentUser, historyDesc, dbEntity.getId());
            validApprove ++;
        }
        if (validApprove == 0){
            throw new PallasException("选择的模板已有变更内容等待审批，请等待审批流程结束再发起变更！");
        }
        return validApprove;
    }

    @RequestMapping(value = "/genapi.json", method = RequestMethod.POST)
    public Map<String, Object> genApi(@RequestBody BaseTemplateOp params) {
        String templateName = params.getTemplateName();
        Long indexId =  params.getIndexId();
        SearchTemplate dbEntity = templateService.findByNameAndIndexId(templateName, indexId);
        if (dbEntity == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "模板不存在");
        }
        if (dbEntity.getType() != SearchTemplate.TYPE_TEMPLATE) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "该模板类型不正确");
        }

        Map<String, Object> apiMap = new HashMap<>();
        templateService.genAPI(dbEntity, apiMap);

        return apiMap;
    }

    @RequestMapping(value = "/id.json", method = RequestMethod.GET)
    public SearchTemplate getTemplate(@RequestParam Long templateId) { // NOSONAR
        return templateService.findById(templateId);
    }

    @RequestMapping(value = "/list.json", method = RequestMethod.GET)
    public PageResultVO<SearchTemplate> list(@RequestParam Long indexId) { // NOSONAR

        List<SearchTemplate> list = templateService.findAllByIndexId(indexId);

        list.parallelStream().forEach(

               t -> {
                   try {
                       t.setHisCount(hisService.count(t.getId()));
                       String content = t.getContent() == null ? "" : t.getContent();
                       t.setResetParams(JSONObject.toJSONString(TemplateParamsExtractUtil.getParams(content, list), SerializerFeature.WriteMapNullValue));
                   } catch (Exception ignore) {
                       //no set the resetPrams if error
                       t.setResetParams("{\\n}");
                   }
               }
        );

        PageResultVO<SearchTemplate> resultVO = new PageResultVO<>();

        List<String> privileges = AuthorizeUtil.loadPrivileges();
        if (privileges != null && (privileges.contains("template." + indexId + "-" + indexService.findById(indexId).getIndexName())
                || privileges.contains("template.all"))) {
            resultVO.setAllPrivilege(true);
        }
        resultVO.setList(list);

        return resultVO;
    }

    @RequestMapping(value = "/hislist.json", method = RequestMethod.GET)
    public List<SearchTemplateHistory> page(@RequestParam Long templateId) { // NOSONAR
        return hisService.findAllByTemplateId(templateId);
    }

    @RequestMapping(value = "/history/id.json", method = RequestMethod.GET)
    public SearchTemplateHistory getId(@RequestParam Long templateId) { // NOSONAR
        return hisService.findLastOnlineById(templateId);
    }

    @RequestMapping(value = "/index/import.json", method = RequestMethod.POST)
    public List<String> templateImprotFromOtherIndex(HttpServletRequest req,
                                                     @RequestBody TemplateImport templateImport) throws Exception{
        String currentUser = SessionUtil.getLoginUser(req);
        if(null == templateImport) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "请输入参数");
        }
        Index index = indexService.findById(templateImport.getIndexId());
        if (null == indexService.findById(templateImport.getIndexId())) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "该索引不存在");
        }

        if (!AuthorizeUtil.authorizeTemplatePrivilege(req, templateImport.getIndexId(), index.getIndexName())) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_FORBIDDEN, "无权限操作");
        }

        List<String> result = templateService.importTemplatesFromOtherIndex(currentUser, templateImport);

        if(!CollectionUtils.isEmpty(templateImport.getTemplateInfos())) {

            List<TemplateInfo> templateInfoList =  templateImport.getTemplateInfos();
            List<String> nameList = templateInfoList.stream().map(TemplateInfo::getTemplateName).collect(toList());

            AuditLogUtil.log("import search template from other index: names - {0}, indexId - {1}, indexName - {2}.fail import : {3}", nameList, templateImport.getIndexId(), index.getIndexName(), result);

            IndexOperation record = new IndexOperation();
            record.setEventDetail(nameList.toString() + ". fail import：" +  result.toString());
            record.setEventName(IndexOperationEventName.IMPORT_TEMPLATE);
            record.setEventType(IndexOperationEventType.TEMPLATE_EVENT);
            record.setIndexId(templateImport.getIndexId());
            record.setOperationTime(new Date());
            record.setOperator(currentUser);
            indexOperationService.insert(record);
        }
        return result;
    }

    @RequestMapping(value = "/import.json", method = RequestMethod.POST)
    public List<SearchTemplate> templateImport(HttpServletRequest req, 
            @RequestParam @NotNull(message = "file不能为空") MultipartFile file,
            @RequestParam @NotNull(message = "indexId不能为空") @Min(value = 1, message = "pluginUpgradeId必须为正数") Long indexId,
            @RequestParam @NotBlank(message = "updateDesc不能为空") String updateDesc) throws Exception { // NOSONAR
        String currentUser = SessionUtil.getLoginUser(req);

        Index index = indexService.findById(indexId);
        if (null == indexService.findById(indexId)) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "该索引不存在");
        }
        
        if (!AuthorizeUtil.authorizeTemplatePrivilege(req, indexId, index.getIndexName())) {
        	throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_FORBIDDEN, "无权限操作");
        }

        List<SearchTemplate> list = new LinkedList<>();

        try(BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
            ZipInputStream zis = new ZipInputStream(bis)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String entryName = entry.getName();
                String content = IOUtils.toString(zis);
                //byte[] bytes = new byte[(int)entry.getSize()];
                //zis.read(bytes);
                SearchTemplate st = new SearchTemplate();
                if (entryName.startsWith("templates/")) {
                    st.setTemplateName(entryName.substring(10));
                    st.setType(TYPE_TEMPLATE);
                } else if (entryName.startsWith("macros/")){
                    st.setTemplateName(entryName.substring(7));
                    st.setType(TYPE_MACRO);
                } else {
                    throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "上传zip内容错误，只能包含templates文件夹和macros文件夹");
                }
                if(!"".equals(st.getTemplateName().trim())){
                    st.setContent(content);
                    st.setIndexId(indexId);
                    list.add(st);
                }
            }
        }

        if (!list.isEmpty()) {
            templateService.importTemplates(currentUser, updateDesc, list);

            try {
                List<String> nameList = list.stream().map(SearchTemplate::getTemplateName).collect(toList());
                AuditLogUtil.log("import search template: names - {0}, indexId - {1}, indexName - {2}", nameList, indexId, index.getIndexName());
                IndexOperation record = new IndexOperation();
                record.setEventDetail(nameList.toString());
                record.setEventName(IndexOperationEventName.IMPORT_TEMPLATE);
                record.setEventType(IndexOperationEventType.TEMPLATE_EVENT);
                record.setIndexId(indexId);
                record.setOperationTime(new Date());
                record.setOperator(currentUser);
                indexOperationService.insert(record);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return templateService.findAllByIndexId(indexId);
    }

    private SearchTemplate checkAndGetSearchTemplate(TemplateOp params) {
        if(params.getParams() == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "Parameters不能为空");
        }
        SearchTemplate dbEntity = templateService.findByNameAndIndexId(params.getTemplateName(), params.getIndexId());
        if (dbEntity == null) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "模板不存在");
        }
        if (dbEntity.getType() != SearchTemplate.TYPE_TEMPLATE) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "该模板类型不正确");
        }
        dbEntity.setParams(params.getParams());
        return dbEntity;
    }


    /**
     * Compress the given directory with all its files.
     */
    private void zipFiles(List<SearchTemplate> allFiles, HttpServletResponse resp) throws IOException {

        try(ZipOutputStream zos = new ZipOutputStream(resp.getOutputStream())) {
            allFiles.forEach(
                    x -> {
                        try{
                            String entName;
                            if (x.getType() == TYPE_TEMPLATE) {
                                entName = "templates/" + x.getTemplateName();
                            } else {
                                entName = "macros/" + x.getTemplateName();
                            }
                            zos.putNextEntry(new ZipEntry(entName));
                            zos.write(x.getContent() == null ? "".getBytes() : x.getContent().getBytes());
                            zos.closeEntry();
                        } catch (IOException e) {
                            logger.error("error parsing zip file", e);
                        }
                    }
            );
            zos.flush();
        }
    }
}