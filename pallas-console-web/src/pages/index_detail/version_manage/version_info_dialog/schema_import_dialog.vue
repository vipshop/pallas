<template>
    <el-dialog :title="schemaImportTitle" v-model="isSchemaImportVisible" :before-close="handleClose">
        <el-form>
            <el-row>
                <el-col :span="18">
                    <el-form-item>
                        <el-upload
                        ref="upload"
                        :action="schemaImportUrl"
                        :auto-upload="false"
                        :multiple="false"
                        :file-list="fileList"
                        :on-remove="handleRemove"
                        :on-error="handleError"
                        :on-success="handleSuccess">
                            <el-button size="small" type="primary" @click="handleUpload">点击上传</el-button>
                            <div slot="tip" class="el-upload__tip">请上传json文件，且不超过10Mb</div>
                        </el-upload>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">    
            <el-button @click="handleClose()">取 消</el-button>
            <el-button type="confirm" @click="submitImportSchema">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['schemaImportTitle', 'schemaImportUrl'],
  data() {
    return {
      isSchemaImportVisible: true,
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
      const arr = response.schema.map((obj) => {
        const rObj = {};
        rObj.dbFieldType = obj.dbFieldType;
        rObj.fieldName = obj.fieldName;
        rObj.fieldType = obj.fieldType;
        rObj.multi = obj.multi;
        rObj.search = obj.search;
        rObj.docValue = obj.docValue;
        rObj.children = obj.children || [];
        return rObj;
      });
      this.$emit('schema-import-success', arr);
    },
    submitImportSchema() {
      this.$refs.upload.submit();
    },
    handleClose() {
      this.$emit('close-schema-import-dialog');
    },
  },
};

</script>
