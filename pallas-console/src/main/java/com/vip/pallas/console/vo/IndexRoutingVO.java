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

import java.util.List;
import java.util.Map;

import com.vip.pallas.mybatis.entity.IndexRouting;

/**
 * Created by owen on 03/11/2017.
 */
public class IndexRoutingVO {

    private String indexName;

    private Long indexId;

    private List<IndexRouting.RoutingCondition> rules;

    private Map<Long, TargetGroupVO> routingTargetGroups;

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public Long getIndexId() {
        return indexId;
    }

    public void setIndexId(Long indexId) {
        this.indexId = indexId;
    }

    public List<IndexRouting.RoutingCondition> getRules() {
        return rules;
    }

    public void setRules(List<IndexRouting.RoutingCondition> rules) {
        this.rules = rules;
    }

    public Map<Long, TargetGroupVO> getRoutingTargetGroups() {
        return routingTargetGroups;
    }

    public void setRoutingTargetGroups(Map<Long, TargetGroupVO> routingTargetGroups) {
        this.routingTargetGroups = routingTargetGroups;
    }
}