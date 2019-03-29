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

package com.vip.pallas.test.utils;

import com.vip.pallas.bean.monitor.MonitorLevelModel;
import com.vip.pallas.console.vo.ClusterVO;
import com.vip.pallas.console.vo.FlowRecordConfigVO;
import com.vip.pallas.console.vo.IndexVO;
import com.vip.pallas.console.vo.TemplateVO;
import com.vip.pallas.test.base.ConstantValue;

public class ObjectJsonUtils {

    public static IndexVO getIndexVO() {
        IndexVO indexVO = new IndexVO();
        indexVO.setIndexName("index_test_" + System.currentTimeMillis());
        indexVO.setDescription("pallas index test");
        indexVO.setConfirm(false);
        
        return indexVO;
    }

    public static ClusterVO getClusterVO() {
        ClusterVO clusterVO = new ClusterVO();
        clusterVO.setClusterId("cluster_test_" + System.currentTimeMillis());
        // clusterVO.setClusterId("pallas-test-cluster");
        clusterVO.setDescription("es cluster test");
        clusterVO.setHttpAddress(ConstantValue.serverHttpAddress);
        clusterVO.setClientAddress(ConstantValue.clintHttpAddress);
        clusterVO.setRealClusters(null);
        clusterVO.setAccessiblePs(ConstantValue.accessiblePallasSearch);
        clusterVO.setMonitorLevelModel(MonitorLevelModel.getDefaultModel());

        return clusterVO;
    }

    public static TemplateVO getTemplateVO() {
        TemplateVO templateVO = new TemplateVO();
        templateVO.setTemplateName("template_test_" + System.currentTimeMillis());
        templateVO.setDescription("template test");
        templateVO.setType(1);

        return templateVO;
    }

    public static FlowRecordConfigVO getFlowRecordConfigVO() {
        FlowRecordConfigVO flowRecordConfigVO = new FlowRecordConfigVO();
        flowRecordConfigVO.setEndTime("2019-12-21T02:22:20.000Z");
        flowRecordConfigVO.setLimit(100L);
        flowRecordConfigVO.setNote("desc");
        flowRecordConfigVO.setSampleRate(0.2);
        flowRecordConfigVO.setStartTime("2019-11-21T02:22:20.000Z");
        flowRecordConfigVO.setTemplateId(-1L);

        return flowRecordConfigVO;
    }

}