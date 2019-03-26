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
          <el-col :xs="24" :sm="24" :md="24" :lg="24" class="chart-auto-size">
            <chart-container title="Index Memory(B)" type="line">
              <div slot="chart">
               <MyLine id="indexMemory" :option-info="indexMemoryInfo"></MyLine>
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
    };
  },
  methods: {
    getIndexMemory() {
      const optionInfo = {
        xAxis: ['09:00', '09:10', '09:20', '09:30', '09:40', '09:50', '10:00', '10:10', '10:20'],
        seriesData: [{ name: 'Index Memory', data: [2, 3, 5, 1, 3, 4, 7, 6, 4] }],
        yAxisName: 'B',
      };
      this.indexMemoryInfo = optionInfo;
    },
    getIndexMonitor() {
      const params = {
        clusterName: this.clusterId,
        indexName: this.indice,
        from: new Date().getTime() - (Number(this.timeInterval) * 60 * 1000),
        to: new Date().getTime(),
      };
      this.$http.post('/monitor/index.json', params).then((data) => {
        if (data) {
          this.gaugeMetricData = [data.gaugeMetric];
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
