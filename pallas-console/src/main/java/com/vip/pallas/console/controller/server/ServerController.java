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

package com.vip.pallas.console.controller.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vip.pallas.console.utils.AuditLogUtil;
import com.vip.pallas.console.utils.AuthorizeUtil;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.exception.BusinessLevelException;
import com.vip.pallas.mybatis.entity.SearchServer;
import com.vip.pallas.mybatis.entity.SearchServerExample;
import com.vip.pallas.service.ClusterService;
import com.vip.pallas.service.SearchServerService;
import com.vip.pallas.thread.PallasThreadFactory;
import com.vip.pallas.utils.HttpClient;
import com.vip.pallas.utils.ObjectMapTool;

@Validated
@RestController
@RequestMapping("/ss")
public class ServerController {
    private static Logger logger = LoggerFactory.getLogger(ServerController.class);

    @Autowired
    private SearchServerService searchServerService;

    @Autowired
    protected ClusterService clusterService;

    String serverUpdateRoutingUrl = "http://%s/_py/update_routing";

    ExecutorService executorService = Executors.newFixedThreadPool(3, new PallasThreadFactory("update-ps-routing"));

    @RequestMapping("/clusters.json")
    public List<String> clusters() {
        return searchServerService.selectDistictCluster();
    }

    @RequestMapping(value = "/find.json", method = RequestMethod.GET)
    public PageResultVO<SearchServer> page(@RequestParam(required = false, defaultValue = "1") @Min(value = 0, message = "currentPage必须为正数") Integer currentPage,
                                    @RequestParam(required = false, defaultValue = "10") @Min(value = 0, message = "pageSize必须为正数") Integer pageSize,
                                    @RequestParam(required = false)  String selectedCluster, HttpServletRequest request) { // NOSONAR
        if (!AuthorizeUtil.authorizePSearchPrivilege(request, null)){
        	throw new BusinessLevelException(403, "无权限操作");
        }

        PageResultVO<SearchServer> resultVO = new PageResultVO<>();

        SearchServerExample example = new SearchServerExample();
        SearchServerExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(selectedCluster)) {
            criteria.andClusterEqualTo(selectedCluster);
        }
        example.setOffset((currentPage-1) * pageSize);
        example.setLimit(pageSize);
        example.setOrderByClause(" healthy desc, cluster asc, ipport asc ");

        long total = searchServerService.countByExample(example);
        int pageCount = (int) (total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
        List<SearchServer> ssList = searchServerService.selectByExampleWithBLOBsAndHealthyInterval(example);
        // ssList = searchServerService.markUnHealthyServer(ssList);
        resultVO.setList(ssList);
        resultVO.setTotal(total);
        resultVO.setPageCount(pageCount);

        return resultVO;
    }

    @RequestMapping(value = "/traffic.json", method = RequestMethod.POST)
    public void deleteTraffic(@RequestBody Map<String, Object> params, HttpServletRequest request) {
    	if (!AuthorizeUtil.authorizePSearchPrivilege(request, null)){
        	throw new BusinessLevelException(403, "无权限操作");
        }
    	
        Long id = ObjectMapTool.getLong(params, "id");
        Boolean takeTraffic = ObjectMapTool.getBoolean(params, "takeTraffic");

        searchServerService.setTakeTraffic(id, takeTraffic);
        AuditLogUtil.log("update take traffic setting for search server: id - {0}", id);
    }

    @RequestMapping(value = "/remote_update.json", method = RequestMethod.POST)
    public void remoteUpdate(@RequestBody Map<String, Object> params, HttpServletRequest request) throws Exception {
    	if (!AuthorizeUtil.authorizePSearchPrivilege(request, null)){
        	throw new BusinessLevelException(403, "无权限操作");
        }
    	
        String ids = ObjectMapTool.getString(params, "ssIds");
        String[] idArray = ids.split(",");
        for (String id: idArray) {
            SearchServer ss = searchServerService.selectByPrimaryKey(Long.valueOf(id));
            logger.info("invoke {} to update its routing rules.", ss.getIpport());
            HttpClient.httpGet(String.format(serverUpdateRoutingUrl, ss.getIpport()));
        }
    }

    @RequestMapping(value = "/update_routing.json", method = RequestMethod.POST)
    public Map<String, Object> updateRouting(HttpServletRequest request) {
    	if (!AuthorizeUtil.authorizePSearchPrivilege(request, null)){
        	throw new BusinessLevelException(403, "无权限操作");
        }
    	
        List<SearchServer> ssList = searchServerService.selectHealthyServers(SearchServerService.HEALTHY_UPLOAD_INTERVAL_TOLERANCE);
        StringBuilder sb = new StringBuilder();
        List<Future<String>> futures = new ArrayList<>(ssList.size());
        for (SearchServer searchServer : ssList) {
            futures.add(executorService.submit(new UpdateRouteCallable(searchServer)));
        }
        for (int i = 0; i < futures.size(); i++) {
            try {
                Future<String> f = futures.get(i);
                sb.append(f.get(10, TimeUnit.SECONDS));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                sb.append(ssList.get(i).getIpport()).append(" failed:").append(e.getMessage()).append(' ');
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("result", sb.toString());
        return resultMap;
    }

    @RequestMapping(value = "/delete.json", method = RequestMethod.POST)
    public void delete(@RequestBody Map<String, Object> params, HttpServletRequest request) {
    	if (!AuthorizeUtil.authorizePSearchPrivilege(request, null)){
        	throw new BusinessLevelException(403, "无权限操作");
        }
    	
        Long id = ObjectMapTool.getLong(params, "id");
        if (id != null) { // delete by id
            searchServerService.deleteByPrimaryKey(id);
            AuditLogUtil.log("delete search server: id - {0}", id);
        } else { // delete by days.
            Integer days = ObjectMapTool.getInteger(params, "days");
            if (days > 0) {
                searchServerService.deleteNDaysOldServer(days);
                AuditLogUtil.log("delete search server by expired days: {0}", days);
            }
        }
    }

    class UpdateRouteCallable implements Callable<String> {
        private SearchServer ss;
        UpdateRouteCallable(SearchServer ss) {
            this.ss = ss;
        }
        @Override
        public String call() throws Exception {
            try {
                logger.info("invoke {} to update its routing rules.", ss.getIpport());
                HttpClient.httpGet(String.format(serverUpdateRoutingUrl, ss.getIpport()));
                return ss.getIpport() + " succeed.";
            } catch(Exception e) {
                logger.error(e.getMessage(), e);
                return ss.getIpport() + " failed:" + e.getMessage() + "  ";
            }
        }
    }
}