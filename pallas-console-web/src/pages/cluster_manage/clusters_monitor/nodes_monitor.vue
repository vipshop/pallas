<template>
    <div class="my-tab-content">
        <div class="content">
            <div class="data-table-filter">
                <div class="pull-left">
                    <el-form :inline="true" class="demo-form-inline">
                        <input type="text" v-show="false"/>
                        <el-form-item label="">
                            <el-input placeholder="请搜索节点" v-model="nodeForSearch" @keyup.enter.native="getNodesList"></el-input>
                        </el-form-item>
                        <el-form-item class="filter-search">
                            <el-button type="primary" icon="search" @click="getNodesList">查询</el-button>
                        </el-form-item>
                    </el-form>
                </div>
            </div>
        </div>
        <div class="content">
            <template>
                <el-table :data="nodesList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
                    <el-table-column label="Name" prop="name">
                        <template scope="scope">
                            <router-link tag="a" :to="{ path: 'node_monitor_detail', query: {clusterId, node: scope.row.name} }">{{scope.row.name}}</router-link>
                        </template>
                    </el-table-column>
                    <el-table-column label="Status" prop="status"></el-table-column>
                    <el-table-column label="CPU Usage" prop="cpu"></el-table-column>
                    <el-table-column label="Load Average" prop="load"></el-table-column>
                    <el-table-column label="JVM Memory" prop="memory"></el-table-column>
                    <el-table-column label="Disk Free Space" prop="disk"></el-table-column>
                    <el-table-column label="Shards" prop="shards"></el-table-column>
                </el-table>
            </template>
        </div>
    </div>
</template>
<script>
export default {
  data() {
    return {
      loading: false,
      nodesList: [],
      nodeForSearch: '',
    };
  },
  methods: {
    getNodesList() {
      const list = [{
        name: 'node1',
        status: 'Online',
        cpu: '592',
        load: '2.6MB',
        memory: '0/s',
        disk: '0/s',
        shards: '0',
      }, {
        name: 'node2',
        status: 'Online',
        cpu: '592',
        load: '2.6MB',
        memory: '0/s',
        disk: '0/s',
        shards: '0',
      }];
      this.nodesList = list;
    },
  },
  computed: {
    clusterId() {
      return this.$route.query.clusterId;
    },
  },
  created() {
    this.getNodesList();
  },
  watch: {
  },
};
</script>
