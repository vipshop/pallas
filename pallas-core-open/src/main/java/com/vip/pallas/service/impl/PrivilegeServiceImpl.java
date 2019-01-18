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

package com.vip.pallas.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vip.pallas.bean.AuthorizeModel;
import com.vip.pallas.bean.PrivilegeResultModel;
import com.vip.pallas.mybatis.entity.Permission;
import com.vip.pallas.mybatis.repository.PermissionRepository;
import com.vip.pallas.service.PrivilegeService;
import com.vip.pallas.utils.PallasConsoleProperties;
import com.vip.vjtools.vjkit.collection.ListUtil;

@Service
public class PrivilegeServiceImpl implements PrivilegeService {
	
	@Autowired
	private PermissionRepository permissionRepository;
	
	@Override
	public void createIndexPrivilege(String indexIdAndName) throws Exception {}

	@Override
	public void createClusterPrivilege(String clusterId) throws Exception {}

	@Override
	public void createVersionPrivilege(String privilegeName) throws Exception {
	}

	@Override
	public void createTemplatePrivilege(String privilegeName) throws Exception {
	}

	@Override
	public void deleteIndexPrivilege(String indexIdAndName) throws Exception {}

	@Override
	public void deleteIndexAsset(String indexIdAndName) throws Exception {}

	@Override
	public void deleteClusterPrivilege(String clusterId) throws Exception {}

	@Override
	public void deleteClusterAsset(String clusterId) throws Exception {}

    @Override
    public Map<String, List<PrivilegeResultModel>> getPrivilegeByUsers(List<String> userAccountIds)
            throws Exception {
        Map<String, List<PrivilegeResultModel>> privilegeResultMap = new HashMap<>(); 
        if (ListUtil.isNotEmpty(userAccountIds)) {
        	for (String userAccountId : userAccountIds) {
				List<Permission> permissions = permissionRepository.selectByUsername(userAccountId);
				List<PrivilegeResultModel> priviles = new ArrayList<>();
				for (Permission permission : permissions) {
					PrivilegeResultModel privilegeResultModel = new PrivilegeResultModel();
	                privilegeResultModel.setAssetCode(PallasConsoleProperties.PALLAS_AUTHROIZE_APP+"."+permission.getPermissionCode());
	                privilegeResultModel.setPrivilegeName(PallasConsoleProperties.PALLAS_AUTHROIZE_APP+"."+permission.getPermissionName());
	                priviles.add(privilegeResultModel);
				}
				privilegeResultMap.put(userAccountId, priviles);
			}
        }
        return privilegeResultMap;
    }

    @Override
    public boolean authorize(AuthorizeModel authorizeModel) throws Exception {
    	List<Permission> permissions = permissionRepository.selectByUsername(authorizeModel.getUserId());
    	String permissionName = authorizeModel.getAssetCode() + "." + authorizeModel.getActionTypeName();
    	for (Permission permission : permissions) {
    		if (permissionName.equals(PallasConsoleProperties.PALLAS_AUTHROIZE_APP + "." + permission.getPermissionName())) {
    			return true;
    		}
		}
        return false;
    }
}