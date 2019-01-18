<template>
    <div class="page-content">
        <div class="my-breadcrumb">
            <el-breadcrumb separator="/" class="my-breadcrumb-content">
                <el-breadcrumb-item><i class="fa fa-home"></i>代理管理</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="data-table-filter">
            <div class="pull-left" v-if="isPrivilege">
                <el-button type="primary" @click="batchUpdateRoute">批量更新路由</el-button>
            </div>
            <div class="pull-right">
                <el-form :inline="true" class="demo-form-inline">
                    <el-form-item label="">
                        <el-select v-model="selectedCluster" @change="toPage">
                            <el-option label="全部集群" value=""></el-option>
                            <el-option v-for="item in clusters" :label="item" :value="item" :key="item"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item class="filter-search">
                        <el-button type="primary" icon="delete" @click="handleDeleteExpired">删除离线数据(3天前)</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
        <el-table ref="multipleTable" @selection-change="handleSelectionChange" :data="agentList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
            <el-table-column type="selection" width="55" :selectable="setSelectable"></el-table-column>
            <el-table-column label="ID" prop="id"></el-table-column>
            <el-table-column label="IP端口" prop="ipport"></el-table-column>
            <el-table-column label="代理集群" prop="cluster"></el-table-column>
            <el-table-column label="节点状态" width="80px">
                <template scope="scope"> 
                    <el-tag :type="scope.row.healthy ? 'success' : 'danger'" close-transition>{{scope.row.healthy | translateState}}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="当前QPS" width="80px">
                <template scope="scope">{{scope.row | currentQps}}</template>
            </el-table-column>
            <el-table-column label="连接数" width="60px">
                <template scope="scope">{{scope.row | currentConns}}</template>
            </el-table-column>
            <el-table-column label="Req.volume" width="100px">
                <template scope="scope">{{scope.row | currentReceiveClientThrougph}}</template>
            </el-table-column>
            <el-table-column label="Res.volume" width="100px">
                <template scope="scope">{{scope.row | currentReceiveEsThrougph}}</template>
            </el-table-column>
            <el-table-column label="启用" width="60px">
                <template scope="scope">
                    <el-tag :type="scope.row.takeTraffic ? 'success' : 'danger'" close-transition>{{scope.row.takeTraffic ? '是' : '否'}}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="最后上报时间" width="160px">
                <template scope="scope">{{scope.row.updateTime | formatDate}}</template>
            </el-table-column>
            <el-table-column label="操作" width="100px">
                <template scope="scope">
                    <el-tooltip content="节点信息" placement="top">
                        <el-button type="text" @click="getInfo(scope.row)"><i class="fa fa-envelope"></i></el-button>
                    </el-tooltip>
                    <el-tooltip content="更新路由" placement="top" v-if="scope.row.healthy">
                        <el-button type="text" @click="updateRoute(scope.row)"><i class="fa fa-repeat"></i></el-button>
                    </el-tooltip>
                    <el-tooltip content="停用" placement="top" v-if="scope.row.takeTraffic">
                        <el-button type="text" @click="handleRemoveTraffic(scope.row)"><i class="fa fa-level-down"></i></el-button>
                    </el-tooltip>
                    <el-tooltip content="启用" placement="top" v-if="!scope.row.takeTraffic">
                        <el-button type="text" @click="handleRecoverTraffic(scope.row)"><i class="fa fa-level-up"></i></el-button>
                    </el-tooltip>
                    <el-tooltip content="删除" placement="top" v-if="!scope.row.healthy">
                        <el-button type="text" @click="handleDelete(scope.row)"><i class="fa fa-trash"></i></el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
        </el-table>
        <div class="my-pagination" v-if="total != 0">
            <el-pagination layout="prev, pager, next, jumper" :total="total" :page-size="pageSize" :current-page="currentPage" @current-change="changePage"></el-pagination>
        </div>
        <div v-if="isAgentInfoVisible">
            <json-content-dialog :title="viewInfoTitle" :content="viewInfo" @close-dialog="closeDialog"></json-content-dialog>
        </div>
    </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      isPrivilege: false,
      isAgentInfoVisible: false,
      multipleSelection: [],
      viewInfoTitle: '节点信息',
      viewInfo: '',
      selectedCluster: this.$route.query.cluster || '',
      clusters: [],
      agentList: [],
      currentPage: Number(this.$route.query.currentPage) || 1,
      pageSize: 10,
      total: 0,
      interval: 0,
    };
  },
  methods: {
    heartbeat() {
      this.interval = setInterval(() => {
        this.getAgents();
      }, 5000);
    },
    batchUpdateRoute() {
      if (this.multipleSelection.length <= 0) {
        this.$message.errorMessage('请选择要批量更新的路由！！！');
      } else {
        const selectedRouteArray = [];
        const selectedRouteNameArray = [];
        this.multipleSelection.forEach((element) => {
          selectedRouteArray.push(element.id);
          selectedRouteNameArray.push(element.ipport);
        });
        const selectedRouteStr = selectedRouteArray.join(',');
        const selectedRouteNameStr = selectedRouteNameArray.join(' ; ');
        this.$message.confirmMessage(`确认更新路由 ${selectedRouteNameStr} 吗?`, () => {
          this.loading = true;
          this.$http.post('/ss/remote_update.json', { ssIds: selectedRouteStr }).then(() => {
            this.$message.successMessage('批量更新路由成功', () => {
              this.refreshPage();
            });
          })
          .finally(() => {
            this.loading = false;
          });
        });
      }
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    setSelectable(row) {
      if (row.healthy) {
        return true;
      }
      return false;
    },
    handleDelete(row) {
      this.$message.confirmMessage(`确认删除路由${row.ipport}吗?`, () => {
        this.loading = true;
        this.$http.post('/ss/delete.json', { id: row.id }).then(() => {
          this.$message.successMessage('删除路由成功', () => {
            this.refreshPage();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    handleDeleteExpired() {
      this.$message.confirmMessage('确认删除3天前的无效代理节点吗?', () => {
        this.loading = true;
        this.$http.post('/ss/delete.json', { days: 3 }).then(() => {
          this.$message.successMessage('删除路由成功', () => {
            this.refreshPage();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    handleRemoveTraffic(row) {
      const filtered = this.agentList.filter(
        e => e.healthy && e.takeTraffic && e.cluster === row.cluster);
      if (filtered.length === 1 && filtered[0].id === row.id) {
        this.$message.errorMessage('不能停用同一个集群下的唯一在线并且启用的路由服务！');
        return;
      }
      this.$message.confirmMessage(`确认要停用${row.ipport}路由服务吗?`, () => {
        this.loading = true;
        this.$http.post('/ss/traffic.json', { id: row.id, takeTraffic: false }).then(() => {
          this.$message.successMessage('停用成功', () => {
            this.refreshPage();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    handleRecoverTraffic(row) {
      this.$message.confirmMessage(`确认要重新启用${row.ipport}路由服务吗?`, () => {
        this.loading = true;
        this.$http.post('/ss/traffic.json', { id: row.id, takeTraffic: true }).then(() => {
          this.$message.successMessage('启用成功', () => {
            this.refreshPage();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    changePage(currentPage) {
      this.currentPage = currentPage;
      this.toPage();
    },
    updateRoute(row) {
      this.$message.confirmMessage(`确认更新路由${row.ipport}吗?`, () => {
        this.loading = true;
        this.$http.post('/ss/remote_update.json', { ssIds: row.id }).then(() => {
          this.$message.successMessage('更新路由成功', () => {
            this.refreshPage();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    getInfo(row) {
      this.viewInfo = JSON.stringify(JSON.parse(row.info), undefined, 2);
      this.isAgentInfoVisible = true;
    },
    closeDialog() {
      this.isAgentInfoVisible = false;
    },
    refreshPage() {
      this.init();
    },
    toPage() {
      this.$router.push({ path: this.$routermapper.GetPath('agentManage'), query: { currentPage: this.currentPage, cluster: this.selectedCluster } });
    },
    getClusters() {
      return this.$http.post('/ss/clusters.json').then((data) => {
        this.clusters = data;
      });
    },
    getAgents() {
      const params = {
        currentPage: this.$route.query.currentPage || 1,
        pageSize: this.pageSize,
        selectedCluster: this.selectedCluster,
      };
      return this.$http.get('/ss/find.json', params).then((data) => {
        this.agentList = data.list;
        this.total = data.total;
        this.isPrivilege = true;
        this.agentList.forEach((ss) => {
          if (ss !== null) {
            try {
              JSON.parse(ss.info);
            } catch (err) {
              this.$set(ss, 'info', '{}');
            }
          }
        });
      });
    },
    init() {
      this.loading = true;
      Promise.all([this.getAgents(), this.getClusters()]).then(() => {
        this.heartbeat();
      })
      .finally(() => {
        this.loading = false;
      });
    },
  },
  created() {
    this.init();
  },
  watch: {
    $route: 'getAgents',
  },
  filters: {
    currentQps(row) {
      const gaugesStatistics = JSON.parse(row.info) ?
        JSON.parse(row.info).gaugesStatistics : undefined;
      const pattern = /(?=((?!\b)\d{3})+$)/g;
      let maxKey = '-1';
      let maxValue = 0;
      if (gaugesStatistics === undefined || !row.healthy) {
        return 'N/A';
      }
      Object.entries(gaugesStatistics.qps).forEach((element) => {
        if (element[0] > maxKey) {
          maxKey = element[0];
          maxValue = element[1];
        }
      });
      return maxValue.toString().replace(pattern, ',');
    },
    currentConns(row) {
      const gaugesStatistics = JSON.parse(row.info) ?
        JSON.parse(row.info).gaugesStatistics : undefined;
      const pattern = /(?=((?!\b)\d{3})+$)/g;
      let maxKey = '-1';
      let maxValue = 0;
      if (gaugesStatistics === undefined || !row.healthy) {
        return 'N/A';
      }
      Object.entries(gaugesStatistics.conns).forEach((element) => {
        if (element[0] > maxKey) {
          maxKey = element[0];
          maxValue = element[1];
        }
      });
      return maxValue.toString().replace(pattern, ',');
    },
    currentReceiveClientThrougph(row) {
      const gaugesStatistics = JSON.parse(row.info) ?
        JSON.parse(row.info).gaugesStatistics : undefined;
      let maxKey = '-1';
      let maxValue = 0;
      if (gaugesStatistics === undefined || !row.healthy) {
        return 'N/A';
      }
      Object.entries(gaugesStatistics.reqThroughput).forEach((element) => {
        if (element[0] > maxKey) {
          maxKey = element[0];
          maxValue = element[1];
        }
      });
      maxValue /= 1000;
      return maxValue.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,').concat(' KB');
    },
    currentReceiveEsThrougph(row) {
      const gaugesStatistics = JSON.parse(row.info) ?
        JSON.parse(row.info).gaugesStatistics : undefined;
      let maxKey = '-1';
      let maxValue = 0;
      if (gaugesStatistics === undefined || !row.healthy) {
        return 'N/A';
      }
      Object.entries(gaugesStatistics.respThroughput).forEach((element) => {
        if (element[0] > maxKey) {
          maxKey = element[0];
          maxValue = element[1];
        }
      });
      maxValue /= 1000;
      return maxValue.toFixed(2).replace(/(\d)(?=(\d{3})+\.)/g, '$1,').concat(' KB');
    },
    translateState(data) {
      const NODE_STATUS = { true: '在线', false: '离线' };
      return NODE_STATUS[data];
    },
  },
  destroyed() {
    clearInterval(this.interval);
  },
};

</script>
