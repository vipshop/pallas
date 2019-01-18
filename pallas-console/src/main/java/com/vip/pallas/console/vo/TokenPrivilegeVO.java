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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.vip.pallas.mybatis.entity.SearchAuthorization.AuthorizationItem;

public class TokenPrivilegeVO {

    @NotNull(message = "id不能为空")
    @Min(value = 1, message = "id必须为正数")
    private Long id;

    @NotEmpty(message = "authorizationItems不能为空")
    private List<AuthorizationItem> authorizationItems;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<AuthorizationItem> getAuthorizationItems() {
        return authorizationItems;
    }

    public void setAuthorizationItems(List<AuthorizationItem> authorizationItems) {
        this.authorizationItems = authorizationItems;
    }
    
    
}