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
import com.vip.pallas.console.vo.ClusterVO;
import com.vip.pallas.test.base.BaseSpringEsTest;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ClusterRoutingControllerTest extends BaseSpringEsTest {

    private static String clusterId = null;
    private static String nodesName = "nodes_test";
    private static String ruleName = "rule_test";
    private static List<Long> groupIds = new ArrayList<>();

    @Test
    public void test1AddCluster() throws Exception{
        ClusterVO clusterVO = insertClusterToTable();
        clusterId = clusterVO.getClusterId();
    }

    @Test
    public void test2RuleList() throws Exception {
        Map resultMap = callGetApi("/cluster/routing/rule/list.json?clusterId=" + clusterId);
        assertThat(resultMap).containsEntry("status", HttpStatus.OK.value());
    }

    @Test
    public void test3AddNodes() throws Exception {
        String requertBody = "{\"clusterId\":\"" + clusterId + "\",\"name\":\"" + nodesName + "\",\"clusterLevel\":0,\"nodes\":[],\"clusters\":[]}";
        assertThat(callRestApi("/cluster/routing/target_group/update.json", requertBody)).isNull();
    }

    @Test
    public void test4AddRule() throws Exception {
        Map<String, JSONObject> resultMap = callGetApi("/cluster/routing/rule/list.json?clusterId=" + clusterId);
        JSONObject  routingTargetGroups =  resultMap.get("data").getJSONObject("data").getJSONObject("routingTargetGroups");
        routingTargetGroups.forEach((String k, Object v) -> {
            JSONObject object = (JSONObject)v;
            groupIds.add(object.getLong("id"));
        });

        Long groupId = groupIds.get(0);
        String requesbody = "{\n" +
                "\t\"clusterId\": \"" + clusterId + "\",\n" +
                "\t\"rules\": [{\n" +
                "\t\t\"name\": \"" + ruleName + "\",\n" +
                "\t\t\"targetGroups\": [{\n" +
                "\t\t\t\"id\":" + groupId + ",\n" +
                "\t\t\t\"weight\": 1\n" +
                "\t\t}],\n" +
                "\t\t\"enable\": true,\n" +
                "\t\t\"conditionRelation\": \"AND\",\n" +
                "\t\t\"priority\": \"\",\n" +
                "\t\t\"conditions\": []\n" +
                "\t}]\n" +
                "}";
        callRestApi("/cluster/routing/rule/update.json", requesbody);
    }

    @Test
    public void test5targetGroupPage() throws Exception {
        assertThat(callGetApi("/cluster/routing/target_group/list.json?clusterId=" + clusterId)).isNotNull();
    }

    @AfterClass
    public static void cleanData() throws Exception{
        //delete rule
        assertThat(callRestApi("/cluster/routing/rule/update.json", "{\"clusterId\":\"" + clusterId + "\",\"rules\":[]}")).isNull();
        //delete nodes
        for(Long id : groupIds) {
            assertThat(callRestApi("/index/routing/target_group/delete.json", "{\"groupId\":" + id + "}"));
        }
        assertThat(callRestApi("/cluster/delete/id.json", "{\"clusterId\": \"" + clusterId + "\"}")).isNull();
    }

}