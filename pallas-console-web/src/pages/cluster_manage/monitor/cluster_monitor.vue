<template>
    <div class="my-tab-content" v-loading="loading" element-loading-text="请稍等···">
        <div class="monitor-top">
            <el-table :data="gaugeMetricData" border style="width: 100%">
                <el-table-column prop="nodeCount" label="Nodes"></el-table-column>
                <el-table-column prop="indexCount" label="Indices"></el-table-column>
                <el-table-column label="Memory">
                  <template scope="scope">
                      {{scope.row.used_memory_byte}} / {{scope.row.total_memory_byte}}
                  </template>
                </el-table-column>
                <el-table-column prop="totalShardCount" label="Total Shards"></el-table-column>
                <el-table-column prop="unassignedShardCount" label="Unassigned Shards"></el-table-column>
                <el-table-column prop="documentCount" label="Documents"></el-table-column>
                <el-table-column label="Data">
                    <template scope="scope">
                        {{bytesToSize(scope.row.document_store_byte)}}
                    </template>
                </el-table-column>
                <el-table-column prop="version" label="Version"></el-table-column>
                <el-table-column prop="health" label="Health"></el-table-column>
            </el-table>
        </div>
        <div>
            <el-row :gutter="10">
                <el-col :span="12">
                    <chart-container title="Indexing Rate(/s)" type="line">
                        <div slot="chart">
                            <MyLine id="indexingRate" :option-info="indexingRateInfo"></MyLine>
                        </div>
                    </chart-container>
                </el-col>
                <el-col :span="12">
                    <chart-container title="Search Rate(/s)" type="line">
                        <div slot="chart">
                            <MyLine id="searchRate" :option-info="searchRateInfo"></MyLine>
                        </div>
                    </chart-container>
                </el-col>
                <el-col :span="12">
                    <chart-container title="Indexing Latentcy" type="line">
                        <div slot="chart">
                            <MyLine id="indexingLatency" :option-info="indexingLatencyInfo"></MyLine>
                        </div>
                    </chart-container>
                </el-col>
                <el-col :span="12">
                    <chart-container title="search Latentcy" type="line">
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
    getIndexingRate(indexingRateResp) {
      const optionInfo = {
        xAxis: indexingRateResp.map(e => e.x),
        seriesData: [
          { name: 'Indexing Rate', data: indexingRateResp.map(e => e.y) },
        ],
        yAxisName: 's',
      };
      this.indexingRateInfo = optionInfo;
    },
    getSearchRate(searchRateResp) {
      const optionInfo = {
        xAxis: searchRateResp.map(e => e.x),
        seriesData: [
          { name: 'Search Rate', data: searchRateResp.map(e => e.y) },
        ],
        yAxisName: 's',
      };
      this.searchRateInfo = optionInfo;
    },
    getSearchLatency(searchLatencyResp) {
      const optionInfo = {
        xAxis: searchLatencyResp.map(e => e.x),
        seriesData: [
          { name: 'Search Latency', data: searchLatencyResp.map(e => e.y) },
        ],
        yAxisName: 's',
      };
      this.searchLatencyInfo = optionInfo;
    },
    getIndexingLatency(indexingLatencyResp) {
      const optionInfo = {
        xAxis: indexingLatencyResp.map(e => e.x),
        seriesData: [
          { name: 'Indexing Latency', data: indexingLatencyResp.map(e => e.y) },
        ],
        yAxisName: 's',
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
          this.getIndexingRate(data.indexingRate.metricModel);
          this.getSearchRate(data.searchRate.metricModel);
          this.getIndexingLatency(data.indexingLatency.metricModel);
          this.getSearchLatency(data.searchLatency.metricModel);
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
