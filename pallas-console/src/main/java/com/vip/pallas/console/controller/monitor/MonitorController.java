package com.vip.pallas.console.controller.monitor;

import com.vip.pallas.bean.monitor.ClusterMetricInfoModel;
import com.vip.pallas.bean.monitor.IndexMetricInfoModel;
import com.vip.pallas.bean.monitor.MonitorQueryModel;
import com.vip.pallas.bean.monitor.NodeMetricInfoModel;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.service.MonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitor")
public class MonitorController {

    private static final Logger logger = LoggerFactory.getLogger(MonitorController.class);

    @Autowired
    private MonitorService monitorService;

    @RequestMapping("/cluster")
    @ResponseBody
    public ClusterMetricInfoModel cluserMoniror(@RequestBody MonitorQueryModel queryModel) throws PallasException {

        ClusterMetricInfoModel clusterMetricInfoModel = monitorService.queryClusterMetrics(queryModel);
        return clusterMetricInfoModel;
    }

    @RequestMapping("/node")
    @ResponseBody
    public NodeMetricInfoModel nodeMonitor(@RequestBody MonitorQueryModel queryModel)throws PallasException {
        NodeMetricInfoModel nodeMetricInfoModel =  monitorService.queryNodeMetrics(queryModel);
        return nodeMetricInfoModel;
    }

    @RequestMapping("/index")
    @ResponseBody
    public IndexMetricInfoModel indexMonitor(@RequestBody MonitorQueryModel queryModel) throws PallasException{
//        queryModel.setFrom();
//        queryModel.setTo();
//        queryModel.setClusterName();
//        queryModel.setIndexName();
        IndexMetricInfoModel indexMetricInfoModel = monitorService.queryIndexMetrices(queryModel);
        return indexMetricInfoModel;
    }
}
