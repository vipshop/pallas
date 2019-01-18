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

import com.alibaba.fastjson.JSONArray;
import com.vip.pallas.bean.IndexVersion;
import com.vip.pallas.console.vo.IndexVO;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.ResultVO;
import com.vip.pallas.console.vo.base.BaseIndexVersionOp;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.test.base.BaseSpringEsTest;
import com.vip.pallas.utils.JsonUtil;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IndexVersionControllerTest extends BaseSpringEsTest {

    private static Long clusterId = null;
    private static Long indexId = null;
    private static Long indexVersionId = null;
    private static IndexVO indexVO = null;

    @Test
    public void test11AddIndexVersion() throws Exception{
        indexVO = insertIndexToTable();
        indexId = indexVO.getIndexId();

        ResultVO<Cluster> resultVO = callGetApi("/cluster/id.json?clusterId=" + EMBEDDED_CLUTER_ID, Cluster.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());
        clusterId = resultVO.getData().getId();

        //add indevVersion
        String requestBody = readFile("request/indexVersionInsert.json").replace("{indexId}", indexId.toString()).replace("{clusterId}", "" + clusterId);
        Map resultMap = callRestApi("/index/version/add.json", requestBody);
        assertThat(resultMap).isNotNull();
        assertThat(resultMap.get("status")).isEqualTo(HttpStatus.OK.value());

        indexVersionId = Long.parseLong(resultMap.get("data").toString());
    }

    @Test
    public void test12FindById() throws Exception{
        ResultVO<IndexVersion> resultVO =  callGetApi("/index/version/id.json?versionId=" + indexVersionId, IndexVersion.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(resultVO.getData()).isNotNull();
    }

    @Test
    public void test13Page() throws Exception {
        ResultVO<PageResultVO> resultVO  = callGetApi("/index/version/page.json?indexId=" + indexId, PageResultVO.class);
        assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());

        PageResultVO<com.vip.pallas.mybatis.entity.IndexVersion> pageResultVO = resultVO.getData();
        assertThat(pageResultVO.getList()).isNotNull();
        assertThat(pageResultVO.getList().size()).isGreaterThanOrEqualTo(1);
        assertThat(pageResultVO.getTotal()).isGreaterThanOrEqualTo(1L);
    }

    @Test
    public void test14Conut() throws Exception {
        String requestBodyString = "{\"indexName\":\"" + indexVO.getIndexName() +"\",\"versionIds\":[" + indexVersionId + "]}";
        Map map = callRestApi("/index/version/count.json", requestBodyString);
        assertThat(map).containsEntry("status", HttpStatus.OK.value());
    }

    @Test
    public void test15Info() throws Exception {

        Map map  = callGetApi("/index/version/info.json?indexName=" + indexVO.getIndexName() + "&versionId=" + indexVersionId);
        assertThat(map.get("status")).isEqualTo(HttpStatus.OK.value());
        assertThat(map.get("data")).isNotNull().isEqualTo("该版本信息未在ES初始化,请先点击开始同步！");
    }

    @Test
    public void test16update() throws Exception {

        String requestBody = readFile("request/indexVersionUpdate.json").replace("{indexId}", indexId.toString()).replace("{clusterId}", "" + clusterId) + "\n" + "\"id\": " + indexVersionId + "\n" + "}";
        assertThat(callRestApi("/index/version/update.json", requestBody)).isNull();
    }

    @Test
    public void test17Metadata() throws Exception {
        //index无datasource
        Assert.assertTrue(((JSONArray)((Map)(callRestApi("/index/version/metadata.json", "{\"indexId\": \"" + indexId + "\"}").get("data"))).get("list")).size() == 0);
        //index有datasource
        //Assert.assertTrue(((JSONArray)((Map)(callRestApi("/index/version/metadata.json", "{\"indexId\": \"" + indexId + "\"}").get("data"))).get("list")).size() > 0);
    }

    @Test
    public void test18CopyVersion() throws Exception {
        BaseIndexVersionOp params = new BaseIndexVersionOp();
        params.setIndexId(indexId);
        params.setVersionId(indexVersionId);

        assertThat(callRestApi("/index/version/copy.json", JsonUtil.toJson(params))).isNotNull();
    }

    @Test
    public void test21CreateIndex() throws Exception {
        BaseIndexVersionOp params = new BaseIndexVersionOp();
        params.setIndexId(indexId);
        params.setVersionId(indexVersionId);

        assertThat(callRestApi("/index/version/create_index.json", JsonUtil.toJson(params))).isNull();
    }


    @Test
    public void test22Enable() throws Exception {

        BaseIndexVersionOp params = new BaseIndexVersionOp();
        params.setIndexId(indexId);
        params.setVersionId(indexVersionId);
        assertThat(callRestApi("/index/version/enable.json", JsonUtil.toJson(params))).isNull();
    }

    @Test
    public void test23Disable() throws Exception {
        BaseIndexVersionOp params = new BaseIndexVersionOp();
        params.setIndexId(indexId);
        params.setVersionId(indexVersionId);
        assertThat(callRestApi("/index/version/disable.json", JsonUtil.toJson(params))).isNull();
    }


    @AfterClass
    public static void cleanData() throws Exception{
        assertThat(callRestApi("/index/version/delete/id.json", "{\"versionId\": \"" + indexVersionId + "\",\"indexId\":" + indexId + "}")).isNull();
        assertThat(callRestApi("/index/delete/id.json", "{\"indexId\": \"" + indexId + "\"}")).isNull();
        // assertThat(callRestApi("/cluster/delete/id.json", "{\"clusterId\": \"" + clusterName + "\"}")).isNull();
    }

}