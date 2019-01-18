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

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.joining;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PluginStates {

    private String clusterId;
    private String nodeIp;
    @JsonProperty("nodeName")
    @JsonIgnore
    private String nodeHost;
    private List<Plugin> plugins;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<Plugin> plugins) {
        this.plugins = plugins;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public void setNodeHost(String nodeHost) {
        this.nodeHost = nodeHost;
    }

    public void addPlugin(Plugin plugin){
        if (getPlugins() == null){
            setPlugins(new ArrayList<>());
        }

        boolean isNew = true;
        for(Plugin p : plugins){
            if(plugin.getName().equals(p.getName())){
                if(p.getAvailableVersions() != null && plugin.getAvailableVersions() != null){
                    p.setAvailableVersions(p.getAvailableVersions() + "," + plugin.getAvailableVersions());
                } else if(p.getAvailableVersions() != null){
                    p.setAvailableVersions(p.getAvailableVersions());
                } else if(plugin.getAvailableVersions() != null){
                    p.setAvailableVersions(plugin.getAvailableVersions());
                }
                isNew = false;
                break;
            }
        }

        if(isNew){
            getPlugins().add(plugin);
        }
    }

    public static class Plugin{
        private String name;
        private String version;
        private PluginType type;
        private String availableVersions;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public PluginType getType() {
            return type;
        }

        public void setType(PluginType type) {
            this.type = type;
        }

        public String getAvailableVersions() {
            if(availableVersions != null){
                availableVersions = Arrays.stream(availableVersions.split(",")).distinct().collect(joining(","));
            }
            return availableVersions;
        }

        public void setAvailableVersions(String availableVersions) {
            this.availableVersions = availableVersions;
        }
    }
}