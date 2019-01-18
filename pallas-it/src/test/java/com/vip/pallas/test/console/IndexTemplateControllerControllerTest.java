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

import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpStatus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vip.pallas.console.vo.IndexVO;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.ResultVO;
import com.vip.pallas.console.vo.TemplateOp;
import com.vip.pallas.console.vo.TemplateVO;
import com.vip.pallas.mybatis.entity.Approve;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.test.base.BaseSpringEsTest;
import com.vip.pallas.test.utils.ObjectJsonUtils;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IndexTemplateControllerControllerTest extends BaseSpringEsTest {

    private static IndexVO indexVO = null;
    private static Long templateId = null;
    private static String templateName = null;
    private static Long clusterId = null;
    private static Long approveId = null;
    private static String templateContent = "{\r\n" +
            "    \"query\": {\r\n" +
            "        \"bool\": {\r\n" +
            "            \"filter\": [\r\n" +
            "                {\"match_all\":{}}    \r\n" +
            "            ]\r\n" +
            "        }\r\n" +
            "        \r\n" +
            "    }\r\n" +
            "    \r\n" +
            "}";

    @Test
    public void test11Add() throws Exception {
        indexVO = insertIndexToTable();

        TemplateVO templateVO = ObjectJsonUtils.getTemplateVO();
        templateVO.setIndexId(indexVO.getIndexId());

        assertThat(callRestApi("/index_template/add.json", JSON.toJSONString(templateVO))).isNull();

        templateName = templateVO.getTemplateName();

        ResultVO resultVO = callGetApi("/cluster/id.json?clusterId=" + indexVO.getClusterId(), Cluster.class);
        Cluster cluster = (Cluster)resultVO.getData();
        clusterId = cluster.getId();
    }

    @Test
    public void test12List() throws Exception {
        Map map = callGetApi("/index_template/list.json?indexId=" + indexVO.getIndexId());
        assertThat(map.get("status")).isEqualTo(HttpStatus.OK.value());

        JSONArray jsonArray =((JSONObject) map.get("data")).getJSONArray("list");

        assertThat(jsonArray.size()).isEqualTo(1);

        JSONObject jsonObject = jsonArray.getJSONObject(0);
        templateId = jsonObject.getLong("id");
        assertThat(templateId).isNotNull();
    }

    @Test
    public void test13UpdateContent() throws Exception {
        TemplateVO updateVO = new TemplateVO();
        updateVO.setIndexId(indexVO.getIndexId());
        updateVO.setTemplateName(templateName);
        updateVO.setParams("");
        updateVO.setContent(templateContent);

        assertThat(callRestApi("/index_template/update.json", JSON.toJSONString(updateVO))).isNull();
    }

    @Test
    public void test14Render() throws Exception {
        String renderRequestBodyString = "{\"indexId\":" + indexVO.getIndexId() + ",\"templateName\":\"" + templateName + "\",\"params\":\"{\\n    \\n}\",\"clusterId\":" + clusterId + "}";
        Map resultMap = callRestApi("/index_template/render.json", renderRequestBodyString);

        assertThat(resultMap).containsEntry("status", 200);

        Assert.assertTrue(resultMap.get("data").toString().contains("query"));
    }

    @Test
    public void test15Debug() throws Exception {
        String renderRequestBodyString = "{\"indexId\":" + indexVO.getIndexId() + ",\"templateName\":\"" + templateName + "\",\"params\":\"{\\n    \\n}\",\"clusterId\":" + clusterId + "}";
        Map resultMap = callRestApi("/index_template/debug.json", renderRequestBodyString);

        assertThat(resultMap).containsEntry("status", 200);

       // Assert.assertTrue(resultMap.get("data").toString().contains("total"));
    }

    @Test
    public void test16genApi() throws Exception {
        String requestBodyString = "{\"indexId\":" + indexVO.getIndexId() + ",\"templateName\":\"" + templateName + "\"}";
        Map resultMap = callRestApi("/index_template/genapi.json", requestBodyString);

        assertThat(resultMap).containsEntry("status", 200);
        Assert.assertTrue(resultMap.get("data").toString().contains("content"));
    }

    //提交，审核部分
    @Test
    public void test61SubmitApprove() throws Exception {
        TemplateOp templateOp = new TemplateOp();
        templateOp.setIndexId(indexVO.getIndexId());
        templateOp.setTemplateName(templateName);
        templateOp.setContent(templateContent);
        templateOp.setParams("\"{↵    ↵}\"");
        templateOp.setHistoryDesc("approve desc");

        Map resultMap = callRestApi("/index_template/approve.json", JSON.toJSONString(templateOp));
        assertThat(resultMap).containsEntry("status", 200);
        approveId = Long.parseLong(String.valueOf(resultMap.get("data")));
        assertThat(approveId).isNotNull();
    }

    @Test
    public void test62ApprovePage() throws Exception {
        ResultVO<PageResultVO> resultVO  = callGetApi("/approve/approve/page.json?currentPage=1&pageSize=10&state=0&conditions=", PageResultVO.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());

        PageResultVO<Approve> pageResultVO = resultVO.getData();
        assertThat(pageResultVO.getList()).isNotNull();
        assertThat(pageResultVO.getList().size()).isGreaterThanOrEqualTo(1);
        assertThat(pageResultVO.getTotal()).isGreaterThanOrEqualTo(1L);
    }

    @Test
    public void test63Approve() throws Exception {
        assertThat(callRestApi("/approve/approve.json", "{\"ids\":\"" + approveId + "\",\"state\":\"1\",\"note\":\"approve\"}")).isNull();
    }

    @Test
    public void test64ApplyPage() throws Exception {
        ResultVO<PageResultVO> resultVO =  callGetApi("/approve/apply/page.json", PageResultVO.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());
    }


    @AfterClass
    public static void cleanData() throws Exception{
        assertThat(callRestApi("/index_template/delete.json", "{\"indexId\":" + indexVO.getIndexId() + ",\"indexName\":\"" + indexVO.getIndexName() + "\",\"templateId\":" + templateId + ",\"templateName\": \"" + templateName +"\"}")).isNull();
        assertThat(callRestApi("/index/delete/id.json", "{\"indexId\": \"" + indexVO.getIndexId() + "\"}")).isNull();
        // assertThat(callRestApi("/cluster/delete/id.json", "{\"clusterId\": \"" + indexVO.getClusterId() + "\"}")).isNull();
    }
}