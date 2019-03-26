<template>
    <div class="my-tab-content" v-loading="loading" element-loading-text="请稍等···">
      <div class="monitor-top">
            <el-table :data="gaugeMetricData" border style="width: 100%">
                <el-table-column prop="transportAddress" label="transportAddress"></el-table-column>
                <el-table-column prop="jvmHeapUsage" label="JVM Heap"></el-table-column>
                <el-table-column prop="availableFS" label="Free Disk"></el-table-column>
                <el-table-column prop="documentCount" label="Documents"></el-table-column>
                <el-table-column prop="documentStore" label="Data"></el-table-column>
                <el-table-column prop="indexCount" label="Indices"></el-table-column>
                <el-table-column prop="shardCount" label="Shards"></el-table-column>
            </el-table>
      </div>  
      <div>
            <el-row :gutter="10">
                <el-col :xs="24" :sm="24" :md="24" :lg="24" class="chart-auto-size">
                    <chart-container title="gc Count" type="line">
                        <div slot="chart">
                            <MyLine id="gcCount" :option-info="gcCountInfo"></MyLine>
                        </div>
                    </chart-container>
                </el-col>
            </el-row>
      </div>
    </div>
</template>
<script>
export default {
  data() {
    return {
      loading: false,
      gaugeMetricData: [],
      gcCountInfo: {},
      gc_duration_old_ms_info: {},
      gc_duration_young_ms_info: {},
      jvm_heap_max_byte_info: {},
      jvm_heap_used_byte_info: {},
      cpuNodePercentInfo: {},
      cpuProcessPerentInfo: {},
      index_memory_lucenc_total_byte_info: {},
      index_memory_terms_bytes_info: {},
      segmentCountInfo: {},
      searchThreadpoolQueueInfo: {},
      searchThreadpoolRejectInfo: {},
      indexThreadpoolQueueInfo: {},
      indexThreadpoolRejectInfo: {},
      bulkThreadpoolQueueInfo: {},
      bulkThreadpoolRejectInfo: {},
      httpOpenCurrentInfo: {},
    };
  },
  methods: {
    getgcCount(gcCountOld, gcCountYoung) {
      const optionInfo = {
        xAxis: gcCountOld.map(e => e.x),
        seriesData: [
          { name: 'gc Count Old', data: gcCountOld.map(e => e.y) },
          { name: 'gc Count Young', data: gcCountYoung.map(e => e.y) },
        ],
        yAxisName: 's',
      };
      this.gcCountInfo = optionInfo;
    },
    getNodeMonitor() {
      const params = {
        clusterName: this.clusterId,
        nodeName: this.node,
        from: new Date().getTime() - (Number(this.timeInterval) * 60 * 1000),
        to: new Date().getTime(),
      };
      this.$http.post('/monitor/node.json', params).then((data) => {
        if (data) {
          this.gaugeMetricData.push(data.gaugeMetric);
          this.getgcCount(data.gcCountOld, data.gcCountYoung);
        }
      });
    },
  },
  computed: {
    timeInterval() {
      return this.$store.state.monitorTimeInterval;
    },
    clusterId() {
      return this.$route.query.clusterId;
    },
    node() {
      return this.$route.query.node;
    },
  },
  created() {
    this.getNodeMonitor();
  },
  watch: {
    '$store.state.monitorTimeInterval': function interval(val) {
      console.log(val);
      this.getNodeMonitor();
    },
  },
};
</script>
