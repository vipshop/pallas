<template>
    <div class="my-tab-content" v-loading="loading" element-loading-text="请稍等···">
        <div class="content">
            <div class="data-table-filter">
                <el-form :inline="true" class="demo-form-inline">
                    <el-form-item label="超时时间(毫秒,需>=50ms)">
                        <el-input-number placeholder="超时时间(毫秒)" v-model="indexInfo.timeout" :min="0"></el-input-number>
                    </el-form-item>
                    <el-form-item label="重试次数">
                        <el-input-number placeholder="重试次数" v-model="indexInfo.retry" :min="0"></el-input-number>
                    </el-form-item>
                    <el-form-item label="慢查询阈值(毫秒)">
                        <el-input-number placeholder="慢查询阈值(毫秒)" v-model="indexInfo.slowerThan" :min="0"></el-input-number>
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
  data() {
    return {
      loading: false,
      indexId: this.$route.query.indexId,
      indexInfo: {},
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
  },
  created() {
    this.getIndexInfo();
  },
};
</script>
