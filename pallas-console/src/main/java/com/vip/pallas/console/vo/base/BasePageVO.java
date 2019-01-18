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

package com.vip.pallas.console.vo.base;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class BasePageVO {
    private Long total = 0l;
    private Integer pageCount;
    private boolean allPrivilege;

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }
    
    @JsonIgnore
    public void setPageCountByCompute (Integer pageSize) {
    	this.pageCount = (int) (this.total % pageSize == 0 ? this.total / pageSize : this.total / pageSize + 1);
    }

    public boolean isAllPrivilege() {
        return allPrivilege;
    }

    public void setAllPrivilege(boolean allPrivilege) {
        this.allPrivilege = allPrivilege;
    }
}