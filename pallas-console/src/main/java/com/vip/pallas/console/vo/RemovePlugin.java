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

package com.vip.pallas.console.vo;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class RemovePlugin {

    @NotNull(message = "pluginUpgradeId不能为空")
    @Min(value = 1, message = "pluginUpgradeId必须为正数")
    private Long pluginUpgradeId;
    
    @NotBlank(message = "clusterId不能为空")
    private String clusterId;
    
    @NotBlank(message = "pluginName不能为空")
    private String pluginName;
    
    private String pluginVersion;
    
    public Long getPluginUpgradeId() {
        return pluginUpgradeId;
    }

    public void setPluginUpgradeId(Long pluginUpgradeId) {
        this.pluginUpgradeId = pluginUpgradeId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getPluginName() {
        return pluginName;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }
    
    
}