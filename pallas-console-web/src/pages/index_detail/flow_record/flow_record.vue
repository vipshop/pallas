<template>
    <div class="my-tab-content">
        <el-collapse v-model="activeNames">
            <el-collapse-item name="1">
                <template slot="title">
                    采集规则
                </template>
                <flow-record-config @get-flow-export="getFlowExport"></flow-record-config>
            </el-collapse-item>
            <el-collapse-item title="采集结果" name="2">
                <template slot="title">
                    采集结果 -- 规则ID：<span style="color: #32cd32;font-weight: bold;">{{flowRecordExportParams.configId}}</span>
                </template>
                <flow-record-export :flow-record-export-params="flowRecordExportParams"></flow-record-export>
            </el-collapse-item>
        </el-collapse>
    </div>
</template>

<script>
import FlowRecordExport from './flow_record_config/flow_record_export';
import FlowRecordConfig from './flow_record_config/flow_record_config';

export default {
  components: {
    'flow-record-export': FlowRecordExport,
    'flow-record-config': FlowRecordConfig,
  },
  data() {
    return {
      activeNames: ['1', '2'],
      flowRecordExportParams: {},
    };
  },
  methods: {
    getFlowExport(params) {
      this.flowRecordExportParams = JSON.parse(JSON.stringify(params));
    },
  },
};
</script>
