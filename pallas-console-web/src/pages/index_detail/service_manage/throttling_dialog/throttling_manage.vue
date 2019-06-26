<template>
    <div class="my-tab-content" :style="{ 'height': temPanelHeight }">
        <div class="template_content" v-loading="loading" element-loading-text="请稍等···">
          <el-table :data="templateList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
            <el-table-column label="模板名称" min-width="380px">
                <template scope="scope">{{scope.row.templateName}}</template>
            </el-table-column>
            <el-table-column label="单台PS最大QPS（0表示不限流）" width="380px">
              <template scope="scope">
                <el-input-number :placeholder="throttlingThreshold" v-model="scope.row.threshold" :min="0" :max="100000"></el-input-number>
              </template>
            </el-table-column>
            <el-table-column label="最大预存流量秒数（限流器参数）" width="380px">
              <template scope="scope">
                <el-input-number :placeholder="maxBurstSeconds" v-model="scope.row.maxBurstSecs" :min="1" :max="5"></el-input-number>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="370px">
              <template scope="scope">
                <el-button type="primary" @click="handleUpdate(scope.row)"><i class="fa fa-refresh"></i>更新</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
    </div>
</template>


<script>
export default {
  data() {
    return {
      loading: false,
      indexId: this.$route.query.indexId,
      indexName: this.$route.query.indexName,
      indexInfo: {},
      isAllPrivilege: false,
      templateInfo: {},
      templateList: [],
      temPanelHeight: {
        height: document.body.clientHeight - 210,
      },
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
    handleCallback(data) {
      this.templateList = data.list;
    },
    getTemplateList() {
      return this.$http.get(`/index_template/list.json?indexId=${this.indexId}`).then((data) => {
        this.handleCallback(data);
      });
    },
    init() {
      this.loading = true;
      Promise.all([this.getTemplateList(), this.getClusters()]).then(() => {
        this.loading = false;
      });
    },
    handleUpdate(row) {
      const params = {
        indexId: this.indexId,
        templateName: row.templateName,
        threshold: row.threshold,
        maxBurstSecs: row.maxBurstSecs,
      };
      this.loading = true;
      this.$http.post('/index_template/update.json', params).then(() => {
        this.$message.successMessage('更新成功');
      })
      .finally(() => {
        this.loading = false;
      });
    },
  },
  mounted() {
    this.temPanelHeight = document.body.clientHeight - 210;
    const that = this;
    window.onresize = function temp() {
      that.temPanelHeight = document.body.clientHeight - 210;
    };
  },
  created() {
    this.getIndexInfo();
    this.init();
  },
};

</script>

<style type="text/css">
.template_content {
  display: table;
  width: 100%;
}
.template_tree {
  padding-right: 30px;
  display: table-cell;
  width: 245px;
}
.template-warning {
  text-align: center;
  color: red;
  font-size: larger;
  font-weight: bolder;
  display: table-cell;
  vertical-align: middle;
}
.template-warning i {
  padding-right: 10px;
}
.template-body {
  display: table-cell;
}
</style>
