<template>
    <div class="my-tab-content" v-loading="loading" element-loading-text="请稍等···">
      <div class="monitor-top">
            <el-table :data="gaugeMetricData" border style="width: 100%">
                <el-table-column prop="transportAddress" label="transportAddress"></el-table-column>
                <el-table-column prop="jvmHeapUsage" label="JVM Heap(%)"></el-table-column>
                <el-table-column label="Free Disk">
                  <template scope="scope">
                    {{bytesToSize(scope.row.availableFS)}}
                  </template>
                </el-table-column>
                <el-table-column prop="documentCount" label="Documents"></el-table-column>
                <el-table-column label="Data">
                  <template scope="scope">
                    {{bytesToSize(scope.row.documentStore)}}
                  </template>
                </el-table-column>
                <el-table-column prop="indexCount" label="Indices"></el-table-column>
                <el-table-column prop="shardCount" label="Shards"></el-table-column>
                <el-table-column prop="type" label="Type"></el-table-column>
                <el-table-column prop="uptime" label="Uptime"></el-table-column>
            </el-table>
      </div>  
      <div>
          <el-row :gutter="10">
              <el-col :span="12">
                  <chart-container :title="`GC Count(${gcCountInfo.yAxisName})`" type="line">
                      <div slot="chart">
                          <MyLine id="gcCount" :option-info="gcCountInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container :title="`GC Duration(${gcDurationInfo.yAxisName})`" type="line">
                      <div slot="chart">
                          <MyLine id="gcDuration" :option-info="gcDurationInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container :title="`JVM Heap(${jvmHeapInfo.yAxisName})`" type="line">
                      <div slot="chart">
                          <MyLine id="jvmHeap" :option-info="jvmHeapInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container :title="`Cpu Percent(${cpuPercentInfo.yAxisName})`" type="line">
                      <div slot="chart">
                          <MyLine id="cpuPercent" :option-info="cpuPercentInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container :title="`Threadpool Threads(${threadpoolThreadsInfo.yAxisName})`" type="line">
                      <div slot="chart">
                          <MyLine id="threadpoolThreads" :option-info="threadpoolThreadsInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container :title="`Threadpool Queue(${threadpoolQueueInfo.yAxisName})`" type="line">
                      <div slot="chart">
                          <MyLine id="threadpoolQueue" :option-info="threadpoolQueueInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container :title="`Threadpool Reject(${threadpoolRejectInfo.yAxisName})`" type="line">
                      <div slot="chart">
                          <MyLine id="threadpoolReject" :option-info="threadpoolRejectInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container :title="`Request Rate(${indexSearchRateInfo.yAxisName})`" type="line">
                      <div slot="chart">
                          <MyLine id="indexSearchRate" :option-info="indexSearchRateInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container :title="`Request Latency(${indexSearchLatencyInfo.yAxisName})`" type="line">
                       <div slot="chart">
                           <MyLine id="indexSearchLatency" :option-info="indexSearchLatencyInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container :title="`Segment Count(${segmentCountInfo.yAxisName})`" type="line">
                      <div slot="chart">
                          <MyLine id="segmentCount" :option-info="segmentCountInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <!-- <el-col :span="12">
                  <chart-container :title="`Http Open Current(${httpOpenCurrentInfo.yAxisName})`" type="line">
                      <div slot="chart">
                          <MyLine id="httpOpenCurrent" :option-info="httpOpenCurrentInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col> -->
               <el-col :span="12">
                  <chart-container :title="`Index Memory(${indexMemoryInfo.yAxisName})`" type="line">
                      <div slot="chart">
                          <MyLine id="indexMemory" :option-info="indexMemoryInfo"></MyLine>
                      </div>
                  </chart-container>
              </el-col>
              <el-col :span="12">
                  <chart-container :title="`System load(${systemLoadInfo.yAxisName})`" type="line">
                      <div slot="chart">
                          <MyLine id="systemLoad" :option-info="systemLoadInfo"></MyLine>
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
      threadpoolThreadsInfo: {},
      httpOpenCurrentInfo: {},
      indexSearchRateInfo: {},
      indexSearchLatencyInfo: {},
      systemLoadInfo: {},
    };
  },
  methods: {
    getgcCount(gcCountOld, gcCountYoung, unit) {
      const optionInfo = {
        xAxis: gcCountOld.map(e => e.x),
        seriesData: [
          { name: 'gc Count Old', data: gcCountOld.map(e => e.y) },
          { name: 'gc Count Young', data: gcCountYoung.map(e => e.y) },
        ],
        yAxisName: unit || '个',
      };
      this.gcCountInfo = optionInfo;
    },
    getgcDuration(gcDurationOld, gcDurationYoung, unit) {
      const optionInfo = {
        xAxis: gcDurationOld.map(e => e.x),
        seriesData: [
          { name: 'gc Duration Old', data: gcDurationOld.map(e => e.y) },
          { name: 'gc Duration Young', data: gcDurationYoung.map(e => e.y) },
        ],
        yAxisName: unit || '个',
      };
      this.gcDurationInfo = optionInfo;
    },
    getJVMHeap(jvmHeapMax, jvmHeapUsed, unit) {
      const optionInfo = {
        xAxis: jvmHeapMax.map(e => e.x),
        seriesData: [
          { name: 'jvm heap max', data: jvmHeapMax.map(e => e.y.toFixed(2)) },
          { name: 'jvm heap used', data: jvmHeapUsed.map(e => e.y.toFixed(2)) },
        ],
        yAxisName: unit || '个',
      };
      this.jvmHeapInfo = optionInfo;
    },
    getCpuPercent(cpuNodePercent, cpuProcessPerent, unit) {
      const optionInfo = {
        xAxis: cpuNodePercent.map(e => e.x),
        seriesData: [
          { name: 'cpu node percent', data: cpuNodePercent.map(e => e.y.toFixed(2)) },
          { name: 'cpu process percent', data: cpuProcessPerent.map(e => e.y.toFixed(2)) },
        ],
        yAxisName: unit || '个',
      };
      this.cpuPercentInfo = optionInfo;
    },
    getIndexMemory(indexMemoryLucencTotal, indexMemoryTerms, unit) {
      const optionInfo = {
        xAxis: indexMemoryLucencTotal.map(e => e.x),
        seriesData: [
          { name: 'index memory Lucenc total', data: indexMemoryLucencTotal.map(e => e.y.toFixed(2)) },
          { name: 'index memory terms', data: indexMemoryTerms.map(e => e.y.toFixed(2)) },
        ],
        yAxisName: unit || '个',
      };
      this.indexMemoryInfo = optionInfo;
    },
    getTheadPoolQueue(search, indexing, bulk, unit) {
      const optionInfo = {
        xAxis: search.map(e => e.x),
        seriesData: [
          { name: 'queue-search', data: search.map(e => e.y) },
          { name: 'queue-indexing', data: indexing.map(e => e.y) },
          { name: 'queue-bulk', data: bulk.map(e => e.y) },
        ],
        yAxisName: unit || '个',
      };
      this.threadpoolQueueInfo = optionInfo;
    },
    getThreadPoolReject(search, indexing, bulk, unit) {
      const optionInfo = {
        xAxis: search.map(e => e.x),
        seriesData: [
          { name: 'reject-search', data: search.map(e => e.y) },
          { name: 'reject-indexing', data: indexing.map(e => e.y) },
          { name: 'reject-bulk', data: bulk.map(e => e.y) },
        ],
        yAxisName: unit || '个',
      };
      this.threadpoolRejectInfo = optionInfo;
    },
    getThreadPoolThreads(search, indexing, bulk, unit) {
      const optionInfo = {
        xAxis: search.map(e => e.x),
        seriesData: [
          { name: 'threads-search', data: search.map(e => e.y) },
          { name: 'threads-indexing', data: indexing.map(e => e.y) },
          { name: 'threads-bulk', data: bulk.map(e => e.y) },
        ],
        yAxisName: unit || '个',
      };
      this.threadpoolThreadsInfo = optionInfo;
    },
    getSegmentCount(segmentCount, unit) {
      const optionInfo = {
        xAxis: segmentCount.map(e => e.x),
        seriesData: [
          { name: 'segment count', data: segmentCount.map(e => e.y) },
        ],
        yAxisName: unit || '个',
      };
      this.segmentCountInfo = optionInfo;
    },
    getHttpOpenCount(httpOpenCurrent, unit) {
      const optionInfo = {
        xAxis: httpOpenCurrent.map(e => e.x),
        seriesData: [
          { name: 'http open current', data: httpOpenCurrent.map(e => e.y) },
        ],
        yAxisName: unit || '个',
      };
      this.httpOpenCurrentInfo = optionInfo;
    },
    getIndexSerachRate(indexingRate, searchRate, unit) {
      const optionInfo = {
        xAxis: indexingRate.map(e => e.x),
        seriesData: [
          { name: 'indexing', data: indexingRate.map(e => e.y.toFixed(2)) },
          { name: 'search', data: searchRate.map(e => e.y.toFixed(2)) },
        ],
        yAxisName: unit || '个',
      };
      this.indexSearchRateInfo = optionInfo;
    },
    getIndexSearchLatency(indexingLatency, searchLatency, unit) {
      const optionInfo = {
        xAxis: indexingLatency.map(e => e.x),
        seriesData: [
          { name: 'indexing', data: indexingLatency.map(e => e.y.toFixed(2)) },
          { name: 'search', data: searchLatency.map(e => e.y.toFixed(2)) },
        ],
        yAxisName: unit || '个',
      };
      this.indexSearchLatencyInfo = optionInfo;
    },
    getSystemLoad(systemLoad1m, unit) {
      const optionInfo = {
        xAxis: systemLoad1m.map(e => e.x),
        seriesData: [
          { name: 'system load 1m', data: systemLoad1m.map(e => e.y.toFixed(2)) },
        ],
        yAxisName: unit,
      };
      this.systemLoadInfo = optionInfo;
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
          this.getgcCount(data.gcCountOld.metricModel,
            data.gcCountYoung.metricModel,
            data.gcCountOld.unit);
          this.getgcDuration(data.gc_duration_old_ms.metricModel,
           data.gc_duration_young_ms.metricModel,
           data.gc_duration_old_ms.unit);
          this.getJVMHeap(data.jvm_heap_max_byte.metricModel,
            data.jvm_heap_used_byte.metricModel,
            data.jvm_heap_max_byte.unit);
          this.getCpuPercent(data.cpuNodePercent.metricModel,
            data.cpuProcessPerent.metricModel,
            data.cpuNodePercent.unit);
          this.getIndexMemory(data.index_memory_lucenc_total_byte.metricModel,
           data.index_memory_terms_bytes.metricModel,
           data.index_memory_lucenc_total_byte.unit);
          this.getTheadPoolQueue(data.searchThreadpoolQueue.metricModel,
           data.indexThreadpoolQueue.metricModel,
           data.bulkThreadpoolQueue.metricModel,
           data.searchThreadpoolQueue.unit);
          this.getThreadPoolThreads(data.searchThreadpoolThreads.metricModel,
           data.indexThreadpoolThreads.metricModel,
           data.bulkThreadpoolThreads.metricModel,
           data.searchThreadpoolThreads.unit);
          this.getThreadPoolReject(data.searchThreadpoolReject.metricModel,
           data.indexThreadpoolReject.metricModel,
           data.bulkThreadpoolReject.metricModel,
           data.searchThreadpoolReject.unit);
          this.getSegmentCount(data.segmentCount.metricModel, data.segmentCount.unit);
          this.getHttpOpenCount(data.httpOpenCurrent.metricModel, data.httpOpenCurrent.unit);
          this.getIndexSerachRate(data.indexingRate.metricModel,
            data.searchRate.metricModel, data.searchRate.unit);
          this.getIndexSearchLatency(data.indexingLatency.metricModel,
            data.searchLatency.metricModel, data.searchLatency.unit);
          this.getSystemLoad(data.systemLoad_1m.metricModel, data.systemLoad_1m.unit);
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
