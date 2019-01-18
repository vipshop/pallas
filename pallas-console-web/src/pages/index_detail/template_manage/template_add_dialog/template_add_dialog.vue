<template>
    <el-dialog title="新建模板" v-model="isTemplateAddVisible" :before-close="closeDialog" size="tiny" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="templateAddInfo" :rules="rules" ref="templateAddInfo" label-width="140px">
            <el-row>
                <el-col :span="23">
                    <el-form-item label="templateName" prop="templateName">
                        <el-input v-model="templateAddInfo.templateName"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col>
                    <el-form-item label="type" prop="type">
                        <el-select v-model="templateAddInfo.type">
                            <el-option label="模板" value="1" key="1"></el-option>
                            <el-option label="宏" value="0" key="0"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="23">
                    <el-form-item label="描述" prop="description">
                        <el-input type="textarea" v-model="templateAddInfo.description"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">    
           <el-button @click="closeDialog()">取消</el-button>
           <el-button type="confirm" @click="submitInfo()">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['templateAddInfo'],
  data() {
    return {
      loading: false,
      isTemplateAddVisible: true,
      rules: {
        templateName: [{ required: true, message: '请输入模板名称', trigger: 'blur' }],
        type: [{ required: true, message: '请输入类型', trigger: 'blur' }],
      },
    };
  },
  methods: {
    submitInfo() {
      this.$refs.templateAddInfo.validate((valid) => {
        if (valid) {
          this.loading = true;
          this.$http.post('/index_template/add.json', this.templateAddInfo).then(() => {
            this.$message.successMessage('新增模板成功', () => {
              this.$emit('submit-close-dialog', this.templateAddInfo.templateName);
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
      this.$refs.templateAddInfo.resetFields();
    },
  },
};

</script>

<style type="text/css">
</style>
