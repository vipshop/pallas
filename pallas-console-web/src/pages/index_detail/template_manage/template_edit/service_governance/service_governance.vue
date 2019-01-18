<template>
    <div class="my-tab-content" v-loading="loading" element-loading-text="请稍等···">
        <div class="content">
            <div class="index-timeout-retry">
                <i class="fa fa-exclamation-circle"></i>
                当前索引级别超时为 <span>{{indexInfo.timeout}}</span> 毫秒，重试为 <span>{{indexInfo.retry}}</span> 次。（若模板超时时间为0毫秒，则以当前索引的超时时间和重试为准。）
            </div>
            <div class="data-table-filter">
                <el-form :inline="true" class="demo-form-inline">
                    <el-form-item label="超时时间(毫秒,需>=50ms)">
                        <el-input-number placeholder="超时时间(毫秒)" v-model="templateInfo.timeout" :min="0"></el-input-number>
                    </el-form-item>
                    <el-form-item label="重试次数">
                        <el-input-number placeholder="重试次数" v-model="templateInfo.retry" :min="0"></el-input-number>
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="handleUpdate"><i class="fa fa-refresh"></i>更新</el-button>
                    </el-form-item>
                </el-form>
            </div>
        </div>
    </div>
</template>

<script>
export default {
  props: ['indexId', 'templateInfo'],
  data() {
    return {
      loading: false,
      indexInfo: {},
    };
  },
  methods: {
    getIndexInfo() {
      this.loading = true;
      this.$http.get(`/index/id.json?indexId=${this.indexId}`).then((data) => {
        this.indexInfo = JSON.parse(JSON.stringify(data));
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleUpdate() {
      const params = {
        indexId: this.indexId,
        templateName: this.templateInfo.templateName,
        timeout: this.templateInfo.timeout,
        retry: this.templateInfo.retry,
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
  created() {
    this.getIndexInfo();
  },
};
</script>
<style scoped>
.index-timeout-retry {
    margin: 10px 0 20px;
    font-size: 15px;
}
.index-timeout-retry span {
    color: #32cd32;
}
</style>
