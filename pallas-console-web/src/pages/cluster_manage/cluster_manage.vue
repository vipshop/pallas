<template>
    <div class="page-content">
        <div class="my-breadcrumb">
            <el-breadcrumb separator="/" class="my-breadcrumb-content">
                <el-breadcrumb-item><i class="fa fa-home"></i>ES集群管理</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="data-table-filter">
            <div class="pull-left">
                <el-form :inline="true" class="demo-form-inline">
                    <input type="text" v-show="false"/>
                    <el-form-item label="">
                        <el-input placeholder="请搜索域名" v-model="clusterIdForSearch" @keyup.enter.native="toPage"></el-input>
                    </el-form-item>
                    <el-form-item class="filter-search">
                        <el-button type="primary" icon="search" @click="toPage">查询</el-button>
                    </el-form-item>
                </el-form>
            </div>
            <div class="pull-right" v-if="isAllPrivilege">
                <el-button type="primary" icon="plus" @click="handleAdd">新增ES集群</el-button>
            </div>
        </div>
        <el-table :data="clusterList.list" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
            <el-table-column label="域名" min-width="80px">
                <template scope="scope">
                    <div v-if="!scope.row.hasPrivilege || scope.row.logicalCluster">{{scope.row.clusterId}}</div>
                    <div v-else class="my-a-link">
                        <router-link tag="a" :to="{ path:'cluster_detail',query:{clusterId: scope.row.clusterId} }">{{scope.row.clusterId}}</router-link>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="集群类型" width="80px">
                <template scope="scope">{{scope.row.logicalCluster | clusterType}}</template>
            </el-table-column>
            <el-table-column label="物理集群">
                <template scope="scope">
                    <div v-if="scope.row.realClusters === ''">-</div>
                    <div v-else v-for="item in getPhysicalsClusterArr(scope.row.realClusters)" :key="item.id" class="my-a-link">
                        <router-link tag="a" :to="{ path:'cluster_detail',query:{clusterId: item.clusterId} }">{{item.clusterId}}</router-link>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="描述" prop="description" show-overflow-tooltip></el-table-column>
            <el-table-column label="HTTP地址" width="170px">
                <template scope="scope">
                    <div v-if="!scope.row.httpAddress">-</div>
                    <div v-else>
                        <el-popover trigger="hover" placement="left">
                            <div v-for="item in $array.strToArray(scope.row.httpAddress)" :key="item">{{item}}</div>
                            <div slot="reference">
                              {{$array.strToArray(scope.row.httpAddress)[0]}}
                              <span v-if="$array.strToArray(scope.row.httpAddress).length > 1">...</span>
                            </div>
                        </el-popover>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="CLIENT地址" width="170px">
                <template scope="scope">
                    <div v-if="!scope.row.clientAddress">-</div>
                    <div v-else>
                        <el-popover trigger="hover" placement="right">
                            <div v-for="item in $array.strToArray(scope.row.clientAddress)" :key="item">{{item}}</div>
                            <div slot="reference">
                              {{$array.strToArray(scope.row.clientAddress)[0]}}
                              <span v-if="$array.strToArray(scope.row.clientAddress).length > 1">...</span>
                            </div>
                        </el-popover>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="绑定代理集群">
                <template scope="scope">
                    <div v-if="!scope.row.accessiblePs">-</div>
                    <div v-else v-for="item in $array.strToArray(scope.row.accessiblePs)" :key="item">
                        {{item}}
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="80px">
                <template scope="scope">
                    <el-dropdown trigger="click">
                      <span class="el-dropdown-link">
                        操作<i class="el-icon-caret-bottom el-icon--right"></i>
                      </span>
                      <el-dropdown-menu class="dropdown-operation" slot="dropdown">
                        <el-dropdown-item v-if="scope.row.hasPrivilege"><a @click="handleEdit(scope.row)"><span><i class="fa fa-pencil-square-o"></i>编辑</span></a></el-dropdown-item>
                        <el-dropdown-item v-if="scope.row.hasPrivilege && !scope.row.logicalCluster"><a @click="handleRouteSetting(scope.row)"><span><i class="fa fa-cog"></i>路由配置</span></a></el-dropdown-item>
                        <el-dropdown-item v-if="scope.row.hasPrivilege && !scope.row.logicalCluster"><a @click="handleManage(scope.$index, scope.row)"><span><i class="fa fa-bars"></i>管理</span></a></el-dropdown-item>
                        <el-dropdown-item v-if="scope.row.hasPrivilege && !scope.row.logicalCluster"><a @click="handleRestart(scope.row)"><span><i class="fa fa-undo"></i>重启</span></a></el-dropdown-item>
                        <el-dropdown-item v-if="scope.row.hasPrivilege"><a @click="handleDelete(scope.row)"><span><i class="fa fa-trash"></i>删除</span></a></el-dropdown-item>
                      </el-dropdown-menu>
                    </el-dropdown>
                </template>
            </el-table-column>
        </el-table>
        <div class="my-pagination" v-if="clusterList.total != 0">
            <el-pagination layout="prev, pager, next, jumper" :total="clusterList.total" :page-size="pageSize" :current-page="currentPage" @current-change="changePage"></el-pagination>
        </div>
        <div v-if="isClusterInfoVisible">
            <cluster-info-dialog :cluster-operation="clusterOperation" :cluster-info-title="clusterInfoTitle" :cluster-info="clusterInfo" :all-physicals="allPhysicals" :all-pallas-searchs="allPallasSearchs" @close-dialog="closeDialog" @operate-close-dialog="operateCloseDialog"></cluster-info-dialog>
        </div>
    </div>
</template>

<script>
import ClusterInfoDialog from './cluster_info_dialog/cluster_info_dialog';

export default {
  data() {
    return {
      loading: false,
      isAllPrivilege: false,
      isClusterInfoVisible: false,
      clusterInfoTitle: '',
      clusterOperation: '',
      currentPage: Number(this.$route.query.currentPage) || 1,
      pageSize: 10,
      clusterIdForSearch: this.$route.query.clusterId || '',
      clusterList: {},
      clusterInfo: {},
      clusterAddInfo: {
        clusterId: '',
        httpAddress: '',
        clientAddress: '',
        description: '',
        realClustersArr: [],
        logicalCluster: false,
        accessiblePs: [],
      },
      allPhysicals: [],
      allPallasSearchs: [],
    };
  },
  methods: {
    handleRouteSetting(row) {
      this.$router.push({ path: 'cluster_route_manage', query: { clusterId: row.clusterId } });
    },
    getPhysicalsClusterArr(str) {
      const arr = str.split(',');
      const resultArr = [];
      arr.forEach((ele1) => {
        this.allPhysicals.forEach((ele2) => {
          if (ele1 === ele2.id) {
            resultArr.push(ele2);
          }
        });
      });
      return resultArr;
    },
    handleAdd() {
      this.isClusterInfoVisible = true;
      this.clusterInfoTitle = '新增集群';
      this.clusterOperation = 'add';
      this.clusterInfo = JSON.parse(JSON.stringify(this.clusterAddInfo));
    },
    handleEdit(row) {
      this.loading = true;
      this.$http.get(`/cluster/id.json?clusterId=${row.clusterId}`).then((data) => {
        const clusterEditInfo = data;
        const realClustersArr = clusterEditInfo.realClusters.split(',');
        this.$set(clusterEditInfo, 'realClustersArr', realClustersArr);
        const accessiblePs = clusterEditInfo.accessiblePs.split(',');
        this.$set(clusterEditInfo, 'accessiblePs', accessiblePs);
        this.clusterInfo = JSON.parse(JSON.stringify(clusterEditInfo));
        this.clusterInfoTitle = '编辑集群';
        this.clusterOperation = 'edit';
        this.isClusterInfoVisible = true;
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleManage(index, row) {
      this.$router.push({ path: 'cluster_detail', query: { clusterId: row.clusterId } });
    },
    handleRestart(row) {
      this.$router.push({ path: 'cluster_node_restart', query: { clusterId: row.clusterId } });
    },
    handleDelete(row) {
      this.$message.confirmMessage(`确定删除集群${row.clusterId}吗?`, () => {
        this.loading = true;
        this.$http.post('/cluster/delete/id.json', { clusterId: row.clusterId }).then(() => {
          this.$message.successMessage('删除成功', () => {
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
    refreshPage() {
      this.init();
    },
    toPage() {
      this.$router.push({ path: this.$routermapper.GetPath('clusterManage'), query: { currentPage: this.currentPage, clusterId: this.clusterIdForSearch } });
    },
    closeDialog() {
      this.isClusterInfoVisible = false;
    },
    operateCloseDialog() {
      this.isClusterInfoVisible = false;
      this.refreshPage();
    },
    getClusterList() {
      const params = {
        clusterId: this.$route.query.clusterId || '',
        currentPage: Number(this.$route.query.currentPage) || 1,
        pageSize: this.pageSize,
      };
      return this.$http.get('/cluster/page.json', params).then((data) => {
        this.clusterList = data;
        this.isAllPrivilege = data.allPrivilege;
      });
    },
    getPhysicals() {
      return this.$http.get('/cluster/all/physicals.json').then((data) => {
        this.allPhysicals = data.list.map((obj) => {
          const rObj = {};
          rObj.id = obj.id.toString();
          rObj.clusterId = obj.clusterId;
          return rObj;
        });
      });
    },
    getAllPallasSearchs() {
      return this.$http.get('/ss/clusters.json').then((data) => {
        this.allPallasSearchs = data;
      });
    },
    init() {
      this.loading = true;
      Promise.all([this.getClusterList(), this.getPhysicals()]).then()
      .finally(() => {
        this.loading = false;
      });
      Promise.all([this.getClusterList(), this.getAllPallasSearchs()]).then()
      .finally(() => {
        this.loading = false;
      });
    },
  },
  components: {
    'cluster-info-dialog': ClusterInfoDialog,
  },
  watch: {
    $route: 'init',
  },
  created() {
    this.init();
  },
  filters: {
    clusterType(data) {
      const CLUSTER_TYPE = { true: '逻辑集群', false: '物理集群' };
      return CLUSTER_TYPE[data];
    },
  },
};

</script>
