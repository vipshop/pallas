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
            <el-row>
                <el-col :span="23">
                    <el-form-item prop="clusterLevel" label="节点集属性">
                        <el-select v-model="targetGroupInfo.clusterLevel" style="width: 100%;">
                            <el-option v-for="item in attrs" :key="item.value" :label="item.label" :value="item.value"></el-option>
                        </el-select>
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
      attrs: [{
        label: '普通级别',
        value: 0,
      }, {
        label: '集群级别',
        value: 1,
      }, {
        label: '集群级别(主分片优先)',
        value: 3,
      }, {
        label: '集群级别(复制分片优先)',
        value: 4,
      }, {
        label: '分片动态绑定',
        value: 2,
      }],
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
      this.$http.post('/index/routing/target_group/update.json', info).then(() => {
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
