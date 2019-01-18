<template>
    <div class="page-content">
        <div class="my-breadcrumb">
            <el-breadcrumb separator="/" class="my-breadcrumb-content">
                <el-breadcrumb-item :to="{ name:'plugin_manage' }"><i class="fa fa-home"></i>插件管理</el-breadcrumb-item>
                <el-breadcrumb-item>{{pluginName}}</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="data-table-filter">
            <div class="pull-left">
                <el-button type="primary" @click="refreshPage"><i class="fa fa-refresh"></i>刷新</el-button>
            </div>
        </div>
        <el-table :data="pluginUpgradeList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
            <el-table-column type="expand">
                <template scope="props">
                    <el-form label-position="left" inline class="my-table-expand">
                        <el-form-item label="路径" style="width: 100%;">
                            <span>{{ props.row.packagePath }};</span>
                        </el-form-item>
                        <div v-if="props.row.state !== 3 && props.row.state !== 4">
                            <div v-for="item in props.row.nodeStates" :key="item.nodeIp" v-show="item.nodeIp !== ''">
                                <el-form-item label="IP" style="width: 40%;">
                                    <span>{{ item.nodeIp }}</span>
                                </el-form-item>
                                <el-form-item label="版本" style="width: 30%;">
                                    <span>{{ item.pluginVersion }}</span>
                                </el-form-item>
                                <el-form-item label="状态" style="width: 30%;">
                                    <span>{{ item.state | translateStat }}</span>
                                </el-form-item>
                            </div>
                        </div>
                    </el-form>
                </template>
            </el-table-column>
            <el-table-column prop="id" label="ID" width="60px"></el-table-column>
            <el-table-column label="所属集群" min-width="135px">
                <template scope="scope">
                    <div v-if="!isAllPrivilege">{{scope.row.clusterId}}</div> 
                    <div v-else class="my-a-link">
                        <router-link tag="a" :to="{ name:'cluster_detail',query:{clusterId: scope.row.clusterId} }">{{scope.row.clusterId}}</router-link>
                    </div>
                </template>
            </el-table-column>
            <el-table-column prop="note" label="插件描述" width="95px" show-overflow-tooltip></el-table-column>
            <el-table-column prop="pluginVersion" label="插件版本"></el-table-column>
            <el-table-column prop="pluginType" label="插件类型" width="100px">
                <template scope="scope">{{pluginTypeMap[scope.row.pluginType]}}</template>
            </el-table-column>
            <el-table-column prop="applyUser" label="申请人">
                <template scope="scope">{{scope.row.applyUser || '-'}}</template>
            </el-table-column>
            <el-table-column prop="approveUser" label="审批人">
                <template scope="scope">{{scope.row.approveUser || '-'}}</template>
            </el-table-column>
            <el-table-column label="状态" width="90px">
                <template scope="scope">{{scope.row.state | translateStat}}
                </template>
            </el-table-column>
            <el-table-column label="更新时间" width="160px">
                <template scope="scope">{{scope.row.updateTime | formatDate}}</template>
            </el-table-column>
            <el-table-column label="操作" width="130px">
                <template scope="scope">
                    <div v-if="isAllPrivilege">
                        <el-tooltip content="开始下载" placement="top" v-if="scope.row.state === 1 || scope.row.state === 5">
                            <el-button type="text" @click="handlePlugin(scope.row, 'download', '开始下载')"><i class="fa fa-download"></i></el-button>
                        </el-tooltip>
                        <el-tooltip content="强制升级" placement="top" v-if="scope.row.state === 5">
                            <el-button type="text" @click="handlePlugin(scope.row, 'upgrade', '强制升级')"><i class="fa fa-arrow-up"></i></el-button>
                        </el-tooltip>
                        <el-tooltip content="开始升级" placement="top" v-if="scope.row.state === 51">
                            <el-button type="text" @click="handlePlugin(scope.row, 'upgrade', '开始升级')"><i class="fa fa-arrow-circle-up"></i></el-button>
                        </el-tooltip>
                        <el-tooltip content="标记完成" placement="top" v-if="scope.row.state === 61 || scope.row.state === 5 || scope.row.state === 51 || scope.row.state === 6">
                            <el-button type="text" @click="handlePlugin(scope.row, 'done', '标记完成')"><i class="fa fa-check-circle"></i></el-button>
                        </el-tooltip>
                        <el-tooltip content="审批不通过" placement="top" v-if="scope.row.state === 1">
                            <el-button type="text" @click="handlePlugin(scope.row, 'deny', '审批不通过')"><i class="fa fa-ban"></i></el-button>
                        </el-tooltip>
                        <el-tooltip content="回滚" placement="top" v-if="scope.row.state === 4">
                            <el-button type="text" @click="handleRollback(scope.row)"><i class="fa fa-undo"></i></el-button>
                        </el-tooltip>
                        <el-tooltip content="取消" placement="top" v-if="scope.row.state === 1">
                            <el-button type="text" @click="handlePlugin(scope.row, 'recall', '取消')"><i class="fa fa-close"></i></el-button>
                        </el-tooltip>
                        <el-tooltip content="终止" placement="top" v-if="scope.row.state === 5 || scope.row.state === 51 || scope.row.state === 6">
                            <el-button type="text" @click="handlePlugin(scope.row, 'stop', '终止')"><i class="fa fa-stop-circle"></i></el-button>
                        </el-tooltip>
                    </div>
                    <div v-else>
                        <el-tooltip content="取消" placement="top" v-if="scope.row.state === 1">
                            <el-button type="text" @click="handlePlugin(scope.row, 'recall', '取消')"><i class="fa fa-close"></i></el-button>
                        </el-tooltip>
                    </div>
                </template>
            </el-table-column>
        </el-table>
        <div class="my-pagination" v-if="total != 0">
            <el-pagination layout="prev, pager, next, jumper" :total="total" :page-size="pageSize" :current-page="currentPage" @current-change="changePage"></el-pagination>
        </div>
    </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      pluginUpgradeList: [],
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
  computed: {
    pluginName() {
      return this.$route.query.pluginName;
    },
  },
  methods: {
    getPluginUpgradeList() {
      const params = {
        currentPage: Number(this.$route.query.currentPage) || 1,
        pageSize: this.pageSize,
        pluginName: encodeURIComponent(this.pluginName || ''),
      };
      return this.$http.get('/plugin/upgrade/list.json', params).then((data) => {
        this.pluginUpgradeList = data.list;
        this.isAllPrivilege = data.allPrivilege;
        this.total = data.total;
      });
    },
    handlePlugin(row, operation, text) {
      const params = {
        pluginUpgradeId: row.id,
        action: operation,
      };
      this.$message.confirmMessage(`确定${text}插件${row.pluginName}吗?`, () => {
        this.loading = true;
        this.$http.post('/plugin/upgrade/action.json', params).then(() => {
          this.$message.successMessage('操作成功', () => {
            this.refreshPage();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    handleRollback(row) {
      const params = {
        clusterId: row.clusterId,
        pluginName: row.pluginName,
        pluginVersion: row.pluginVersion,
        pluginType: row.pluginType,
        note: row.note,
        packagePath: row.packagePath,
      };
      this.$message.confirmMessage(`确定回滚插件${row.pluginName}吗?`, () => {
        this.loading = true;
        this.$http.post('/plugin/upgrade/add.json', params).then(() => {
          this.$message.successMessage('操作成功，已创建新工单', () => {
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
      this.$router.push({ path: this.$routermapper.GetPath('pluginUpgrade'), query: { currentPage: this.currentPage, pluginName: this.pluginName } });
    },
    init() {
      this.loading = true;
      Promise.all([this.getPluginUpgradeList()]).then()
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
  filters: {
    translateStat(data) {
      const NODE_STATUS = { 0: '创建', 1: '待审批', 2: '审批不通过', 3: '取消', 4: '标记完成', 5: '下载中', 51: '下载完成', 6: '升级中', 61: '升级完成', 7: '插件已移除' };
      return NODE_STATUS[data];
    },
  },
};

</script>
<style>
</style>
