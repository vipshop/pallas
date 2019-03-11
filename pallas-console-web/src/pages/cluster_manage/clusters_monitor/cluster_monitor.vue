<template>
    <div class="my-tab-content">
        <div>
            <el-row :gutter="10">
                <el-col :xs="24" :sm="24" :md="24" :lg="24" class="chart-auto-size">
                    <chart-container title="Search Rate(/s)" type="line">
                        <div slot="chart">
                            <MyLine id="searchRate" :option-info="searchRateInfo"></MyLine>
                        </div>
                    </chart-container>
                </el-col>
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
      searchRateInfo: {},
      indexingRateInfo: {},
    };
  },
  methods: {
    getSearchRate() {
      const optionInfo = {
        xAxis: ['09:00', '09:10', '09:20', '09:30', '09:40', '09:50', '10:00', '10:10', '10:20'],
        seriesData: [{ name: 'Search Rate', data: [2, 3, 5, 1, 3, 4, 7, 6, 4] }],
        yAxisName: 's',
      };
      this.searchRateInfo = optionInfo;
    },
    getIndexingRate() {
      const optionInfo = {
        xAxis: ['09:00', '09:10', '09:20', '09:30', '09:40', '09:50', '10:00', '10:10', '10:20'],
        seriesData: [
          { name: 'Total Shards', data: [2, 3, 5, 1, 3, 4, 7, 6, 4] },
          { name: 'Primary Shards', data: [1, 4, 2, 4, 2, 3, 5, 2, 7] }],
        yAxisName: 's',
      };
      this.indexingRateInfo = optionInfo;
    },
  },
  computed: {
    clusterId() {
      return this.$route.query.clusterId;
    },
  },
  created() {
    this.getSearchRate();
    this.getIndexingRate();
  },
  watch: {
  },
};
</script>
