<template>
    <div class="my-tab-content" v-loading="loading" element-loading-text="请稍等···">
        <div class="monitor-top">
            <el-table :data="gaugeMetricData" border style="width: 100%">
                <el-table-column prop="nodeCount" label="Nodes"></el-table-column>
                <el-table-column prop="indexCount" label="Indices"></el-table-column>
                <el-table-column label="Memory">
                  <template scope="scope">
                      {{bytesToSize(scope.row.used_memory_byte)}} / {{bytesToSize(scope.row.total_memory_byte)}}
                  </template>
                </el-table-column>
                <el-table-column prop="totalShardCount" label="Total Shards"></el-table-column>
                <el-table-column prop="unassignedShardCount" label="Unassigned Shards" width="150px"></el-table-column>
                <el-table-column prop="documentCount" label="Documents"></el-table-column>
                <el-table-column label="Data">
                    <template scope="scope">
                        {{bytesToSize(scope.row.document_store_byte)}}
                    </template>
                </el-table-column>
                <el-table-column prop="max_uptime" label="Uptime"></el-table-column>
                <el-table-column prop="version" label="Version"></el-table-column>
                <el-table-column prop="health" label="Health"></el-table-column>
            </el-table>
        </div>
        <div>
            <el-row :gutter="10">
                <el-col :span="12">
                    <chart-container :title="`Indexing Rate(${indexingRateInfo.yAxisName})`" type="line">
                        <div slot="chart">
                            <MyLine id="indexingRate" :option-info="indexingRateInfo"></MyLine>
                        </div>
                    </chart-container>
                </el-col>
                <el-col :span="12">
                    <chart-container :title="`Search Rate(${searchRateInfo.yAxisName})`" type="line">
                        <div slot="chart">
                            <MyLine id="searchRate" :option-info="searchRateInfo"></MyLine>
                        </div>
                    </chart-container>
                </el-col>
                <el-col :span="12">
                    <chart-container :title="`Indexing Latentcy(${indexingLatencyInfo.yAxisName})`" type="line">
                        <div slot="chart">
                            <MyLine id="indexingLatency" :option-info="indexingLatencyInfo"></MyLine>
                        </div>
                    </chart-container>
                </el-col>
                <el-col :span="12">
                    <chart-container :title="`search Latentcy(${searchLatencyInfo.yAxisName})`" type="line">
                        <div slot="chart">
                            <MyLine id="searchLatency" :option-info="searchLatencyInfo"></MyLine>
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
      indexingRateInfo: {},
      searchRateInfo: {},
      searchLatencyInfo: {},
      indexingLatencyInfo: {},

    };
  },
  methods: {
    getIndexingRate(indexingRateResp, unit) {
      const optionInfo = {
        xAxis: indexingRateResp.map(e => e.x),
        seriesData: [
          { name: 'Indexing Rate', data: indexingRateResp.map(e => e.y.toFixed(2)) },
        ],
        yAxisName: unit || '个',
      };
      this.indexingRateInfo = optionInfo;
    },
    getSearchRate(searchRateResp, unit) {
      const optionInfo = {
        xAxis: searchRateResp.map(e => e.x),
        seriesData: [
          { name: 'Search Rate', data: searchRateResp.map(e => e.y.toFixed(2)) },
        ],
        yAxisName: unit || '个',
      };
      this.searchRateInfo = optionInfo;
    },
    getSearchLatency(searchLatencyResp, unit) {
      const optionInfo = {
        xAxis: searchLatencyResp.map(e => e.x),
        seriesData: [
          { name: 'Search Latency', data: searchLatencyResp.map(e => e.y.toFixed(2)) },
        ],
        yAxisName: unit || '个',
      };
      this.searchLatencyInfo = optionInfo;
    },
    getIndexingLatency(indexingLatencyResp, unit) {
      const optionInfo = {
        xAxis: indexingLatencyResp.map(e => e.x),
        seriesData: [
          { name: 'Indexing Latency', data: indexingLatencyResp.map(e => e.y.toFixed(2)) },
        ],
        yAxisName: unit || '个',
      };
      this.indexingLatencyInfo = optionInfo;
    },
    getClusterMonitor() {
      const params = {
        ...this.timeInterval,
        clusterName: this.clusterId,
      };
      this.$http.post('/monitor/cluster.json', params).then((data) => {
        if (data) {
          this.gaugeMetricData = [data.gaugeMetric];
          this.getIndexingRate(data.indexingRate.metricModel, data.indexingRate.unit);
          this.getSearchRate(data.searchRate.metricModel, data.searchRate.unit);
          this.getIndexingLatency(data.indexingLatency.metricModel, data.indexingLatency.unit);
          this.getSearchLatency(data.searchLatency.metricModel, data.searchLatency.unit);
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
  },
  created() {
    this.getClusterMonitor();
  },
  watch: {
    '$store.state.monitorTimeInterval': function interval(val) {
      console.log(val);
      this.getClusterMonitor();
    },
  },
};
</script>
<style>
.monitor-top {
  margin-bottom: 10px;
}
.monitor-top .el-table .cell, .monitor-top .el-table th>div{
  padding-top: 0px;
  padding-bottom: 0px;
}
.monitor-top .el-table td, .monitor-top .el-table th.is-leaf {
    border-bottom: none;
}
</style>
