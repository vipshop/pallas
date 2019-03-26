<template>
    <div class="page-content" v-loading="loading" element-loading-text="请稍等···">
        <div class="my-breadcrumb" style="width: 100%">
            <div class="pull-left" style="display: inline;">
              <el-breadcrumb separator="/" class="my-breadcrumb-content">
                  <el-breadcrumb-item :to="{ name:'cluster_manage' }"><i class="fa fa-home"></i>ES集群管理</el-breadcrumb-item>
                  <el-breadcrumb-item :to="{ name:'cluster_detail', query: { clusterId } }">{{clusterId}}</el-breadcrumb-item>
                  <el-breadcrumb-item :to="{ name:'cluster_monitor', query: { clusterId } }">监控</el-breadcrumb-item>
                  <el-breadcrumb-item :to="item.route" v-for="(item, index) in breadcrumbs" :key="index">{{item.name}}</el-breadcrumb-item>
              </el-breadcrumb>
            </div>
            <div class="pull-right" style="display: inline;margin-right: 10px;height: 30px;line-height: 30px;">
                <el-dropdown trigger="click" @command="handleCommand">
                  <span class="el-dropdown-link">
                    <i class="fa fa-clock-o"></i>
                    {{periodTimeMap[timeInterval]}}<i class="el-icon-caret-bottom el-icon--right"></i>
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="15">最近15分钟</el-dropdown-item>
                    <el-dropdown-item command="30">最近30分钟</el-dropdown-item>
                    <el-dropdown-item command="60">最近1小时</el-dropdown-item>
                  </el-dropdown-menu>
              </el-dropdown>
            </div>
        </div>
        <div class="page-tab">
            <el-tabs v-model="activeTab" @tab-click="onTabClick">
                <el-tab-pane name="cluster_monitor">
                    <span slot="label"><i class="fa fa-cube"></i>集群</span>
                </el-tab-pane>
                <el-tab-pane name="indices_monitor">
                    <span slot="label"><i class="fa fa-search"></i>索引 ({{indicesNum}})</span>
                </el-tab-pane>
                <el-tab-pane name="nodes_monitor">
                    <span slot="label"><i class="fa fa-cubes"></i>节点 ({{nodesNum}})</span>
                </el-tab-pane>
            </el-tabs>
            <router-view></router-view>
        </div>
    </div>
</template>

<script>
import {
  SET_MONITOR_TIME_INTERVAL,
} from '../../../store/types';

export default {
  data() {
    return {
      loading: false,
      activeTab: 'cluster_monitor',
      nodesNum: 0,
      indicesNum: 0,
      periodTimeMap: {
        15: '最近15分钟',
        30: '最近30分钟',
        60: '最近1小时',
      },
    };
  },
  methods: {
    handleCommand(command) {
      this.$store.dispatch(SET_MONITOR_TIME_INTERVAL, command);
    },
    onTabClick() {
      this.$router.push({
        name: this.activeTab,
        query: {
          clusterId: this.clusterId,
        },
      });
    },
    getActiveTab() {
      const str = this.$route.name;
      if (str) {
        if (str === 'indice_monitor_detail') {
          this.activeTab = 'indices_monitor';
        } else if (str === 'node_monitor_detail') {
          this.activeTab = 'nodes_monitor';
        } else {
          this.activeTab = str;
        }
      }
    },
    getIndicesNum() {
      return this.$http.get('/monitor/indices/count.json', { clusterName: this.clusterId }).then((data) => {
        if (data) {
          this.indicesNum = data;
        }
      });
    },
    getNodesNum() {
      return this.$http.get('/monitor/nodes/count.json', { clusterName: this.clusterId }).then((data) => {
        if (data) {
          this.nodesNum = data;
        }
      });
    },
    init() {
      this.loading = true;
      Promise.all([this.getIndicesNum(), this.getNodesNum()]).then()
      .finally(() => {
        this.loading = false;
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
    node() {
      return this.$route.query.node;
    },
    breadcrumbs() {
      const result = [];
      if (this.$route.name === 'cluster_monitor') {
        result.push({ name: '集群' });
      } else if (this.$route.name === 'indices_monitor') {
        result.push({ name: '索引' });
      } else if (this.$route.name === 'indice_monitor_detail') {
        result.push({ name: '索引', route: { name: 'indices_monitor', query: { clusterId: this.clusterId } } });
        result.push({ name: this.indice });
      } else if (this.$route.name === 'nodes_monitor') {
        result.push({ name: '节点' });
      } else if (this.$route.name === 'node_monitor_detail') {
        result.push({ name: '节点', route: { name: 'nodes_monitor', query: { clusterId: this.clusterId } } });
        result.push({ name: this.node });
      }
      return result;
    },
  },
  created() {
    this.getActiveTab();
    this.init();
  },
  watch: {
    $route: 'getActiveTab',
  },
};
</script>
