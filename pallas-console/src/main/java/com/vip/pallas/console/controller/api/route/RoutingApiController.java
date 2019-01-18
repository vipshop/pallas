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

package com.vip.pallas.console.controller.api.route;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.mybatis.entity.IndexRouting;
import com.vip.pallas.mybatis.entity.IndexRoutingTargetGroup;
import com.vip.pallas.mybatis.entity.SearchAuthorization;
import com.vip.pallas.service.IndexRoutingService;
import com.vip.pallas.service.SearchAuthorizationService;

@RestController
public class RoutingApiController {

    @Resource
    private SearchAuthorizationService authService;

    @Resource
    private IndexRoutingService routingService;

    @RequestMapping("/route/index_routing_authorization/all.json")
    public List<SearchAuthorization> selectAll() {
        return authService.selectAll();
    }

    @RequestMapping("/route/index_routing/all.json")
    public List<IndexRouting> getAllIndexRouting() {
        return routingService.getAllIndexRouting();
    }

    @RequestMapping("/route/index_routing_target_group/all.json")
    public List<IndexRoutingTargetGroup> getAllIndexRoutingTargetGroup() {
        return routingService.getAllIndexRoutingTargetGroup();
    }
}