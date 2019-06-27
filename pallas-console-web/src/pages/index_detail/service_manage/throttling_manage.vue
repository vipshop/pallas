<template>
    <div class="my-tab-content">
        <div class="template_content">
          <div style="padding-bottom: 10px;"><i class="fa fa-th-list"></i>模板限流配置</div>
          <el-table :data="templateList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
            <el-table-column label="模板名称" show-overflow-tooltip>
                <template scope="scope">{{scope.row.templateName}}</template>
            </el-table-column>
            <el-table-column label="单台PS最大QPS（0表示不限流）">
              <template scope="scope">
                <el-input-number v-model="scope.row.threshold" :min="0" :max="100000"></el-input-number>
              </template>
            </el-table-column>
            <el-table-column label="最大预存流量秒数（限流器参数）">
              <template scope="scope">
                <el-input-number v-model="scope.row.maxBurstSecs" :min="1" :max="5"></el-input-number>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120px">
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
  props: ['templateList'],
  data() {
    return {
      loading: false,
      indexId: this.$route.query.indexId,
    };
  },
  methods: {
    handleUpdate(row) {
      const params = {
        indexId: this.indexId,
        templateName: row.templateName,
        threshold: row.threshold,
        maxBurstSecs: row.maxBurstSecs,
      };
      this.loading = true;
      this.$http.post('/index_template/update.json', params).then(() => {
        this.$message.successMessage('更新成功', () => {
          this.$emit('refresh-template');
        });
      })
      .finally(() => {
        this.loading = false;
      });
    },
  },
};

</script>

<style type="text/css">
</style>
