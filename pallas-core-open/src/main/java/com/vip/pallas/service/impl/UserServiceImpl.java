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
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vip.pallas.bean.RoleModel;
import com.vip.pallas.bean.UserModel;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.Role;
import com.vip.pallas.mybatis.entity.User;
import com.vip.pallas.mybatis.entity.UserRole;
import com.vip.pallas.mybatis.repository.RoleRepository;
import com.vip.pallas.mybatis.repository.UserRepository;
import com.vip.pallas.mybatis.repository.UserRoleRepository;
import com.vip.pallas.orika.OrikaBeanMapper;
import com.vip.pallas.service.UserService;
import com.vip.vjtools.vjkit.collection.ListUtil;

@Service
@Transactional(rollbackFor=Exception.class)
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private RoleRepository roleRepository;
	
	@Autowired
	private UserRoleRepository userRoleRepository;
	
	@Autowired
	private OrikaBeanMapper beanMapper;
	
	@Override
	public UserModel findByAuthenticationCode(String username, String password) {
		User user = userRepository.selectByUsername(username);
		if (null == user) {
			throw new BusinessLevelException(500, "用户不存在");
		}
		if (!user.getPassword().equals(password)){
			throw new BusinessLevelException(500, "用户密码错误");
		}
		return beanMapper.map(user, UserModel.class);
	}
	
	@Override
	public void createOrUpdateUser(UserModel userModule) {
		Date createTime = new Date();
		List<Role> dbRoles = new ArrayList<>();
		if (ListUtil.isNotEmpty(userModule.getRoles())) {
			for (RoleModel roleName : userModule.getRoles()) {
				Role role = roleRepository.selectByRoleName(roleName.getRoleName());
				if (null == role) {
					throw new BusinessLevelException(500, "用户信息中存在异常角色信息"); 
				} else {
					dbRoles.add(role);
				}
			}
		} else {
			throw new BusinessLevelException(500, "用户信息中未选择角色信息");
		}
		User user = beanMapper.map(userModule, User.class);
		User dbUser = userRepository.selectByUsername(userModule.getUsername());
		if (null == userModule.getId()){
			if (null != dbUser) {
				throw new BusinessLevelException(500, "用户名已存在"); 
			}
			user.setCreatedBy(userModule.getLastUpdatedBy());
			user.setCreateTime(createTime);
			
			userRepository.insertSelective(user);
		} else {
			if (null != dbUser && dbUser.getId() != userModule.getId()) {
				throw new BusinessLevelException(500, "用户名已存在"); 
			}
			userRepository.updateByPrimaryKeySelective(user);
		}
		
		List<UserRole> userRoles = new ArrayList<>();
		for (Role role : dbRoles) {
			UserRole userRole = new UserRole();
			userRole.setUserId(user.getId());
			userRole.setRoleId(role.getId());
			userRole.setCreateTime(createTime);
			userRole.setCreatedBy(user.getLastUpdatedBy());
			userRole.setLastUpdatedBy(user.getLastUpdatedBy());
			
			userRoles.add(userRole);
		}
		
		List<UserRole> dbUserRoles = userRoleRepository.selectByUserId(user.getId());
		List<UserRole> needAddRoles = subtract(userRoles, dbUserRoles);
		List<UserRole> needRemoveRoles = subtract(dbUserRoles, userRoles);
		
		if (ListUtil.isNotEmpty(needAddRoles)) {
			for (UserRole userRole : needAddRoles) {
				userRoleRepository.insertSelective(userRole);
			}
		}
		
		if (ListUtil.isNotEmpty(needRemoveRoles)) {
			for (UserRole userRole : needRemoveRoles) {
				userRoleRepository.deleteByPrimaryKey(userRole.getId());
			}
		}
	}
	
	private List<UserRole> subtract(List<UserRole> a, List<UserRole> b) {
		if (null == a){
            return ListUtil.emptyList();
        } else if (null == b){
            return a;
        } else {
            List<UserRole> intersect = intersection(a, b);
            if (ListUtil.isNotEmpty(intersect)) {
            	List<UserRole> results = new ArrayList<>(a);
            	results.removeAll(intersect);
            	return results;
            } else {
            	return a;
            }
        }
	}
	
	private List<UserRole> intersection(List<UserRole> a, List<UserRole> b) {
		List<UserRole> intersect = new ArrayList<>();
		for (UserRole aUserRole : a) {
			for (UserRole bUserRole : b) {
				if (aUserRole.getUserId() == bUserRole.getUserId() && aUserRole.getRoleId() == bUserRole.getRoleId()) {
					intersect.add(aUserRole);
					break;
				}
			}
		}
		return intersect;
	}

	@Override
	public void deleteUserById(Long id) {
		userRepository.deleteByPrimaryKey(id);
	}

	@Override
	public List<UserModel> queryByKeywords(String keywords, Integer page, Integer size) {
		int offest = size * (page - 1);
		return beanMapper.mapAsList(userRepository.selectUserBykeywords(keywords, offest, size), UserModel.class);
	}

	@Override
	public int queryCountBykeywords(String keywords) {
		return userRepository.selectCountBykeywords(keywords);
	}

	@Override
	public UserModel findById(Long id) {
		User user = userRepository.selectById(id);
		if (null == user) {
			throw new BusinessLevelException(500, "用户名不存在"); 
		}
		return beanMapper.map(user, UserModel.class);
	}
}