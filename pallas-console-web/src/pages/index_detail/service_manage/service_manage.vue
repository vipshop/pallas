<template>
    <div class="my-tab-content">
        <el-tabs v-model="tabActive" v-loading="loading" element-loading-text="请稍等···">
            <el-tab-pane label="超时重试" name="timeout_retry">
                <timeout-retry-manage :template-list="templateList" @refresh-template="getTemplateList"></timeout-retry-manage>
            </el-tab-pane>
            <el-tab-pane label="限流配置" name="throttling">
                <throttling-manage :template-list="templateList" @refresh-template="getTemplateList"></throttling-manage>
            </el-tab-pane>
        </el-tabs>
    </div>
</template>

<script>
import TimeoutRetryManage from './timeout_retry_manage';
import ThrottlingManage from './throttling_manage';

export default {
  components: {
    'timeout-retry-manage': TimeoutRetryManage,
    'throttling-manage': ThrottlingManage,
  },
  data() {
    return {
      loading: false,
      indexId: this.$route.query.indexId,
      tabActive: 'timeout_retry',
      templateList: [],
    };
  },
  methods: {
    getTemplateList() {
      this.loading = true;
      this.$http.get(`/index_template/list.json?indexId=${this.indexId}`).then((data) => {
        this.templateList = data.list;
      })
      .finally(() => {
        this.loading = false;
      });
    },
  },
  created() {
    this.getTemplateList();
  },
};
</script>
