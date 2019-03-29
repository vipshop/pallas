<template>
    <div class="my-tab-content">
        <div class="content">
            <div class="data-table-filter">
                <div class="pull-left">
                    <el-form :inline="true" class="demo-form-inline">
                        <input type="text" v-show="false"/>
                        <el-form-item label="">
                            <el-input placeholder="请搜索节点" v-model="nodeForSearch" @keyup.enter.native="nodesFilter"></el-input>
                        </el-form-item>
                        <el-form-item class="filter-search">
                            <el-button type="primary" icon="search" @click="nodesFilter">查询</el-button>
                        </el-form-item>
                    </el-form>
                </div>
            </div>
        </div>
        <div class="content">
            <template>
                <el-table :data="nodesList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
                    <el-table-column label="Name" prop="nodeName">
                        <template scope="scope">
                            <router-link tag="a" :to="{ path: 'node_monitor_detail', query: {clusterId, node: scope.row.nodeName} }">{{scope.row.nodeName}}</router-link>
                        </template>
                    </el-table-column>
                    <el-table-column label="CPU Usage" prop="osCpuPercent"></el-table-column>
                    <el-table-column label="Load Average" prop="load_1m"></el-table-column>
                    <el-table-column label="JVM Memory(%)" prop="jvmHeapUsage"></el-table-column>
                    <el-table-column label="Transport Address" prop="transportAddress"></el-table-column>
                    <el-table-column label="Shards" prop="shardCount"></el-table-column>
                </el-table>
            </template>
        </div>
    </div>
</template>
<script>
export default {
  props: [],
  data() {
    return {
      loading: false,
      nodeForSearch: '',
      nodesList: [],
      nodes: [],
    };
  },
  methods: {
    nodesFilter() {
      let filtered = this.nodes.slice();
      filtered = filtered.filter(e => e.nodeName.indexOf(this.nodeForSearch) > -1);
      this.nodesList = filtered;
    },
    getNodes() {
      const params = {
        clusterName: this.clusterId,
        ...this.timeInterval,
      };
      return this.$http.post('/monitor/nodes/info.json', params).then((data) => {
        if (data) {
          this.nodes = data;
          this.nodesNum = data.length;
          this.nodesFilter();
        }
      });
    },
    init() {
      this.loading = true;
      Promise.all([this.getNodes()]).then()
      .finally(() => {
        this.loading = false;
      });
    },
  },
  computed: {
    clusterId() {
      return this.$route.query.clusterId;
    },
    timeInterval() {
      return this.$store.state.monitorTimeInterval;
    },
  },
  created() {
    this.init();
  },
  watch: {
    '$store.state.monitorTimeInterval': function interval(val) {
      console.log(val);
      this.getNodes();
    },
  },
};
</script>
