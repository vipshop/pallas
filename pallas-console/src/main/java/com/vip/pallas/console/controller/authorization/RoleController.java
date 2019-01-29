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
import java.util.List;

import javax.validation.constraints.Min;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.bean.RoleModel;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.service.RoleService;

@Validated
@RestController
@RequestMapping(path="/authorization/role")
public class RoleController {

	@Autowired
	private RoleService roleService;
	
	@RequestMapping(path="page.json", method = RequestMethod.GET)
	public PageResultVO<RoleModel> page(
			@RequestParam(required = false, defaultValue = "1") @Min(value = 0, message = "currentPage必须为正数") Integer currentPage,
			@RequestParam(required = false, defaultValue = "10") @Min(value = 0, message = "pageSize必须为正数") Integer pageSize,
			@RequestParam(required = false, defaultValue = "") String keywords) { 
		keywords = StringUtils.defaultIfEmpty(keywords, StringUtils.EMPTY);
        try {
            keywords = URLDecoder.decode(keywords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new BusinessLevelException(500, "keywords无法正常解析");
        }
        Page<RoleModel> search = new Page<>();
		search.setPageNo(currentPage);
		search.setPageSize(pageSize);
		if (StringUtils.isNoneBlank(keywords)){
			search.setParam("keywords", keywords);
		}
        List<RoleModel> roles = roleService.queryByKeywords(search);
        
        PageResultVO<RoleModel> result = new PageResultVO<>();
        result.setTotal(search.getTotalRecord());
        result.setPageCount(search.getTotalPage());
        result.setList(roles);
        return result;
	}
}