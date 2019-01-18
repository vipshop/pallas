<template>
    <el-dialog title="提交模板" v-model="isEditSaveVisible" :before-close="handleClose" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="editSaveInfo" :rules="rules" ref="editSaveInfo" label-width="120px">
            <div>
                <div class="edit-save-span">
                    <i class="el-icon-warning"></i>
                    确定提交模板 <span>{{templateInfo.templateName}}</span> 吗？
                </div>
                <el-row>
                    <el-col :span="18">
                        <el-form-item label="修改描述" prop="historyDesc">
                            <el-input v-model="editSaveInfo.historyDesc"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
            </div>
        </el-form>
        <div slot="footer" class="dialog-footer">    
            <el-button @click="handleClose()">取消</el-button>
            <el-button type="confirm" @click="handleSubmit">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['indexId', 'templateInfo'],
  data() {
    return {
      loading: false,
      isEditSaveVisible: true,
      editSaveInfo: {
        historyDesc: '',
      },
      rules: {
        historyDesc: [{ required: true, message: '请输入描述', trigger: 'blur' }],
      },
    };
  },
  methods: {
    handleSubmit() {
      this.$refs.editSaveInfo.validate((valid) => {
        if (valid) {
          const dataParams = {
            indexId: this.indexId,
            templateName: this.templateInfo.templateName,
            content: this.templateInfo.content,
            params: this.templateInfo.params,
            historyDesc: this.editSaveInfo.historyDesc,
          };
          this.loading = true;
          this.$http.post('/index_template/approve.json', dataParams).then(() => {
            this.$message.successMessage('提交成功，请在模板变更中查看模板审批状态', () => {
              this.$emit('edit-save-success');
            });
          })
          .finally(() => {
            this.loading = false;
          });
        }
      });
    },
    handleClose() {
      this.$emit('close-edit-save-dialog');
    },
  },
};
</script>
<style type="text/css">
.edit-save-span {
    color: #fff;
    font-size: 15px;
    font-weight: bolder;
    padding: 15px 40px;
}
.edit-save-span i {
    color: red;
}
.edit-save-span span {
    color: #32cd32;
}
</style>
