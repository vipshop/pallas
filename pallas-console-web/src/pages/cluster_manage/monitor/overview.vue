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
                  <span class="el-dropdown-link" :title="timeInterval.command === 'custom' ? `${formatDate(timeInterval.from, 'MM-DD HH:mm')} - ${formatDate(timeInterval.to, 'MM-DD HH:mm')}` : ''">
                    <i class="fa fa-clock-o"></i>
                    {{periodTimeMap[timeInterval.command]}}<i class="el-icon-caret-bottom el-icon--right"></i>
                  </span>
                  <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item command="30">最近30分钟</el-dropdown-item>
                    <el-dropdown-item command="60">最近1小时</el-dropdown-item>
                    <el-dropdown-item command="180">最近3小时</el-dropdown-item>
                    <el-dropdown-item command="360">最近6小时</el-dropdown-item>
                    <el-dropdown-item command="720">最近12小时</el-dropdown-item>
                    <el-dropdown-item command="1440">最近24小时</el-dropdown-item>
                    <el-dropdown-item command="4320">最近3天</el-dropdown-item>
                    <el-dropdown-item command="10080">最近7天</el-dropdown-item>
                    <el-dropdown-item divided command="custom">自定义时间</el-dropdown-item>
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
        <div v-if="customTimeVisible">
          <custom-time-dialog @set-custom-time="setCustomTime" @close-dialog="closeCustomTimeDialog"></custom-time-dialog>
        </div>
    </div>
</template>

<script>
import Moment from 'moment';
import {
  SET_MONITOR_TIME_INTERVAL,
} from '../../../store/types';
import CustomTimeDialog from './custom_time_dialog';

export default {
  components: {
    'custom-time-dialog': CustomTimeDialog,
  },
  data() {
    return {
      loading: false,
      activeTab: 'cluster_monitor',
      nodesNum: 0,
      indicesNum: 0,
      periodTimeMap: {
        30: '最近30分钟',
        60: '最近1小时',
        180: '最近3小时',
        360: '最近6小时',
        720: '最近12小时',
        1440: '最近24小时',
        4320: '最近3天',
        10080: '最近7天',
        custom: '自定义时间',
      },
      customTimeVisible: false,
    };
  },
  methods: {
    formatDate(time, format) {
      const date = new Date(time);
      const formatTime = Moment(date).format(format);
      return formatTime;
    },
    handleCommand(command) {
      if (command !== 'custom') {
        const params = {
          command,
          from: new Date().getTime() - (Number(command) * 60 * 1000),
          to: new Date().getTime(),
        };
        this.$store.dispatch(SET_MONITOR_TIME_INTERVAL, params);
      } else {
        this.customTimeVisible = true;
      }
    },
    setCustomTime(params) {
      this.$store.dispatch(SET_MONITOR_TIME_INTERVAL, params);
      this.closeCustomTimeDialog();
    },
    closeCustomTimeDialog() {
      this.customTimeVisible = false;
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
