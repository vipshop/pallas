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

package com.vip.pallas.console.controller.index;

import java.io.IOException;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.Index;
import com.vip.pallas.service.IndexService;

@Validated
@RestController
public class IndexDsExportController {

    @Autowired
    private IndexService indexService;

    @RequestMapping(value = "/ds/export.json", method = RequestMethod.GET)
	public void export(@RequestParam @NotNull(message = "indexId不能为空") @Min(value = 1, message = "indexId必须为大于1") Long indexId,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
        Index index = indexService.findById(indexId);
        if (null == index){
        	throw new BusinessLevelException(500, "index不存在");
        }
        if (!AuthorizeUtil.authorizeIndexPrivilege(request, indexId, index.getIndexName())) {
        	throw new BusinessLevelException(403, "无权限操作");
        }
        response.setStatus(200);
        response.setContentType("application/text");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + index.getIndexName() + "_" + index.getId() + "_datasource.txt\"");
        if (index.getDataSourceList() != null) {
            String s = index.getDataSourceList().stream().map(d -> d.getIp() + " " + d.getPort() + " " +
                    d.getUsername() + " " + d.getPassword() + " " + d.getDbname() + " " + d.getTableName()).collect(Collectors.joining("\r\n"));
            response.getWriter().write(s);
            response.flushBuffer();
        }
    }
}