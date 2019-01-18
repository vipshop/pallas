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

package com.vip.pallas.bean;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterSettings {

    @JsonProperty("transient")
    private Transient transient0;

    public Transient getTransient() {
        return transient0;
    }

    public void setTransient(Transient transient0) {
        this.transient0 = transient0;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Transient{
        private Cluster cluster;

        public Cluster getCluster() {
            return cluster;
        }

        public void setCluster(Cluster cluster) {
            this.cluster = cluster;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Cluster{
        private Routing routing;

        public ClusterSettings.Routing getRouting() {
            return routing;
        }

        public void setRouting(ClusterSettings.Routing routing) {
            this.routing = routing;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Routing{
        private Rebalance rebalance;
        private Allocation allocation;

        public Rebalance getRebalance() {
            return rebalance;
        }

        public void setRebalance(Rebalance rebalance) {
            this.rebalance = rebalance;
        }

        public Allocation getAllocation() {
            return allocation;
        }

        public void setAllocation(Allocation allocation) {
            this.allocation = allocation;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Rebalance{
        private String enable;

        public String getEnable() {
            return enable;
        }

        public void setEnable(String enable) {
            this.enable = enable;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Allocation{
        private String enable;

        public String getEnable() {
            return enable;
        }

        public void setEnable(String enable) {
            this.enable = enable;
        }
    }
}