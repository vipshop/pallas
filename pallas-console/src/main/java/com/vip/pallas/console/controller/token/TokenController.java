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

package com.vip.pallas.console.controller.token;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.vo.TokenPrivilegeVO;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.mybatis.entity.SearchAuthorization;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.IndexService;
import com.vip.pallas.service.SearchAuthorizationService;

@Validated
@RestController
@RequestMapping("/token")
public class TokenController {

    private static Logger logger = LoggerFactory.getLogger(TokenController.class);
    
    @Autowired
    private SearchAuthorizationService authService;
    
    @Autowired
    private ClusterService clusterService;
    
    @Autowired
    private IndexService indexServcie;
    
    @RequestMapping(path="/list.json", method={RequestMethod.GET, RequestMethod.POST})
    public List<SearchAuthorization> queryTokens(HttpServletRequest request){
        if (!AuthorizeUtil.authorizeTokenPrivilege(request, null)) {
        	throw new BusinessLevelException(403, "无权限操作");
        }

        return authService.selectAll();
    }
    
    @RequestMapping(path="/insert.json", method={RequestMethod.POST})
    public void createOrUpdateToken(@RequestBody @Validated SearchAuthorization token, HttpServletRequest request){
    	if (!AuthorizeUtil.authorizeTokenPrivilege(request, null)) {
        	throw new BusinessLevelException(403, "无权限操作");
        }

        if (StringUtils.isEmpty(token.getAuthorizationItems())){
            token.setAuthorizationItems("[]");
        }

        SearchAuthorization tokenInDB = authService.findByToken(token.getClientToken());

        SearchAuthorization auth = null;
        if (token.getId() == null) {
            if (tokenInDB != null) {
                throw new BusinessLevelException(500, "token 已存在");
            }
            auth = new SearchAuthorization();
            try {
                List<SearchAuthorization.AuthorizationItem> list = SearchAuthorization.fromXContent(token.getAuthorizationItems());
                auth.setAuthorizationItems(SearchAuthorization.toXContent(list));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new BusinessLevelException(500, "authorizationItems 解析错误：" + token.getAuthorizationItems());
            }
        } else {
            if (tokenInDB != null && !tokenInDB.getId().equals(token.getId())) {
                throw new BusinessLevelException(500, "token已存在，请插入别的token。");
            }
            auth = authService.findById(token.getId());
        }
        auth.setTitle(token.getTitle());
        auth.setClientToken(token.getClientToken());
        auth.setEnabled(token.isEnabled());
        authService.addOrUpdateAuthorization(auth);
    }
    
    @RequestMapping(path="/token_privileges.json", method={RequestMethod.GET, RequestMethod.POST})
    public List<SearchAuthorization.AuthorizationItem> privileges(HttpServletRequest request, 
            @RequestParam(required = false) @NotNull(message = "id不能为空") @Min(value = 1, message = "id必须为正数") Long id) {
    	if (!AuthorizeUtil.authorizeTokenPrivilege(request, null)) {
        	throw new BusinessLevelException(403, "无权限操作");
        }

        List<SearchAuthorization.AuthorizationItem> privileges = new LinkedList<>();
        SearchAuthorization sa = authService.findById(id);
        if (sa != null) {
            List<Cluster> clusters = clusterService.findAll();
            List<Index> indices = indexServcie.findAll();
            List<SearchAuthorization.AuthorizationItem> authItems = sa.getAuthorizationItemList();
            for(Cluster c : clusters) {
                privileges.add(parseClusterPrivilege(c, indices, authItems));
            }

        }
        return privileges;
    }
    
    private SearchAuthorization.AuthorizationItem parseClusterPrivilege(Cluster c, List<Index> indices,
            List<SearchAuthorization.AuthorizationItem> authItems) {
        SearchAuthorization.AuthorizationItem clusterItem = authItems.stream()
                .filter((SearchAuthorization.AuthorizationItem i) -> i.getId().equals(c.getId())).findAny()
                .orElseGet(() -> {
                    SearchAuthorization.AuthorizationItem newObj = new SearchAuthorization.AuthorizationItem();
                    newObj.setId(c.getId());
                    newObj.setName(c.getClusterId());
                    newObj.setPrivileges(new HashMap<>());
                    newObj.setIndexPrivileges(new LinkedList<>());
                    return newObj;
                });
        List<SearchAuthorization.AuthorizationItem> indexItems = clusterItem.getIndexPrivileges();
        for(Index index : indices) {
            if (!index.getClusterName().equals(c.getClusterId())) {
                continue;
            }
            boolean found = indexItems.stream()
                    .anyMatch((SearchAuthorization.AuthorizationItem idxItem) -> idxItem.getId().equals(index.getId()));
            
            if (!found) {
                SearchAuthorization.AuthorizationItem newObj = new SearchAuthorization.AuthorizationItem();
                newObj.setId(index.getId());
                newObj.setName(index.getIndexName());
                newObj.setPrivileges(new HashMap<>());
                indexItems.add(newObj);
            }
        }
        return clusterItem;
    }
    
    @RequestMapping(path="/token_privilege/update.json", method = {RequestMethod.POST})
    public void updatePrivilege(@RequestBody @Validated TokenPrivilegeVO tokenPrivilegeVO, HttpServletRequest request) {
        if (!AuthorizeUtil.authorizeTokenPrivilege(request, null)){
        	throw new BusinessLevelException(403, "无权限操作");
        }

        SearchAuthorization sa = authService.findById(tokenPrivilegeVO.getId());
        if (sa == null) {
            throw new BusinessLevelException(500, "SearchAuthorization 不存在");
        }
        
        try {
            sa.setAuthorizationItems(SearchAuthorization.toXContent(tokenPrivilegeVO.getAuthorizationItems()));
        } catch (Exception e) {
            logger.error("error", e);
            throw new BusinessLevelException(500, "authorizationItems 解析错误：" + tokenPrivilegeVO.getAuthorizationItems());
        }
        
        authService.addOrUpdateAuthorization(sa);
    }
    
    @RequestMapping(path="/security/token.json")
    public String generateToken() {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
            throw new BusinessLevelException(500, e.getMessage());
        }
        return Base64.getEncoder().encodeToString(messageDigest.digest(UUID.randomUUID().toString().getBytes()));
    }
}