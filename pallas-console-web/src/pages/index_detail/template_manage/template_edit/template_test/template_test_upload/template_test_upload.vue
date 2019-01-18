<template>
    <div class="template-test-upload">
        <el-table :data="addDataSource" :show-header="false">
            <el-table-column>
                <template scope="scope">
                    <el-input v-model="scope.row.paramNameDef"></el-input>
                </template>
            </el-table-column>
            <el-table-column>
                <template scope="scope">
                    <el-upload
                    ref="upload"
                    action="/pallas/index_template/performance_script/upload.json"
                    :auto-upload="false"
                    :data="addDataSource[0]"
                    :on-remove="handleRemove"
                    :on-error="handleError"
                    :on-success="handleSuccess">
                        <el-button size="small" type="primary" @click="handleUpload">选择文件</el-button>
                    </el-upload>
                </template>
            </el-table-column>
            <el-table-column>
                <template scope="scope">
                    <el-button size="small" type="primary" @click="handleAdd(scope.row)">上传</el-button>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>

<script>
export default {
  props: ['addDataSource', 'dataSource'],
  data() {
    return {
    };
  },
  methods: {
    handleUpload() {
      this.$refs.upload.clearFiles();
      this.$refs.upload.$refs['upload-inner'].$refs.input.value = '';
    },
    handleError(err) {
      this.$message.errorMessage(`上传失败: ${err}`);
    },
    handleRemove() {
      this.$refs.upload.$refs['upload-inner'].$refs.input.value = '';
    },
    handleAdd(row) {
      if (row.paramNameDef && this.$refs.upload.uploadFiles.length) {
        if (row.paramNameDef.indexOf(',') <= 0) {
          if (row.paramNameDef.indexOf(';') <= 0) {
            if (this.$array.isContainValue(this.paramValues, row.paramNameDef)) {
              this.$message.errorMessage('不允许相同参数名');
            } else {
              this.$refs.upload.submit();
            }
          } else {
            const arr = row.paramNameDef.split(';');
            const isError = arr.some((element) => {
              if (this.$array.isContainValue(this.paramValues, element)) {
                this.$message.errorMessage('不允许相同参数名');
                return true;
              }
              return false;
            });
            if (!isError) {
              this.$refs.upload.submit();
            }
          }
        } else {
          this.$message.errorMessage('请用;符号分隔参数');
        }
      } else {
        this.$message.errorMessage('参数和文件不能为空');
      }
    },
    handleSuccess(response, file) {
      if (response.status === 200) {
        this.$message.confirmMessage('上传成功！', () => {
          const dataSourceRowInfo = {};
          dataSourceRowInfo.paramNameDef =
          JSON.parse(JSON.stringify(this.addDataSource[0].paramNameDef));
          dataSourceRowInfo.fileName = JSON.parse(JSON.stringify(file.name));
          this.$emit('template-test-upload-success', dataSourceRowInfo);

          this.$refs.upload.clearFiles();
          this.$refs.upload.$refs['upload-inner'].$refs.input.value = '';
          this.addDataSource[0].paramNameDef = '';
        });
      } else {
        this.$message.errorMessage(response.description);
      }
    },
  },
  computed: {
    paramValues() {
      const arr = [];
      this.dataSource.forEach((element1) => {
        if (element1.paramNameDef.indexOf(';') <= 0) {
          arr.push(element1.paramNameDef);
        } else {
          const arr1 = element1.paramNameDef.split(';');
          arr1.forEach((element2) => {
            arr.push(element2);
          });
        }
      });
      return arr;
    },
  },
};
</script>
<style type="text/css">
.template-test-upload .el-table--enable-row-hover .el-table__body tr:hover>td{
    background-color: transparent;
}
</style>
