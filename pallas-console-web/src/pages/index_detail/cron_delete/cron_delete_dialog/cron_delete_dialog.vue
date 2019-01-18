<template>
  <div class="index-info">
    <el-dialog :title="crondeleteTitle" size="large" v-model="isCronDeleteVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="crondeleteInfo" :rules="rules" ref="crondeleteInfo" label-width="150px">
            <el-row :gutter="20">
                <el-col :span="10">
                    <el-form-item prop="cron" label="cron表达式">
                        <el-input v-model="crondeleteInfo.cron" ></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="10">
                    <el-form-item prop="scrollSize" label="每次删除多少">
                        <el-input-number style="width: 100%;" v-model="crondeleteInfo.scrollSize" ></el-input-number>
                    </el-form-item>
                </el-col>
                <el-col :span="10">
                    <el-form-item label="版本" prop="versionId" required>
                        <el-select v-model="crondeleteInfo.versionId" clearable style="width: 100%;">
                            <el-option v-for="item in versionIdList" :key="item.value" :label="item.label" :value="item.value"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="20">
                    <el-form-item label="delete by query dsl" prop="dsl" required>
                        <el-input type="textarea" :rows="10" v-model="crondeleteInfo.dsl" ></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">                
          <el-button @click="closeDialog()">取消</el-button>
          <el-button type="confirm" @click="submitUpsert()">保存</el-button>
        </div>        
    </el-dialog>
  </div>
</template>

<script>
export default {
  props: ['indexId', 'crondeleteInfo', 'crondeleteOperation', 'crondeleteTitle'],
  data() {
    return {
      loading: false,
      isCronDeleteVisible: true,
      rules: {
        cron: [{ required: true, message: '请参照saturn的cron格式', trigger: 'blur' }],
        versionId: [{ validator: this.$validate.validateSelect, trigger: 'blur' }],
        scrollSize: [{ required: true, message: 'scrollSize不能为空' }, { type: 'number', message: 'scrollSize值必须为数字值' }],
        dsl: [{ required: true, message: '请输入delete by query的查询dsl', trigger: 'blur' }],
      },
      versionIdList: [],
      showImportObject: {
        show: false,
      },
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
    submitUpsert() {
      this.$refs.crondeleteInfo.validate((valid) => {
        if (valid) {
          const successMsg = this.crondeleteOperation === 'edit' ? '修改成功' : '新增成功';
          this.loading = true;
          this.$http.post('/crondelete/upsert.json', this.crondeleteInfo).then(() => {
            this.$message.successMessage(successMsg, () => {
              this.$emit('operate-close-dialog');
            });
          })
          .finally(() => {
            this.loading = false;
          });
        }
      });
    },
    closeDialog() {
      this.$emit('close-dialog');
    },
  },
  computed: {
  },
  components: {
  },
  created() {
    this.initVersionIdList();
  },
};
</script>
<style>
.clusters-select {
  width: 100%;
}
</style>
