<template>
    <div class="my-tab-content">
        <div class="content">
            <template>
                <el-table :data="flowRecordList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
                    <el-table-column label="记录id" prop="id" width="100px"></el-table-column>
                    <el-table-column label="采集规则ID" prop="configId" width="100px"></el-table-column>
                    <el-table-column label="最后更新时间" prop="updateTime" min-width="110">
                        <template scope="scope">{{scope.row.updateTime | formatDate}}</template>
                    </el-table-column>
                    <el-table-column label="抽样系数" prop="sampleRate" min-width="110"></el-table-column>
                    <el-table-column label="目标采集数" prop="limit" min-width="110"></el-table-column>
                    <el-table-column label="当前采集数" prop="total" min-width="110"></el-table-column>
                    <el-table-column label="采集进度" min-width="110">
                        <template scope="scope">{{scope.row.percentage}}%</template>
                    </el-table-column>
                    <el-table-column label="状态" prop="state" min-width="50">
                        <template scope="scope">
                            <el-tag :type="scope.row.state != 3 ? 'success' : 'danger'">{{flowRecordStateMap[scope.row.state]}}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" width="80">
                        <template scope="scope">
                            <el-dropdown trigger="click">
                              <span class="el-dropdown-link">
                                操作<i class="el-icon-caret-bottom el-icon--right"></i>
                              </span>
                              <el-dropdown-menu class="dropdown-operation" slot="dropdown">
                                <el-dropdown-item v-show="scope.row.total > 0" v-if="isAllPrivilege"><a @click="exportFlow(scope.row)"><span><i class="fa fa-file"></i>导出</span></a></el-dropdown-item>
                                <el-dropdown-item v-show="scope.row.state != 2 && scope.row.state != 3" v-if="isAllPrivilege"><a @click="stopRecord(scope.row)"><span><i class="fa fa-stop-circle-o"></i>终止</span></a></el-dropdown-item>
                                <el-dropdown-item v-show="scope.row.state == 2 || scope.row.state == 3" v-if="isAllPrivilege"><a @click="handleDelete(scope.row)"><span><i class="fa fa-trash"></i>删除</span></a></el-dropdown-item>
                              </el-dropdown-menu>
                            </el-dropdown>
                        </template>
                    </el-table-column>
                </el-table>
            </template>
            <div class="my-pagination" v-if="total > 0">
                <el-pagination layout="prev, pager, next" :total="total" :page-size="pageSize" @current-change="changePage"></el-pagination>
            </div>
        </div>
    </div>
</template>

<script>
export default {
  props: ['flowRecordExportParams'],
  data() {
    return {
      loading: false,
      flowRecordList: [],
      indexId: this.$route.query.indexId,
      indexName: this.$route.query.indexName,
      flowRecordStateMap: {
        0: '就绪',
        1: '正在采集',
        2: '已完成',
        3: '已终止',
        4: '已结束',
      },
      isAllPrivilege: false,
      pageSize: 10,
      total: 0,
      currentPage: 0,
    };
  },
  methods: {
    changePage(currentPage) {
      this.currentPage = currentPage;
      this.getFlowRecordList(currentPage);
    },
    getFlowRecordList(_currentPage) {
      const params = {
        ...this.flowRecordExportParams,
        currentPage: _currentPage,
        pageSize: this.pageSize,
      };
      this.loading = true;
      this.$http.get('/record/flow_record/page_by_config.json', params).then((data) => {
        this.flowRecordList = data.list;
        this.isAllPrivilege = data.allPrivilege;
        this.total = data.total;
      })
      .finally(() => {
        this.loading = false;
      });
    },
    stopRecord(row) {
      this.$message.confirmMessage(`确定停止采集${row.id}吗，停止会同时禁用相关规则?`, () => {
        this.loading = true;
        this.$http.post('/record/flow_record/stop.json', { indexId: this.indexId, recordId: row.id }).then(() => {
          this.$message.successMessage('停止采集成功', () => {
            this.getFlowRecordList(1);
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    handleDelete(row) {
      this.$message.confirmMessage(`确定要删除采集数据${row.id}吗，删除将清除相关记录数据?`, () => {
        this.loading = true;
        this.$http.post('/record/flow_record/delete.json', { indexId: this.indexId, recordId: row.id }).then(() => {
          this.$message.successMessage('删除采集数据成功', () => {
            this.getFlowRecordList(1);
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    refreshList() {
      this.interval = setInterval(() => {
        this.getFlowRecordList(1);
      }, 3000);
    },
    exportFlow(row) {
      window.location.href = `/pallas/record/flow_record/export.json?recordId=${row.id}`;
    },
  },
  filters: {
    translateIsEnable(data) {
      const IS_ENABLE = { true: '是', false: '否' };
      return IS_ENABLE[data];
    },
  },
  components: {
  },
  watch: {
    flowRecordExportParams: {
      handler() {
        this.getFlowRecordList(1);
      },
      deep: true,
    },
  },
};

</script>
