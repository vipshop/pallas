<template>
    <div class="page-content">
        <div class="my-breadcrumb">
            <el-breadcrumb separator="/" class="my-breadcrumb-content">
                <el-breadcrumb-item><i class="fa fa-home"></i>索引管理</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="data-table-filter">
            <div class="pull-left">
                <el-form :inline="true" class="demo-form-inline">
                    <el-form-item label="">
                        <el-select v-model="selectedCluster" clearable @change="toPage">
                            <el-option label="全部集群" value=""></el-option>
                            <el-option-group v-for="cluster in clusters" :key="cluster.label" :label="cluster.label">
                                <el-option v-for="item in cluster.options" :key="item.id" :label="item.clusterId" :value="item.clusterId"></el-option>
                            </el-option-group>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="">
                        <el-input placeholder="请搜索索引" v-model="indexNameForSearch" @keyup.enter.native="toPage"></el-input>
                    </el-form-item>
                    <el-form-item class="filter-search">
                        <el-button type="primary" icon="search" @click="toPage">查询</el-button>
                    </el-form-item>
                </el-form>
            </div>
            <div class="pull-right" v-if="isAllPrivilege">
                <el-button type="primary" icon="plus" @click="handleAdd">新增索引</el-button>
            </div>
        </div>
        <el-table :data="indexList.list" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
            <el-table-column type="expand">
              <template scope="props">
                <el-form label-position="left" inline class="my-table-expand">
                  <el-form-item label="id">
                    <span>{{ props.row.id}}</span>
                  </el-form-item>
                  <el-form-item label="索引名">
                    <span>{{ props.row.indexName}}</span>
                  </el-form-item>
                  <el-form-item label="索引描述">
                    <span>{{ props.row.description || 'null' }}</span>
                  </el-form-item>
                  <el-form-item label="所属集群">
                    <span>{{ props.row.clusterName}}</span>
                  </el-form-item>
                  <el-form-item label="集群描述">
                    <span>{{ props.row.clusterDesc || 'null' }}</span>
                  </el-form-item>
                  <el-form-item label="创建人">
                    <span>{{ props.row.createUser || 'null' }}</span>
                  </el-form-item>
                  <el-form-item label="创建时间">
                    <span>{{ props.row.createTime | formatDate }}</span>
                  </el-form-item>
                  <el-form-item label="更新时间">
                    <span>{{ props.row.updateTime | formatDate }}</span>
                  </el-form-item>
                  <el-form-item label="数据源数量">
                    <span>{{ props.row.dataSourceList.length }}</span>
                  </el-form-item>
                  <el-form-item label="数据源" style="width: 100%;">
                      <span class="data-source-text" v-for="item in props.row.dataSourceList" :key="item.id">{{ item.dbname }} - {{ item.tableName }};</span>
                  </el-form-item>
                </el-form>
              </template>
            </el-table-column>
            <el-table-column label="id" width="80px">
              <template scope="scope">
                  <div v-if="!scope.row.hasPrivilege">{{scope.row.id}}</div>
                  <div v-else class="my-a-link">
                      <router-link tag="a" :to="{ path: 'index_detail', query: {indexId: scope.row.id, indexName: scope.row.indexName} }">{{scope.row.id}}</router-link>
                  </div>
              </template>
            </el-table-column>
            <el-table-column label="索引名" min-width="100px">
              <template scope="scope">
                  <div v-if="!scope.row.hasPrivilege">{{scope.row.indexName}}</div>
                  <div v-else class="my-a-link">
                      <router-link tag="a" :to="{ path: 'index_detail', query: {indexId: scope.row.id, indexName: scope.row.indexName} }">{{scope.row.indexName}}</router-link>
                  </div>
              </template>
            </el-table-column>
            <el-table-column label="索引描述" show-overflow-tooltip>
              <template scope="scope">{{scope.row.description || '-'}}</template>
            </el-table-column>
            <el-table-column label="所属集群" min-width="100px">
              <template scope="scope">
                  <div v-if="!scope.row.hasClusterPrivilege || !scope.row.httpAddress">{{scope.row.clusterName}}</div>
                  <div v-else class="my-a-link">
                      <router-link tag="a" :to="{ path:'cluster_detail',query:{clusterId: scope.row.clusterName} }">{{scope.row.clusterName}}</router-link>
                  </div>
              </template>
            </el-table-column>
            <el-table-column label="数据源" width="70px">
              <template scope="scope">{{scope.row.dataSourceList.length}}</template>
            </el-table-column>
            <el-table-column label="创建人">
              <template scope="scope">{{scope.row.createUser || '-'}}</template>
            </el-table-column>
            <el-table-column label="更新时间" prop="updateTime" width="160px">
                <template scope="scope">{{scope.row.updateTime | formatDate}}</template>
            </el-table-column>
            <el-table-column label="操作" width="100px">
                <template scope="scope">
                    <el-tooltip content="编辑" placement="top" v-if="scope.row.hasPrivilege">
                        <el-button type="text" @click="handleEdit(scope.row)"><i class="fa fa-edit"></i></el-button>
                    </el-tooltip>
                    <el-tooltip content="管理" placement="top" v-if="scope.row.hasPrivilege">
                        <el-button type="text" @click="handleManage(scope.$index, scope.row)"><i class="fa fa-bars"></i></el-button>
                    </el-tooltip>
                    <el-tooltip content="删除" placement="top" v-if="scope.row.hasPrivilege">
                        <el-button type="text" @click="handleDelete(scope.row)"><i class="fa fa-trash"></i></el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
        </el-table>
        <div class="my-pagination" v-if="indexList.total != 0">
            <el-pagination layout="prev, pager, next, jumper" :total="indexList.total" :page-size="pageSize" :current-page="currentPage" @current-change="changePage"></el-pagination>
        </div>
        <div v-if="isIndexInfoVisible">
            <index-info-dialog :index-operation="indexOperation" :index-info-title="indexInfoTitle" :index-info="indexInfo" :clusters="clusters" @close-dialog="closeDialog" @operate-close-dialog="operateCloseDialog"></index-info-dialog>
        </div>
    </div>
</template>

<script>

import '../../components/filter';
import IndexInfoDialog from './index_info_dialog/index_info_dialog';

export default {
  data() {
    return {
      loading: false,
      isAllPrivilege: false,
      isIndexInfoVisible: false,
      indexInfoTitle: '',
      indexOperation: '',
      //  索引列表信息
      indexList: {},
      //  集群下拉信息
      clusters: [],
      selectedCluster: this.$route.query.cluster || '',
      indexNameForSearch: this.$route.query.indexName || '',
      currentPage: Number(this.$route.query.currentPage) || 1,
      pageSize: 10,
      indexInfo: {},
      indexAddInfo: {
        indexName: '',
        clusterId: '',
        description: '',
        confirm: false,
        dataSourceList: [{
          ip: '',
          port: '',
          username: '',
          password: '',
          dbname: '',
          tableName: '',
          isGeneratePwd: true,
        }],
      },
    };
  },
  methods: {
    handleDelete(row) {
      this.$message.confirmMessage(`确定删除索引${row.indexName}(id=${row.id})吗?`, () => {
        this.loading = true;
        this.$http.post('/index/delete/id.json', { indexId: row.id }).then(() => {
          this.$message.successMessage('删除成功', () => {
            this.refreshPage();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    handleAdd() {
      this.isIndexInfoVisible = true;
      this.indexInfoTitle = '新增索引';
      this.indexOperation = 'add';
      this.indexInfo = JSON.parse(JSON.stringify(this.indexAddInfo));
    },
    handleEdit(row) {
      this.loading = true;
      this.$http.get(`/index/id.json?indexId=${row.id}`).then((data) => {
        const indexEditData = data;
        const indexEditInfo = {
          indexId: indexEditData.id,
          clusterId: indexEditData.clusterName,
          description: indexEditData.description,
          dataSourceList: indexEditData.dataSourceList,
          indexName: indexEditData.indexName,
        };
        this.indexInfo = JSON.parse(JSON.stringify(indexEditInfo));
        this.isIndexInfoVisible = true;
        this.indexInfoTitle = '编辑索引';
        this.indexOperation = 'edit';
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleManage(index, row) {
      this.$router.push({ path: 'index_detail', query: { indexId: row.id, indexName: row.indexName } });
    },
    changePage(currentPage) {
      this.currentPage = currentPage;
      this.toPage();
    },
    refreshPage() {
      this.init();
    },
    toPage() {
      this.$router.push({ path: this.$routermapper.GetPath('indexManage'), query: { currentPage: this.currentPage, cluster: this.selectedCluster, indexName: this.indexNameForSearch } });
    },
    closeDialog() {
      this.isIndexInfoVisible = false;
    },
    operateCloseDialog() {
      this.isIndexInfoVisible = false;
      this.refreshPage();
    },
    getIndexList() {
      const params = {
        currentPage: this.$route.query.currentPage || 1,
        pageSize: this.pageSize,
        indexName: this.$route.query.indexName || '',
        clusterId: this.$route.query.cluster || '',
      };
      return this.$http.get('/index/page.json', params).then((data) => {
        this.indexList = data;
        this.isAllPrivilege = data.allPrivilege;
      });
    },
    getClusters() {
      return this.$http.get('/cluster/all.json').then((data) => {
        const logicClusters = [];
        const physicalClusters = [];
        data.forEach((element) => {
          if (element.logicalCluster) {
            logicClusters.push(element);
          } else {
            physicalClusters.push(element);
          }
        });
        this.clusters.push({ label: '物理集群', options: physicalClusters });
        this.clusters.push({ label: '逻辑集群', options: logicClusters });
      });
    },
    init() {
      this.loading = true;
      Promise.all([this.getIndexList(), this.getClusters()]).then()
      .finally(() => {
        this.loading = false;
      });
    },
  },
  components: {
    'index-info-dialog': IndexInfoDialog,
  },
  created() {
    this.init();
  },
  watch: {
    $route: 'init',
  },
};

</script>
<style>
.my-table-expand {
  font-size: 0;
  padding-left: 100px;
}
.my-table-expand label {
  width: 90px;
  color: #99a9bf;
}
.my-table-expand .el-form-item {
  margin-right: 0;
  margin-bottom: 0;
  width: 33.3333333%;
  text-align: left;
}
.data-source-text {
  margin-right: 8px;
}
</style>
