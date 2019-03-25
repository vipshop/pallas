package com.vip.pallas.console.controller.monitor;


import com.vip.pallas.bean.monitor.*;
import com.vip.pallas.service.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/monitor")
public class MonitorController {

    private static final Logger logger = LoggerFactory.getLogger(MonitorController.class);

    @Autowired
    private MonitorService monitorService;

    @RequestMapping("/cluster.json")
    @ResponseBody
    public ClusterMetricInfoModel cluserMoniror(@RequestBody MonitorQueryModel queryModel) throws Exception {

        ClusterMetricInfoModel clusterMetricInfoModel = monitorService.queryClusterMetrics(queryModel);
        return clusterMetricInfoModel;
    }

    @RequestMapping("/node.json")
    @ResponseBody
    public NodeMetricInfoModel nodeMonitor(@RequestBody MonitorQueryModel queryModel)throws Exception {
        NodeMetricInfoModel nodeMetricInfoModel =  monitorService.queryNodeMetrics(queryModel);
        return nodeMetricInfoModel;
    }

    @RequestMapping("/index.json")
    @ResponseBody
    public IndexMetricInfoModel indexMonitor(@RequestBody MonitorQueryModel queryModel) throws Exception{

        IndexMetricInfoModel indexMetricInfoModel = monitorService.queryIndexMetrices(queryModel);
        return indexMetricInfoModel;
    }

    @RequestMapping("/nodes/info.json")
    @ResponseBody
    public List<NodeGaugeMetricModel> getGenericNodeInfos(@RequestBody MonitorQueryModel queryModel) throws Exception{
        List<NodeGaugeMetricModel> result = monitorService.queryNodesInfo(queryModel);
        return result;
    }

    @RequestMapping("/indices/info.json")
    @ResponseBody
    public List<IndexGaugeMetricModel> getGenericIndexInfos(@RequestBody MonitorQueryModel queryModel) throws Exception{
        List<IndexGaugeMetricModel> result = monitorService.queryIndicesInfo(queryModel);
        return result;
    }

}
