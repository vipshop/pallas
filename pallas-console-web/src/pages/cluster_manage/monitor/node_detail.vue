<template>
    <div class="my-tab-content">
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
    this.getIndexMemory();
  },
};
</script>
