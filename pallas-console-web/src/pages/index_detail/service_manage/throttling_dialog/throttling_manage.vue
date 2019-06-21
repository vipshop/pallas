<template>
    <div class="my-tab-content" v-loading="loading" element-loading-text="请稍等···">
        <div class="content">
            <div class="data-table-filter">
                <el-form :inline="true" class="demo-form-inline">

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
  },
  created() {
    this.getIndexInfo();
  },
};
</script>
