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
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.type.TypeReference;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Created by owen on 2/11/2017.
 */
public class IndexRoutingSecurity {

    private Long id;

    private Long indexId;

    private String indexName;

    private String criteria;

    private String protocolControls;

    private int state = 0;

    private Date createTime;

    private Date updateTime;

    @JsonIgnore
    private Criteria criteriaObj;

    @JsonIgnore
    private Map<String, List<String>> protocolControlMap;

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

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public String getProtocolControls() {
        return protocolControls;
    }

    public void setProtocolControls(String protocolControls) {
        this.protocolControls = protocolControls;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
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

    @JsonIgnore
    public Criteria getCriteriaObj() throws Exception {
        if (criteriaObj == null) {
            criteriaObj = fromCriteriaXContent(criteria);
        }
        return criteriaObj;
    }

    @JsonIgnore
    public Map<String, List<String>> getProtocolControlsObj() throws Exception {
        if (protocolControlMap == null) {
            protocolControlMap = fromProtocolControlXContent(protocolControls);
        }
        return protocolControlMap;
    }

    @org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown = true)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class Criteria {

        private boolean enable = true;

        List<Map<String, String>> headers;

        public boolean isEnable() {
            return enable;
        }

        public void setEnable(boolean enable) {
            this.enable = enable;
        }

        public List<Map<String, String>> getHeaders() {
            return headers;
        }

        public void setHeaders(List<Map<String, String>> headers) {
            this.headers = headers;
        }
    }

    public static <T> String toXContent(T c) throws Exception {
        if (c == null) {
            throw new IllegalArgumentException("Criteria can not be null");
        }
        return JsonUtil.toJson(c);
    }

    public static Criteria fromCriteriaXContent(String json) throws Exception {
        return JsonUtil.readValue(json, new TypeReference<Criteria>(){});
    }

    public static Map<String, List<String>> fromProtocolControlXContent(String json) throws Exception {
        String newJson = json;
        if (newJson == null || "".equals(newJson.trim())) {
            newJson = "[]";
        }
        return JsonUtil.readValue(newJson, new TypeReference<Map<String, List<String>>>(){});
    }


    public static IndexRoutingSecurity generateDefault(Long indexId, String indexName) throws Exception {
        IndexRoutingSecurity irs = new IndexRoutingSecurity();
        irs.setCreateTime(new Date());
        irs.setIndexId(indexId);
        irs.setIndexName(indexName);
        Criteria c = new Criteria();
        c.setEnable(false);
        Map<String, String> headers = new HashMap<>();
        headers.put("key", "X-PALLAS-SEARCH-CLIENT-TOKEN");
        headers.put("value", "");
        c.setHeaders(Stream.of(headers).collect(toList()));
        irs.setCriteria(toXContent(c));
        Map<String, String[]> map = new LinkedHashMap<>();
        //map.put(Protocol_Control_Cat_IndexConfigs, new String[]{Protocol_Control_Action_Search});
        //map.put(Protocol_Control_Cat_IndexDocs, new String[]{Protocol_Control_Action_Search});
        irs.setProtocolControls(toXContent(map));
        return irs;
    }
}