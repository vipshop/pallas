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
              <el-input-number placeholder="超时时间(毫秒)" v-model="indexConfigInfo.timeout" :min="0"></el-input-number>
            </el-form-item>
            <el-form-item label="重试次数(上限1次)">
              <el-input-number placeholder="重试次数" v-model="indexConfigInfo.retry" :min="0" :max="1"></el-input-number>
            </el-form-item>
            <el-form-item label="慢查询阈值(毫秒)">
                <el-input-number placeholder="慢查询阈值(毫秒)" v-model="indexConfigInfo.slowerThan" :min="0"></el-input-number>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :disabled="!indexConfigInfo.hasPrivilege"
                :title="!indexConfigInfo.hasPrivilege ? '索引权限不足' : ''"
                @click="handleUpdate">
                <i class="fa fa-refresh"></i>更新
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-collapse-item>
      <el-collapse-item title="模板级别配置" name="2">
        <template slot="title">
            模板级别配置
            <span style="color: #ddd">（若模板超时时间为0毫秒，则以当前索引的超时时间和重试为准。）</span>
        </template>
        <el-table :data="templateList" border style="width: 100%">
          <el-table-column label="模板名称" show-overflow-tooltip>
              <template slot-scope="scope">{{scope.row.templateName}}</template>
          </el-table-column>
          <el-table-column label="超时时间(毫秒,需>=50ms)">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.timeout" :min="0"></el-input-number>
            </template>
          </el-table-column>
          <el-table-column label="重试次数(上限1次)">
            <template slot-scope="scope">
              <el-input-number v-model="scope.row.retry" :min="0" :max="1"></el-input-number>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120px">
            <template slot-scope="scope">
              <el-button
                type="primary"
                :disabled="!allPrivilege"
                :title="!allPrivilege ? '权限不足' : ''"
                @click="handleTemplateUpdate(scope.row)">
                <i class="fa fa-refresh"></i>更新
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-collapse-item>
    </el-collapse>
  </div>
</template>

<script>
export default {
  props: ['templateList', 'allPrivilege'],
  data() {
    return {
      loading: false,
      indexId: this.$route.query.indexId,
      indexConfigInfo: {},
      activeNames: ['1', '2'],
    };
  },
  methods: {
    getIndexConfigInfo() {
      this.loading = true;
      this.$http.get(`/index/id.json?indexId=${this.indexId}`).then((data) => {
        this.indexConfigInfo = {
          ...data,
          slowerThan: data.slowerThan == null ? '200' : data.slowerThan,
        };
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleUpdate() {
      const params = {
        indexId: this.indexId,
        timeout: this.indexConfigInfo.timeout,
        retry: this.indexConfigInfo.retry,
        slowerThan: this.indexConfigInfo.slowerThan,
      };
      this.loading = true;
      this.$http.post('/index/update/timeout_retry.json', params).then(() => {
        this.$message.successMessage('索引配置更新成功', () => {
          this.getIndexConfigInfo();
        });
      })
      .finally(() => {
        this.loading = false;
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
        this.$message.successMessage('模板配置更新成功', () => {
          this.$emit('refresh-template');
        });
      })
      .finally(() => {
        this.loading = false;
      });
    },
  },
  created() {
    this.getIndexConfigInfo();
  },
};
</script>
