<template>
    <div class="my-tab-content" v-loading="loading" element-loading-text="请稍等···">
        <div class="content">
            <div class="data-table-filter">
                <div class="pull-left">
                    <el-button type="primary" icon="delete" @click="handleDelete">删除索引动态(3月前)</el-button>
                </div>
                <div class="pull-right">
                    <el-form :inline="true" class="demo-form-inline">
                        <el-form-item>
                            <el-select v-model="selectedType" @change="getOperationList">
                                <el-option label="全部事件" value=""></el-option>
                                <el-option v-for="item in typeList" :label="item.value" :value="item.value" :key="item.value"></el-option>
                            </el-select>
                        </el-form-item>
                        <el-form-item>
                            <el-select v-model="filterVersion" filterable allow-create @change="getOperationList">
                                <el-option label="全部版本" value=""></el-option>
                                <el-option v-for="item in versionIdList" :label="item.value" :value="item.value" :key="item.value"></el-option>
                            </el-select>
                        </el-form-item>
                        <el-date-picker
                          v-model="timeRange"
                          type="datetimerange"
                          @change="getOperationList"
                          placeholder="选择时间范围">
                        </el-date-picker>
                        <el-form-item>
                            <el-button type="primary" icon="search" @click="getOperationList">查询</el-button>
                        </el-form-item>
                    </el-form>
                </div>
            </div>
        </div>
        <div class="content">
          <div v-if="operationList.length === 0" class="empty-operation">暂无数据</div>
          <Timeline v-else>
              <Timeline-item v-for="op in operationList" :key="op.id">
                  <div slot="date" style="margin-top: -8px;">
                      {{op.operationTime | formatOnlyDate}}<br/>{{op.operationTime | formatOnlyTime}}
                  </div>
                  <div slot="title" style="margin-top: -8px;">
                      {{op.eventType}}<br/>{{op.eventName}}
                  </div>
                  <div slot="content" class="operation-content">
                      <el-row :gutter="20">
                          <el-col :span="2">操作员：</el-col>
                          <el-col :span="9">{{op.operator || '无'}}</el-col>
                          <el-col :span="2">版本号：</el-col>
                          <el-col :span="9">{{op.versionId || '无'}}</el-col>
                          <div v-if="op.isShowIcon">
                              <el-col v-show="!op.isFold" :span="2"><div align="right"><a @click="op.isFold = !op.isFold"><i class="el-icon-arrow-down"></i></a></div></el-col>
                              <el-col v-show="op.isFold" :span="2"><div align="right"><a @click="op.isFold = !op.isFold"><i class="el-icon-arrow-right"></i></a></div></el-col>
                          </div>
                      </el-row>
                      <div :class="[op.isFold ? 'operation-content-fold' : 'operation-content-unfold']">
                          <el-row>
                              <el-col :span="2">操作内容：</el-col>
                              <el-col :span="22" style="word-wrap: break-word">{{op.eventDetail || '无'}}</el-col>
                          </el-row>
                      </div>
                  </div>
              </Timeline-item>
          </Timeline>
          <div align="right" class="dynamic-pagination" v-if="operationData.total != 0">
            <el-pagination layout="prev, pager, next" :total="operationData.total" :page-size="pageSize" :current-page="currentPage" @current-change="changePage"></el-pagination>
          </div>
        </div>
        <div v-if="isDynamicInfoVisible">
           <dynamic-info-dialog :dynamic-operation="dynamicOperation" :dynamic-info-title="dynamicInfoTitle" :dynamic-info="dynamicInfo" :typeList="typeList" :versionIdList="versionIdList" @close-dialog="closeDialog" @operate-close-dialog="operateCloseDialog"></dynamic-info-dialog>
        </div>
    </div>
</template>

<script>
import DynamicManageDialog from './dynamic_manage_dialog/dynamic_manage_dialog';

export default {
  data() {
    return {
      loading: false,
      indexId: this.$route.query.indexId,
      pageSize: 10,
      currentPage: 1,
      operationData: {},
      operationList: [],
      selectedType: '',
      filterVersion: '',
      timeRange: '',
      typeList: [{
        value: '索引事件',
      }, {
        value: '版本事件',
      }, {
        value: '同步事件',
      }, {
        value: '模板事件',
      }],
      dynamicOperation: '',
      dynamicInfoTitle: '',
      isDynamicInfoVisible: false,
      dynamicInfo: {},
      dynamicDeleteInfo: {
        indexId: this.$route.query.indexId,
        indexName: this.$route.query.indexName,
        filterVersion: '',
        selectedType: '',
        eventName: '',
        timeRange: 90,
      },
      versionIdList: [],
    };
  },
  methods: {
    initVersionIdList() {
      const params = {
        currentPage: 1,
        pageSize: 100,
        indexId: this.indexId,
      };
      this.loading = true;
      this.$http.get('/index/version/page.json', params).then((data) => {
        if (data.list.length > 0) {
          data.list.forEach((element) => {
            let item = {};
            if (element.isUsed === true) {
              item = { label: `${element.id}(当前启用版本)`, value: element.id };
            } else {
              item = { label: element.id, value: element.id };
            }
            this.versionIdList.push(item);
          });
        }
      })
      .finally(() => {
        this.loading = false;
      });
    },
    getOperationList() {
      if (JSON.stringify(this.timeRange) === '[null,null]') {
        this.timeRange = '';
      }
      const params = {
        currentPage: this.currentPage,
        pageSize: this.pageSize,
        indexId: this.indexId,
        selectedType: this.selectedType,
        filterVersion: this.filterVersion,
        timeRange: this.timeRange,
      };

      return this.$http.post('/index/dynamic/page.json', params).then((data) => {
        this.operationData = data;
        this.operationList = data.list.map((obj) => {
          const rObj = {};
          rObj.id = obj.id;
          rObj.eventType = obj.eventType;
          rObj.eventName = obj.eventName;
          rObj.eventDetail = obj.eventDetail;
          rObj.endTime = obj.endTime;
          rObj.operationTime = obj.operationTime;
          rObj.operator = obj.operator;
          rObj.versionId = obj.versionId;
          if (obj.eventDetail.length > 500) {
            rObj.isFold = true;
            rObj.isShowIcon = true;
          } else {
            rObj.isFold = false;
            rObj.isShowIcon = false;
          }
          return rObj;
        });
      });
    },
    handleDelete() {
      this.isDynamicInfoVisible = true;
      this.dynamicOperation = 'delete';
      this.dynamicInfoTitle = '删除数据(3月前)';
      this.dynamicInfo = JSON.parse(JSON.stringify(this.dynamicDeleteInfo));
    },
    closeDialog() {
      this.isDynamicInfoVisible = false;
    },
    operateCloseDialog() {
      this.isDynamicInfoVisible = false;
      this.init();
    },
    changePage(currentPage) {
      this.currentPage = currentPage;
      this.init();
    },
    init() {
      this.loading = true;
      Promise.all([this.getOperationList()]).then()
      .finally(() => {
        this.loading = false;
      });
    },
  },
  components: {
    'dynamic-info-dialog': DynamicManageDialog,
  },
  created() {
    this.init();
    this.initVersionIdList();
  },
};

</script>

<style type="text/css">
.content {
    margin: 0 20px 20px;
}
.dynamic-pagination {
    padding-right: 5px;
}
.operation-content a {
    cursor: pointer;
}
.operation-content .operation-content-fold {
    height: 21px;
    overflow: hidden;
}
.operation-content .operation-content-unfold {
    height: initial;
}
.empty-operation {
    position: relative;
    text-align: center;
    height: 60px;
    border: 1px solid gray;
    margin-top: 20px;
    line-height: 60px;
    color: #5e7382;
    font-size: 14px;
}
</style>
