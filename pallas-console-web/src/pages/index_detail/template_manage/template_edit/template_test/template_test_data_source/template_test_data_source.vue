<template>
    <el-table :data="dataSource" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
        <el-table-column label="数据源参数">
            <template scope="scope">
                <el-popover trigger="hover" placement="top" popper-class="template-test-popper">
                    <div class="pull-left"><el-input v-model="scope.row.editParamNameDef"></el-input></div>
                    <div class="popper-button"><el-button size="small" @click="handleSave(scope.row)">保存</el-button></div>
                    <div slot="reference">
                        {{ scope.row.paramNameDef }}
                    </div>
                </el-popover>
            </template>
        </el-table-column>
        <el-table-column prop="fileName" label="数据源文件"></el-table-column>
        <el-table-column label="操作">
            <template scope="scope">
                <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </template>
        </el-table-column>
    </el-table>
</template>

<script>
export default {
  props: ['indexId', 'templateName', 'dataSource'],
  data() {
    return {
      loading: false,
    };
  },
  methods: {
    handleSave(row) {
      if (row.editParamNameDef) {
        if (row.editParamNameDef.indexOf(',') <= 0) {
          const dataParams = {
            indexId: this.indexId,
            templateName: this.templateName,
            fileName: row.fileName,
            paramNameDef: row.editParamNameDef,
          };
          this.loading = true;
          this.$http.post('/index_template/performance_script/update.json', dataParams).then(() => {
            this.$message.successMessage('保存成功', () => {
              this.$set(row, 'paramNameDef', row.editParamNameDef);
              this.$set(row, 'editParamNameDef', '');
            });
          })
          .finally(() => {
            this.loading = false;
          });
        } else {
          this.$message.errorMessage('请用;符号分隔参数');
        }
      } else {
        this.$message.errorMessage('参数不能为空!');
      }
    },
    handleDelete(row) {
      this.$message.confirmMessage(`确定删除文件${row.fileName}吗?`, () => {
        const dataParams = {
          indexId: this.indexId,
          templateName: this.templateName,
          fileName: row.fileName,
        };
        this.loading = true;
        this.$http.post('/index_template/performance_script/delete.json', dataParams).then(() => {
          this.$message.successMessage('删除成功', () => {
            this.$array.removeByValue(this.dataSource, row);
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
  },
};
</script>
