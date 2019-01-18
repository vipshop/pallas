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

import com.vip.pallas.console.utils.AuditLogUtil;
import com.vip.pallas.console.utils.SessionUtil;
import com.vip.pallas.utils.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class LogoutController {

    private static Logger logger = LoggerFactory.getLogger(LogoutController.class);

    @RequestMapping(value = "/logout", method = {RequestMethod.GET, RequestMethod.POST})
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
    	AuditLogUtil.getAuditLog().info("{} logouted in, ip {}. ", SessionUtil.getLoginUser(req), SessionUtil.getRemoteAddr(req));

    	SessionUtil.invalidateSession(req);

        redirectToCasLogoutUrl(req, resp);
    }

    public void redirectToCasLogoutUrl(HttpServletRequest req, HttpServletResponse resp) {
        try {
            resp.sendRedirect(ConfigReader.getProperty("pallas.login.url"));
        } catch (IOException e) {
            logger.error(e.getClass() + " " + e.getMessage(), e);
        }
    }
}