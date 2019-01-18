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
import java.util.Map;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpStatus;

import com.alibaba.fastjson.JSON;
import com.vip.pallas.console.vo.IndexOp;
import com.vip.pallas.console.vo.IndexVO;
import com.vip.pallas.console.vo.ResultVO;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.test.base.BaseSpringEsTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IndexControllerTest extends BaseSpringEsTest {

    private static String clusterId = null;
    private static Long indexId = null;
    private static IndexVO indexVO = null;

    @Test
    public void test11AddIndex() throws Exception{
        indexVO = insertIndexToTable();
        clusterId = indexVO.getClusterId();
        indexId = indexVO.getIndexId();
    }

    @Test
    public void test12All() throws Exception {
        ResultVO<List> resultVO =  callGetApi("/index/list/all.json", List.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());

        List<Index> indices = resultVO.getData();
        assertThat(indices.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void test13FindById() throws Exception{
        ResultVO<Index> resultVO = callGetApi("/index/id.json?indexId=" + indexId, Index.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());

        Index index = resultVO.getData();
        assertThat(index).isNotNull();
    }

    @Test
    public void test14Update() throws Exception{
        indexVO.setDescription("update index");
        Map resultMap = callRestApi("/index/update.json", JSON.toJSONString(indexVO));
        assertThat(resultMap).containsEntry("status", HttpStatus.OK.value());
        assertThat(resultMap.get("data")).isEqualTo("更新成功");
    }

    @Test
    public void test15UpdateTimeoutRetry() throws Exception{
        IndexOp params = new IndexOp();
        params.setIndexId(indexId);
        params.setRetry(0);
        params.setTimeout(0);
        params.setSlowerThan(200);
        assertThat(callRestApi("/index/update/timeout_retry.json", JSON.toJSONString(params))).isNull();
    }

    //index dynamic
    @Test
    public void test41Dynamic() throws Exception {
        String requestBodyString = "{\"currentPage\":1,\"pageSize\":10,\"indexId\":" + indexId + ",\"selectedType\":\"\",\"filterVersion\":\"\",\"timeRange\":\"\"}";
        Map resultMap =  callRestApi("/index/dynamic/page.json", requestBodyString);
        assertThat(resultMap).containsEntry("status", HttpStatus.OK.value());
        assertThat(resultMap.get("data")).isNotNull();

        requestBodyString =  "{\"currentPage\":1,\"pageSize\":10,\"indexId\":" + indexId + ",\"selectedType\":\"索引事件\",\"filterVersion\":\"\",\"timeRange\":\"\"}";
        resultMap =  callRestApi("/index/dynamic/page.json", requestBodyString);
        assertThat(resultMap).containsEntry("status", HttpStatus.OK.value());
        assertThat(resultMap.get("data")).isNotNull();

    }

    @AfterClass
    public static void cleanData() throws IOException {
        assertThat(callRestApi("/index/delete/id.json", "{\"indexId\": \"" + indexId + "\"}")).isNull();
        // assertThat(callRestApi("/cluster/delete/id.json", "{\"clusterId\": \"" + clusterId + "\"}")).isNull();
    }
}