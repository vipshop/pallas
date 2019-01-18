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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpStatus;

import com.alibaba.fastjson.JSONObject;
import com.vip.pallas.console.vo.IndexVO;
import com.vip.pallas.test.base.BaseSpringEsTest;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IndexRoutingControllerControllerTest extends BaseSpringEsTest {
    private static Long indexId = null;
    private static String indexName = null;
    private static List<Long> groupIds = new ArrayList<>();
    private static Long groupId = null;
    private static String clusterId = null;

    @Test
    public void test1AddIndex() throws Exception {
        IndexVO indexVO = insertIndexToTable();
        indexId = indexVO.getIndexId();
        indexName = indexVO.getIndexName();
        clusterId = indexVO.getClusterId();
    }

    @Test
    public void test2RuleList() throws Exception {
        Map resultMap =  callRestApi("/index/routing/list.json", "{\"indexId\":" + indexId + ",\"indexName\":\"" + indexName +  "\"}");
        assertThat(resultMap).containsEntry("status", HttpStatus.OK.value());

    }

    @Test
    public void test3GroupList() throws Exception {
        Map resultMap =  callRestApi("/index/routing/target_group/list.json", "{\"indexId\":" + indexId + ",\"indexName\":\"" + indexName +  "\"}");
        assertThat(resultMap).containsEntry("status", HttpStatus.OK.value());
    }

    @Test
    public void test4AddNodes() throws Exception {
        String requestBody = "{\"indexId\":" + indexId + ",\"indexName\":\"" + indexName + "\",\"name\":\"12345\",\"clusterLevel\":0,\"nodes\":[],\"clusters\":[]}";

        assertThat(callRestApi("/index/routing/target_group/update.json", requestBody)).isNull();
    }

    @Test
    public void test5AddRule() throws Exception {
        Map resultMap =  callRestApi("/index/routing/list.json", "{\"indexId\":" + indexId + ",\"indexName\":\"" + indexName +  "\"}");
        JSONObject jsonObject = ((JSONObject) resultMap.get("data")).getJSONObject("data").getJSONObject("routingTargetGroups");

        Map<String, JSONObject> map = JSONObject.toJavaObject(jsonObject, Map.class);
        for(Map.Entry<String, JSONObject> entry : map.entrySet()) {
            groupIds.add(Long.parseLong(entry.getKey()));
        }
        groupId = groupIds.get(0);

        String ruleString = "{\n" +
                "\t\"indexId\": " + indexId + ",\n" +
                "\t\"indexName\": \"" + indexName + "\",\n" +
                "\t\"rules\": [{\n" +
                "\t\t\"name\": \"rule-test\",\n" +
                "\t\t\"targetGroups\": [{\n" +
                "\t\t\t\"id\": " + groupId + ",\n" +
                "\t\t\t\"weight\": 1\n" +
                "\t\t}],\n" +
                "\t\t\"enable\": false,\n" +
                "\t\t\"conditionRelation\": \"AND\",\n" +
                "\t\t\"priority\": \"\",\n" +
                "\t\t\"conditions\": []\n" +
                "\t}]\n" +
                "}";


        assertThat(callRestApi("/index/routing/rule/update.json", ruleString)).isNull();

    }

    @AfterClass
    public static void cleanData() throws Exception {
        //delete rule
        String deleteRules = "{\n" +
                "\t\"indexId\":" + indexId +  ",\n" +
                "\t\"indexName\": \"" + indexName + "\",\n" +
                "\t\"rules\": []\n" +
                "}";
        assertThat(callRestApi("/index/routing/rule/update.json", deleteRules)).isNull();
        //delete node
        for(Long id : groupIds) {
            assertThat(callRestApi("/index/routing/target_group/delete.json", "{\"groupId\":" + id + "}")).isNull();
        }
        assertThat(callRestApi("/index/delete/id.json", "{\"indexId\": \"" + indexId + "\"}")).isNull();
        // assertThat(callRestApi("/cluster/delete/id.json", "{\"clusterId\": \"" + clusterId + "\"}")).isNull();
    }
}