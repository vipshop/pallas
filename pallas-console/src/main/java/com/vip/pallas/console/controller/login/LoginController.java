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

package com.vip.pallas.console.controller.login;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.bean.UserModel;
import com.vip.pallas.console.utils.AuditLogUtil;
import com.vip.pallas.console.utils.SessionUtil;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.service.UserService;

@RestController
@RequestMapping(path = "/authorization")
public class LoginController {

    private static Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @Autowired
    private UserService userService;

    @Validated
    @RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
	public void login(@RequestParam @NotBlank(message = "username不能为空") String username,
			@RequestParam @NotBlank(message = "passwor不能为空") String password, HttpServletRequest request,
			HttpServletResponse response) {
    	try {
    		UserModel user = userService.findByAuthenticationCode(username, password);
    		request.getSession().setAttribute(SessionUtil.SESSION_USERNAME, user.getUsername());
    		request.getSession().setAttribute(SessionUtil.SESSION_REAL_USERNAME, user.getRealName());
    		AuditLogUtil.getAuditLog().info("{} logged in, ip {}. ", user.getRealName(), SessionUtil.getRemoteAddr(request));
		} catch (Exception e) {
			logger.error(e.toString());
			throw new BusinessLevelException(500, e.getMessage());
		}
    }
}