<template>
    <el-dialog :title="templateImportTitle" v-model="isTemplateImportVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
        <div style="margin: 20px 15px;">
            <el-radio v-model="importWay" label="localUpload">本地上传</el-radio>
            <el-radio v-model="importWay" label="otherIndexTemp">导入其他索引模板</el-radio>
        </div>
        <el-form :model="importInfo" :rules="rules" ref="importInfo" label-width="90px" v-if="importWay === 'localUpload'">
            <el-row>
                <el-col :span="18">
                    <div class="template-import-upload">
                        <el-upload
                        ref="upload"
                        :action="templateImportUrl"
                        :auto-upload="false"
                        :data="importInfo"
                        :multiple="false"
                        :file-list="fileList"
                        :on-remove="handleRemove"
                        :on-error="handleError"
                        :on-success="handleSuccess">
                            <el-button size="small" type="primary" @click="handleUpload">点击上传</el-button>
                            <div slot="tip" class="el-upload__tip">请上传zip文件，且不超过10Mb</div>
                        </el-upload>
                    </div>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="18">
                    <el-form-item label="变更描述" prop="updateDesc">
                        <el-input v-model="importInfo.updateDesc"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <div style="margin: 15px" v-if="importWay === 'otherIndexTemp'">
            <el-select v-model="indexSelected" filterable style="width: 100%;margin-bottom: 10px;" placeholder="请选择索引" @change="indexChange">
              <el-option
                v-for="item in indexAllList"
                :key="item.id"
                :label="item.indexName"
                :value="item.id">
              </el-option>
            </el-select>
            <el-transfer
            filterable
            v-model="templateSelected"
            :data="importTemplateList"
            :props="{
              key: 'id',
              label: 'templateName'
            }"
            :titles="['可选模板', '已选模板']"></el-transfer>
        </div>
        <div slot="footer" class="dialog-footer">    
            <el-button @click="closeDialog()">取 消</el-button>
            <el-button type="confirm" @click="submitImportTemplate">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['indexId', 'templateImportTitle', 'templateImportUrl'],
  data() {
    return {
      loading: false,
      isTemplateImportVisible: true,
      importWay: 'localUpload',
      rules: {
        updateDesc: [{ required: true, message: '请输入变更描述', trigger: 'blur' }],
      },
      templateSelected: [],
      importTemplateList: [],
      fileList: [],
      importInfo: {
        updateDesc: '',
      },
      indexSelected: '',
      indexAllList: [],
    };
  },
  methods: {
    indexChange() {
      this.templateSelected = [];
      this.getTemplateList();
    },
    getTemplateList() {
      this.loading = true;
      this.$http.get(`/index_template/list.json?indexId=${this.indexSelected}`).then((data) => {
        this.importTemplateList = data.list;
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
        this.$message.successMessage('导入模板成功！', () => {
          this.$emit('close-submit-dialog');
        });
      } else {
        this.$message.errorMessage(response.description);
      }
    },
    submitImportTemplate() {
      if (this.importWay === 'localUpload') {
        this.$refs.importInfo.validate((valid) => {
          if (valid) {
            this.$refs.upload.submit();
          }
        });
      } else {
        console.log(this.templateSelected);
        if (this.templateSelected.length === 0) {
          this.$message.errorMessage('已选模板为空！请选择模板！');
        } else {
          this.indexTemplateImport();
        }
      }
    },
    indexTemplateImport() {
      const arr = this.importTemplateList.filter(e =>
        this.templateSelected.indexOf(e.id) > -1,
      );
      const templateResult = arr.map(e => ({ id: e.id, templateName: e.templateName }));
      const params = {
        indexId: this.indexId,
        templateInfos: templateResult,
      };
      this.loading = true;
      this.$http.post('/index_template/index/import.json', params).then((data) => {
        if (data.length === 0) {
          this.$message.successMessage('导入模板成功', () => {
            this.$emit('close-submit-dialog');
          });
        } else {
          this.$message.errorMessage(`模板 ${data.join(',')} 导入失败`);
        }
      })
      .finally(() => {
        this.loading = false;
      });
    },
    closeDialog() {
      this.$emit('close-dialog');
      if (this.importWay === 'localUpload') {
        this.$refs.upload.clearFiles();
        this.$refs.upload.$refs['upload-inner'].$refs.input.value = '';
      }
    },
    getAllIndexList() {
      return this.$http.get('/index/all.json').then((data) => {
        this.indexAllList = data.filter(a => a.id !== Number(this.indexId));
      });
    },
  },
  created() {
    this.getAllIndexList();
  },
};

</script>

<style type="text/css">
.template-import-upload {
  margin: 0 20px 20px;
}
</style>
