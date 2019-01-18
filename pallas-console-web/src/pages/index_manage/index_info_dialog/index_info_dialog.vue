<template>
  <div class="index-info">
    <el-dialog :title="indexInfoTitle" size="large" v-model="isIndexInfoVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="indexInfo" :rules="rules" ref="indexInfo" label-width="135px">
            <el-row :gutter="20">
                <el-col :span="11">
                    <el-form-item prop="indexName" label="索引名" required>
                        <el-input v-model="indexInfo.indexName" :disabled="isEditable"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="10">
                    <el-form-item label="所属集群" prop="clusterId">
                        <el-select v-model="indexInfo.clusterId" clearable style="width: 100%;">
                            <el-option-group v-for="cluster in clusters" :key="cluster.label" :label="cluster.label">
                                <el-option v-for="item in cluster.options" :key="item.id" :label="item.clusterId" :value="item.clusterId"></el-option>
                            </el-option-group>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="21">
                    <el-form-item label="描述" prop="description">
                        <el-input type="textarea" v-model="indexInfo.description"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <index-data-sources :data-sources="indexInfo.dataSourceList" :index-operation="indexOperation" ref="dataSources" :show-import-object="showImportObject" :index-id="indexInfo.indexId"></index-data-sources>
        <div slot="footer" class="dialog-footer">                
                <el-button @click="closeDialog()">取消</el-button>
                <el-button type="confirm" @click="submitIndexInfo()">保存</el-button>
        </div>        
    </el-dialog>
    
    <div  v-if="showImportObject.show">
      <datasource-import-dialog
        @ds-import-success="dsImportSuccess" @close-ds-import-dialog="closeDsImportDialog"></datasource-import-dialog>
    </div>
  </div>
</template>

<script>
import indexDataSources from './index_data_sources/index_data_sources';
import datasourceImportDialog from './index_data_sources/datasource_import_dialog';

export default {
  props: ['indexOperation', 'indexInfo', 'indexInfoTitle', 'clusters'],
  data() {
    return {
      loading: false,
      isIndexInfoVisible: true,
      rules: {
        indexName: [{ validator: this.$validate.validateCharacterAndNumberIsExcludePointAndBar, trigger: 'blur' }],
        clusterId: [{ required: true, message: '请选择所属集群', trigger: 'change' }],
      },
      showImportObject: {
        show: false,
      },
    };
  },
  methods: {
    submitIndexInfo() {
      this.$refs.indexInfo.validate((valid1) => {
        if (valid1) {
          let count = 0;
          if (this.indexInfo.dataSourceList.length > 0) {
            this.$refs.dataSources.$refs.dataSourceItem.forEach((element) => {
              element.$refs.dataSource.validate((valid2) => {
                if (valid2) {
                  count += 1;
                }
              });
            });
          }
          if (count === this.indexInfo.dataSourceList.length) {
            if (this.indexOperation === 'add') {
              this.loading = true;
              this.$http.post('/index/add.json', this.indexInfo).then((response) => {
                if (response) {
                  this.$message.confirmMessage(response, () => {
                    this.indexInfo.confirm = true;
                    this.$http.post('/index/add.json', this.indexInfo).then(() => {
                      this.$message.successMessage('新增索引成功', () => {
                        this.$emit('operate-close-dialog');
                      });
                    });
                  });
                } else {
                  this.$message.successMessage('新增索引成功', () => {
                    this.$emit('operate-close-dialog');
                  });
                }
              })
              .finally(() => {
                this.loading = false;
              });
            } else {
              this.loading = true;
              this.$http.post('/index/update.json', this.indexInfo).then((response) => {
                if (response && response.indexOf('成功') < 0) {
                  this.$message.confirmMessage(response, () => {
                    this.indexInfo.confirm = true;
                    this.$http.post('/index/update.json', this.indexInfo).then(() => {
                      this.$message.successMessage('更新索引成功', () => {
                        this.$emit('operate-close-dialog');
                      });
                    });
                  });
                } else {
                  this.$message.successMessage('更新索引成功', () => {
                    this.$emit('operate-close-dialog');
                  });
                }
              })
              .finally(() => {
                this.loading = false;
              });
            }
          }
        }
      });
    },
    closeDialog() {
      this.$emit('close-dialog');
      this.$refs.indexInfo.resetFields();
    },
    closeDsImportDialog() {
      this.showImportObject.show = false;
    },
    dsImportSuccess(data) {
      console.log('dsImportSuccess', data);
      const dsList = data.split(/[\r\n]/);
      this.indexInfo.dataSourceList = [];
      dsList.forEach((element) => {
        const ds = element.split(/\s+/);
        if (ds.length === 6) {
          const oneDataSource = {
            ip: ds[0],
            port: ds[1],
            username: ds[2],
            password: ds[3],
            dbname: ds[4],
            tableName: ds[5],
            isGeneratePwd: false,
          };
          this.indexInfo.dataSourceList.push(oneDataSource);
        }
      });
      this.showImportObject.show = false;
    },
  },
  computed: {
    isEditable() {
      return this.indexOperation === 'edit';
    },
  },
  components: {
    'index-data-sources': indexDataSources,
    'datasource-import-dialog': datasourceImportDialog,
  },
};
</script>
<style>
.clusters-select {
  width: 100%;
}
</style>
