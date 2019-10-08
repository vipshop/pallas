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
import com.vip.pallas.bean.monitor.ExtMetricInfoModel;
import com.vip.pallas.bean.monitor.MonitorQueryModel;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.entity.BusinessLevelExceptionCode;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.*;
import com.vip.pallas.service.*;
import com.vip.pallas.utils.ObjectMapTool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RestController
@Validated
public class IndexOperationPageController{
    private static Logger logger = LoggerFactory.getLogger(IndexOperationPageController.class);
    @Autowired
    private IndexOperationService indexOperationService;

    @Autowired
    private MonitorService monitorService;

    @Autowired
    private IndexVersionService indexVersionService;

    @Autowired
    private IndexService indexService;

    @Autowired
    private ClusterService clusterService;

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

        // 获取request rate & request latency
        List<MonitorQueryModel> queryModels = constructMonitorQuery(indexId,versionId,timeRangeList);
        if (queryModels != null&&queryModels.size()>0){
            List<ExtMetricInfoModel> metricInfoModelList = new ArrayList<>(queryModels.size());
            for (MonitorQueryModel query : queryModels) {
                try{
                    ExtMetricInfoModel metricInfoModel = new ExtMetricInfoModel(monitorService.getMetricInfoModel(query),query.getClusterName());
                    metricInfoModelList.add(metricInfoModel);
                }catch (Exception e){
                    logger.info("get cluster metric error：{},clusterName:{}",e.getMessage(),query.getClusterName());
                }
            }
            resultMap.put("metric",metricInfoModelList);
        }
        resultMap.put("list", indexOperationService.selectByExampleWithBLOBs(example));
        resultMap.put("total", total);
        resultMap.put("pageCount", pageCount);
        return resultMap;
    }

    private List<MonitorQueryModel> constructMonitorQuery(Long indexId,Integer versionId,List<String> timeRangeList) throws ParseException {
        List<MonitorQueryModel> result = new ArrayList<>();
        Index index = indexService.findById(indexId);
        if (null == index)return null;
        Cluster cluster =  clusterService.findByName(index.getClusterName());
        if(null == cluster) return null;
        if (StringUtils.isNotEmpty(cluster.getRealClusters())){
            // 逻辑集群
            String[] clusterArr =  cluster.getRealClusters().split(",");
            for (String clusterId : clusterArr) {
                Cluster logicCluster = clusterService.selectByPrimaryKey(Long.parseLong(clusterId));
                MonitorQueryModel query = getQueryModel(index,versionId,logicCluster.getClusterId(),timeRangeList);
                if (query!=null){
                    result.add(query);
                }
            }
        }else {
            MonitorQueryModel query = getQueryModel(index,versionId,index.getClusterName(),timeRangeList);
            if (query!=null){
                result.add(query);
            }

        }

        return result;
    }

    private MonitorQueryModel getQueryModel(Index index,Integer versionId,String clusterName,List<String> timeRangeList) throws ParseException {
        MonitorQueryModel query = new MonitorQueryModel();
        if (null == versionId || 0 == versionId){
            // 没传则拿启用中的
            IndexVersion version = indexVersionService.findUsedIndexVersionByIndexId(index.getId());
            if (null == version)return null;
            query.setIndexName(index.getIndexName()+"_"+version.getId());
        }else {
            query.setIndexName(index.getIndexName()+"_"+versionId);
        }
        query.setClusterName(clusterName);
        if (timeRangeList != null && timeRangeList.size() == 2 ) {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            format.setTimeZone(TimeZone.getTimeZone("Asia/Beijing"));
            Date start = format.parse(timeRangeList.get(0).replaceAll("\"", ""));
            Date end = format.parse(timeRangeList.get(1).replaceAll("\"", ""));
            query.setFrom(start.getTime());
            query.setTo(end.getTime());
        }else {
            // set last day time
            long cur = System.currentTimeMillis();
            query.setFrom(cur-TimeUnit.DAYS.toMillis(1));
            query.setTo(cur);
        }
        return query;
    }

    @RequestMapping("/index/dynamic/delete.json")
    public void deleteIndexDynamic(@RequestBody @Validated IndexOperationParams params, HttpServletRequest request) {
        if (!AuthorizeUtil.authorizeIndexPrivilege(request, params.getIndexId(), params.getIndexName())) {
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_FORBIDDEN, "无权限操作");
        }
        int result = indexOperationService.deleteByCondition(params);
    }

    private void validatePaging(Integer currentPage, Integer pageSize, Long indexId) { // NOSONAR

        if(ObjectUtils.isEmpty(currentPage)){
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "currentPage不能为空");
        }

        if(currentPage <= 0){
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "currentPage必须为正数");
        }

        if(ObjectUtils.isEmpty(pageSize)){
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "pageSize不能为空");
        }

        if(pageSize <= 0){
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "pageSize必须为正数");
        }

        if(ObjectUtils.isEmpty(indexId)){
            throw new BusinessLevelException(BusinessLevelExceptionCode.HTTP_INTERNAL_SERVER_ERROR, "indexId不能为空");
        }
    }
}