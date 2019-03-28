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
              <el-col :span="12">
                  <chart-container title="gc Count" type="line">
                      <div slot="chart">
                          <MyLine id="gcCount" :option-info="gcCountInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container title="gc Duration" type="line">
                      <div slot="chart">
                          <MyLine id="gcDuration" :option-info="gcDurationInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container title="jvm heap" type="line">
                      <div slot="chart">
                          <MyLine id="jvmHeap" :option-info="jvmHeapInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container title="cpu percent" type="line">
                      <div slot="chart">
                          <MyLine id="cpuPercent" :option-info="cpuPercentInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container title="index memory" type="line">
                      <div slot="chart">
                          <MyLine id="indexMemory" :option-info="indexMemoryInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container title="threadpool Queue" type="line">
                      <div slot="chart">
                          <MyLine id="threadpoolQueue" :option-info="threadpoolQueueInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container title="threadpool Reject" type="line">
                      <div slot="chart">
                          <MyLine id="threadpoolReject" :option-info="threadpoolRejectInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container title="segment count" type="line">
                      <div slot="chart">
                          <MyLine id="segmentCount" :option-info="segmentCountInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container title="http open current" type="line">
                      <div slot="chart">
                          <MyLine id="httpOpenCurrent" :option-info="httpOpenCurrentInfo"></MyLine>
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
      gcDurationInfo: {},
      jvmHeapInfo: {},
      cpuPercentInfo: {},
      indexMemoryInfo: {},
      segmentCountInfo: {},
      threadpoolQueueInfo: {},
      threadpoolRejectInfo: {},
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
    getgcDuration(gcDurationOld, gcDurationYoung) {
      const optionInfo = {
        xAxis: gcDurationOld.map(e => e.x),
        seriesData: [
          { name: 'gc Duration Old', data: gcDurationOld.map(e => e.y) },
          { name: 'gc Duration Young', data: gcDurationYoung.map(e => e.y) },
        ],
        yAxisName: 'ms',
      };
      this.gcDurationInfo = optionInfo;
    },
    getJVMHeap(jvmHeapMax, jvmHeapUsed) {
      const optionInfo = {
        xAxis: jvmHeapMax.map(e => e.x),
        seriesData: [
          { name: 'jvm heap max', data: jvmHeapMax.map(e => e.y) },
          { name: 'jvm heap used', data: jvmHeapUsed.map(e => e.y) },
        ],
        yAxisName: 'mb',
      };
      this.jvmHeapInfo = optionInfo;
    },
    getCpuPercent(cpuNodePercent, cpuProcessPerent) {
      const optionInfo = {
        xAxis: cpuNodePercent.map(e => e.x),
        seriesData: [
          { name: 'cpu node percent', data: cpuNodePercent.map(e => e.y) },
          { name: 'cpu process percent', data: cpuProcessPerent.map(e => e.y) },
        ],
        yAxisName: '%',
      };
      this.cpuPercentInfo = optionInfo;
    },
    getIndexMemory(indexMemoryLucencTotal, indexMemoryTerms) {
      const optionInfo = {
        xAxis: indexMemoryLucencTotal.map(e => e.x),
        seriesData: [
          { name: 'index memory Lucenc total', data: indexMemoryLucencTotal.map(e => e.y) },
          { name: 'index memory terms', data: indexMemoryTerms.map(e => e.y) },
        ],
        yAxisName: 'mb',
      };
      this.indexMemoryInfo = optionInfo;
    },
    getTheadPoolQueue(search, indexing, bulk) {
      const optionInfo = {
        xAxis: search.map(e => e.x),
        seriesData: [
          { name: 'queue-search', data: search.map(e => e.y) },
          { name: 'queue-indexing', data: indexing.map(e => e.y) },
          { name: 'queue-bulk', data: bulk.map(e => e.y) },
        ],
        yAxisName: '',
      };
      this.threadpoolQueueInfo = optionInfo;
    },
    getThreadPoolReject(search, indexing, bulk) {
      const optionInfo = {
        xAxis: search.map(e => e.x),
        seriesData: [
          { name: 'reject-search', data: search.map(e => e.y) },
          { name: 'reject-indexing', data: indexing.map(e => e.y) },
          { name: 'reject-bulk', data: bulk.map(e => e.y) },
        ],
        yAxisName: '',
      };
      this.threadpoolRejectInfo = optionInfo;
    },
    getSegmentCount(segmentCount) {
      const optionInfo = {
        xAxis: segmentCount.map(e => e.x),
        seriesData: [
          { name: 'segment count', data: segmentCount.map(e => e.y) },
        ],
        yAxisName: '',
      };
      this.segmentCountInfo = optionInfo;
    },
    getHttpOpenCount(httpOpenCurrent) {
      const optionInfo = {
        xAxis: httpOpenCurrent.map(e => e.x),
        seriesData: [
          { name: 'http open current', data: httpOpenCurrent.map(e => e.y) },
        ],
        yAxisName: '',
      };
      this.httpOpenCurrentInfo = optionInfo;
    },
    getNodeMonitor() {
      const params = {
        clusterName: this.clusterId,
        nodeName: this.node,
        ...this.timeInterval,
      };
      this.$http.post('/monitor/node.json', params).then((data) => {
        if (data) {
          this.gaugeMetricData = [data.gaugeMetric];
          this.getgcCount(data.gcCountOld.metricModel, data.gcCountYoung.metricModel);
          this.getgcDuration(data.gc_duration_old_ms.metricModel,
           data.gc_duration_young_ms.metricModel);
          this.getJVMHeap(data.jvm_heap_max_byte.metricModel, data.jvm_heap_used_byte.metricModel);
          this.getCpuPercent(data.cpuNodePercent.metricModel, data.cpuProcessPerent.metricModel);
          this.getIndexMemory(data.index_memory_lucenc_total_byte.metricModel,
           data.index_memory_terms_bytes.metricModel);
          this.getTheadPoolQueue(data.searchThreadpoolQueue.metricModel,
           data.indexThreadpoolQueue.metricModel, data.bulkThreadpoolQueue.metricModel);
          this.getThreadPoolReject(data.searchThreadpoolReject.metricModel,
           data.indexThreadpoolReject.metricModel, data.bulkThreadpoolReject.metricModel);
          this.getSegmentCount(data.segmentCount.metricModel);
          this.getHttpOpenCount(data.httpOpenCurrent.metricModel);
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
