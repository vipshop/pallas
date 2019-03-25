<template>
    <div class="my-tab-content" v-loading="loading" element-loading-text="请稍等···">
        <div>
            <el-row :gutter="10">
                <el-col :xs="24" :sm="24" :md="24" :lg="24" class="chart-auto-size">
                    <chart-container title="Indexing Rate(/s)" type="line">
                        <div slot="chart">
                            <MyLine id="indexingRate" :option-info="indexingRateInfo"></MyLine>
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
      searchRateInfo: {},
      indexingRateInfo: {},
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
    getClusterMonitor(interval) {
      const params = {
        clusterName: this.clusterId,
        from: new Date().getTime() - (Number(interval) * 60 * 1000),
        to: new Date().getTime(),
      };
      this.loading = true;
      this.$http.post('/monitor/cluster.json', params).then((data) => {
        if (data) {
          this.getIndexingRate(data.indexingRate);
        }
      })
      .finally(() => {
        this.loading = false;
      });
    },
  },
  computed: {
    timeInterval() {
      const interval = this.$store.state.monitorTimeInterval;
      this.getClusterMonitor(interval);
      return interval;
    },
    clusterId() {
      return this.$route.query.clusterId;
    },
  },
  watch: {
  },
};
</script>
