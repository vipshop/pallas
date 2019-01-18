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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.vip.pallas.bean.RoleModel;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.mybatis.repository.RoleRepository;
import com.vip.pallas.orika.OrikaBeanMapper;
import com.vip.pallas.service.RoleService;

@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private OrikaBeanMapper beanMapper;
	
	@Override
	public List<RoleModel> queryByKeywords(Page<RoleModel> search) {
		return beanMapper.mapAsList(roleRepository.selectRoleByKeyword(search), RoleModel.class);
	}
}