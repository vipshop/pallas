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

package com.vip.pallas.test.console;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpStatus;

import com.alibaba.fastjson.JSONObject;
import com.vip.pallas.console.vo.FlowRecordConfigOp;
import com.vip.pallas.console.vo.FlowRecordConfigVO;
import com.vip.pallas.console.vo.FlowRecordOp;
import com.vip.pallas.console.vo.IndexVO;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.ResultVO;
import com.vip.pallas.console.vo.base.BaseFlowRecordConfigOp;
import com.vip.pallas.console.vo.base.BaseFlowRecordOp;
import com.vip.pallas.mybatis.entity.FlowRecordConfig;
import com.vip.pallas.test.base.BaseSpringEsTest;
import com.vip.pallas.test.utils.ObjectJsonUtils;
import com.vip.pallas.utils.JsonUtil;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FlowRecordConfigControllerTest extends BaseSpringEsTest {

    private static String clusterId = null;
    private static Long indexId = null;
    private static IndexVO indexVO = null;
    private static FlowRecordConfigVO flowRecordConfigVO =ObjectJsonUtils.getFlowRecordConfigVO();
    private static Long flowConfigId = null;
    private static Long flowRecordId = null;

    @Test
    public void test11AddIndex() throws Exception{
        indexVO = insertIndexToTable();
        clusterId = indexVO.getClusterId();
        indexId = indexVO.getIndexId();
    }

    @Test
    public void test12Page() throws Exception {
        ResultVO<PageResultVO> resultVO = callGetApi("/record/flow_record_config/page.json?currentPage=1&pageSize=10&indexId=" + indexId, PageResultVO.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());

        FlowRecordConfigOp flowRecordConfigOp = new FlowRecordConfigOp();
        flowRecordConfigOp.setClusterName(clusterId);
        flowRecordConfigOp.setIndexName(indexVO.getIndexName());
       // assertThat(callRestApi("/record/flow_record_config/find.json", JsonUtil.toJson(flowRecordConfigOp))).isNull();
    }

    @Test
    public void test13AddFlowConfig() throws Exception{
        flowRecordConfigVO.setIndexId(indexId);
        assertThat(callRestApi("/record/flow_record_config/add.json", JsonUtil.toJson(flowRecordConfigVO))).isNull();

        ResultVO<PageResultVO> resultVO = callGetApi("/record/flow_record_config/page.json?currentPage=1&pageSize=10&indexId=" + indexId, PageResultVO.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());

        PageResultVO pageResultVO = resultVO.getData();
        assertThat(pageResultVO).isNotNull();
        List<JSONObject> configs = pageResultVO.getList();
        assertThat(configs).isNotNull();
        assertThat(configs.size()).isEqualTo(1);
        flowConfigId = configs.get(0).getLong("id");

        ResultVO<FlowRecordConfig> configResultVO = callGetApi("/record/flow_record_config/id.json?configId=" + flowConfigId, FlowRecordConfig.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void test14List() throws Exception {
        ResultVO<List> resultVO =  callGetApi("/record/flow_record_config/list.json", List.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void test15EditFLowConfig() throws Exception{

        flowRecordConfigVO.setId(flowConfigId);
        flowRecordConfigVO.setNote("desc edit");
        assertThat(callRestApi("/record/flow_record_config/edit.json", JsonUtil.toJson(flowRecordConfigVO))).isNull();
    }

    @Test
    public void test161EnableFlowConfig() throws Exception{
        BaseFlowRecordConfigOp params = new BaseFlowRecordConfigOp();
        params.setIndexId(indexId);
        params.setConfigId(flowConfigId);
        assertThat(callRestApi("/record/flow_record_config/enable.json", JsonUtil.toJson(params))).isNull();

        ResultVO<PageResultVO> resultVO = callGetApi("/record/flow_record/page_by_config.json?configId=" + flowConfigId + "&indexId=" + indexId + "&currentPage=1&pageSize=10", PageResultVO.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());
        PageResultVO pageResultVO= resultVO.getData();
        List<JSONObject> records = pageResultVO.getList();
        assertThat(records).isNotNull();
        assertThat(records.size()).isEqualTo(1);
        flowRecordId = records.get(0).getLong("id");

        ResultVO<List> resultListVO =  callGetApi("/record/flow_record/list.json?indexId=" + indexId, List.class);
        assertThat(resultListVO.getStatus()).isEqualTo(HttpStatus.OK.value());


    }

    @Test
    public void test162FlowRecordApi() throws Exception {
        ResultVO<List> resultVO =  callGetApi("/record/flow_record/available/list.json", List.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());

        FlowRecordOp recordOp = new FlowRecordOp();
        recordOp.setRecordId(flowRecordId);
        recordOp.setRecordNum(2);
        assertThat(callRestApi("/record/flow_record/update_num.json", JsonUtil.toJson(recordOp))).isNull();

       // assertThat(callRestApi("/record/flow_record/export.json?recordId="+ flowRecordId, JsonUtil.toJson(recordOp))).isNull();

        ResultVO<List> resultVOTemplate =  callGetApi("/record/index_template/list.json?indexId=" + indexId, List.class);
        assertThat(resultVOTemplate.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void test17DisableFlowRecord() throws Exception{
        BaseFlowRecordOp params = new BaseFlowRecordOp();
        params.setIndexId(indexId);
        params.setRecordId(flowRecordId);
        assertThat(callRestApi("/record/flow_record/stop.json", JsonUtil.toJson(params))).isNull();

    }

    @Test
    public void test18DisableFlowConfig() throws Exception{
        BaseFlowRecordConfigOp params = new BaseFlowRecordConfigOp();
        params.setIndexId(indexId);
        params.setConfigId(flowConfigId);

        assertThat(callRestApi("/record/flow_record_config/disable.json", JsonUtil.toJson(params))).containsEntry("status", 500);
    }

    @Test
    public void test19DeleteFlowRecord() throws Exception{
        BaseFlowRecordOp params = new BaseFlowRecordOp();
        params.setIndexId(indexId);
        params.setRecordId(flowRecordId);
        assertThat(callRestApi("/record/flow_record/delete.json", JsonUtil.toJson(params))).isNull();
    }

    @Test
    public void test21DeleteFlowConfig() throws Exception{
        BaseFlowRecordConfigOp params = new BaseFlowRecordConfigOp();
        params.setIndexId(indexId);
        params.setConfigId(flowConfigId);
        assertThat(callRestApi("/record/flow_record_config/delete.json", JsonUtil.toJson(params))).isNull();
    }


    @AfterClass
    public static void cleanData() throws IOException {
        assertThat(callRestApi("/index/delete/id.json", "{\"indexId\": \"" + indexId + "\"}")).isNull();
        // assertThat(callRestApi("/cluster/delete/id.json", "{\"clusterId\": \"" + clusterId + "\"}")).isNull();
    }

}