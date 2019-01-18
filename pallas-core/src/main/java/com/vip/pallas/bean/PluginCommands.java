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

import java.util.ArrayList;
import java.util.List;

public class PluginCommands {

    private String clusterId;

    private List<Action> actions;

    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }

    public void addAction(Action action){
        if (getActions() == null){
            setActions(new ArrayList<>());
        }
        getActions().add(action);
    }

    public static class Action{
        private PluginActionType actionType;

        private List<Plugin> plugins;

        public List<Plugin> getPlugins() {
            return plugins;
        }

        public void setPlugins(List<Plugin> plugins) {
            this.plugins = plugins;
        }

        public PluginActionType getActionType() {
            return actionType;
        }

        public void setActionType(PluginActionType actionType) {
            this.actionType = actionType;
        }

        public void addPlugin(Plugin plugin){
            if (getPlugins() == null){
                setPlugins(new ArrayList<>());
            }
            getPlugins().add(plugin);
        }
    }

    public static class Plugin{
        private String name;
        private String version;
        private PluginType type;

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

        @JsonIgnore
        public String getWorkDir(){
            if(type != null){
                switch (type){
                    case PALLAS:
                        return PluginDictionary.PALLAS_PLUGIN_DIR;
                    case NATIVE:
                        return PluginDictionary.PALLAS_PLUGIN_DIR;
                    case ES:
                        return PluginDictionary.ES_PLUGIN_DIR;
                    default:
                        return null;
                }
            }
            return null;
        }

        @JsonIgnore
        public String getZipFileName(){
            return name + "-" + version + ".zip";
        }

        @JsonIgnore
        public String getFullZipFileName(){
            return PluginDictionary.DOWNLOAD_DIR + "/" + getZipFileName();
        }

        @JsonIgnore
        public boolean isPallasPlugin(){
            return PluginType.PALLAS == getType() || PluginType.NATIVE == getType();
        }
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}