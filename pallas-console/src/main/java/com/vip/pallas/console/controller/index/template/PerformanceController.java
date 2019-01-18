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

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.net.HostAndPort;
import com.vip.pallas.bean.PerformanceData;
import com.vip.pallas.bean.QueryParamSetting;
import com.vip.pallas.console.utils.AuditLogUtil;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.utils.JsonUtil;
import com.vip.pallas.utils.ObjectMapTool;


@RestController
@RequestMapping("/index_template/performance_script")
public class PerformanceController extends PerformanceBaseController{
    private static Logger logger = LoggerFactory.getLogger(PerformanceController.class);

    protected static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @RequestMapping(value = "/param.json")
    public List<Map<String, String>> param(@RequestBody Map<String, Object> params, HttpServletRequest request) { // NOSONAR
        SearchTemplate template = this.getSearchTemplateFromParams(params);
        clearDataCache(template, request);
        return performanceScriptService.getQueryParamNames(template);
    }

    /**
     * 生成性能测试脚本，并打包下载
     */
    @RequestMapping(value = "/gen.json")
    public void gen(@RequestParam @NotEmpty(message = "params不能为空") String params
    		, HttpServletRequest request, HttpServletResponse response) throws Exception { // NOSONAR
        Map<String, Object> paramMap = getJsonObj(params);
        SearchTemplate template = this.getSearchTemplateFromParams(paramMap);
        List<HostAndPort> hps = getHostAndPorts(template);
        JsonNode paramsNode = ObjectMapTool.getObject(paramMap, "params", JsonNode.class);
        List<PerformanceData> pds = getPerformanceDataFromCache(request);
        List<List<QueryParamSetting>> paramSettingsList = new ArrayList<List<QueryParamSetting>>();
        if (paramsNode != null) {
            List<QueryParamSetting> paramSettings = OBJECT_MAPPER.readValue(paramsNode,
                    new TypeReference<List<QueryParamSetting>>() {
                    });
            paramSettingsList.add(paramSettings);
        }
        String jmxScript = performanceScriptService.genJmxScript(template,pds, paramSettingsList);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/zip");
        String testname = template.getIndexId() + "_" + template.getTemplateName();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + testname + ".zip\"");
        try {
            performanceScriptService.zipFiles(testname, jmxScript, hps,pds, response.getOutputStream());
        } catch (Exception e) {
            logger.error("生成压缩包的时候出错", e);
            throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * 上传性能测试脚本中的数据源文件以及对应的查询请求参数名称
     */
    @RequestMapping(value = "/upload.json")
	public String upload(@RequestParam @NotNull(message = "file不能为空") MultipartFile file,
			@RequestParam @NotNull(message = "indexId不能为空") @Min(value = 1, message = "indexId不能小于1") Long indexId,
			@RequestParam @NotBlank(message = "paramNameDef不能为空") String paramNameDef,
			@RequestParam @NotBlank(message = "templateName不能为空") String templateName, 
			HttpServletRequest request) { // NOSONAR
    	Index index = indexService.findById(indexId);
    	if (index == null) {
            throw new BusinessLevelException(500, "index不存在");
        }
        if (!AuthorizeUtil.authorizeIndexPrivilege(request, index.getId(), index.getIndexName())) {
        	throw new BusinessLevelException(403, "无权限操作");
        }
        
        SearchTemplate template = templateService.findByNameAndIndexId(templateName, indexId);
        if (template == null) {
            throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, "模板不存在");
        }
        if (template.getType() != SearchTemplate.TYPE_TEMPLATE) {
            throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, "该模板类型不正确");
        }
        
        paramNameDef = paramNameDef.replace(';', ',');
        String fileName = FilenameUtils.getName(file.getOriginalFilename());
        PerformanceData performanceData = new PerformanceData(null, paramNameDef, fileName, null);
        addDataCache(template, request, performanceData);
        String realFileName = saveDataFile(template, file, fileName);
        performanceData.setRealFileName(realFileName);
        AuditLogUtil.log("upload perf test data: {0}", realFileName);
        return fileName;
    }

    @RequestMapping(value = "/update.json")
    public void update(@RequestBody Map<String, Object> params, HttpServletRequest request) { // NOSONAR
        SearchTemplate template = this.getSearchTemplateFromParams(params);
        Long indexId = ObjectMapTool.getLong(params, "indexId");
    	Index index = indexService.findById(indexId);
    	if (index == null) {
            throw new BusinessLevelException(500, "index不存在");
        }
        if (!AuthorizeUtil.authorizeIndexPrivilege(request, index.getId(), index.getIndexName())) {
        	throw new BusinessLevelException(403, "无权限操作");
        }
        String fileName = ObjectMapTool.getString(params, "fileName");
        String paramNameDef = ObjectMapTool.getString(params, "paramNameDef");
        if (ObjectUtils.isEmpty(paramNameDef)) {
            throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, "paramNameDef不能为空");
        }
        if (ObjectUtils.isEmpty(fileName)) {
            throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, "fileName不能为空");
        }
        paramNameDef = paramNameDef.replace(';', ',');
        updatePerformanceData(template, request, paramNameDef, fileName);
    }

    /**
     *update.json
     */
    @SuppressWarnings("unchecked")
    private void updatePerformanceData(SearchTemplate st, HttpServletRequest request, String paramNameDef, String fileName) {
        Object dataObj = request.getSession().getAttribute(DATA_CACHE_KEY);
        if (dataObj != null) {
            String dataKey = getDataCacheKey(st, fileName);
            Map<String, PerformanceData> dataCacheMap = (Map<String, PerformanceData>) dataObj;
            PerformanceData pd = dataCacheMap.get(dataKey);
            if (pd != null) {
                checkReqParamName(dataCacheMap, paramNameDef,pd.getParamNameDef());
                pd.setParamNameDef(paramNameDef);
                AuditLogUtil.log("update perf test data: {0}", dataKey);
            }
        }
    }

    @RequestMapping(value = "/delete.json")
    public void delete(@RequestBody Map<String, Object> params, HttpServletRequest request) { // NOSONAR
    	SearchTemplate template = this.getSearchTemplateFromParams(params);
    	Long indexId = ObjectMapTool.getLong(params, "indexId");
    	Index index = indexService.findById(indexId);
    	if (index == null) {
            throw new BusinessLevelException(500, "index不存在");
        }
        if (!AuthorizeUtil.authorizeIndexPrivilege(request, index.getId(), index.getIndexName())) {
        	throw new BusinessLevelException(403, "无权限操作");
        }
        String fileName = ObjectMapTool.getString(params, "fileName");
        if (ObjectUtils.isEmpty(fileName)) {
            throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, "fileName不能为空");
        }
        deleteDataFile(template, request, fileName);
    }

    /**
     *delete.json
     */
    @SuppressWarnings("unchecked")
    private void deleteDataFile(SearchTemplate st, HttpServletRequest request, String fileName) {
        Object dataObj = request.getSession().getAttribute(DATA_CACHE_KEY);
        if (dataObj != null) {
            String dataKey = getDataCacheKey(st, fileName);
            Map<String, PerformanceData> dataCacheMap = (Map<String, PerformanceData>) dataObj;
            dataCacheMap.remove(dataKey);
            File dir = this.getTemplateDataFileSaveDirFile(st);
            File file = new File(dir, fileName);
            FileUtils.deleteQuietly(file);
            AuditLogUtil.log("delete perf test data: {0}", file.getAbsolutePath());

        }
    }
    /**
     * upload.json
     */
    private String saveDataFile(SearchTemplate template, MultipartFile file, String fileName) {
        File dir = getTemplateDataFileSaveDirFile(template);
        File toSaveFile = new File(dir, fileName);
        try (OutputStream fos  = Files.newOutputStream(toSaveFile.toPath())) { //NOSONAR
            IOUtils.copy(file.getInputStream(), fos);
        } catch (IOException ioe) {
            String errorMsg = "保存文件的时候出错,文件:" + fileName;
            logger.error(errorMsg, ioe);
            throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, errorMsg);
        }
        return toSaveFile.getAbsolutePath();
    }

    @SuppressWarnings("unchecked")
    private void addDataCache(SearchTemplate st, HttpServletRequest request, PerformanceData performanceData) {
        Object dataObj = request.getSession().getAttribute(DATA_CACHE_KEY);
        Map<String, PerformanceData> dataCacheMap;
        String dataKey = getDataCacheKey(st, performanceData.getFileName());
        if (dataObj == null) {
            dataCacheMap = new HashMap<>();
        } else {
            dataCacheMap = (Map<String, PerformanceData>) dataObj;
            if (dataCacheMap.containsKey(dataKey)) {
                throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, "已经存在同名文件," + performanceData.getFileName());
            }
            checkReqParamName(dataCacheMap,performanceData.getParamNameDef(),null);
        }
        dataCacheMap.put(dataKey, performanceData);
        request.getSession().setAttribute(DATA_CACHE_KEY, dataCacheMap);
    }

    /**
     *gen.json
     */
    private List<HostAndPort> getHostAndPorts(SearchTemplate template) {
        Index index = indexService.findById(template.getIndexId());
        Cluster cluster = clusterService.findByName(index.getClusterName());
        //Cluster cluster = clusterService.selectUsedPhysicalClustersByIndexId(index.getId());
        String httpAddressStr = cluster.getHttpAddress();
        String[] httpAddressList = httpAddressStr.split(",");

        return Stream.of(httpAddressList).map(address -> HostAndPort.fromString(address)).collect(Collectors.toList());
    }

    protected Map<String, Object> getJsonObj(String jsonStr) throws Exception {
        return JsonUtil.parseJsonObject(jsonStr);
    }

    @SuppressWarnings("unchecked")
    private List<PerformanceData> getPerformanceDataFromCache(HttpServletRequest request) {
        Object dataObj = request.getSession().getAttribute(DATA_CACHE_KEY);
        Map<String, PerformanceData> dataCacheMap = null;
        if (dataObj != null) {
            dataCacheMap = (Map<String, PerformanceData>) dataObj;
            return new ArrayList<>(dataCacheMap.values());
        }
        return new ArrayList<>();

    }

    /**
     * param.json
     */
    private void clearDataCache(SearchTemplate st, HttpServletRequest request) {
        File dir = this.getTemplateDataFileSaveDirFile(st);
        try {
            FileUtils.cleanDirectory(dir);
        } catch (IOException e) {
            String errorMsg = "在初始化清空数据文件临时保存目录的时候报错，" + dir.getAbsolutePath();
            logger.error(errorMsg, e);
            throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, errorMsg);
        }
        request.getSession().removeAttribute(DATA_CACHE_KEY);
    }

}