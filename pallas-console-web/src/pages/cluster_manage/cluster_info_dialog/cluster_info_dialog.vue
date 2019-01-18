<template>
    <el-dialog :title="clusterInfoTitle" size="small" v-model="isClusterInfoVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="clusterInfo" :rules="rules" ref="clusterInfo" label-width="140px">
            <el-row>
                <el-col :span="22">
                    <el-form-item prop="clusterId" label="域名" required>
                        <el-input v-model="clusterInfo.clusterId" :disabled="isEditable"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22">
                    <el-form-item prop="logicalCluster" label="集群类型">
                        <el-select v-model="clusterInfo.logicalCluster" style="width:100%;" :disabled="isEditable">
                            <el-option label="物理集群" :value="false"></el-option>
                            <el-option label="逻辑集群" :value="true"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row v-if="clusterInfo.logicalCluster">
                <el-col :span="22">
                    <el-form-item label="物理集群" prop="realClustersArr" required>
                        <el-select v-model="clusterInfo.realClustersArr" multiple filterable style="width:100%;">
                            <el-option v-for="item in allPhysicals" :label="item.clusterId" :value="item.id" :key="item.id"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row v-if="!clusterInfo.logicalCluster">
                <el-col :span="22">
                    <el-form-item label="HTTP地址" prop="httpAddress" required>
                        <el-input v-model="clusterInfo.httpAddress"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row v-if="!clusterInfo.logicalCluster">
                <el-col :span="22">
                    <el-form-item label="ES client连接地址" prop="clientAddress" required>
                        <el-input v-model="clusterInfo.clientAddress"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22">
                    <el-form-item label="绑定代理集群" prop="accessiblePs" required>
                        <el-select v-model="clusterInfo.accessiblePs" multiple filterable style="width:100%;">
                            <el-option v-for="item in allPallasSearchs" :label="item" :value="item" :key="item"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22">
                    <el-form-item label="描述" prop="description">
                        <el-input type="textarea" v-model="clusterInfo.description"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">    
           <el-button @click="closeDialog()">取消</el-button>
           <el-button type="confirm" @click="submitClusterInfo()">保存</el-button> 
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['clusterOperation', 'clusterInfo', 'clusterInfoTitle', 'allPhysicals', 'allPallasSearchs'],
  data() {
    return {
      loading: false,
      isClusterInfoVisible: true,
      rules: {
        clusterId: [{ validator: this.$validate.validateCharacterAndNumber, trigger: 'blur' }],
        httpAddress: [{ required: true, message: 'HTTP地址不能为空', trigger: 'change' }],
        clientAddress: [{ required: true, message: 'ES client连接地址不能为空', trigger: 'change' }],
        description: [{ required: true, message: '描述不能为空', trigger: 'change' }],
        realClustersArr: [{ validator: this.$validate.validateArray, trigger: 'blur' }],
        accessiblePs: [{ validator: this.$validate.validateArray, trigger: 'blur' }],
      },
    };
  },
  methods: {
    submitClusterInfo() {
      this.$refs.clusterInfo.validate((valid) => {
        if (valid) {
          this.$set(this.clusterInfo, 'realClusters', this.clusterInfo.realClustersArr.join(','));
          this.$set(this.clusterInfo, 'accessiblePs', this.clusterInfo.accessiblePs.join(','));
          if (this.clusterOperation === 'add') {
            this.loading = true;
            this.$http.post('/cluster/add.json', this.clusterInfo).then(() => {
              this.$message.successMessage('新增集群成功', () => {
                this.$emit('operate-close-dialog');
              });
            })
            .finally(() => {
              this.loading = false;
            });
          } else {
            this.loading = true;
            this.$http.post('/cluster/update.json', this.clusterInfo).then(() => {
              this.$message.successMessage('修改集群成功', () => {
                this.$emit('operate-close-dialog');
              });
            })
            .finally(() => {
              this.loading = false;
            });
          }
        } else {
          console.log('error submit!!');
        }
      });
    },
    closeDialog() {
      this.$emit('close-dialog');
    },
  },
  computed: {
    isEditable() {
      return this.clusterOperation === 'edit';
    },
  },
};

</script>
