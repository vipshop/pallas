<template>
    <el-dialog title="批量提交" v-model="isBatchSubmitVisible" :before-close="closeDialog">
        <el-form :model="batchSubmitInfo" :rules="rules" ref="batchSubmitInfo" label-width="80px">
            <div class="batch-submit-template">
                <el-transfer
                filterable
                v-model="templateSelected"
                :data="batchSubmitList"
                :titles="['可选模板', '已选模板']"></el-transfer>
            </div>
            <el-form-item label="修改描述" prop="approveInfo" style="margin-right: 10px">
                <el-input v-model="batchSubmitInfo.approveInfo"></el-input>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeDialog()">取 消</el-button>
            <el-button type="confirm" @click="submitTemplate">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['indexId', 'modifiedTemplateList'],
  data() {
    return {
      batchSubmitInfo: {
        approveInfo: '',
      },
      rules: {
        approveInfo: [{ required: true, message: '请输入描述', trigger: 'blur' }],
      },
      isBatchSubmitVisible: true,
      templateSelected: [],
      batchSubmitList: [],
    };
  },
  methods: {
    closeDialog() {
      this.$emit('close-batch-submit-dialog');
    },
    submitTemplate() {
      if (this.templateSelected.length <= 0) {
        this.$message.errorMessage('请选择要提交审批的模板！');
        return;
      }
      const tplIds = this.templateSelected.join(',');
      this.$refs.batchSubmitInfo.validate((valid) => {
        if (valid) {
          const dataParams = {
            indexId: this.indexId,
            templateIds: tplIds,
            historyDesc: this.batchSubmitInfo.approveInfo,
          };
          this.$http.post('/index_template/batch/approve.json', dataParams).then(() => {
            this.$message.successMessage('提交成功，请在模板变更中查看模板审批状态', () => {
              this.$emit('close-batch-submit-dialog');
            });
          })
          .finally(() => {
          });
        }
      });
    },
  },
  created() {
    const arr = JSON.parse(JSON.stringify(this.modifiedTemplateList));
    this.batchSubmitList = arr.map((obj) => {
      const rObj = {};
      rObj.key = obj.id;
      rObj.label = obj.templateName;
      return rObj;
    });
  },
};
</script>

<style>
.batch-submit-template {
    margin: 10px;
}
.batch-submit-template .el-transfer-panel {
    width: 250px;
}
</style>
