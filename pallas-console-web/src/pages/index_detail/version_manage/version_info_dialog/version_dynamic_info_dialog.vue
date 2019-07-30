  <template>
    <div class="version-info-dialog">
        <el-dialog size="large" v-model="isVersionInfoVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
            <span slot="title">
              <span>{{versionInfoTitle}}</span>
            </span>
            <el-form :model="versionInfo" :rules="rules" ref="versionInfo" label-position="left">
                <el-tabs value="first">
                    <el-tab-pane label="索引配置" name="first">
                        <div class="label-title">
                                    <span class="span-title"><i class="fa fa-th-large"></i>分片路由属性</span>
                          <span v-if="isLogical" style="color: #C8C8C8;">（所属集群：{{clusterArray.join()}}）</span>
                        </div>
                        <div class="label-content">
                            <el-row :gutter="20">
                                <el-col :span="12">
                                    <el-form-item label="复制数量" prop="replicationNum" label-width="120px">
                                        <el-input v-model.number="versionInfo.replicationNum"></el-input>
                                    </el-form-item>
                                </el-col>
                                <el-col :span="12">
                                    <el-form-item label="ShardPerNode" prop="totalShardsPerNode" label-width="120px">
                                        <el-input v-model.number="versionInfo.totalShardsPerNode"></el-input>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </div>
                        <div class="label-content">
                            <el-row :gutter="20" v-if="isLogical">
                                <el-col :span="24">
                                    <el-form-item label="所属节点" prop="nodes" label-width="120px">
                                        <el-select multiple filterable value-key="name" v-model="versionInfo.nodes" placeholder="请选择机器"  style="width: 100%">
                                            <el-option-group v-for="group in clusterGroups" :key="group.clusterId" :label="group.clusterId">
                                                <el-option v-for="item in group.nodes" :key="item.name" :label="item.name" :value="item">
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
                                            </el-option-group>
                                        </el-select>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                            <el-row :gutter="20" v-else>
                                <el-col :span="24">
                                    <el-form-item label="所属节点" prop="nodes" label-width="120px">
                                        <el-select multiple filterable v-model="versionInfo.nodes" placeholder="请选择机器"  style="width: 100%">
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
                        <div class="label-title"><span class="span-title"><i class="fa fa-th-large"></i>慢日志落盘配置（单位ms，-1不限定）</span></div>
                        <div class="label-content">
                            <el-row :gutter="20">
                                <el-col :span="8">
                                    <el-form-item label="Index Slow" prop="indexSlowThreshold" label-width="120px">
                                        <el-input v-model.number="versionInfo.indexSlowThreshold" ></el-input>
                                    </el-form-item>
                                </el-col>
                                <el-col :span="8">
                                    <el-form-item label="Fetch Slow" prop="fetchSlowThreshold" label-width="120px">
                                        <el-input v-model.number="versionInfo.fetchSlowThreshold" ></el-input>
                                    </el-form-item>
                                </el-col>
                                <el-col :span="8">
                                    <el-form-item label="Query Slow" prop="querySlowThreshold" label-width="120px">
                                        <el-input v-model.number="versionInfo.querySlowThreshold" ></el-input>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </div>
                        <div class="label-title"><span class="span-title"><i class="fa fa-th-large"></i>索引其他配置</span></div>
                        <div class="label-content">
                            <el-row :gutter="20">
                                <el-col :span="12">
                                    <el-form-item label="max_result_window" prop="maxResultWindow" label-width="180px">
                                        <el-input placeholder="10000" v-model.number="versionInfo.maxResultWindow" ></el-input>
                                    </el-form-item>
                                </el-col>
                                <el-col :span="12">
                                    <el-form-item label="flush_threshold_size" prop="flushThresholdSize" label-width="180px">
                                        <el-input placeholder="512mb" v-model="versionInfo.flushThresholdSize" ></el-input>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </div>
                        <div class="label-content">
                            <el-row :gutter="20">
                                <el-col :span="12">
                                    <el-form-item label="sync_interval" prop="syncInterval" label-width="180px">
<!--                                        该字段无法动态更新，在这里纯展示，不做更新操作-->
                                        <el-input v-model="versionInfo.syncInterval" :disabled="true" ></el-input>
                                    </el-form-item>
                                </el-col>
                                <el-col :span="12">
                                    <el-form-item label="translog_durability" prop="translogDurability" label-width="180px">
                                        <el-input placeholder="async" v-model="versionInfo.translogDurability" ></el-input>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </div>
                        <div class="label-content">
                            <el-row :gutter="20">
                                <el-col :span="8">
                                    <el-form-item label="refresh（秒）" prop="refreshInterval" label-width="120px">
                                        <el-input v-model.number="versionInfo.refreshInterval" ></el-input>
                                    </el-form-item>
                                </el-col>
                            </el-row>
                        </div>
                    </el-tab-pane>
                    <el-tab-pane label="Mapping配置" name="second">
                        <div class="label-title">
                            <span class="span-title"><i class="fa fa-th-large"></i>ES映射关系配置</span>
                            <el-button size="mini" type="success" @click="addField(0)"><i class="fa fa-plus"></i>新增</el-button>
                        </div>
                        <div>
                            <div style="margin: 10px">
                                <el-alert
                                        title="如何选择ES类型"
                                        type="info"
                                        description=" "
                                        show-icon>
                                    <div style="font-size: 12px">
                                        1.某些数据库字段，尽管是number类型，但是在做业务查询时仅仅只是做term(s)这类非数学运算非聚合查询，我们非常建议你采用"keyword as number"类型，在这个类型下，ES将会用string格式来建索引以达到更高的检索性能，
                                        而获取 _source 时我们仍然会以number的格式返回给 Client。
                                        <br/>
                                        2.当你需要做模糊匹配，比如数据库值是 AbC，但是仍然希望abc和ABC都能检索出来，那请选择"keyword[全大写处理]"类型，我们在建索引和查询都做大写处理，
                                        而获取 _source 时我们仍然会以原值 AbC 的格式返回给 Client。
                                        <br/>
                                        3.所有的DB类型为TINYINT 的字段，我们都假设它是一些枚举值并且不会用于数学运算，因此我们为这些字段自动匹配了"keyword as number"类型，请自行检查。
                                    </div>
                                </el-alert>
                            </div>
                            <el-table :data="versionInfo.schema" border style="width: 100%" :max-height="550">
                                <el-table-column label="操作">
                                    <template slot-scope="scope">
                                        <el-button size="small" type="success" @click="addField(scope.$index)"><i class="el-icon-plus"></i></el-button>
                                        <el-button size="small" type="danger" @click="deleteField(scope.row)"><i class="el-icon-minus"></i></el-button>
                                    </template>
                                </el-table-column>
                                <el-table-column label="字段名" min-width="180">
                                    <template slot-scope="scope">
                                        <el-button type="text" v-show="!scope.row.isNew">
                                            <span>{{scope.row.fieldName}}</span>
                                        </el-button>
                                        <el-input style="width:50%" v-model="scope.row.fieldName" placeholder="请输入字段名" v-show="scope.row.isNew"></el-input>
                                        <el-button type="text" v-show="scope.row.isNew">
                                            <span>子字段</span>
                                        </el-button>
                                        <el-button type="warning" size="mini" @click="viewSchemaChildrenWithNoCheck(scope.row)" v-if="scope.row.children.length !== 0" ><i class="fa"></i>nested</el-button>
                                        <el-button type="warning" size="mini" @click="viewSchemaMultiFields(scope.row)" v-if="scope.row.multiField.length !== 0" ><i class="fa"></i>subFields</el-button>
                                        <div>
                                            <el-tag type="success" v-if="scope.row.copyTo.length > 0">copy to: {{scope.row.copyTo}}</el-tag>
                                        </div>
                                    </template>
                                </el-table-column>
                                <el-table-column label="ES类型">
                                    <template slot-scope="scope">
                                        <select v-model="scope.row.fieldType" size="small" @change="fieldTypeChange(scope.row)">
                                            <option v-for="item in fieldTypes" :value="item.value" :key="item.value">{{item.label}}</option>
                                        </select>
                                    </template>
                                </el-table-column>
                                <el-table-column label="多值/单值">
                                    <template slot-scope="scope">
                                        <select v-model="scope.row.multi" size="small">
                                            <option label="单值" :value="false">单值</option>
                                            <option label="多值" :value="true">多值</option>
                                        </select>
                                    </template>
                                </el-table-column>
                                <el-table-column label="是否创建索引" min-width="90">
                                    <template slot-scope="scope">
                                        <el-checkbox v-model="scope.row.search" :disabled="scope.row.fieldType === 'nested'">创建索引</el-checkbox>
                                    </template>
                                </el-table-column>
                                <el-table-column :render-header="renderDocValueHeader">
                                    <template slot-scope="scope">
                                        <el-checkbox v-model="scope.row.docValue" :disabled="scope.row.fieldType === 'nested' || scope.row.fieldType === 'text'">启用doc value</el-checkbox>
                                    </template>
                                </el-table-column>
                                <el-table-column label="是否启用store">
                                    <template slot-scope="scope">
                                        <el-checkbox v-model="scope.row.store">启用store</el-checkbox>
                                    </template>
                                </el-table-column>
                                <el-table-column label="更多操作" width="80">
                                    <template slot-scope="scope">
                                        <el-dropdown trigger="click">
                              <span class="el-dropdown-link">
                                操作<i class="el-icon-caret-bottom el-icon--right"></i>
                              </span>
                                            <el-dropdown-menu class="dropdown-operation" slot="dropdown">
                                                <el-dropdown-item v-if="scope.row.multiField.length === 0"><a @click="viewSchemaChildren(scope.row)"><span><i class="fa fa-play-circle"></i>添加nested/object</span></a></el-dropdown-item>
                                                <el-dropdown-item v-if="scope.row.children.length === 0 && scope.row.fieldType !== 'nested'"><a @click="viewSchemaMultiFields(scope.row)"><span><i class="fa fa-play-circle"></i>添加subFields</span></a></el-dropdown-item>
                                                <el-dropdown-item><a @click="viewSchemaCopyTo(scope.row)"><span><i class="fa fa-play-circle"></i>添加copyTo</span></a></el-dropdown-item>
                                            </el-dropdown-menu>
                                        </el-dropdown>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>
                    </el-tab-pane>
                </el-tabs>


            </el-form>
            <div slot="footer" class="dialog-footer">                
                <el-button @click="closeDialog()">取 消</el-button>
                <el-button type="confirm" @click="submitVersionInfo()">确 定</el-button>
            </div>
        </el-dialog>
        <schema-child-dialog :is-schema-child-visible="isSchemaChildVisible" :schema-child-info="schemaExtInfo" :version-operation="versionOperation" :schema-parent-field-name="schemaParentFieldName" :version-info="versionInfo" @close-schema-dialog="closeSchemaDialog" @add-schema-child="addSchemaChild"></schema-child-dialog>
        <schema-multi-field-dialog :is-schema-multi-fields-visible="isSchemaMultiFieldsVisible" :schema-multi-fields-info="schemaExtInfo" :version-operation="versionOperation" :schema-parent-field-name="schemaParentFieldName" @close-schema-dialog="closeSchemaMultiFieldsDialog" @add-schema-multi-field="addSchemaMultiFields"></schema-multi-field-dialog>
        <schema-copy-to-dialog :is-copy-to-fields-visible="isCopyToFieldsVisible" :schema-copy-to-info="schemaExtInfo" :copy-to-list="validCopyToFields" :schema-parent-field-name="schemaParentFieldName" @close-schema-dialog="closeSchemaCopyToDialog" @add-schema-copy-to="addSchemaCopyTo" ></schema-copy-to-dialog>
    </div>
</template>

<script>
import SchemaChildDialog from './schema_child_dialog';
import SchemaMultiFieldDialog from './schema_multi_field_dialog';
import SchemaCopyToDialog from './schema_copy_to_dialog';

export default {
  components: {
    'schema-child-dialog': SchemaChildDialog,
    'schema-multi-field-dialog': SchemaMultiFieldDialog,
    'schema-copy-to-dialog': SchemaCopyToDialog,
  },
  props: ['versionOperation', 'versionInfo', 'versionInfoTitle', 'clusters', 'isLogical'],
  data() {
    return {
      loading: false,
      isVersionInfoVisible: true,
      isSchemaChildVisible: false,
      isSchemaMultiFieldsVisible: false,
      isCopyToFieldsVisible: false,
      schemaExtInfo: {},
      validCopyToFields: [],
      schemaParentFieldName: '',
      rules: {
        replicationNum: [{ required: true, message: '复制数量不能为空' }, { type: 'number', message: '复制数量必须为数字值' }],
        indexSlowThreshold: [{ required: true, message: 'Index Slow Log不能为空' }, { type: 'number', message: 'Index Slow Log必须为数字值' }],
        fetchSlowThreshold: [{ required: true, message: 'Fetch Slow Log不能为空' }, { type: 'number', message: 'Fetch Slow Log必须为数字值' }],
        querySlowThreshold: [{ required: true, message: 'Query Slow Log不能为空' }, { type: 'number', message: 'Query Slow Log必须为数字值' }],
        refreshInterval: [{ required: true, message: 'refresh_interval不能为空' }, { type: 'number', message: 'refresh_interval必须为数字值' }],
        maxResultWindow: [{ required: true, message: 'max_result_window不能为空' }, { type: 'number', message: 'max_result_window必须为数字值' }],
        totalShardsPerNode: [{ required: true, message: 'total_shards_per_node不能为空' }, { type: 'number', message: 'total_shards_per_node必须为数字值' }],
        flushThresholdSize: [{ required: true, message: 'flush_threshold_size不能为空' }],
        translogDurability: [{ required: true, message: 'translog_durability不能为空' }],
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
    fieldTypeChange(row) {
      if (row.fieldType === 'text') {
        this.$set(row, 'docValue', false);
      } else if (row.fieldType === 'nested') {
        this.$set(row, 'search', false);
        this.$set(row, 'docValue', false);
        this.$set(row, 'multi', true);
      }
    },
    renderDocValueHeader(h) {
      return h(
        'span',
        [
          h('span', { slot: 'reference', style: { 'font-size': '14px', 'margin-right': '5px' } }, '是否启用doc value'),
          h('el-popover', { props: { placement: 'top', trigger: 'hover' } },
            [
              h('div', '是否需要用到 Sort,Aggs,Script 查询'),
              h('i', { slot: 'reference', class: { fa: true, 'fa-question-circle': true } }),
            ],
          ),
        ],
      );
    },
    closeSchemaDialog() {
      this.isSchemaChildVisible = false;
    },
    closeSchemaMultiFieldsDialog() {
      this.isSchemaMultiFieldsVisible = false;
    },
    closeSchemaCopyToDialog() {
      this.isCopyToFieldsVisible = false;
    },
    viewSchemaChildrenWithNoCheck(row) {
      this.isSchemaChildVisible = true;
      this.schemaExtInfo = row;
    },
    viewSchemaChildren(row) {
      if (row.fieldType !== 'nested' && row.fieldType !== 'object') {
        this.$message.errorMessage('ES类型必须为nested或者object');
        return;
      }
      this.isSchemaChildVisible = true;
      this.schemaExtInfo = row;
    },
    viewSchemaMultiFields(row) {
      this.isSchemaMultiFieldsVisible = true;
      this.schemaExtInfo = row;
    },
    viewSchemaCopyTo(row) {
      this.schemaExtInfo = row;
      this.validCopyToFields = [];
      this.versionInfo.schema.forEach((el) => {
        if (el.dbFieldType === 'N/A' || el.dbFieldType === null) {
          if (el.fieldType === 'nested') {
            this.getNestedFieldName(el, '', this.validCopyToFields);
            return;
          }
          this.validCopyToFields.push(el.fieldName);
        }
      });
      this.copyToListFilter(this.validCopyToFields, row.fieldName);
      this.isCopyToFieldsVisible = true;
    },
    getNestedFieldName(field, parentFieldName, fieldArr) {
      if (field.fieldType !== 'nested') {
        fieldArr.push(`${parentFieldName}${field.fieldName}`);
        return;
      }
      field.children.forEach((child) => {
        if (child.fieldType === 'nested') {
          this.getNestedFieldName(child, `${parentFieldName}${field.fieldName}.`, fieldArr);
        } else {
          fieldArr.push(`${parentFieldName}${field.fieldName}.${child.fieldName}`);
        }
      });
    },
    copyToListFilter(fieldArr, fieldName) {
      const index = fieldArr.indexOf(fieldName);
      if (index >= 0) {
        fieldArr.splice(index, 1);
      }
    },
    addSchemaChild() {
      this.isSchemaChildVisible = false;
    },
    addSchemaMultiFields(array) {
      console.log(JSON.stringify(array));
      this.isSchemaMultiFieldsVisible = false;
    },
    addSchemaCopyTo(array) {
      console.log(JSON.stringify(array));
      this.isCopyToFieldsVisible = false;
    },
    addField(index) {
      const newRow = {
        fieldName: '',
        dbFieldType: 'N/A',
        docValue: false,
        fieldType: 'keyword',
        multi: false,
        children: [],
        multiField: [],
        copyTo: [],
        search: false,
        isNew: true,
        dynamic: false,
        store: false,
      };
      this.versionInfo.schema.splice(index + 1, 0, newRow);
    },
    getLogicClusterAllocationNodes() {
      const result = [];
      this.clusterArray.forEach((ele) => {
        const obj = {
          cluster: ele,
          nodes: this.versionInfo.nodes.filter(e => ele === e.parent).map(v => v.name),
        };
        result.push(obj);
      });
      let str = '';
      result.forEach((element) => {
        str += `${element.cluster}:${element.nodes.join()};`;
      });
      return str;
    },
    submitVersionInfo() {
      this.$refs.versionInfo.validate((valid) => {
        if (valid) {
          if (this.isLogical) {
            this.$set(this.versionInfo, 'allocationNodes', this.getLogicClusterAllocationNodes());
          } else {
            this.$set(this.versionInfo, 'allocationNodes', this.versionInfo.nodes.join(','));
          }
          this.loading = true;
          this.$http.post('/index/version/dynamic_update.json', this.versionInfo).then(() => {
            this.$message.successMessage('编辑版本成功', () => {
              this.$emit('template-operate-success');
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
      this.$refs.versionInfo.resetFields();
    },
  },
  computed: {
    clusterArray() {
      const arr = this.clusters.map(e => e.clusterId);
      return arr;
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
    clusterGroups() {
      const result = this.clusters.map((obj) => {
        const rObj = { ...obj };
        rObj.nodes = obj.nodes.map((obj2) => {
          const rObj2 = { ...obj2 };
          rObj2.parent = obj.clusterId;
          return rObj2;
        });
        return rObj;
      });
      return result;
    },
  },
  created() {
    this.versionInfo.schema = [];
  },
};

</script>

<style>
.label-title {
    height: 30px;
    line-height: 30px;
    margin-bottom: 5px;
    padding-left: 5px;
    background-color: #333;
}
.label-title .span-title{
    color: #eee;
    font-size: 15px;
    margin-right: 5px;
}
.label-content {
  margin: 10px 15px 20px;
}
.label-content span {
  color: #eee;
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
