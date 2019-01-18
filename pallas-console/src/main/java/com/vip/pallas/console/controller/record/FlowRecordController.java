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

package com.vip.pallas.console.controller.record;

import com.vip.pallas.bean.FlowRecordState;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.base.BaseFlowRecordOp;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.*;
import com.vip.pallas.service.ElasticSearchService;
import com.vip.pallas.service.FlowRecordService;
import com.vip.pallas.service.IndexService;
import com.vip.pallas.utils.PallasConsoleProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import java.util.List;

@Validated
@RestController
@RequestMapping("/record/flow_record")
public class FlowRecordController{
    private static Logger logger = LoggerFactory.getLogger(FlowRecordController.class);

    @Autowired
    private FlowRecordService flowRecordService;
    
    @Autowired
    private IndexService indexService;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @RequestMapping(path = "/page_by_config.json", method = {RequestMethod.POST, RequestMethod.GET})
	public PageResultVO<FlowRecord> queryPageByConfig(HttpServletRequest req,
			@RequestParam(required = false, defaultValue = "1") @Min(value = 0, message = "currentPage必须为正数") Integer currentPage,
			@RequestParam(required = false, defaultValue = "10") @Min(value = 0, message = "pageSize必须为正数") Integer pageSize,
			@RequestParam @NotNull(message = "indexId不能为空") @Min(value = 1, message = "indexId不能小于1") Long indexId, 
			@RequestParam @NotNull(message = "configId不能为空") @Min(value = 1, message = "configId不能小于1") Long configId) {
    	PageResultVO<FlowRecord> resultVO = new PageResultVO<>();
    	Index index = indexService.findById(indexId);
    	if (ObjectUtils.isEmpty(index)) {
    		throw new BusinessLevelException(500, "index不存在");
    	}
    	if (AuthorizeUtil.authorizeIndexPrivilege(req, indexId, index.getIndexName())) {
    		resultVO.setAllPrivilege(true);
    	}
    	
        Page<FlowRecord> page = new Page<>();
        page.setPageNo(currentPage);
        page.setPageSize(pageSize);

        List<FlowRecord> flowRecordList = flowRecordService.findFlowRecordByConfig(page, indexId, configId);

        resultVO.setList(flowRecordList);
        resultVO.setTotal(page.getTotalRecord());
        resultVO.setPageCount(page.getTotalPage());
        return resultVO;
    }

    @RequestMapping(path = "/list.json", method = {RequestMethod.GET})
    public List<FlowRecord> getTemplates(@RequestParam(required = false, defaultValue = "true") boolean doneOnly,
                                          @RequestParam(required = false) Long indexId) {
        List<FlowRecord> list = flowRecordService.findAllFlowRecord();

        if (doneOnly) {
            list.removeIf( r -> r.getState() != FlowRecordState.FINISH.getValue() || r.getTotal() <= 0 || r.getIsDeleted());
        }

        if (indexId != null) {
            list.removeIf( r -> !r.getIndexId().equals(indexId));
        }
        return list;
    }

    @RequestMapping(path = "/delete.json", method = RequestMethod.POST)
    public void delete(@RequestBody @Validated BaseFlowRecordOp params, HttpServletRequest request) {
    	Index index = indexService.findById(params.getIndexId());
    	
    	if (ObjectUtils.isEmpty(index)) {
    		throw new BusinessLevelException(500, "index不存在");
    	}
    	
    	if (! AuthorizeUtil.authorizeIndexPrivilege(request, params.getIndexId(), index.getIndexName())) {
    		throw new BusinessLevelException(403, "无权限操作");
    	}
    	
        Long recordId = params.getRecordId();
        FlowRecord flowRecord = flowRecordService.findFlowRecordById(recordId);

        if(flowRecord == null) {
            throw new BusinessLevelException(500, "根据recordId + " + recordId + "找不到相关记录");
        }

        int state = flowRecord.getState();
        if(state != FlowRecordState.FINISH.getValue()
                && state != FlowRecordState.STOP.getValue()) {
            throw new BusinessLevelException(500, "当前不处于完成或者停止状态，不允许删除");
        }

        flowRecord.setIsDeleted(Boolean.TRUE);
        flowRecordService.update(flowRecord);

        //把相关联的配置也禁用掉（如果没有该配置关联的其他采集在进行的话）
        flowRecordService.disableConfigIfNecessary(recordId);

        try{
            elasticSearchService.deleteIndex(PallasConsoleProperties.FLOW_RECORD_SAVE_CLUSTER_REST_ADDRESS,
                    ".pallas_search_record_" + recordId);
        }catch(Exception e){
            logger.error(e.toString(), e);
        }

    }

    @RequestMapping(path = "/stop.json", method = RequestMethod.POST)
    public void stop(@RequestBody @Validated BaseFlowRecordOp params, HttpServletRequest request) {
    	Index index = indexService.findById(params.getIndexId());
    	
    	if (ObjectUtils.isEmpty(index)) {
    		throw new BusinessLevelException(500, "index不存在");
    	}
    	
    	if (!AuthorizeUtil.authorizeIndexPrivilege(request, params.getIndexId(), index.getIndexName())) {
    		throw new BusinessLevelException(403, "无权限操作");
    	}
    	
        Long recordId = params.getRecordId();
        FlowRecord flowRecord = flowRecordService.findFlowRecordById(recordId);

        if(flowRecord == null) {
            throw new BusinessLevelException(500, "根据recordId + " + recordId + "找不到相关记录");
        }

        flowRecord.setState((int) FlowRecordState.STOP.getValue());
        flowRecordService.update(flowRecord);

        //把相关联的配置也禁用掉（如果没有该配置关联的其他采集在进行的话）
        flowRecordService.disableConfigIfNecessary(recordId);
    }
}