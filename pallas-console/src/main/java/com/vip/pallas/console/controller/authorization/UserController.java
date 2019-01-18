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

package com.vip.pallas.console.controller.authorization;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.bean.RoleModel;
import com.vip.pallas.bean.UserModel;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.utils.SessionUtil;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.UserVO;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.service.UserService;
import com.vip.vjtools.vjkit.collection.ListUtil;

@Validated
@RestController
@RequestMapping(path="/authorization/user")
public class UserController {
	
	@Autowired
	private UserService userService;

	@RequestMapping(path="page.json", method = RequestMethod.GET)
	public PageResultVO<UserModel> page(
			@RequestParam(required = false, defaultValue = "1") @Min(value = 0, message = "currentPage必须为正数") Integer currentPage,
			@RequestParam(required = false, defaultValue = "10") @Min(value = 0, message = "pageSize必须为正数") Integer pageSize,
			@RequestParam(required = false, defaultValue = "") String keywords) {
		keywords = StringUtils.defaultIfEmpty(keywords, StringUtils.EMPTY);
        try {
            keywords = URLDecoder.decode(keywords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BusinessLevelException(500, "keywords无法正常解析");
        }
        
        PageResultVO<UserModel> result = new PageResultVO<>();
        List<String> privileges = AuthorizeUtil.loadPrivileges();

        if(privileges == null || !privileges.contains("user.all")) {
            throw new BusinessLevelException(403, "无权限操作");
        } else {
        	result.setAllPrivilege(true);
        }
        
        int count = userService.queryCountBykeywords(keywords);
        List<UserModel> users = Collections.emptyList();
        if (count > 0 ){
        	users = userService.queryByKeywords(keywords, currentPage, pageSize);
        }
        
        result.setTotal((long)count);
        result.setPageCountByCompute(pageSize);
        result.setList(users);
		return result;
	}
	
	@RequestMapping(path = "/update.json", method = RequestMethod.POST)
	public void createOrUpdateUser(@RequestBody @Validated UserVO userVO, HttpServletRequest request) {
		UserModel user = new UserModel();
		user.setId(userVO.getId());
		user.setUsername(userVO.getUsername());
		user.setPassword(userVO.getPassword());
		user.setRealName(userVO.getRealName());
		user.setEmployeeId(userVO.getEmployeeId());
		user.setEmail(userVO.getEmail());
		user.setLastUpdatedBy(SessionUtil.getLoginUser(request));
		
		List<RoleModel> roles = new ArrayList<>();
		if (ListUtil.isNotEmpty(userVO.getRoleNames())) {
			for (String roleName : userVO.getRoleNames()) {
				RoleModel role = new RoleModel();
				role.setRoleName(roleName);
				roles.add(role);
			}
		}
		
		user.setRoles(roles);
		userService.createOrUpdateUser(user);
	}
	
	@RequestMapping(path = "delete/{id}.json", method = RequestMethod.GET)
	public void deleteUser(@PathVariable("id") @Min(value = 1, message = "id必须大于等于1")Long id) {
		UserModel user = userService.findById(id);
		String loginUser = SessionUtil.getLoginUser();
		if (null != loginUser && user != null && loginUser.equals(user.getUsername())) {
			throw new BusinessLevelException(500, "不允许删除自己");
		}
		userService.deleteUserById(id);
	}
}