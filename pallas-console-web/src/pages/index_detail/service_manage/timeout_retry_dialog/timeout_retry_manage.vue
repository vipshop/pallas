<template>
  <div class="my-tab-content" v-loading="loading" element-loading-text="请稍等···">
    <el-collapse v-model="activeNames">
      <el-collapse-item name="1" :v-show="true">
        <template slot="title">
            索引级别配置
        </template>
        <div class="data-table-filter">
          <el-form :inline="true" class="demo-form-inline">
            <el-form-item label="超时时间(毫秒,需>=50ms)">
              <el-input-number placeholder="超时时间(毫秒)" v-model="indexInfo.timeout" :min="0"></el-input-number>
            </el-form-item>
            <el-form-item label="重试次数(上限1次)">
              <el-input-number placeholder="重试次数" v-model="indexInfo.retry" :min="0" :max="1"></el-input-number>
            </el-form-item>
            <el-form-item label="慢查询阈值(毫秒)">
                <el-input-number placeholder="慢查询阈值(毫秒)" v-model="indexInfo.slowerThan" :min="0"></el-input-number>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdate"><i class="fa fa-refresh"></i>更新</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-item>
      <el-collapse-item title="模板级别配置" name="2">
        <template slot="title">
            模板级别配置（若模板超时时间为0毫秒，则以当前索引的超时时间和重试为准。）
        </template>
        <el-table :data="templateList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
          <el-table-column label="模板名称" min-width="380px">
              <template scope="scope">{{scope.row.templateName}}</template>
          </el-table-column>
          <el-table-column label="超时时间(毫秒,需>=50ms)" width="380px">
            <template scope="scope">
              <el-input-number :placeholder="timeout" v-model="scope.row.timeout" :min="0"></el-input-number>
            </template>
          </el-table-column>
          <el-table-column label="重试次数(上限1次)" width="380px">
            <template scope="scope">
              <el-input-number :placeholder="retry" v-model="scope.row.retry" :min="0" :max="1"></el-input-number>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="370px">
            <template scope="scope">
              <el-button type="primary" @click="handleTemplateUpdate(scope.row)"><i class="fa fa-refresh"></i>更新</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>
export default {
  data() {
    return {
      loading: false,
      indexId: this.$route.query.indexId,
      indexInfo: {},
      isAllPrivilege: false,
      templateInfo: {},
      templateList: [],
      temPanelHeight: {
        height: document.body.clientHeight - 210,
      },
      activeNames: ['1', '2'],
    };
  },
  methods: {
    getIndexInfo() {
      this.loading = true;
      this.$http.get(`/index/id.json?indexId=${this.indexId}`).then((data) => {
        this.indexInfo = JSON.parse(JSON.stringify(data));
        if (this.indexInfo.slowerThan == null) {
          this.$set(this.indexInfo, 'slowerThan', '200');
        }
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleUpdate() {
      const params = {
        indexId: this.indexId,
        timeout: this.indexInfo.timeout,
        retry: this.indexInfo.retry,
        slowerThan: this.indexInfo.slowerThan,
      };
      this.loading = true;
      this.$http.post('/index/update/timeout_retry.json', params).then(() => {
        this.$message.successMessage('更新成功');
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleCallback(data) {
      this.templateList = data.list;
    },
    getTemplateList() {
      return this.$http.get(`/index_template/list.json?indexId=${this.indexId}`).then((data) => {
        this.handleCallback(data);
      });
    },
    handleTemplateUpdate(row) {
      const params = {
        indexId: this.indexId,
        templateName: row.templateName,
        timeout: row.timeout,
        retry: row.retry,
      };
      this.loading = true;
      this.$http.post('/index_template/update.json', params).then(() => {
        this.$message.successMessage('更新成功');
      })
      .finally(() => {
        this.loading = false;
      });
    },
    init() {
      this.loading = true;
      Promise.all([this.getTemplateList(), this.getClusters()]).then(() => {
        this.loading = false;
      });
    },
  },
  created() {
    this.getIndexInfo();
    this.init();
  },
};
</script>
