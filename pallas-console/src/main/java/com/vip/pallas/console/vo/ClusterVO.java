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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class ClusterVO implements Serializable {
    private Long id;

    @NotNull(message = "clusterId不能为空")
    private String clusterId;

    private String httpAddress;

    private String clientAddress;

    @NotNull(message = "description不能为空")
    private String description;

    private String realClusters;

    @NotEmpty(message = "代理集群不能为空")
    private String accessiblePs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getHttpAddress() {
        return httpAddress;
    }

    public void setHttpAddress(String httpAddress) {
        this.httpAddress = httpAddress;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRealClusters() {
        return realClusters;
    }

    public void setRealClusters(String realClusters) {
        this.realClusters = realClusters;
    }

    public String getAccessiblePs() {
        return accessiblePs;
    }

    public void setAccessiblePs(String accessiblePs) {
        this.accessiblePs = accessiblePs;
    }
}