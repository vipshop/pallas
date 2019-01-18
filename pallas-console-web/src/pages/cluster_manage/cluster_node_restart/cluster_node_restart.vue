<template>
    <div class="page-content">
        <div class="my-breadcrumb">
            <el-breadcrumb separator="/" class="my-breadcrumb-content">
                <el-breadcrumb-item :to="{ name:'cluster_manage' }"><i class="fa fa-home"></i>ES集群管理</el-breadcrumb-item>
                <el-breadcrumb-item>ES集群重启</el-breadcrumb-item>
                <el-breadcrumb-item>{{clusterId}}</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="data-table-filter">
            <div class="pull-left">
                <el-button @click="resetClusterSettings"><i class="fa fa-repeat"></i>恢复集群默认设置</el-button>
            </div>
            <div class="pull-right">
                <el-form :inline="true" class="demo-form-inline">
                    <el-form-item class="filter-search">
                        <el-button @click="init"><i class="fa fa-refresh"></i>刷新集群信息</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
        <div class="cluster_restart_setting">
            <el-row :gutter="6">
                <el-col :span="6" v-for="(item, index) in clusterSettingsArray" :key="index">
                    <Panel :type="clusterStateTag[clusterHealth]">
                        <div slot="title">{{item.key}}</div>
                        <div slot="content">
                            <el-popover trigger="hover" placement="right" v-if="item.key === 'index.blocks.write'">
                                <div style="width: 600px;" v-if="item.value !== ''">
                                    <el-row :gutter="10">
                                        <el-col v-for="(item1, index) in $array.strToArray(item.value)" :key="index" :span="8">{{item1}}</el-col>
                                    </el-row>
                                </div>
                                <div v-else>暂无数据</div>
                                <el-button type="text" slot="reference">查看</el-button>
                            </el-popover>
                            <div v-else>{{item.value || '-'}}</div>
                        </div>
                    </Panel>
                </el-col>
            </el-row>
        </div>
        <el-table :data="nodeRestartList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
            <el-table-column prop="nodeName" label="节点名称"></el-table-column>
            <el-table-column prop="nodeIp" label="节点IP"></el-table-column>
            <el-table-column prop="indices" label="分片分布" width="80px">
                <template scope="scope">
                    <el-popover trigger="hover" placement="right" :disabled="scope.row.indices === ''">
                        <div style="width: 600px;" v-if="scope.row.indices">
                            <el-row :gutter="10">
                                <el-col v-for="(item, index) in $array.strToArray(scope.row.indices)" :key="index" :span="8">{{item}}</el-col>
                            </el-row>
                        </div>
                        <div v-else>暂无分片</div>
                        <el-button type="text" slot="reference" size="small"><i class="fa fa-file-o"></i></el-button>
                    </el-popover>
                </template>
            </el-table-column>
            <el-table-column prop="nodeState" label="节点状态" width="100px">
                <template scope="scope">
                    <el-tag :type="noteStateTag[scope.row.nodeState]">{{noteStateMap[scope.row.nodeState]}}</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="nodeTime" label="状态时间">
                <template scope="scope">{{scope.row.nodeTime | formatDate}}</template>
            </el-table-column>
            <el-table-column prop="lastStartupTime" label="最近启动时间">
                <template scope="scope"><span class="text-danger" v-if="scope.row.isNew">新 </span>{{scope.row.lastStartupTime | formatDate}}</template>
            </el-table-column>
            <el-table-column prop="onlyMaster" label="OnlyMaster">
                <template scope="scope">
                    <el-tag :type="scope.row.onlyMaster ? 'success' : 'danger'" close-transition>{{onlyMasterMap[scope.row.onlyMaster]}}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="80px">
                <template scope="scope">
                    <el-button size="small" type="success" @click="handleRestart(scope.row)" :disabled="!scope.row.healthy || isEnableRestart !== 'could_be_restart'">重启</el-button>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>
<script>
import Moment from 'moment';

export default {
  data() {
    return {
      loading: false,
      clusterId: this.$route.query.clusterId,
      nodeRestartList: [],
      onlyMasterMap: {
        true: '是',
        false: '否',
      },
      noteStateMap: {
        1: '即将重启',
        2: '正在重启',
        3: '已启动',
        4: '正常',
      },
      noteStateTag: {
        1: 'info',
        2: 'warning',
        3: 'success',
        4: 'success',
        5: 'danger',
        6: 'warning',
      },
      clusterStateTag: {
        green: 'success',
        red: 'danger',
        yellow: 'warning',
      },
      clusterHealth: '',
      clusterSettings: [],
      isEnableRestart: '',
      interval: 0,
    };
  },
  methods: {
    resetClusterSettings() {
      this.$message.confirmMessage('确定恢复集群默认设置吗?', () => {
        this.loading = true;
        this.$http.get(`/cluster/settings/default/reset.json?clusterName=${this.clusterId}`).then(() => {
          this.$message.successMessage('恢复集群默认设置操作成功', () => {
            this.getCluterSetting();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    handleRestart(row) {
      const params = {
        clusterName: this.clusterId,
        nodeIp: row.nodeIp,
      };
      this.$message.confirmMessage(`确定要重启节点${row.nodeName}吗? 注意，提交重启后，请密切关注集群及节点状态！`, () => {
        this.loading = true;
        this.$http.post('/cluster/node/restart.json', params).then(() => {
          this.$message.successMessage('提交重启请求成功', () => {
            this.init();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    getClusterState() {
      this.$http.get(`/cluster/state.json?clusterName=${this.clusterId}`).then((data) => {
        this.clusterHealth = data.status;
        this.isEnableRestart = data.cause;
        this.clusterSettings = data.settings;
        this.nodeRestartList = data.nodes;
        const now = Moment().format('x');
        this.nodeRestartList.forEach((ele) => {
          const diff = (now - ele.lastStartupTime) / 1000 / 60 / 60;
          if (diff < 1) {
            this.$set(ele, 'isNew', true);
          } else {
            this.$set(ele, 'isNew', false);
          }
        });
      });
    },
    init() {
      this.getClusterState();
    },
    refreshClusterRestart() {
      this.interval = setInterval(() => {
        this.getClusterState();
      }, 3000);
    },
  },
  destroyed() {
    clearInterval(this.interval);
  },
  computed: {
    clusterSettingsArray() {
      const arr = [];
      Object.entries(this.clusterSettings).forEach((ele) => {
        const params = {
          key: ele[0],
          value: ele[1],
        };
        arr.push(params);
      });
      return arr;
    },
  },
  created() {
    this.init();
    this.refreshClusterRestart();
  },
};
</script>
<style scoped>
.cluster_restart_tab {
    margin-bottom: 10px;
}
.cluster_restart_setting {
    margin-bottom: 10px;
}
.cluster_restart_setting .el-button--text:focus, .cluster_restart_setting .el-button--text:hover {
    color: #fff;
}
.cluster_restart_setting .el-table .cell, .el-table th>div {
    padding-top: 0;
    padding-bottom: 0;
}
</style>
