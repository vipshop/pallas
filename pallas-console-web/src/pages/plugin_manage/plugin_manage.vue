<template>
    <div class="page-content">
        <div class="my-breadcrumb">
            <el-breadcrumb separator="/" class="my-breadcrumb-content">
                <el-breadcrumb-item><i class="fa fa-home"></i>插件管理</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="data-table-filter">
            <div class="pull-left">
                <el-form :inline="true" class="demo-form-inline">
                    <input type="text" v-show="false"/>
                    <el-form-item label="">
                        <el-input placeholder="请搜索插件" v-model="pluginNameForSearch" @keyup.enter.native="toPage"></el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" icon="search" @click="toPage">查询</el-button>
                    </el-form-item>
                    <el-form-item class="filter-search">
                        <el-button type="primary" @click="refreshPage"><i class="fa fa-refresh"></i>刷新</el-button>
                    </el-form-item>
                </el-form>
            </div>
            <div class="pull-right">
                <el-button type="primary" icon="plus" @click="handleAdd">新增插件</el-button>
            </div>
        </div>
        <el-table :data="pluginRuntimeList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
            <el-table-column prop="id" label="升级ID" width="80px"></el-table-column>
            <el-table-column prop="clusterId" label="所属集群"></el-table-column>
            <el-table-column prop="pluginName" label="运行插件">
                <template scope="scope">
                    <div class="my-a-link">
                        <router-link tag="a" :to="{ name: 'plugin_upgrade', query: {pluginName: scope.row.pluginName} }">{{scope.row.pluginName}}</router-link>
                    </div>
                </template>
            </el-table-column>
            <el-table-column prop="pluginVersion" label="插件版本" width="120px">
                <template scope="scope">
                    <div class="plugin-version" v-for="item in getPluginVersion(scope.row.nodeStates)" :key="item">
                        <el-popover trigger="hover" placement="right">
                            <div v-for="node in getNodesOfVersion(scope.row.nodeStates, item)" :key="node">{{node}}</div>
                            <el-button type="text" slot="reference">{{item}}</el-button>
                        </el-popover>
                    </div>
                </template>
            </el-table-column>
            <el-table-column prop="pluginType" label="插件类型" width="130px">
                <template scope="scope">{{pluginTypeMap[scope.row.pluginType]}}</template>
            </el-table-column>
            <el-table-column label="更新时间" width="190px">
                <template scope="scope">{{scope.row.updateTime | formatDate}}</template>
            </el-table-column>
            <el-table-column label="操作" width="70px">
                <template scope="scope">
                    <el-tooltip content="升级" placement="top" v-if="scope.row.creatable">
                        <el-button type="text" @click="handleUpgrade(scope.row)"><i class="fa fa-arrow-circle-up"></i></el-button>
                    </el-tooltip>
                    <el-tooltip content="移除" placement="top" v-if="isAllPrivilege && scope.row.creatable">
                        <el-button type="text" @click="handleRemove(scope.row)"><i class="fa fa-close"></i></el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
        </el-table>
        <div class="my-pagination" v-if="total != 0">
            <el-pagination layout="prev, pager, next, jumper" :total="total" :page-size="pageSize" :current-page="currentPage" @current-change="changePage"></el-pagination>
        </div>
        <div v-if="isPluginInfoDialogVisible">
            <plugin-info-dialog :plugin-info="pluginInfo" :plugin-info-title="pluginInfoTitle" :plugin-info-operation="pluginInfoOperation" @operate-close-dialog="operateCloseDialog" @close-dialog="closeDialog"></plugin-info-dialog>
        </div>
    </div>
</template>

<script>
import '../../components/filter';
import PluginInfoDialog from './plugin_info_dialog/plugin_info_dialog';

export default {
  data() {
    return {
      loading: false,
      isPluginInfoDialogVisible: false,
      pluginInfo: {},
      pluginInfoTitle: '',
      pluginInfoOperation: '',
      pluginNameForSearch: this.$route.query.pluginName || '',
      pluginRuntimeList: [],
      pageSize: 10,
      total: 0,
      currentPage: Number(this.$route.query.currentPage) || 1,
      isAllPrivilege: false,
      pluginTypeMap: {
        0: 'PALLAS',
        1: 'ES',
      },
    };
  },
  methods: {
    getNodesOfVersion(arr, item) {
      const nodes = [];
      arr.forEach((element) => {
        if (element.pluginVersion === item) {
          nodes.push(element.nodeIp);
        }
      });
      return nodes;
    },
    getPluginVersion(arr) {
      const pluginVersions = [];
      arr.forEach((element) => {
        if (element.pluginVersion !== '') {
          pluginVersions.push(element.pluginVersion);
        }
      });
      return Array.from(new Set(pluginVersions));
    },
    handleAdd() {
      this.isPluginInfoDialogVisible = true;
      this.pluginInfoTitle = '新建插件';
      this.pluginInfoOperation = 'add';
      const pluginAddInfo = { clusterId: '', pluginName: '', pluginVersion: '', pluginType: 0, note: '', packagePath: '' };
      this.pluginInfo = JSON.parse(JSON.stringify(pluginAddInfo));
    },
    handleRemove(row) {
      const params = {
        pluginUpgradeId: row.id,
        clusterId: row.clusterId,
        pluginName: row.pluginName,
        pluginVersion: row.nodeStates[0].pluginVersion || '',
      };
      this.$message.confirmMessage(`确定移除插件${row.pluginName}吗? 请注意，确定后会马上移除插件！`, () => {
        this.loading = true;
        this.$http.post('/plugin/remove.json', params).then(() => {
          this.$message.successMessage('提交移除请求成功并等待后台执行，请稍后刷新页面', () => {
            this.refreshPage();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    handleUpgrade(row) {
      this.isPluginInfoDialogVisible = true;
      this.pluginInfoTitle = `${row.pluginName}插件升级`;
      this.pluginInfoOperation = 'upgrade';
      const pluginEditInfo = {
        clusterId: row.clusterId,
        pluginName: row.pluginName,
        pluginVersion: '',
        pluginType: 0,
        note: '',
        packagePath: '',
      };
      this.pluginInfo = JSON.parse(JSON.stringify(pluginEditInfo));
    },
    closeDialog() {
      this.isPluginInfoDialogVisible = false;
    },
    operateCloseDialog() {
      this.isPluginInfoDialogVisible = false;
      this.$message.successMessage('新增插件成功，等待审核，请稍后刷新页面', () => {
        this.refreshPage();
      });
    },
    getPluginRuntimeList() {
      const params = {
        currentPage: Number(this.$route.query.currentPage) || 1,
        pageSize: this.pageSize,
        pluginName: encodeURIComponent(this.$route.query.pluginName || ''),
      };
      return this.$http.get('/plugin/runtime/list.json', params).then((data) => {
        this.pluginRuntimeList = data.list;
        this.isAllPrivilege = data.allPrivilege;
        this.total = data.total;
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
      this.$router.push({ path: this.$routermapper.GetPath('pluginManage'), query: { currentPage: this.currentPage, pluginName: this.pluginNameForSearch } });
    },
    init() {
      this.loading = true;
      Promise.all([this.getPluginRuntimeList()]).then()
      .finally(() => {
        this.loading = false;
      });
    },
  },
  created() {
    this.init();
  },
  watch: {
    $route: 'init',
  },
  components: {
    'plugin-info-dialog': PluginInfoDialog,
  },
};

</script>
<style>
.plugin-version .el-button+.el-button {
      margin-left: 0;
}
</style>
