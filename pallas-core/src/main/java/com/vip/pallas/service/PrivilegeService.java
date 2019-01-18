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

package com.vip.pallas.service;

import java.util.List;
import java.util.Map;

import com.vip.pallas.bean.AuthorizeModel;
import com.vip.pallas.bean.PrivilegeResultModel;

public interface PrivilegeService {
	
	public void createIndexPrivilege(String indexIdAndName) throws Exception;
	
	public void deleteIndexPrivilege(String indexIdAndName) throws Exception;
	
	public void deleteIndexAsset(String indexIdAndName) throws Exception;
	
	public void createClusterPrivilege(String clusterId) throws Exception;

	public void createVersionPrivilege(String privilegeName) throws Exception;

	public void createTemplatePrivilege(String privilegeName) throws Exception;

	public void deleteClusterPrivilege(String clusterId) throws Exception;
	
	public void deleteClusterAsset(String clusterId) throws Exception;
	
	Map<String, List<PrivilegeResultModel>> getPrivilegeByUsers(List<String> userAccountIds) throws Exception;
	
	boolean authorize(AuthorizeModel authorizeModel) throws Exception;
}