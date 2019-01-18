<template>
    <div class="my-tab-content">
        <div class="content" v-show="isAllPrivilege">
            <el-button type="primary" icon="plus" @click="handleAdd">新增</el-button>
        </div>
        <div class="content">
            <template>
                <el-table :data="cronDeleteList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
                    <el-table-column label="id" prop="id" width="40px"></el-table-column>
                    <el-table-column label="版本id" prop="versionId" width="100px"></el-table-column>
                    <el-table-column label="scroll_size" prop="scrollSize" width="100px"></el-table-column>
                    <el-table-column label="cron表达式" prop="cron" width="140px"></el-table-column>
                    <el-table-column label="更新时间" prop="updateTime" min-width="100">
                        <template scope="scope">{{scope.row.updateTime | formatDate}}</template>
                    </el-table-column>
                    <el-table-column label="是否启用" prop="isUsed" width="100px">
                        <template scope="scope"> 
                            <el-tag :type="scope.row.isSyn ? 'success' : 'danger'" close-transition>{{scope.row.isSyn}}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="300px">
                      <template scope="scope">
                          <el-tooltip content="dsl" placement="top">
                              <el-button type="text" @click="getDsl(scope.row)"><i class="fa fa-file-code-o"></i></el-button>
                          </el-tooltip>
                          <el-tooltip content="执行查询" placement="top" v-if="isAllPrivilege">
                              <el-button type="text" @click="executeSearchDsl(scope.row)"><i class="fa fa-search"></i></el-button>
                          </el-tooltip>
                          <el-tooltip content="停用" placement="top" v-if="isAllPrivilege" v-show="scope.row.isSyn">
                              <el-button type="text" @click="handleDisableAndDeleteJob(scope.row)"><i class="fa fa-level-down"></i></el-button>
                          </el-tooltip>
                          <el-tooltip content="启用" placement="top" v-if="isAllPrivilege" v-show="!scope.row.isSyn">
                              <el-button type="text" @click="handleCreateAndEnableJob(scope.row)"><i class="fa fa-level-up"></i></el-button>
                          </el-tooltip>
                          <el-tooltip content="立即执行" placement="top" v-if="isAllPrivilege" v-show="scope.row.isSyn">
                              <el-button type="text" @click="handleRunAtOnce(scope.row)"><i class="fa fa-play"></i></el-button>
                          </el-tooltip>
                          <el-tooltip content="编辑" placement="top" v-if="isAllPrivilege" v-show="!scope.row.isSyn">
                              <el-button type="text" @click="handleEdit(scope.row)"><i class="fa fa-pencil-square-o"></i></el-button>
                          </el-tooltip>
                          <el-tooltip content="删除" placement="top" v-if="isAllPrivilege" v-show="!scope.row.isSyn">
                              <el-button type="text" @click="handleDelete(scope.row)"><i class="fa fa-trash"></i></el-button>
                          </el-tooltip>
                      </template>
                    </el-table-column>
                </el-table>
            </template>
        </div>
        <div v-if="showDsl">
            <json-content-dialog :title="viewInfoTitle" :content="viewInfo" @close-dialog="closeDialog"></json-content-dialog>
        </div>
        <div v-if="isCronDeleteVisible">
            <crondelete-dialog :index-id="indexId" :crondelete-operation="crondeleteOperation" :crondelete-title="crondeleteTitle" :crondelete-info="crondeleteInfo" @close-dialog="closeDialog" @operate-close-dialog="operateCloseDialog"></crondelete-dialog>
        </div>
    </div>
</template>

<script>
import CronDeleteDialog from './cron_delete_dialog/cron_delete_dialog';

export default {
  data() {
    return {
      loading: false,
      indexId: this.$route.query.indexId,
      indexName: this.$route.query.indexName,
      isAllPrivilege: false,
      showDsl: false,
      isCronDeleteVisible: false,
      crondeleteOperation: '',
      crondeleteTitle: '',
      viewInfoTitle: '',
      viewInfo: '',
      cronDeleteList: [],
      crondeleteInfo: {},
      crondeleteAddInfo: {
        versionId: null,
        cron: '',
        dsl: '',
        scrollSize: 1000,
        id: '',
      },
    };
  },
  methods: {
    getCronDeleteList() {
      const params = {
        indexId: this.indexId,
      };
      this.loading = true;
      this.$http.get(`/crondelete/find_by_index.json?indexId=${params.indexId}`).then((data) => {
        this.isAllPrivilege = data.allPrivilege;
        this.cronDeleteList = data.list;
      })
      .finally(() => {
        this.loading = false;
      });
    },
    getDsl(row) {
      this.viewInfo = JSON.stringify(JSON.parse(row.dsl), undefined, 2);
      this.showDsl = true;
      this.viewInfoTitle = 'delete_by_query dsl';
    },
    executeSearchDsl(row) {
      const params = {
        id: row.id,
      };
      this.loading = true;
      this.$http.get(`/crondelete/query_dsl.json?id=${params.id}`).then((data) => {
        this.viewInfo = JSON.stringify(JSON.parse(data), undefined, 2);
        this.showDsl = true;
        this.viewInfoTitle = 'dsl查询结果';
      })
      .finally(() => {
        this.loading = false;
      });
    },
    closeDialog() {
      this.showDsl = false;
      this.isCronDeleteVisible = false;
    },
    operateCloseDialog() {
      this.isCronDeleteVisible = false;
      this.refreshPage();
    },
    refreshPage() {
      this.getCronDeleteList();
    },
    handleAdd() {
      this.isCronDeleteVisible = true;
      this.crondeleteTitle = '新增';
      this.crondeleteOperation = 'add';
      this.crondeleteInfo = JSON.parse(JSON.stringify(this.crondeleteAddInfo));
    },
    handleEdit(row) {
      this.isCronDeleteVisible = true;
      this.crondeleteTitle = '修改';
      this.crondeleteOperation = 'edit';
      this.crondeleteInfo = row;
    },
    handleRunAtOnce(row) {
      this.$message.confirmMessage(`确定立刻执行作业${this.indexName}_${this.indexId}_crondelete_${row.id}吗? `, () => {
        this.loading = true;
        this.$http.post('/crondelete/run.json', { id: row.id }).then(() => {
          this.$message.successMessage(`已通知saturn执行，请前往saturn查看作业：${this.indexName}_${this.indexId}_crondelete_${row.id} 的执行结果`, () => {
            this.getCronDeleteList();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    handleDelete(row) {
      this.$message.confirmMessage(`确定删除${row.id}吗?`, () => {
        this.loading = true;
        this.$http.post('/crondelete/delete.json', { id: row.id }).then(() => {
          this.$message.successMessage('删除成功', () => {
            this.getCronDeleteList();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    handleDisableAndDeleteJob(row) {
      this.$message.confirmMessage(`确定禁用${row.id}吗?`, () => {
        this.loading = true;
        this.$http.post('/crondelete/disable_and_delete_job.json', { id: row.id }).then(() => {
          this.$message.successMessage('禁用成功', () => {
            this.getCronDeleteList();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    handleCreateAndEnableJob(row) {
      this.$message.confirmMessage(`确定启用${row.id}吗?`, () => {
        this.loading = true;
        this.$http.post('/crondelete/create_and_enable_job.json', { id: row.id }).then(() => {
          this.$message.successMessage('启用成功', () => {
            this.getCronDeleteList();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
  },
  filters: {
    translateIsUsed(data) {
      const IS_ENABLED = { true: '是', false: '否' };
      return IS_ENABLED[data];
    },
  },
  components: {
    'crondelete-dialog': CronDeleteDialog,
  },
  created() {
    this.getCronDeleteList();
  },
};

</script>
