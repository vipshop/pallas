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

package com.vip.pallas.console.controller.monitor;


import com.vip.pallas.bean.monitor.*;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.service.MonitorService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    private static final Logger logger = LoggerFactory.getLogger(MonitorController.class);

    @Autowired
    private MonitorService monitorService;

    @RequestMapping("/cluster.json")
    @ResponseBody
    public ClusterMetricInfoModel cluserMoniror(@RequestBody @Validated MonitorQueryModel queryModel) throws Exception {

        ClusterMetricInfoModel clusterMetricInfoModel = monitorService.queryClusterMetrics(queryModel);
        return clusterMetricInfoModel;
    }

    @RequestMapping("/node.json")
    @ResponseBody
    public NodeMetricInfoModel nodeMonitor(@RequestBody @Validated MonitorQueryModel queryModel)throws Exception {
        if(StringUtils.isEmpty(queryModel.getNodeName())) {
            throw new PallasException("节点名不能为空");
        }
        NodeMetricInfoModel nodeMetricInfoModel =  monitorService.queryNodeMetrics(queryModel);
        return nodeMetricInfoModel;
    }

    @RequestMapping("/index.json")
    @ResponseBody
    public IndexMetricInfoModel indexMonitor(@RequestBody @Validated MonitorQueryModel queryModel) throws Exception{
        if(StringUtils.isEmpty(queryModel.getIndexName())) {
            throw new PallasException("索引名不能为空");
        }
        IndexMetricInfoModel indexMetricInfoModel = monitorService.queryIndexMetrices(queryModel);
        return indexMetricInfoModel;
    }

    @RequestMapping("/nodes/info.json")
    @ResponseBody
    public List<NodeGaugeMetricModel> getGenericNodeInfos(@Validated @RequestBody MonitorQueryModel queryModel) throws Exception{
        List<NodeGaugeMetricModel> result = monitorService.queryNodesInfo(queryModel);

        return result;
    }

    @RequestMapping("/indices/info.json")
    @ResponseBody
    public List<IndexGaugeMetricModel> getGenericIndexInfos(@Validated @RequestBody MonitorQueryModel queryModel) throws Exception{
        List<IndexGaugeMetricModel> result = monitorService.queryIndicesInfo(queryModel);
        return result;
    }

    @RequestMapping("nodes/count.json")
    @ResponseBody
    public Integer getNodeCount(@Validated @RequestBody MonitorQueryModel queryModel) throws Exception{
        return monitorService.getNodeCount(queryModel);
    }

    @RequestMapping("indices/count.json")
    @ResponseBody
    public Integer getIndexCount(@Validated @RequestBody MonitorQueryModel queryModel) throws Exception{
        return monitorService.getIndexCount(queryModel);
    }

}
