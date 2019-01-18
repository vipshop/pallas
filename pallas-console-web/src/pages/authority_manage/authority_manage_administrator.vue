<template>
    <div class="page-content">
        <div class="my-breadcrumb">
            <el-breadcrumb separator="/" class="my-breadcrumb-content">
                <el-breadcrumb-item :to="{ name:'authority_manage' }"><i class="fa fa-home"></i>模板变更</el-breadcrumb-item>
                <el-breadcrumb-item>模板审批</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="data-table-filter">
            <div class="pull-left">
                <el-form :inline="true" class="demo-form-inline">
                    <el-form-item label="">
                        <el-select v-model="selectedState" @change="toPage">
                            <el-option label="全部状态" value=""></el-option>
                            <el-option v-for="item in states" :label="item.label" :value="item.value" :key="item.value"></el-option>
                        </el-select>
                    </el-form-item>
                    <el-form-item label="">
                        <el-input placeholder="输入索引,模板或提交人" v-model="conditionForSearch" @keyup.enter.native="toPage"></el-input>
                    </el-form-item>
                    <el-form-item class="filter-search">
                        <el-button type="primary" icon="search" @click="toPage">查询</el-button>
                    </el-form-item>
                </el-form>
            </div>
            <div class="pull-right">
                <el-button type="primary" icon="circle-check" @click="handleBatchApprove">批量处理</el-button>
            </div>
        </div>
        <el-table ref="multipleTable" @selection-change="handleSelectionChange" :data="approveList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
            <el-table-column type="selection" width="55"></el-table-column>
            <el-table-column prop="id" label="审批ID" width="70px"></el-table-column>
            <el-table-column prop="title" label="标题" width="80px" show-overflow-tooltip></el-table-column>
            <el-table-column label="索引名称">
              <template scope="scope">
                  <div class="my-a-link">
                      <router-link tag="a" :to="{ path: 'index_detail', query: {indexId: scope.row.indexId, indexName: scope.row.indexName} }">{{scope.row.indexName}}</router-link>
                  </div>
              </template>
            </el-table-column>
            <el-table-column prop="templateName" label="模板名称"></el-table-column>
            <el-table-column prop="clusterId" label="所属集群"></el-table-column>
            <el-table-column prop="approveState" label="当前状态" width="80px">
                <template scope="scope">{{scope.row.approveState | translateStat}}</template>
            </el-table-column>
            <el-table-column prop="applyUser" label="提交人" width="80px"></el-table-column>
            <el-table-column label="提交时间" width="160px">
                <template scope="scope">{{scope.row.createTime | formatDate}}</template>
            </el-table-column>
            <el-table-column label="审批人" width="80px">
                <template scope="scope">
                  <span v-if="scope.row.approveState === 1 || scope.row.approveState === 2">{{scope.row.approveUser}}</span>
                  <span v-else>-</span>
                </template>
            </el-table-column>
            <el-table-column label="审批时间" width="160px">
                <template scope="scope">
                  <span v-if="scope.row.approveState === 1 || scope.row.approveState === 2">{{scope.row.approveTime | formatDate}}</span>
                  <span v-else>-</span>
                </template>
            </el-table-column>
            <el-table-column label="操作" width="70px">
                <template scope="scope">
                  <div v-show="scope.row.approveState === 0">
                    <el-tooltip content="查看" placement="top">
                        <el-button type="text" @click="handleView(scope.row)"><i class="fa fa-eye"></i></el-button>
                    </el-tooltip>
                    <el-tooltip content="处理" placement="top">
                        <el-button type="text" @click="handleApprove(scope.row)"><i class="fa fa-arrow-circle-right"></i></el-button>
                    </el-tooltip>
                  </div>
                </template>
            </el-table-column>
        </el-table>
        <div class="my-pagination" v-if="total != 0">
            <el-pagination layout="prev, pager, next, jumper" :total="total" :page-size="pageSize" :current-page="currentPage" @current-change="changePage"></el-pagination>
        </div>
        <div v-if="isApproveDialogVisible">
            <approve-dialog :approve-states="approveStates" :approve-info="approveInfo" @close-approve-dialog="closeApproveDialog" @approve-complete="approveComplete"></approve-dialog>
        </div>
        <div v-if="isViewDialogVisible">
            <json-diff :json-diff-info="jsonDiffInfo" @close-dialog="closeViewContentDialog"></json-diff>
        </div>
    </div>
</template>

<script>
import ApproveDialog from './approve_dialog/approve_dialog';

export default {
  data() {
    return {
      loading: false,
      isViewDialogVisible: false,
      jsonDiffInfo: {},
      isApproveDialogVisible: false,
      approveStates: [{
        key: '通过',
        value: '1',
      }, {
        key: '不通过',
        value: '2',
      }],
      approveInfo: {},
      total: 0,
      pageSize: 10,
      currentPage: Number(this.$route.query.currentPage) || 1,
      approveList: [],
      states: [{
        value: '0',
        label: '待审核',
      }, {
        value: '1',
        label: '已上线',
      }, {
        value: '2',
        label: '审核不通过',
      }, {
        value: '3',
        label: '未提交',
      }],
      selectedState: this.$route.query.state || '',
      conditionForSearch: this.$route.query.condition || '',
      multipleSelection: [],
    };
  },
  methods: {
    handleBatchApprove() {
      if (this.multipleSelection.length <= 0) {
        this.$message.errorMessage('请选择要审批的模板！！！');
      } else {
        const selectedApproveTemplateArray = [];
        this.multipleSelection.forEach((element) => {
          selectedApproveTemplateArray.push(element.id);
        });
        const selectedApproveTemplateStr = selectedApproveTemplateArray.join(',');
        const setBatchApproveInfo = {
          ids: selectedApproveTemplateStr,
          state: this.approveStates[0].value,
          note: '',
        };
        this.approveInfo = JSON.parse(JSON.stringify(setBatchApproveInfo));
        this.isApproveDialogVisible = true;
      }
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    handleView(row) {
      this.loading = true;
      this.$http.get(`/index_template/id.json?templateId=${row.relateId}`).then((data) => {
        this.jsonDiffInfo.left = data.lastContent;
        this.jsonDiffInfo.right = data.content;
        this.isViewDialogVisible = true;
      })
      .finally(() => {
        this.loading = false;
      });
    },
    closeViewContentDialog() {
      this.isViewDialogVisible = false;
    },
    handleApprove(row) {
      const setApproveInfo = {
        ids: row.id,
        state: this.approveStates[0].value,
        note: '',
      };
      this.approveInfo = JSON.parse(JSON.stringify(setApproveInfo));
      this.isApproveDialogVisible = true;
    },
    approveComplete() {
      this.isApproveDialogVisible = false;
      this.refreshPage();
    },
    closeApproveDialog() {
      this.isApproveDialogVisible = false;
    },
    changePage(currentPage) {
      this.currentPage = currentPage;
      this.toPage();
    },
    refreshPage() {
      this.init();
    },
    toPage() {
      this.$router.push({ path: this.$routermapper.GetPath('authorityManageAdministrator'), query: { currentPage: this.currentPage, state: this.selectedState, condition: this.conditionForSearch } });
    },
    getApproveList() {
      const params = {
        currentPage: Number(this.$route.query.currentPage) || 1,
        pageSize: this.pageSize,
        state: this.$route.query.state || '',
        conditions: this.$route.query.condition || '',
      };
      return this.$http.get('/approve/approve/page.json', params).then((data) => {
        this.total = data.total;
        this.approveList = data.list;
      });
    },
    init() {
      this.loading = true;
      Promise.all([this.getApproveList()]).then()
      .finally(() => {
        this.loading = false;
      });
    },
  },
  created() {
    this.init();
  },
  components: {
    'approve-dialog': ApproveDialog,
  },
  watch: {
    $route: 'init',
  },
  filters: {
    translateStat(data) {
      const APPROVE_STATUS = { 0: '待审核', 1: '已上线', 2: '审核不通过', 3: '未提交' };
      return APPROVE_STATUS[data];
    },
  },
};
</script>
