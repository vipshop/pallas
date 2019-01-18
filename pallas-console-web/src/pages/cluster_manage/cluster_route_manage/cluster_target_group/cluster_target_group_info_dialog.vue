<template>
    <el-dialog :title="targetGroupInfoTitle" v-model="isVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="targetGroupInfo" :rules="rules" ref="targetGroupInfo" label-width="130px">
            <el-row>
                <el-col :span="23">
                    <el-form-item prop="name" label="节点集名称">
                        <el-input v-model="targetGroupInfo.name"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">    
           <el-button @click="closeDialog()">取消</el-button>
           <el-button type="confirm" @click="submitInfo()">保存</el-button> 
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['rulesList', 'targetGroupInfo', 'targetGroupInfoTitle'],
  data() {
    return {
      loading: false,
      isVisible: true,
      rules: {
        name: [{ required: true, message: '节点集名称不能为空', trigger: 'blur' }],
      },
    };
  },
  methods: {
    submitInfo() {
      this.$refs.targetGroupInfo.validate((valid) => {
        if (valid) {
          this.targetGroupRequest(this.targetGroupInfo);
        }
      });
    },
    targetGroupRequest(info) {
      this.loading = true;
      this.$http.post('/cluster/routing/target_group/update.json', info).then(() => {
        this.$message.successMessage('操作成功', () => {
          this.$emit('operation-success');
        });
      })
      .finally(() => {
        this.loading = false;
      });
    },
    closeDialog() {
      this.$emit('close-dialog');
    },
  },
};
</script>
