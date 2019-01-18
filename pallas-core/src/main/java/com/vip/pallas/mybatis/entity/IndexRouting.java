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

package com.vip.pallas.mybatis.entity;
import com.vip.pallas.utils.JsonUtil;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.type.TypeReference;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by owen on 2/11/2017.
 */
public class IndexRouting {

    public final static String ROUTE_TYPE_INDEX = "index";

    public final static String ROUTE_TYPE_CLUSTER = "cluster";

    private Long id;

    private Long indexId;

    private String indexName;

    private String type;

    private String routingsInfo;

    private Date createTime;

    private Date updateTime;

    private List<RoutingCondition> conditionList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIndexId() {
        return indexId;
    }

    public void setIndexId(Long indexId) {
        this.indexId = indexId;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoutingsInfo() {
        return routingsInfo;
    }

    public void setRoutingsInfo(String routingsInfo) {
        this.routingsInfo = routingsInfo;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public static IndexRouting genDefault(Index index, List<IndexRoutingTargetGroup> groups) throws Exception {
        IndexRouting routing = new IndexRouting();
        routing.setIndexId(index.getId());
        routing.setIndexName(index.getIndexName());
        routing.setCreateTime(new Date());
        List<ConditionTarget> targets = groups.stream().map((IndexRoutingTargetGroup g) -> {
            ConditionTarget target = new ConditionTarget();
            target.setId(g.getId());
            target.setWeight(1);
            return target;
        }).collect(Collectors.toList());

        RoutingCondition cond = new RoutingCondition();
        cond.setConditions(new Condtion[0]);
        cond.setEnable(true);
        cond.setName("Default");
        cond.setTargetGroups(targets);
        routing.setRoutingsInfo(toXContent(Stream.of(cond).collect(Collectors.toList())));
        return routing;
    }

    @org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoutingCondition {

        private String name;

        private String conditionRelation = "AND";

        private Condtion[] conditions;

        private List<ConditionTarget> targetGroups;

        private String preference = "";

        private boolean enable;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getConditionRelation() {
            return conditionRelation;
        }

        public void setConditionRelation(String conditionRelation) {
            this.conditionRelation = conditionRelation;
        }

        public Condtion[] getConditions() {
            return conditions;
        }

        public void setConditions(Condtion[] conditions) {
            this.conditions = conditions;
        }

        public List<ConditionTarget> getTargetGroups() {
            return targetGroups;
        }

        public void setTargetGroups(List<ConditionTarget> targetGroups) {
            this.targetGroups = targetGroups;
        }

        public String getPreference() {
            return preference;
        }

        public void setPreference(String preference) {
            this.preference = preference;
        }

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }
    }

    @org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class ConditionTarget {

        public ConditionTarget(){}

        public ConditionTarget(Long id, int weight) {
            this.id = id;
            this.weight = weight;
        }

        private Long id;

        private int weight;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }
    }

    @org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class Condtion {

        private String paramType;

        private String paramName;

        private String paramValue;

        private String exprOp;

        public String getParamType() {
            return paramType;
        }

        public void setParamType(String paramType) {
            this.paramType = paramType;
        }

        public String getParamName() {
            return paramName;
        }

        public void setParamName(String paramName) {
            this.paramName = paramName;
        }

        public String getParamValue() {
            return paramValue;
        }

        public void setParamValue(String paramValue) {
            this.paramValue = paramValue;
        }

        public String getExprOp() {
            return exprOp;
        }

        public void setExprOp(String exprOp) {
            this.exprOp = exprOp;
        }
    }

    public static List<RoutingCondition> fromXContent(String json) throws Exception {
        String newJson = json;
        if (newJson == null || "".equals(newJson.trim())) {
            newJson = "[]";
        }
        return JsonUtil.readValue(newJson, new TypeReference<List<RoutingCondition>>(){});
    }

    public static String toXContent(List<RoutingCondition> list) throws Exception {
        if (list == null) {
            throw new IllegalArgumentException("condition list can not be null");
        }
        return JsonUtil.toJson(list);
    }

    public List<RoutingCondition> getConditionList() {
        return conditionList;
    }

    public void setConditionList(List<RoutingCondition> conditionList) {
        this.conditionList = conditionList;
    }
}