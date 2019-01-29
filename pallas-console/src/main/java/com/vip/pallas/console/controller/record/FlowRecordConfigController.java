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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.bean.FlowRecordState;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.utils.SessionUtil;
import com.vip.pallas.console.vo.FlowRecordConfigOp;
import com.vip.pallas.console.vo.FlowRecordConfigVO;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.base.BaseFlowRecordConfigOp;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.FlowRecord;
import com.vip.pallas.mybatis.entity.FlowRecordConfig;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.service.FlowRecordService;
import com.vip.pallas.service.IndexService;

@Validated
@RestController
@RequestMapping("/record/flow_record_config")
public class FlowRecordConfigController {

    @Autowired
    private FlowRecordService flowRecordService;
    
    @Autowired
    private IndexService indexService;

    @RequestMapping(path = "/page.json", method = {RequestMethod.GET})
    public PageResultVO<FlowRecordConfig>queryPage(HttpServletRequest req ,
                     @RequestParam(required = false, defaultValue = "1") @Min(value = 1, message = "currentPage必须为正数") Integer currentPage,
                     @RequestParam(required = false, defaultValue = "10") @Min(value = 1, message = "pageSize必须为正数") Integer pageSize,
                     @RequestParam @NotNull(message = "indexId不能为空") @Min(value = 1, message = "indexId必须为正数") Long indexId) {
    	PageResultVO<FlowRecordConfig> resultVO = new PageResultVO<>();
    	Index index = indexService.findById(indexId);
    	if (ObjectUtils.isEmpty(index)) {
    		throw new BusinessLevelException(500, "index不存在");
    	}
    	if (AuthorizeUtil.authorizeIndexPrivilege(req, indexId, index.getIndexName())) {
    		resultVO.setAllPrivilege(true);
    	}
    	
        Page<FlowRecordConfig> page = new Page<>();
        page.setPageNo(currentPage);
        page.setPageSize(pageSize);
        List<FlowRecordConfig> flowRecordConfigList = flowRecordService.findFlowRecordConfigByPage(page, indexId);
        
        resultVO.setList(flowRecordConfigList);
        resultVO.setTotal(page.getTotalRecord());
        resultVO.setPageCount(page.getTotalPage());

        return resultVO;

    }

    @RequestMapping(path = "/list.json", method = {RequestMethod.GET})
    public List<FlowRecordConfig>  getList() {
        List<FlowRecordConfig> list = flowRecordService.findAllFlowRecordConfig();
        return list;
    }

    @RequestMapping(path = "/add.json", method = RequestMethod.POST)
    public void addConfig(HttpServletRequest req, @RequestBody @Validated FlowRecordConfigVO params) throws Exception {
    	Index index = indexService.findById(params.getIndexId());
    	if (ObjectUtils.isEmpty(index)) {
    		throw new BusinessLevelException(500, "index不存在");
    	}
    	if (!AuthorizeUtil.authorizeIndexPrivilege(req, params.getIndexId(), index.getIndexName())) {
    		throw new BusinessLevelException(403, "无权限操作");
    	}
    	
        FlowRecordConfig config = getFlowRecordConfig(req, params);
        flowRecordService.insert(config);
    }

    @RequestMapping(path = "/edit.json", method = {RequestMethod.POST, RequestMethod.PUT})
    public void editConfig(HttpServletRequest req, @RequestBody @Validated FlowRecordConfigVO params) throws Exception{
    	Long id = params.getId();
        if(id == null) {
            throw new BusinessLevelException(500, "id不能为空");
        }
        
    	Index index = indexService.findById(params.getIndexId());
    	if (ObjectUtils.isEmpty(index)) {
    		throw new BusinessLevelException(500, "index不存在");
    	}
    	if (!AuthorizeUtil.authorizeIndexPrivilege(req, params.getIndexId(), index.getIndexName())) {
    		throw new BusinessLevelException(403, "无权限操作");
    	}

        FlowRecordConfig config = getFlowRecordConfig(req, params);
        config.setId(id);
        flowRecordService.update(config);
    }

    @RequestMapping(path = "/enable.json", method = RequestMethod.POST)
    public void enableConfig(@RequestBody @Validated BaseFlowRecordConfigOp params, HttpServletRequest req) {
    	Index index = indexService.findById(params.getIndexId());
    	if (ObjectUtils.isEmpty(index)) {
    		throw new BusinessLevelException(500, "index不存在");
    	}
    	if (!AuthorizeUtil.authorizeIndexPrivilege(req, params.getIndexId(), index.getIndexName())) {
    		throw new BusinessLevelException(403, "无权限操作");
    	}
    	
        FlowRecordConfig config = flowRecordService.findFlowRecordConfigById(params.getConfigId());
        if(config == null) {
            throw new BusinessLevelException(500, "找不到相关记录");
        }

        if(config.getIsEnable()){
            throw new BusinessLevelException(500, "当前已经是启用状态");
        }

        if(System.currentTimeMillis() > config.getEndTime().getTime()){
            throw new BusinessLevelException(500, "采集结束时间早于当前时间，请修改后再启用");
        }

        config.setIsEnable(Boolean.TRUE);
        flowRecordService.update(config);

        //初始化记录
        FlowRecord record = new FlowRecord();
        record.setTotal(0L);
        record.setConfigId(config.getId());
        record.setState((int) FlowRecordState.PENDING.getValue());
        record.setNote(config.getNote());
        record.setIndexId(config.getIndexId());
        record.setTemplateId(config.getTemplateId());
        record.setSampleRate(config.getSampleRate());
        record.setLimit(config.getLimit());
        record.setStartTime(config.getStartTime());
        record.setEndTime(config.getEndTime());
        record.setCreateTime(new Date());

        flowRecordService.insert(record);
    }

    @RequestMapping(path = "/disable.json", method = RequestMethod.POST)
    public void disableConfig(@RequestBody @Validated BaseFlowRecordConfigOp params, HttpServletRequest req) {
    	Index index = indexService.findById(params.getIndexId());
    	if (ObjectUtils.isEmpty(index)) {
    		throw new BusinessLevelException(500, "index不存在");
    	}
    	if (!AuthorizeUtil.authorizeIndexPrivilege(req, params.getIndexId(), index.getIndexName())) {
    		throw new BusinessLevelException(403, "无权限操作");
    	}

        FlowRecordConfig config = flowRecordService.findFlowRecordConfigById(params.getConfigId());
        if(config == null) {
            throw new BusinessLevelException(500, "找不到相关记录");
        }

        if(!config.getIsEnable()){
            throw new BusinessLevelException(500, "当前已经是禁用状态");
        }

        config.setIsEnable(Boolean.FALSE);
        flowRecordService.update(config);

        List<FlowRecord> recordList = flowRecordService.getRecordingByConfigId(params.getConfigId());
        if(recordList != null){
            recordList.stream().forEach(record -> {
                record.setState((int) FlowRecordState.STOP.getValue());
                flowRecordService.update(record);
            });
        }
    }

    @RequestMapping(path = "/delete.json", method = RequestMethod.POST)
    public void deleteConfig(@RequestBody @Validated BaseFlowRecordConfigOp params, HttpServletRequest req) {
    	Index index = indexService.findById(params.getIndexId());
    	if (ObjectUtils.isEmpty(index)) {
    		throw new BusinessLevelException(500, "index不存在");
    	}
    	if (!AuthorizeUtil.authorizeIndexPrivilege(req, params.getIndexId(), index.getIndexName())) {
    		throw new BusinessLevelException(403, "无权限操作");
    	}

        Long configId = params.getConfigId();
        FlowRecordConfig config = flowRecordService.findFlowRecordConfigById(configId);

        if(config == null) {
            throw new BusinessLevelException(500, "找不到相关记录");
        }

        if(config.getIsDeleted()){
            throw new BusinessLevelException(500, "记录已经被删除");
        }

        config.setIsDeleted(Boolean.TRUE);
        flowRecordService.update(config);

        List<FlowRecord> recordList = flowRecordService.getRecordingByConfigId(configId);
        if(recordList != null){
            recordList.stream().forEach(record -> {
                record.setState((int) FlowRecordState.STOP.getValue());
                flowRecordService.update(record);
            });
        }
    }

    @RequestMapping(path = "/find.json", method = {RequestMethod.POST})
    public List<FlowRecordConfig> find(@RequestBody FlowRecordConfigOp params) {
        String clusterName = params.getClusterName();

        if(clusterName == null) {
            throw new BusinessLevelException(500, "clusterName不能为空");
        }

        String indexName = params.getIndexName();

        if(indexName == null) {
            throw new BusinessLevelException(500, "indexName不能为空");
        }

        List<FlowRecordConfig> list = flowRecordService.findFlowRecordConfigByClusterAndIndex(clusterName, indexName);
        return list;
    }

    @RequestMapping(path = "/id.json", method = {RequestMethod.GET})
	public FlowRecordConfig findById(
			@RequestParam @NotNull(message = "configId不能为空") @Min(value = 1, message = "configId必须为正数") Long configId) {
        FlowRecordConfig config = flowRecordService.findFlowRecordConfigById(configId);

        if(config == null) {
            throw new BusinessLevelException(500, "找不到相关记录");
        }

        return config;
    }

    private FlowRecordConfig getFlowRecordConfig(HttpServletRequest req,FlowRecordConfigVO params) throws Exception{
        if(params.getSampleRate()  <= 0 || params.getSampleRate() > 1) {
            throw new BusinessLevelException(500, "sampleRate必须介于0到1之间");
        }

        FlowRecordConfig config = new FlowRecordConfig();
        BeanUtils.copyProperties(params, config);

        config.setIsEnable(Boolean.FALSE);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        try{
            config.setStartTime(format.parse(params.getStartTime().replaceAll("\"", "")));
            config.setStartTime(new Date(config.getStartTime().getTime() + 8 * 60 * 60 * 1000));
        }catch(Exception ignore){
            config.setStartTime(new Date(Long.parseLong(params.getStartTime())));
        }

        try{
            config.setEndTime(format.parse(params.getEndTime().replaceAll("\"", "")));
            config.setEndTime(new Date(config.getEndTime().getTime() + 8 * 60 * 60 * 1000));
        }catch(Exception ignore){
            config.setEndTime(new Date(Long.parseLong(params.getEndTime())));
        }

        String currentUser = SessionUtil.getLoginUser(req); 
        config.setCreateUser(currentUser);

        if(config.getEndTime().getTime() < config.getStartTime().getTime()) {
            throw new BusinessLevelException(500, "结束时间必须大于开始时间");
        }

        if(config.getEndTime().getTime() <= System.currentTimeMillis()) {
            throw new BusinessLevelException(500, "结束时间不能晚于当前时间");
        }
        return config;
    }

}