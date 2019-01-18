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

package com.vip.pallas.service;

import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.mybatis.entity.PluginCommand;
import com.vip.pallas.mybatis.entity.PluginRuntime;
import com.vip.pallas.mybatis.entity.PluginUpgrade;

import java.util.List;

/**
 * Created by owen on 13/9/2017.
 */
public interface PallasPluginService {

    int addOrUpdatePluginRuntime(List<PluginRuntime> runtimeList);

    List<PluginRuntime> getAllPluginRuntimes();

    List<PluginRuntime> getPluginRuntimes(String clusterId, String pluginName);

    PluginRuntime getPluginRuntime(String clusterId, String pluginName, String nodeIp);

    List<String> getNodeIPsByCluster(String clusterId);

    int deletePluginRuntime(long id);

    int addPluginUpgrade(PluginUpgrade upgrade);

    PluginUpgrade getLatestUpgrade(String clusterId, String pluginName);

    List<PluginUpgrade> getPluginUpgrade(Page<PluginUpgrade> page);

    PluginUpgrade getPluginUpgrade(Long id);

    int setUppgradeState(String loginUser, long id, int nextState);

    int addPluginCommand(PluginCommand command);

    int deleteCommand(Long... ids);

    int deleteCommand(String clusterId, String pluginName);

    List<PluginCommand> getCommands(String clusterId, String pluginName);

    List<PluginCommand> getCommandsByIp(String clusterId, String nodeIp);

    PluginRuntime getPluginRuntime(String clusterId, String pluginName, String nodeIp, String pluginVersion);

    void deletePluginRuntime(String clusterId, String pluginName, String nodeIp, String pluginVersion);


}