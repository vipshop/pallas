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

import com.vip.pallas.bean.PerformanceData;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.IndexService;
import com.vip.pallas.service.PerformanceScriptService;
import com.vip.pallas.service.SearchTemplateService;
import com.vip.pallas.utils.ObjectMapTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

public class PerformanceBaseController{

    protected static final String DATA_CACHE_KEY = "performanceDataCache";

    @Autowired
    protected PerformanceScriptService performanceScriptService;

    @Resource
    protected SearchTemplateService templateService;

    @Resource
    IndexService indexService;

    @Resource
    ClusterService clusterService;

    protected SearchTemplate getSearchTemplateFromParams(Map<String, Object> params) {
        String templateName = ObjectMapTool.getString(params, "templateName");
        Long indexId =  ObjectMapTool.getLong(params, "indexId");
        if (ObjectUtils.isEmpty(indexId)) {
            throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, "indexId不能为空");
        }
        if (ObjectUtils.isEmpty(templateName)) {
            throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, "templateName不能为空");
        }
        SearchTemplate template = templateService.findByNameAndIndexId(templateName, indexId);
        if (template == null) {
            throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, "模板不存在");
        }
        if (template.getType() != SearchTemplate.TYPE_TEMPLATE) {
            throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, "该模板类型不正确");
        }

        return template;
    }

    /**
     * 根据查询模板，获取该查询模板的数据文件临时存储路径
     *
     * 存储路径为系统的temp目录下面的indexId子目录下面的templateName子目录
     *
     * 例如 /var/tmp/msearch/merchandiseList
     *
     * @param st
     * @return
     */
    protected String getTemplateDataFileSaveDir(SearchTemplate st) {
        StringBuilder builder = new StringBuilder();
        String tmpDir = System.getProperty("java.io.tmpdir");
        builder.append(tmpDir).append(File.separator).append(st.getIndexId()).append(File.separator)
                .append(st.getTemplateName());
        return builder.toString();
    }

    /**
     * 获取文件保存路径的File对象，如果目录不存在的话，创建该目录
     *
     * @param st
     */
    protected File getTemplateDataFileSaveDirFile(SearchTemplate st) {
        String templateDataFileSaveDir = getTemplateDataFileSaveDir(st);
        File dirFile = new File(templateDataFileSaveDir);
        if (!dirFile.exists()) {
            if(!dirFile.mkdirs()){
                throw new RuntimeException("创建模板临时上传文件夹失败");
            }
        }
        return dirFile;
    }

    /**
     * 获取存放在缓存中的数据文件的唯一标识key
     *
     * @param st
     * @param fileName
     * @return
     */
    protected String getDataCacheKey(SearchTemplate st, String fileName) {
        Index index = indexService.findById(st.getIndexId());
        String uniqueIdentifier = index.getIndexName() + "_" + st.getTemplateName();
        return uniqueIdentifier + "_" + fileName;
    }

    protected void checkReqParamName(Map<String, PerformanceData> dataCacheMap, String newParamNameDef,
                                     String oldParamNameDef) {
        List<String> cacheReqParamNames = getReqParamNames(dataCacheMap);
        if (oldParamNameDef != null) {
            removeOldReqParamName(cacheReqParamNames, oldParamNameDef);
        }
        String[] reqParamNameArr = newParamNameDef.split(",");
        for (String reqParamName : reqParamNameArr) {
            if (cacheReqParamNames.contains(reqParamName.trim())) {
                throw new BusinessLevelException(SC_INTERNAL_SERVER_ERROR, "已经存在同名的参数定义," + reqParamName);
            }
        }
    }

    private void removeOldReqParamName(List<String> cacheReqParamNames, String paramNameDef) {
        String[] reqParamNameArr = paramNameDef.split(",");
        for (String reqParamName : reqParamNameArr) {
            if (cacheReqParamNames.contains(reqParamName.trim())) {
                cacheReqParamNames.remove(reqParamName);
            }
        }
    }

    private List<String> getReqParamNames(Map<String, PerformanceData> dataCacheMap) {
        return dataCacheMap.values().stream().flatMap(pd -> Stream.of(pd.getParamNameDef().trim().split(",")))
                .collect(Collectors.toList());
    }
}