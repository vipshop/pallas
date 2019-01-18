<template>
    <el-dialog title="导入Mysql数据源" v-model="isDsImportVisible" :before-close="handleClose">
        <el-form>
            <el-row>
                <el-col :span="18">
                    <el-form-item>
                        <el-upload
                        ref="upload"
                        action="/pallas/ds/import.json"
                        :auto-upload="false"
                        :multiple="false"
                        :file-list="fileList"
                        :on-remove="handleRemove"
                        :on-error="handleError"
                        :on-success="handleSuccess">
                            <el-button size="small" type="primary" @click="handleUpload">点击上传</el-button>
                            <div slot="tip" class="el-upload__tip">请上传文本文件，每行一条数据源，内容以空格分开</br>字段分别为：ip port user passwd database table</div>
                        </el-upload>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">    
            <el-button @click="handleClose()">取 消</el-button>
            <el-button type="confirm" @click="submitImportDs">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  data() {
    return {
      isDsImportVisible: true,
      fileList: [],
    };
  },
  methods: {
    cancelBtn() {
      this.$emit('close-dialog');
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
      this.$emit('ds-import-success', response);
    },
    submitImportDs() {
      this.$refs.upload.submit();
    },
    handleClose() {
      this.$emit('close-ds-import-dialog');
    },
  },
};

</script>
