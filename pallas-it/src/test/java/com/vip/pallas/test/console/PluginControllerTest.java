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

import java.util.List;
import java.util.Map;

import com.vip.pallas.mybatis.entity.PluginCommand;
import com.vip.pallas.mybatis.repository.PluginCommandRepository;
import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpStatus;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vip.pallas.console.vo.PluginAction;
import com.vip.pallas.console.vo.RemovePlugin;
import com.vip.pallas.mybatis.entity.PluginUpgrade;
import com.vip.pallas.test.base.BaseSpringEsTest;

import javax.annotation.Resource;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PluginControllerTest extends BaseSpringEsTest {
    private static Long id = null;
    private static String pluginName = "ivyTestPlugin";
    private static String pluginVersion = "1.0.0";
    private static String packagePath = "";
    private static PluginAction action = new PluginAction();
    private static Long removeId = null;

	@Resource
	private PluginCommandRepository commandRepository;

    @Test
    public void test21FieUpload() throws Exception {

    	String path = BaseSpringEsTest.class.getClassLoader().getResource("").getPath() + "request/ivyTestPlugin-1.0.0.zip";
        Map resultMap = uploadFile("/plugin/upgrade/fileUpload.json?clusterId=" + EMBEDDED_CLUTER_ID + "&pluginName=" + pluginName + "&pluginVersion=" + pluginVersion, path);
        assertThat(resultMap).containsEntry("status", HttpStatus.OK.value());
        packagePath = (String)resultMap.get("data");
        assertThat(packagePath).isNotEmpty();

    }

    @Test
    public void test22AddPlugin() throws Exception {
        PluginUpgrade pluginUpgrade = new PluginUpgrade();
        pluginUpgrade.setClusterId(EMBEDDED_CLUTER_ID);
        pluginUpgrade.setPluginName(pluginName);
        pluginUpgrade.setPluginVersion(pluginVersion);
        pluginUpgrade.setPluginType(0);
        pluginUpgrade.setNote("it is a test");
        pluginUpgrade.setPackagePath(packagePath);
        assertThat(callRestApi("/plugin/upgrade/add.json", JSON.toJSONString(pluginUpgrade))).isNull();
        //再次插入报500
        // assertThat(callRestApi("/plugin/upgrade/add.json", JSON.toJSONString(pluginUpgrade))).containsEntry("status", 500);
    }

    @Test
    public void test23UpgradeList() throws Exception {
        //get update id

        Map resultMap = callGetApi("/plugin/upgrade/list.json?currentPage=1&pageSize=100&pluginName=" + pluginName);
        JSONArray jsonArray = ((JSONObject) resultMap.get("data")).getJSONArray("list");
        id = jsonArray.getJSONObject(0).getLong("id");
        assertThat(id).isGreaterThanOrEqualTo(1L);
        action.setPluginUpgradeId(id);
    }

    @Test
    public void test24DownloadAction() throws Exception {
        action.setAction("download");
        assertThat(callRestApi("/plugin/upgrade/action.json", JSON.toJSONString(action))).isNull();
		int commandNum = getCommandCount(PluginCommand.COMMAND_DOWNLOAD, null);
		assertThat(commandNum).isEqualTo(1);
	}

	@Test
	public void test251UpdateInNodeLevel() throws Exception {
		action.setAction("upgrade");
		String nodeIp = CLUSTER_HTTPADDRESS.split(":")[0];
		action.setNodeIp(nodeIp);
		callRestApi("/plugin/upgrade/action.json", JSON.toJSONString(action));

		int commandNum = getCommandCount(PluginCommand.COMMAND_UPGRADE, nodeIp);
		assertThat(commandNum).isEqualTo(1);
	}

	@Test
    public void test252Update() throws Exception {
        action.setAction("upgrade");
        assertThat(callRestApi("/plugin/upgrade/action.json", JSON.toJSONString(action))).isNull();

		int commandNum = getCommandCount(PluginCommand.COMMAND_UPGRADE, null);
		assertThat(commandNum).isEqualTo(2);
    }

    @Test
    public void test26MarkComplete() throws Exception {
        action.setAction("done");
        assertThat(callRestApi("/plugin/upgrade/action.json", JSON.toJSONString(action))).isNull();
    }

    @Test
    public void test27Recall() throws Exception{
        test22AddPlugin();
        test23UpgradeList();
        action.setAction("recall");
        assertThat(callRestApi("/plugin/upgrade/action.json", JSON.toJSONString(action))).isNull();
    }

    @Test
    public void test28Deny() throws Exception {
        test22AddPlugin();
        test23UpgradeList();
        action.setAction("deny");
        assertThat(callRestApi("/plugin/upgrade/action.json", JSON.toJSONString(action))).isNull();
    }

    @Test
    public void test41PluginStateList() throws Exception {
        Map resultMap = callGetApi("/plugin/runtime/list.json?currentPage=1&pageSize=10&pluginName" + pluginName);
        JSONArray jsonArray = ((JSONObject) resultMap.get("data")).getJSONArray("list");
        removeId = jsonArray.getJSONObject(0).getLong("id");
        assertThat(removeId).isGreaterThanOrEqualTo(1L);
    }

    @Test
    public void test42remove() throws Exception {
        RemovePlugin removePlugin = new RemovePlugin();
        removePlugin.setClusterId(EMBEDDED_CLUTER_ID);
        removePlugin.setPluginName(pluginName);
        removePlugin.setPluginUpgradeId(removeId);
        removePlugin.setPluginVersion("");
        assertThat(callRestApi("/plugin/remove.json", JSON.toJSONString(removePlugin))).isNull();

		int commandNum = getCommandCount(PluginCommand.COMMAND_REMOVE, null);
		assertThat(commandNum).isEqualTo(1);
    }

    private int getCommandCount(String command, String nodeIp){
		List<PluginCommand> commandList = commandRepository.selectByClusterAndPluginName(EMBEDDED_CLUTER_ID, pluginName);
		int commandNum = 0;
		for (PluginCommand commandInDb : commandList){
			if (pluginName.equals(commandInDb.getPluginName())  && command.equals(commandInDb.getCommand())
					&& (null == nodeIp || nodeIp.equals(commandInDb.getNodeIp())) ){
				commandNum++;
			}
		}
		return commandNum;
	}

	@AfterClass
	public static void cleanData() throws Exception {
		// assertThat(callRestApi("/cluster/delete/id.json", "{\"clusterId\": \"" + clusterId + "\"}")).isNull();
	}
}