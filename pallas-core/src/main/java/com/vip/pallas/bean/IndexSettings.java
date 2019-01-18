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
public class IndexSettings {

    private Settings settings;

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Settings{
        private Index index;

        public Index getIndex() {
            return index;
        }

        public void setIndex(Index index) {
            this.index = index;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Index{
        private Unassigned unassigned;
        private Blocks blocks;

        public Unassigned getUnassigned() {
            return unassigned;
        }

        public void setUnassigned(Unassigned unassigned) {
            this.unassigned = unassigned;
        }

        public Blocks getBlocks() {
            return blocks;
        }

        public void setBlocks(Blocks blocks) {
            this.blocks = blocks;
        }
    }

    public static class Blocks{
        private String write;

        public String getWrite() {
            return write;
        }

        public void setWrite(String write) {
            this.write = write;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Unassigned{
        @JsonProperty("node_left")
        private NodeLeft nodeLeft;

        public NodeLeft getNodeLeft() {
            return nodeLeft;
        }

        public void setNodeLeft(NodeLeft nodeLeft) {
            this.nodeLeft = nodeLeft;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NodeLeft{
        @JsonProperty("delayed_timeout")
        private String delayedTimeout;

        public String getDelayedTimeout() {
            return delayedTimeout;
        }

        public void setDelayedTimeout(String delayedTimeout) {
            this.delayedTimeout = delayedTimeout;
        }
    }
}