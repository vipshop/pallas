<template>
    <el-dialog title="审批处理" v-model="isVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="approveInfo" :rules="rules" ref="approveInfo" label-width="100px">
            <div>
                <el-row>
                    <el-col :span="22">
                        <el-form-item label="审批" prop="state">
                            <el-select v-model="approveInfo.state" style="width: 100%;">
                                <el-option v-for="item in approveStates" :label="item.key" :value="item.value" :key="item.value"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="22">
                        <el-form-item label="描述" prop="note">
                            <el-input type="textarea" v-model="approveInfo.note"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
            </div>
        </el-form>
        <div slot="footer" class="dialog-footer">    
            <el-button @click="closeDialog()">取消</el-button>
            <el-button type="confirm" @click="submitSyncInfo()">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>

export default {
  props: ['approveStates', 'approveInfo'],
  data() {
    return {
      loading: false,
      isVisible: true,
      rules: {
        state: [{ required: true, message: '请选择', trigger: 'blur' }],
        note: [{ required: true, message: '请输入审批描述', trigger: 'blur' }],
      },
    };
  },
  methods: {
    submitSyncInfo() {
      this.$refs.approveInfo.validate((valid) => {
        if (valid) {
          this.loading = true;
          this.$http.post('/approve/approve.json', this.approveInfo).then(() => {
            this.$message.successMessage('审批完成', () => {
              this.$emit('approve-complete');
            });
          })
          .finally(() => {
            this.loading = false;
          });
        }
      });
    },
    closeDialog() {
      this.$emit('close-approve-dialog');
    },
  },
};

</script>

<style type="text/css">
</style>

