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

package com.vip.pallas.console.controller.api.record;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.bean.FlowRecordState;
import com.vip.pallas.console.vo.FlowRecordOp;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.FlowRecord;
import com.vip.pallas.mybatis.entity.FlowRecordConfig;
import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.service.FlowRecordService;

@Validated
@RestController
@RequestMapping("/record/flow_record")
public class FlowRecordApiController{

    @Autowired
    private FlowRecordService flowRecordService;

    @RequestMapping(path = "/export.json", method = {RequestMethod.GET, RequestMethod.POST})
    public void export(@RequestParam Long recordId, HttpServletResponse response) throws Exception{
        FlowRecord flowRecord = flowRecordService.getFlowRecordById(recordId);
        if(flowRecord == null) {
            throw new BusinessLevelException(500, "根据recordId + " + recordId + "找不到相关记录");
        }

        response.setStatus(200);
        response.setContentType("application/csv;charset=UTF-8");

        FlowRecordConfig config = flowRecord.getFlowRecordConfig();
        String clusterName = config.getCluster().getClusterId();
        String indexName = config.getIndex().getIndexName();

        String fileName = clusterName + "_" + indexName;

        SearchTemplate template = config.getTemplate();
        if(template != null){
            fileName += "_" + template.getTemplateName();
        }

        fileName += "_flow_record_" + recordId + ".csv";

        response.setStatus(200);
        response.setContentType("application/csv;charset=UTF-8");
        response.setHeader("Content-Disposition","attachment; filename=" + fileName);
        flowRecordService.downloadRecord(response.getOutputStream(), recordId);
    }
    
    @RequestMapping(path = "/update_num.json", method = RequestMethod.POST)
    public void updateNum(@RequestBody FlowRecordOp params) {
        Long recordId = params.getRecordId();
        Integer recordNum = params.getRecordNum();

        if(recordNum == null) {
            throw new BusinessLevelException(500, "recordNum不能为空");
        }

        FlowRecord flowRecord = flowRecordService.findFlowRecordById(recordId);

        if(flowRecord == null) {
            throw new BusinessLevelException(500, "根据recordId + " + recordId + "找不到相关记录");
        }

        if(flowRecord.getState() == FlowRecordState.PENDING.getValue()){
            flowRecord.setState((int) FlowRecordState.RECORDING.getValue());
            flowRecordService.update(flowRecord);
        }

        flowRecordService.increRecordTotal(recordId, recordNum);
    }

    @RequestMapping(path = "/available/list.json", method = { RequestMethod.GET})
    public List<FlowRecord> getAvailableList() {
        return flowRecordService.getAvailableRecording();
    }
}