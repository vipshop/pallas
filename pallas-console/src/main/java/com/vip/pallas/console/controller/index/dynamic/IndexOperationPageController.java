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

package com.vip.pallas.console.controller.index.dynamic;

import com.vip.pallas.bean.IndexOperationParams;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.IndexOperationExample;
import com.vip.pallas.mybatis.entity.IndexVersion;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.service.IndexOperationService;
import com.vip.pallas.utils.ObjectMapTool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@Validated
public class IndexOperationPageController{

    @Autowired
    private IndexOperationService indexOperationService;

    @RequestMapping("/index/dynamic/page.json")
    public Map<String, Object> list(@RequestBody Map<String, Object> params) throws Exception {
        Integer currentPage = ObjectMapTool.getInteger(params, "currentPage");
        Integer pageSize = ObjectMapTool.getInteger(params, "pageSize");
        Long indexId = ObjectMapTool.getLong(params, "indexId");

        validatePaging(currentPage, pageSize, indexId);

        Page<IndexVersion> page = new Page<>();
        page.setPageNo(currentPage);
        page.setPageSize(pageSize);

        Integer versionId = ObjectMapTool.getInteger(params, "filterVersion");
        String selectedType = ObjectMapTool.getString(params, "selectedType");
        List<String> timeRangeList = ObjectMapTool.getStringList(params, "timeRange");
        Map<String, Object> resultMap = new HashMap<>();
        IndexOperationExample example = new IndexOperationExample();
        IndexOperationExample.Criteria criteria = example.createCriteria();
        criteria.andIndexIdEqualTo(indexId);
        if (StringUtils.isNotBlank(selectedType)) {
            criteria.andEventTypeEqualTo(selectedType);
        }
        if (versionId != null) {
            criteria.andVersionIdEqualTo(versionId);
        }
        if (timeRangeList != null && timeRangeList.size() == 2 ) {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            format.setTimeZone(TimeZone.getTimeZone("Asia/Beijing"));
            Date start = format.parse(timeRangeList.get(0).replaceAll("\"", ""));
            Date end = format.parse(timeRangeList.get(1).replaceAll("\"", ""));
            criteria.andOperationTimeBetween(start, end);
        }
        example.setOffset((currentPage-1) * pageSize);
        example.setLimit(pageSize);
        example.setOrderByClause(" operation_time desc ");

        long total = indexOperationService.countByExample(example);
        long pageCount = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);

        resultMap.put("list", indexOperationService.selectByExampleWithBLOBs(example));
        resultMap.put("total", total);
        resultMap.put("pageCount", pageCount);

        return resultMap;
    }

    @RequestMapping("/index/dynamic/delete.json")
    public void deleteIndexDynamic(@RequestBody @Validated IndexOperationParams params, HttpServletRequest request) {
        if (!AuthorizeUtil.authorizeIndexPrivilege(request, params.getIndexId(), params.getIndexName())) {
            throw new BusinessLevelException(403, "无权限操作");
        }
        int result = indexOperationService.deleteByCondition(params);
    }

    private void validatePaging(Integer currentPage, Integer pageSize, Long indexId) { // NOSONAR

        if(ObjectUtils.isEmpty(currentPage)){
            throw new BusinessLevelException(500, "currentPage不能为空");
        }

        if(currentPage <= 0){
            throw new BusinessLevelException(500, "currentPage必须为正数");
        }

        if(ObjectUtils.isEmpty(pageSize)){
            throw new BusinessLevelException(500, "pageSize不能为空");
        }

        if(pageSize <= 0){
            throw new BusinessLevelException(500, "pageSize必须为正数");
        }

        if(ObjectUtils.isEmpty(indexId)){
            throw new BusinessLevelException(500, "indexId不能为空");
        }
    }
}