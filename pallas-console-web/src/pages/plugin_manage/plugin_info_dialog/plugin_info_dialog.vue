<template>
    <el-dialog :title="pluginInfoTitle" :visible.sync="isVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="pluginInfo" :rules="rules" ref="pluginInfo" label-width="120px">
            <div>
                <el-row>
                    <el-col :span="22">
                        <el-form-item label="插件名称" prop="pluginName">
                            <el-input v-model="pluginInfo.pluginName" :disabled="isEditable"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="22">
                        <el-form-item prop="clusterId" label="所属集群">
                            <el-select v-model="pluginInfo.clusterId" style="width:100%;"  :disabled="isEditable">
                                <el-option v-for="item in clusters" :label="item.description" :value="item.clusterId" :key="item.clusterId"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="22">
                        <el-form-item label="插件版本" prop="pluginVersion">
                            <el-input v-model="pluginInfo.pluginVersion"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="22">
                        <el-form-item label="插件类型" prop="pluginType">
                            <el-select v-model="pluginInfo.pluginType" style="width:100%;">
                                <el-option v-for="item in $option.pluginTypes" :label="item.label" :value="item.value" :key="item.value"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="22">
                        <el-form-item label="路径" prop="packagePath">
                            <el-input v-model="pluginInfo.packagePath" placeholder="请上传插件包，获取包路径" :disabled="true"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row>
                    <el-col :span="22">
                        <el-form-item label="变更描述" prop="note">
                            <el-input type="textarea" v-model="pluginInfo.note"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <div class="plugin-upload-warning">对于ES原生插件，需要通过"ES集群管理"->"重启"重启全部节点后才生效</div>
                <div class="plugin-upload-warning" v-if="!enableUpload">*上传插件包前，请先输入插件名称，所属集群以及插件版本</div>
                <div class="plugin-upload" v-else>
                    <el-upload
                    ref="upload"
                    action="/pallas/plugin/upgrade/fileUpload.json"
                    :data="uploadData"
                    :auto-upload="true"
                    :multiple="false"
                    :on-remove="handleRemove"
                    :on-error="handleError"
                    :on-success="handleSuccess">
                        <el-button size="small" type="primary" icon="upload" @click="handleUpload">上传插件zip包</el-button>
                        <div slot="tip" class="el-upload__tip">请先上传插件zip文件，获取包路径</div>
                    </el-upload>
                </div>
            </div>
        </el-form>
        <div slot="footer" class="dialog-footer">    
            <el-button @click="closeDialog()">取消</el-button>
            <el-button type="confirm" @click="handleSubmit()">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['pluginInfo', 'pluginInfoTitle', 'pluginInfoOperation'],
  data() {
    return {
      loading: false,
      isVisible: true,
      rules: {
        clusterId: [{ required: true, message: '请选择集群', trigger: 'change' }],
        pluginName: [{ required: true, message: '请输入插件名称', trigger: 'blur' }],
        pluginVersion: [{ required: true, message: '请输入插件版本', trigger: 'blur' }],
        packagePath: [{ required: true, message: '请上传文件获取包路径', trigger: 'blur' }],
        note: [{ required: true, message: '请输入变更描述', trigger: 'blur' }],
      },
      clusters: [],
    };
  },
  methods: {
    handleSubmit() {
      this.$refs.pluginInfo.validate((valid) => {
        if (valid) {
          this.loading = true;
          this.$http.post('/plugin/upgrade/add.json', this.pluginInfo).then(() => {
            this.$message.successMessage('操作成功', () => {
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
    getClusters() {
      this.loading = true;
      this.$http.get('/cluster/all.json').then((data) => {
        data.forEach((ele) => {
          if (!ele.logicalCluster) {
            this.clusters.push(ele);
          }
        });
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleUpload() {
      this.$refs.upload.clearFiles();
      this.$refs.upload.$refs['upload-inner'].$refs.input.value = '';
    },
    handleRemove() {
      this.$refs.upload.$refs['upload-inner'].$refs.input.value = '';
    },
    handleError(err) {
      this.$message.errorMessage(`上传失败: ${err}`);
    },
    handleSuccess(response) {
      if (response.status === 200) {
        this.pluginInfo.packagePath = response.data;
      } else {
        this.$message.errorMessage(response.data.message);
      }
    },
  },
  computed: {
    isEditable() {
      return this.pluginInfoOperation === 'upgrade';
    },
    uploadData() {
      const data = {
        clusterId: this.pluginInfo.clusterId,
        pluginName: this.pluginInfo.pluginName,
        pluginVersion: this.pluginInfo.pluginVersion,
      };
      return data;
    },
    enableUpload() {
      if (this.pluginInfo.clusterId === '' || this.pluginInfo.pluginName === '' || this.pluginInfo.pluginVersion === '') {
        return false;
      }
      return true;
    },
  },
  created() {
    this.getClusters();
  },
};

</script>

<style type="text/css">
.plugin-upload {
    margin-left: 50px;
}
.plugin-upload-tooltip.el-tooltip__popper.is-dark {
    background: #333;
}
.plugin-upload-warning {
    margin-left: 50px;
    color: red;
}
</style>
