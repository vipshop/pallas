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

import java.util.Date;

public class Mapping {
    private Long id;

    private Long parentId;

    private Long versionId;

    private String fieldName;

    private String fieldType;

    private Boolean multi;

    private Boolean search;

    private Boolean docValue;

    private Date createTime;

    private Date updateTime;

    private Boolean dynamic;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName == null ? null : fieldName.trim();
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getESRealFieldType() {
        if (isNGramText()) {
            return "text";
        } else if (isNormalizedKeyword() || isKeywordAsNumber()) {
            return "keyword";
        } else {
            return getFieldType();
        }
    }

    public boolean isNGramText() {
        return "text_ngram".equals(fieldType);
    }

    public boolean isNormalizedKeyword() {
        return "keyword_normalized".equals(fieldType);
    }

    public boolean isKeywordAsNumber() {
        return "keyword_as_number".equals(fieldType);
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType == null ? null : fieldType.trim();
    }

    public Boolean getMulti() {
        return multi;
    }

    public void setMulti(Boolean multi) {
        this.multi = multi;
    }

    public Boolean getSearch() {
        return search;
    }

    public void setSearch(Boolean search) {
        this.search = search;
    }

    public Boolean getDocValue() {
        return docValue;
    }

    public void setDocValue(Boolean docValue) {
        this.docValue = docValue;
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

    public Boolean getDynamic() {
        return dynamic;
    }

    public void setDynamic(Boolean dynamic) {
        this.dynamic = dynamic;
    }
}