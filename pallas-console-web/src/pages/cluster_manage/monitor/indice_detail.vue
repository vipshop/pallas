<template>
    <div class="my-tab-content" v-loading="loading" element-loading-text="请稍等···">
      <div class="monitor-top">
        <el-table :data="gaugeMetricData" border style="width: 100%">
          <el-table-column prop="document_store_byte_total" label="Total"></el-table-column>
          <el-table-column prop="document_store_byte_primary" label="Primaries"></el-table-column>
          <el-table-column prop="documentCount" label="Documents"></el-table-column>
          <el-table-column prop="totalShardCount" label="Total Shards"></el-table-column>
          <el-table-column prop="unassignedShardCount" label="Unassigned Shards"></el-table-column>            
          <el-table-column prop="health" label="Health"></el-table-column>
        </el-table>
      </div>
      <div>
        <el-row :gutter="10">
          <el-col :span="12">
            <chart-container title="Index Memory" type="line">
              <div slot="chart">
               <MyLine id="indexMemory" :option-info="indexMemoryInfo"></MyLine>
              </div>
            </chart-container>
          </el-col>
          <el-col :span="12">
            <chart-container title="Index Disk" type="line">
              <div slot="chart">
               <MyLine id="indexDisk" :option-info="indexDiskInfo"></MyLine>
              </div>
            </chart-container>
          </el-col>
          <el-col :span="12">
            <chart-container title="Segment Count" type="line">
              <div slot="chart">
               <MyLine id="segmentCount" :option-info="segmentCountInfo"></MyLine>
              </div>
            </chart-container>
          </el-col>
          <el-col :span="12">
            <chart-container title="Document Count" type="line">
              <div slot="chart">
               <MyLine id="documentCount" :option-info="documentCountInfo"></MyLine>
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
      indexMemoryInfo: {},
      indexDiskInfo: {},
      segmentCountInfo: {},
      documentCountInfo: {},
    };
  },
  methods: {
    getIndexMemory(lucencTotal, terms) {
      const optionInfo = {
        xAxis: lucencTotal.map(e => e.x),
        seriesData: [
          { name: 'index memory lucenc total', data: lucencTotal.map(e => e.y.toFixed(2)) },
          { name: 'index memory terms', data: terms.map(e => e.y.toFixed(2)) },
        ],
        yAxisName: 'mb',
      };
      this.indexMemoryInfo = optionInfo;
    },
    getIndexDisk(total, primary) {
      const optionInfo = {
        xAxis: total.map(e => e.x),
        seriesData: [
          { name: 'disk-total', data: total.map(e => e.y.toFixed(2)) },
          { name: 'disk-primary', data: primary.map(e => e.y.toFixed(2)) },
        ],
        yAxisName: 'gb',
      };
      this.indexDiskInfo = optionInfo;
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
    getDocumentCount(documentCount) {
      const optionInfo = {
        xAxis: documentCount.map(e => e.x),
        seriesData: [
          { name: 'document count', data: documentCount.map(e => e.y) },
        ],
        yAxisName: '',
      };
      this.documentCountInfo = optionInfo;
    },
    getIndexMonitor() {
      const params = {
        clusterName: this.clusterId,
        indexName: this.indice,
        ...this.timeInterval,
      };
      this.$http.post('/monitor/index.json', params).then((data) => {
        if (data) {
          this.gaugeMetricData = [data.gaugeMetric];
          this.getIndexMemory(data.index_memory_lucenc_total_in_byte.metricModel,
           data.index_memory_terms_in_byte.metricModel);
          this.getIndexDisk(data.index_disk_total.metricModel, data.index_disk_primary.metricModel);
          this.getSegmentCount(data.segmentCount.metricModel);
          this.getDocumentCount(data.documentCount.metricModel);
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
    indice() {
      return this.$route.query.indice;
    },
  },
  created() {
    this.getIndexMonitor();
  },
  watch: {
    '$store.state.monitorTimeInterval': function interval(val) {
      console.log(val);
      this.getIndexMemory();
    },
  },
};
</script>
