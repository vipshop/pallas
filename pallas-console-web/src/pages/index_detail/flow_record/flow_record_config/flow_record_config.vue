<template>
    <div class="my-tab-content">
        <div class="content">
            <el-button type="primary" icon="plus" @click="handleAdd" :disabled="!isAllPrivilege">新增规则</el-button>
        </div>
        <div class="content">
            <template>
                <el-table :data="flowRecordConfigList" border style="width: 100%" highlight-current-row @cell-click="cellClick" v-loading="loading" element-loading-text="请稍等···">
                    <el-table-column label="规则id" prop="id" width="70px"></el-table-column>
                    <el-table-column label="目标模板" prop="template.templateName" min-width="110">
                        <template scope="scope">
                            <div v-if="scope.row.template === null">全部</div>
                            <div v-else>{{scope.row.template.templateName}}</div>
                        </template>
                    </el-table-column>
                    <el-table-column label="采集开始时间" prop="startTime" min-width="110">
                        <template scope="scope">{{scope.row.startTime | formatDate}}</template>
                    </el-table-column>
                    <el-table-column label="采集结束时间" prop="endTime" min-width="110">
                        <template scope="scope">{{scope.row.endTime | formatDate}}</template>
                    </el-table-column>
                    <el-table-column label="抽样系数" prop="sampleRate" min-width="60"></el-table-column>
                    <el-table-column label="目标采集数" prop="limit" min-width="70"></el-table-column>
                    <el-table-column label="最近更新人" min-width="110">
                        <template scope="scope">{{scope.row.createUser || '-'}}</template>
                    </el-table-column>
                    <el-table-column label="最近更新时间" prop="endTime" min-width="110">
                        <template scope="scope">{{scope.row.updateTime | formatDate}}</template>
                    </el-table-column>
                    <el-table-column label="状态" prop="isUsed" min-width="50">
                        <template scope="scope">
                            <el-tag :type="scope.row.isEnable ? 'success' : 'danger'" close-transition>{{scope.row.isEnable || false | translateIsEnable}}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="80">
                        <template scope="scope">
                            <el-dropdown trigger="click">
                              <span class="el-dropdown-link">
                                操作<i class="el-icon-caret-bottom el-icon--right"></i>
                              </span>
                                <el-dropdown-menu class="dropdown-operation" slot="dropdown">
                                    <el-dropdown-item v-if="isAllPrivilege" v-show="!scope.row.isEnable"><a @click="enableConfig(scope.row)"><span><i class="fa fa-hand-o-right"></i>启用</span></a></el-dropdown-item>
                                    <el-dropdown-item v-if="isAllPrivilege" v-show="scope.row.isEnable"><a @click="disableConfig(scope.row)"><span><i class="fa fa-stop-circle-o"></i>禁用</span></a></el-dropdown-item>
                                    <el-dropdown-item v-if="isAllPrivilege" v-show="!scope.row.isEnable"><a @click="handleEdit(scope.row, 'edit')"><span><i class="fa fa-pencil-square-o"></i>编辑</span></a></el-dropdown-item>
                                    <el-dropdown-item v-if="isAllPrivilege" v-show="!scope.row.isEnable"><a @click="handleDelete(scope.row)"><span><i class="fa fa-trash"></i>删除</span></a></el-dropdown-item>
                                </el-dropdown-menu>
                            </el-dropdown>
                        </template>
                    </el-table-column>
                </el-table>
            </template>
            <div class="my-pagination" v-if="total != 0">
                <el-pagination layout="prev, pager, next" :total="total" :page-size="pageSize" :current-page="currentPage" @current-change="changePage"></el-pagination>
            </div>
            <div v-if="isConfigInfoVisible">
                <config-info-dialog :config-operation="configOperation" :config-info-title="configInfoTitle" :config-info="configInfo" :templates="templates" @close-dialog="closeDialog" @operate-close-dialog="operateCloseDialog"></config-info-dialog>
            </div>
        </div>
    </div>
</template>

<script>
  import ConfigInfoDialog from './config_info_dialog';

  export default {
    data() {
      return {
        indexId: this.$route.query.indexId,
        loading: false,
        activeName: 'first',
        flowRecordConfigList: [],
        indexName: this.$route.query.indexName,
        isMetaDataNull: false,
        pageSize: 10,
        configOperation: '',
        configInfoTitle: '',
        total: 0,
        currentPage: 1,
        configInfo: {},
        templates: [],
        isConfigInfoVisible: false,
        isAllPrivilege: false,
      };
    },
    methods: {
      cellClick(row) {
        const params = {
          configId: row.id,
          indexId: row.indexId,
        };
        this.$emit('get-flow-export', params);
      },
      init() {
        this.getFlowRecordConfigList(1);
        this.getTemplateByIndex();
      },
      changePage(currentPage) {
        this.getFlowRecordConfigList(currentPage);
      },
      getTemplateByIndex() {
        const params = {
          indexId: this.$route.query.indexId,
        };
        this.$http.get(`/record/index_template/list.json?indexId=${params.indexId}`).then((data) => {
          this.templates = data;
        });
      },
      handleAdd() {
        this.isConfigInfoVisible = true;
        this.configInfoTitle = '新增规则';
        this.configOperation = 'add';
        const configAddInfo = {
          templateId: '',
          startTime: '',
          endTime: '',
          indexId: this.$route.query.indexId,
        };
        this.configInfo = JSON.parse(JSON.stringify(configAddInfo));
      },
      handleDelete(row) {
        this.$message.confirmMessage(`确定删除规则${row.id}吗，删除会同时停止此规则相关正在采集的作业?`, () => {
          this.loading = true;
          this.$http.post('/record/flow_record_config/delete.json', { indexId: this.indexId, configId: row.id }).then(() => {
            this.$message.successMessage('删除规则成功', () => {
              this.getFlowRecordConfigList(1);
            });
          })
          .finally(() => {
            this.loading = false;
          });
        });
      },
      handleEdit(row) {
        this.loading = true;
        this.$http.get(`/record/flow_record_config/id.json?configId=${row.id}`).then((data) => {
          const configEditData = data;
          this.isConfigInfoVisible = true;
          this.configInfoTitle = '编辑规则';
          this.configOperation = 'edit';
          const configEditInfo = {
            id: row.id,
            templateId: configEditData.templateId,
            startTime: configEditData.startTime,
            endTime: configEditData.endTime,
            indexId: this.$route.query.indexId,
            sampleRate: JSON.stringify(configEditData.sampleRate),
            limit: JSON.stringify(configEditData.limit),
            note: configEditData.note || '',
          };
          this.configInfo = JSON.parse(JSON.stringify(configEditInfo));
        })
        .finally(() => {
          this.loading = false;
        });
      },
      enableConfig(row) {
        this.$message.confirmMessage(`确定启用规则${row.id}吗?`, () => {
          this.loading = true;
          this.$http.post('/record/flow_record_config/enable.json', { indexId: this.indexId, configId: row.id }).then(() => {
            this.$message.successMessage('启用规则成功', () => {
              this.getFlowRecordConfigList(1);
            });
          })
          .finally(() => {
            this.loading = false;
          });
        });
      },
      disableConfig(row) {
        this.$message.confirmMessage(`确定禁用规则${row.id}吗，禁用会同时停止此规则相关正在采集的作业?`, () => {
          this.loading = true;
          this.$http.post('/record/flow_record_config/disable.json', { indexId: this.indexId, configId: row.id }).then(() => {
            this.$message.successMessage('禁用规则成功', () => {
              this.getFlowRecordConfigList(1);
            });
          })
          .finally(() => {
            this.loading = false;
          });
        });
      },
      getFlowRecordConfigList(_currentPage) {
        const params = {
          currentPage: _currentPage,
          pageSize: this.pageSize,
          indexId: this.$route.query.indexId || '',
        };
        this.loading = true;
        this.$http.get('/record/flow_record_config/page.json', params).then((data) => {
          this.flowRecordConfigList = data.list;
          this.total = data.total;
          this.isAllPrivilege = data.allPrivilege;
        })
        .finally(() => {
          this.loading = false;
        });
      },
      closeDialog() {
        this.isConfigInfoVisible = false;
      },
      operateCloseDialog() {
        this.isConfigInfoVisible = false;
        this.getFlowRecordConfigList(1);
      },
    },
    filters: {
      translateIsEnable(data) {
        const IS_ENABLE = { true: '已启用', false: '待启用' };
        return IS_ENABLE[data];
      },
    },
    components: {
      'config-info-dialog': ConfigInfoDialog,
    },
    created() {
      this.init();
    },
    watch: {
      $route: 'getFlowRecordConfigList',
    },
  };

</script>
