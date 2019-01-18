  <template>
    <div class="version-info-dialog">
        <el-dialog :title="versionInfoTitle" size="large" v-model="isVersionInfoVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
            <el-form :model="versionInfo" :rules="rules" ref="versionInfo" label-position="left">
                <div class="label-title"><span class="span-title">索引配置</span></div>
                <div class="label-content">
                    <el-row :gutter="20">
                        <el-col :span="12">
                            <el-form-item label="分片数量" prop="shardNum" label-width="120px">
                                <el-input v-model.number="versionInfo.shardNum" :disabled="isEditable"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="复制数量" prop="replicationNum" label-width="120px">
                                <el-input v-model.number="versionInfo.replicationNum" :disabled="isEditable"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </div>
                <div class="label-content">
                    <el-row :gutter="20">
                        <el-col :span="12">
                            <el-form-item label="所属集群" prop="clusterId" label-width="120px" required>
                                <el-select v-model="versionInfo.clusterId" placeholder="请选择集群" @change="clusterChange" :disabled="isEditable" style="width: 100%">
                                    <el-option v-for="item in clusters" :key="item.id" :label="item.clusterId" :value="item.id"></el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :span="12">
                            <el-form-item label="所属节点" prop="nodes" label-width="120px">
                                <el-select multiple v-model="versionInfo.nodes" placeholder="请选择机器" :disabled="isEditable" style="width: 100%">
                                    <el-option v-for="item in clusterNodes" :key="item.name" :label="item.name" :value="item.name">
                                        <span style="float: left">{{ item.name }}</span>
                                        <el-tooltip placement="right">
                                            <div slot="content">
                                                <div v-if="item.indicis.length > 0" style="width: 600px;">
                                                    <el-col v-for="item1 in item.indicis" :key="item1" :span="8">{{item1}}</el-col>
                                                </div>
                                                <div v-else>暂无索引</div>
                                            </div>
                                            <el-tag style="float: left;margin-left: 10px;">{{ item.indicis.length }}</el-tag>
                                        </el-tooltip>
                                    </el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </div>
                <div class="label-content">
                    <el-row :gutter="20">
                        <el-col :span="routingKeyWidth">
                            <el-form-item label="Routing Key" prop="routingKey" label-width="120px">
                                <el-select filterable v-model="versionInfo.routingKey" :disabled="isEditable" style="width: 100%">
                                    <el-option v-for="item in versionInfo.schema" :label="item.fieldName" :value="item.fieldName" :key="item.fieldName"></el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :span="routingKeyWidth">
                            <el-form-item label="Id列名" prop="idField" label-width="120px">
                                <el-select filterable v-model="versionInfo.idField" :disabled="isEditable" style="width: 100%">
                                    <el-option v-for="item in versionInfo.schema" :label="item.fieldName" :value="item.fieldName" :key="item.fieldName"></el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                        <el-col :span="routingKeyWidth" v-if="!isMetaDataNull">
                            <el-form-item label="Update Time列名" prop="updateTimeField" label-width="150px">
                                <el-select filterable v-model="versionInfo.updateTimeField" :disabled="isEditable" style="width: 100%">
                                    <el-option v-for="item in versionInfo.schema" :label="item.fieldName" :value="item.fieldName" :key="item.fieldName"></el-option>
                                </el-select>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </div>
                <div class="label-title"><span class="span-title">慢日志落盘配置（单位ms，-1不限定）</span></div>
                <div class="label-content">
                    <el-row :gutter="20">
                        <el-col :span="8">
                            <el-form-item label="Index Slow" prop="indexSlowThreshold" label-width="120px">
                                <el-input v-model.number="versionInfo.indexSlowThreshold" :disabled="isEditable"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="Fetch Slow" prop="fetchSlowThreshold" label-width="120px">
                                <el-input v-model.number="versionInfo.fetchSlowThreshold" :disabled="isEditable"></el-input>
                            </el-form-item>
                        </el-col>
                        <el-col :span="8">
                            <el-form-item label="Query Slow" prop="querySlowThreshold" label-width="120px">
                                <el-input v-model.number="versionInfo.querySlowThreshold" :disabled="isEditable"></el-input>
                            </el-form-item>
                        </el-col>
                    </el-row>
                </div>
                <div class="label-title"><span class="span-title">索引其他配置</span></div>
                <div class="label-content">
                    <el-row :gutter="20">
                        <el-col :span="8">
                            <el-form-item label="refresh（秒）" prop="refreshInterval" label-width="120px">
                                <el-input v-model.number="versionInfo.refreshInterval" :disabled="isEditable"></el-input>
                            </el-form-item>
                        </el-col>
                        <div v-if="!isMetaDataNull">
                            <el-col :span="8">
                                <el-form-item label="只导局部字段" prop="filterFields" label-width="120px">
                                  <div class="my-switch">
                                    <el-switch v-model="versionInfo.filterFields" :disabled="isEditable"></el-switch>
                                  </div>
                                </el-form-item>
                            </el-col>
                            <el-col :span="8" v-if="$option.versionVdpTypes && $option.versionVdpTypes.length > 0">
                                <el-form-item label="同步数据类型" prop="vdp" label-width="100px">
                                  <div class="my-switch">
                                    <el-radio-group v-model="versionInfo.vdp" :disabled="isEditable" size="small">
                                        <el-radio-button :label="item.value" v-for="item in $option.versionVdpTypes" :key="item.value">{{item.label}}</el-radio-button>
                                    </el-radio-group>
                                  </div>
                                </el-form-item>
                            </el-col>
                        </div>
                    </el-row>
                </div>
                <div class="label-title">
                    <span class="span-title">mapping配置</span>
                    <el-button size="mini" type="success" @click="addField(0)" v-if="isMetaDataNull && versionInfo.schema.length === 0"><i class="fa fa-plus"></i>新增</el-button>
                    <el-button size="mini" type="warning" v-show="!isEditable" @click="importSchema"><i class="fa fa-arrow-circle-o-down"></i>导入schema</el-button>
                    <el-button size="mini" type="warning" v-show="isEditable" @click="exportSchema"><i class="fa fa-arrow-circle-o-up"></i>导出schema</el-button>
                </div>
                <div>
                    <el-table :data="versionInfo.schema" border style="width: 100%" :max-height="280">
                        <el-table-column label="操作">
                          <template scope="scope">
                              <el-button size="small" type="success" @click="addField(scope.$index)" :disabled="isEditable"><i class="el-icon-plus"></i></el-button>
                              <el-button size="small" type="danger" @click="deleteField(scope.row)" :disabled="isEditable"><i class="el-icon-minus"></i></el-button>
                          </template>
                        </el-table-column>
                        <el-table-column label="字段名" min-width="180">
                            <template scope="scope">
                                <el-button type="text" @click="viewSchemaChildren(scope.row)"  v-show="!scope.row.isNew">
                                    <span v-if="scope.row.children.length !== 0" class="red">*</span>
                                    <span>{{scope.row.fieldName}}</span>
                                </el-button>
                                <el-input style="width:50%" v-model="scope.row.fieldName" placeholder="请输入字段名" v-show="scope.row.isNew"></el-input>
                                <el-button type="text" @click="viewSchemaChildren(scope.row)"  v-show="scope.row.isNew">
                                    <span>子字段</span>
                                </el-button>
                            </template>
                        </el-table-column>
                        <el-table-column label="DB类型" v-if="!isMetaDataNull">
                            <template scope="scope">
                              {{scope.row.dbFieldType || 'N/A'}}
                            </template>
                        </el-table-column>
                        <el-table-column label="ES类型">
                            <template scope="scope">
                                <select v-model="scope.row.fieldType" size="small" :disabled="isEditable" @change="fieldTypeChange(scope.row)">
                                    <option v-for="item in fieldTypes" :value="item.value" :key="item.value">{{item.label}}</option>
                                </select>
                            </template>
                        </el-table-column>
                        <el-table-column label="多值/单值">
                            <template scope="scope">
                                <select v-model="scope.row.multi" size="small" :disabled="isEditable">
                                    <option label="单值" :value="false">单值</option>
                                    <option label="多值" :value="true">多值</option>
                                </select>
                            </template>
                        </el-table-column>
                        <el-table-column label="是否查询关键字" min-width="90">
                            <template scope="scope">
                                <el-checkbox v-model="scope.row.search" :disabled="isEditable || scope.row.fieldType === 'nested'">查询关键字</el-checkbox>
                            </template>
                        </el-table-column>
                        <el-table-column label="排序或聚合">
                            <template scope="scope">
                                <el-checkbox v-model="scope.row.docValue" :disabled="isEditable || scope.row.fieldType === 'nested' || scope.row.fieldType === 'text'">用于排序或聚合</el-checkbox>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>
            </el-form>
            <div slot="footer" class="dialog-footer">                
                <el-button @click="closeDialog()">取 消</el-button>
                <el-button v-if="!isEditable" type="confirm" @click="submitVersionInfo()">确 定</el-button>
            </div>
        </el-dialog>

        <schema-child-dialog :is-schema-child-visible="isSchemaChildVisible" :schema-child-info="schemaChildInfo" :version-operation="versionOperation" :schema-parent-field-name="schemaParentFieldName" @close-schema-dialog="closeSchemaDialog" @add-schema-child="addSchemaChild"></schema-child-dialog>
        <div v-if="isSchemaImportVisible">
            <schema-import-dialog :schema-import-title="schemaImportTitle" :schema-import-url="schemaImportUrl" @schema-import-success="schemaImportSuccess" @close-schema-import-dialog="closeSchemaImportDialog"></schema-import-dialog>
        </div>
    </div>
</template>

<script>
import SchemaChildDialog from './schema_child_dialog';
import SchemaImportDialog from './schema_import_dialog';

export default {
  components: {
    'schema-child-dialog': SchemaChildDialog,
    'schema-import-dialog': SchemaImportDialog,
  },
  props: ['versionOperation', 'versionInfo', 'versionInfoTitle', 'isMetaDataNull', 'clusters'],
  data() {
    return {
      loading: false,
      isVersionInfoVisible: true,
      isSchemaImportVisible: false,
      schemaImportTitle: '',
      schemaImportUrl: '',
      isSchemaChildVisible: false,
      schemaChildInfo: {},
      schemaParentFieldName: '',
      rules: {
        shardNum: [{ required: true, message: '分片数量不能为空' }, { type: 'number', message: '分片数量必须为数字值' }],
        replicationNum: [{ required: true, message: '复制数量不能为空' }, { type: 'number', message: '复制数量必须为数字值' }],
        routingKey: [{ required: true, message: '请选择Routing Key', trigger: 'change' }],
        idField: [{ required: true, message: '请选择Id列名', trigger: 'change' }],
        updateTimeField: [{ required: true, message: '请选择Update Time', trigger: 'change' }],
        indexSlowThreshold: [{ required: true, message: 'Index Slow Log不能为空' }, { type: 'number', message: 'Index Slow Log必须为数字值' }],
        fetchSlowThreshold: [{ required: true, message: 'Fetch Slow Log不能为空' }, { type: 'number', message: 'Fetch Slow Log必须为数字值' }],
        querySlowThreshold: [{ required: true, message: 'Query Slow Log不能为空' }, { type: 'number', message: 'Query Slow Log必须为数字值' }],
        refreshInterval: [{ required: true, message: 'refresh_interval不能为空' }, { type: 'number', message: 'refresh_interval必须为数字值' }],
      },
      fieldTypes: [{
        value: 'text',
        label: 'text(标准分词)',
      }, {
        value: 'text_ngram',
        label: 'text(逐字分词)',
      }, {
        value: 'keyword',
        label: 'keyword',
      }, {
        value: 'keyword_normalized',
        label: 'keyword(全大写处理)',
      }, {
        value: 'keyword_as_number',
        label: 'keyword as number',
      }, {
        value: 'date',
        label: 'date',
      }, {
        value: 'boolean',
        label: 'boolean',
      }, {
        value: 'object',
        label: 'object',
      }, {
        value: 'nested',
        label: 'nested',
      }, {
        value: 'long',
        label: 'long',
      }, {
        value: 'integer',
        label: 'integer',
      }, {
        value: 'short',
        label: 'short',
      }, {
        value: 'byte',
        label: 'byte',
      }, {
        value: 'double',
        label: 'double',
      }, {
        value: 'float',
        label: 'float',
      }],
    };
  },
  methods: {
    clusterChange() {
      this.versionInfo.nodes = [];
    },
    fieldTypeChange(row) {
      if (row.fieldType === 'text') {
        this.$set(row, 'docValue', false);
      } else if (row.fieldType === 'nested') {
        this.$set(row, 'search', false);
        this.$set(row, 'docValue', false);
        this.$set(row, 'multi', true);
      }
    },
    importSchema() {
      this.isSchemaImportVisible = true;
      this.schemaImportTitle = '导入schema';
      this.schemaImportUrl = `/pallas/index/version/schema_import.json?indexId=${this.versionInfo.indexId}`;
    },
    closeSchemaImportDialog() {
      this.isSchemaImportVisible = false;
    },
    schemaImportSuccess(data) {
      data.forEach((element1) => {
        this.versionInfo.schema.forEach((element2, index2) => {
          if (element1.fieldName === element2.fieldName) {
            this.versionInfo.schema.splice(index2, 1);
          }
        });
      });
      data.forEach((ele) => {
        this.versionInfo.schema.push(ele);
      });
      this.isSchemaImportVisible = false;
    },
    exportSchema() {
      window.location.href = `/pallas/index/version/schema_export.json?versionId=${this.versionInfo.versionId}`;
    },
    submitVersionInfo() {
      this.$refs.versionInfo.validate((valid) => {
        if (valid) {
          if (this.isSchemaSelectSearch() && !this.isSchemaSelectError()) {
            this.$set(this.versionInfo, 'allocationNodes', this.versionInfo.nodes.join(','));
            if (this.versionOperation === 'add' || this.versionOperation === 'copy') {
              this.loading = true;
              this.$http.post('/index/version/add.json', this.versionInfo).then(() => {
                this.$message.successMessage('保存版本成功', () => {
                  this.$emit('template-operate-success');
                });
              })
              .finally(() => {
                this.loading = false;
              });
            } else if (this.versionOperation === 'edit') {
              this.loading = true;
              this.$http.post('/index/version/update.json', this.versionInfo).then(() => {
                this.$message.successMessage('编辑版本成功', () => {
                  this.$emit('template-operate-success');
                });
              })
              .finally(() => {
                this.loading = false;
              });
            }
          }
        }
      });
    },
    isSchemaSelectError() {
      return this.versionInfo.schema.some((element) => {
        if (element.fieldName === '') {
          this.$message.errorMessage('字段名不允许为空！');
          return true;
        }
        if (element.fieldName === this.versionInfo.idField && element.fieldType !== 'long') {
          this.$message.errorMessage(`Id列名 ${element.fieldName} 的ES类型必须为long！`);
          return true;
        }
        if (element.fieldName === this.versionInfo.updateTimeField && element.fieldType !== 'date') {
          this.$message.errorMessage(`Update Time列名 ${element.fieldName} 的ES类型必须为date！`);
          return true;
        }
        return false;
      });
    },
    isSchemaSelectSearch() {
      let flag = true;
      const errorArray = [];
      const fieldTypeNestedErrorArray = [];
      this.versionInfo.schema.forEach((element) => {
        if (!element.search || !element.docValue) {
          if (this.versionInfo.routingKey === element.fieldName) {
            errorArray.push(this.versionInfo.routingKey);
          }
          if (this.versionInfo.idField === element.fieldName) {
            errorArray.push(this.versionInfo.idField);
          }
          if (this.versionInfo.updateTimeField === element.fieldName) {
            errorArray.push(this.versionInfo.updateTimeField);
          }
        }
        if (element.fieldType === 'nested' && !element.multi) {
          fieldTypeNestedErrorArray.push(element);
        }
      });
      if (errorArray.length > 0) {
        this.$message.errorMessage(`${Array.from(new Set(errorArray)).join(', ')}查询关键字、排序或聚合必须同时选中`);
        flag = false;
      }
      if (fieldTypeNestedErrorArray.length > 0) {
        this.$message.errorMessage(`${fieldTypeNestedErrorArray.map(v => v.fieldName).join(', ')}的ES类型为nested，请选为多值`);
        flag = false;
      }
      return flag;
    },
    closeDialog() {
      this.$emit('close-dialog');
      this.$refs.versionInfo.resetFields();
    },
    closeSchemaDialog() {
      this.isSchemaChildVisible = false;
    },
    viewSchemaChildren(row) {
      this.isSchemaChildVisible = true;
      this.schemaChildInfo = row;
    },
    addSchemaChild(array) {
      console.log(JSON.stringify(array));
      this.isSchemaChildVisible = false;
    },
    deleteField(row) {
      this.$message.confirmMessage(`确定删除字段${row.fieldName}吗?`, () => {
        this.$array.removeByValue(this.versionInfo.schema, row);
      });
    },
    addField(index) {
      const newRow = {
        fieldName: '',
        dbFieldType: 'N/A',
        docValue: false,
        fieldType: 'keyword',
        multi: false,
        children: [],
        search: false,
        isNew: true,
        dynamic: false,
      };
      this.versionInfo.schema.splice(index + 1, 0, newRow);
    },
  },
  computed: {
    isEditable() {
      return this.versionOperation === 'view';
    },
    routingKeyWidth() {
      let num;
      if (this.isMetaDataNull) {
        num = 12;
      } else {
        num = 8;
      }
      return num;
    },
    clusterNodes() {
      let arr = [];
      this.clusters.forEach((ele) => {
        if (ele.id === this.versionInfo.clusterId) {
          arr = ele.nodes;
        }
      });
      return arr;
    },
  },
};

</script>

<style scoped>
.label-title {
    height: 30px;
    line-height: 30px;
    margin-bottom: 5px;
    margin-left: 5px;
}
.label-title .span-title{
    color: #eee;
    font-size: 13px;
    margin-right: 5px;
    font-weight: bold;
}
.label-content {
  margin: 0 15px 20px;
}
.version-info-dialog .el-dialog__body {
  padding: 10px 5px 10px 0px;
}
.version-info-dialog .el-dialog__footer {
  padding: 10px 20px 0 0;
}

.version-info-dialog form{
  margin-bottom:0px;
}

.version-info-dialog .el-form-item {
  margin-bottom: 0px;
}

.red {
  color: red;
}

.version-info-dialog .el-button--text {
    color: #eee;
}

.version-info-dialog .el-button--text:focus, .el-button--text:hover {
    color: gray;
}

.version-info-dialog .el-checkbox {
    color: #eee;
}
</style>
