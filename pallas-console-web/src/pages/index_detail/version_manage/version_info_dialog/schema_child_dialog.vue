<template>
    <div class="schema-info-dialog">
        <el-dialog :title="childTitle" size="large" v-model="isSchemaChildVisible" @open="openDialog" :show-close="false" :close-on-click-modal="false" :close-on-press-escape="false">
            <div class="schema-content">
                <el-button type="primary" icon="plus" @click="handleAdd" v-if="!isEditable">新增字段</el-button>
                <span style="color: #fff;margin-left: 5px;" v-if="childInfo.length > 0">Dynamic: <el-switch v-model="initDynamic" :disabled="isEditable"></el-switch></span>
            </div>
            <el-table :data="childInfo" border style="width: 100%">
                <el-table-column label="字段名">
                    <template scope="scope">
                        <el-input class="nested-input" v-model="scope.row.fieldName" :disabled="isEditable"></el-input>
                        <el-tag type="success" v-if="checkArrayNotEmpty(scope.row.copyTo)">copy to: {{scope.row.copyTo}}</el-tag>
                        <el-button type="warning" @click="viewSchemaMultiFields(scope.row)" v-if="scope.row.multiField.length !== 0" ><i class="fa"></i>subFields</el-button>
                    </template>
                </el-table-column>
                <el-table-column label="ES类型">
                    <template scope="scope">
                        <select v-model="scope.row.fieldType" size="small" :disabled="isEditable">
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
                <el-table-column label="查询关键字">
                    <template scope="scope">
                        <el-checkbox v-model="scope.row.search" :disabled="isEditable">是否查询</el-checkbox>
                    </template>
                </el-table-column>
                <el-table-column label="排序或聚合">
                    <template scope="scope">
                        <el-checkbox v-model="scope.row.docValue" :disabled="isEditable">用于排序或聚合</el-checkbox>
                    </template>
                </el-table-column>
                <el-table-column v-if="!isEditable" label="操作" min-width="60">
                    <template scope="scope">
                        <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
                        <el-button type="warning" @click="viewSchemaCopyTo(scope.row)">copyTo</el-button>
                    </template>
                    <template scope="scope">
                        <el-dropdown trigger="click">
                              <span class="el-dropdown-link">
                                操作<i class="el-icon-caret-bottom el-icon--right"></i>
                              </span>
                            <el-dropdown-menu class="dropdown-operation" slot="dropdown">
                                <el-dropdown-item ><a @click="handleDelete(scope.row)"><span><i class="fa fa-play-circle"></i>删除</span></a></el-dropdown-item>
                                <el-dropdown-item ><a @click="viewSchemaMultiFields(scope.row)"><span><i class="fa fa-play-circle"></i>添加subFields</span></a></el-dropdown-item>
                                <el-dropdown-item ><a @click="viewSchemaCopyTo(scope.row)"><span><i class="fa fa-play-circle"></i>添加copyTo</span></a></el-dropdown-item>
                            </el-dropdown-menu>
                        </el-dropdown>
                    </template>
                </el-table-column>
            </el-table>
            <div slot="footer" class="dialog-footer">    
                     
              <el-button @click="cancelBtn()">取消</el-button>
              <el-button v-if="!isEditable" type="confirm" @click="confirmBtn()">确定</el-button>     
            </div>
        </el-dialog>
        <schema-multi-field-dialog :is-schema-multi-fields-visible="isSchemaMultiFieldsVisible" :schema-multi-fields-info="schemaExtInfo" :version-operation="versionOperation" :schema-parent-field-name="schemaParentFieldName" @close-schema-dialog="closeSchemaMultiFieldsDialog" @add-schema-multi-field="addSchemaMultiFields"></schema-multi-field-dialog>
        <schema-copy-to-dialog :is-copy-to-fields-visible="isCopyToFieldsVisible" :schema-copy-to-info="schemaExtInfo" :copy-to-list="validCopyToFields" :schema-parent-field-name="schemaParentFieldName" @close-schema-dialog="closeSchemaCopyToDialog" @add-schema-copy-to="addSchemaCopyTo" ></schema-copy-to-dialog>
    </div>
</template>

<script>
import SchemaCopyToDialog from './schema_copy_to_dialog';
import SchemaMultiFieldDialog from './schema_multi_field_dialog';

export default {
  components: {
    'schema-copy-to-dialog': SchemaCopyToDialog,
    'schema-multi-field-dialog': SchemaMultiFieldDialog,
  },
  props: ['isSchemaChildVisible', 'schemaChildInfo', 'versionOperation', 'schemaParentFieldName', 'versionInfo'],
  data() {
    return {
      childInfo: [],
      schemaExtInfo: {},
      validCopyToFields: [],
      isCopyToFieldsVisible: false,
      isSchemaMultiFieldsVisible: false,
      initDynamic: false,
      fieldTypes: [{
        value: 'text',
        label: 'text',
      }, {
        value: 'keyword',
        label: 'keyword',
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
    handleAdd() {
      const addChildInfo = {
        fieldName: '',
        fieldType: '',
        multi: '',
        copyTo: [],
        multiField: [],
        search: false,
        docValue: false,
      };
      this.childInfo.push(addChildInfo);
    },
    cancelBtn() {
      this.$emit('close-schema-dialog');
    },
    handleDelete(row) {
      this.$message.confirmMessage(`确定删除字段${row.fieldName}吗?`, () => {
        this.$array.removeByValue(this.childInfo, row);
      });
    },
    confirmBtn() {
      if (this.checkChildInput(this.childInfo)) {
        this.schemaChildInfo.children.splice(0, this.schemaChildInfo.children.length);
        this.childInfo.forEach((element) => {
          this.schemaChildInfo.children.push(element);
        });
        if (this.schemaChildInfo.children.length === 0) {
          this.schemaChildInfo.dynamic = false;
        } else {
          this.schemaChildInfo.dynamic = this.initDynamic;
        }
        this.$emit('add-schema-child', this.schemaChildInfo.children);
      }
    },
    checkChildInput(arr) {
      let flag = true;
      Object.keys(arr).forEach((element, index) => {
        if (arr[index].fieldName === '') {
          this.$message.errorMessage('字段名不能为空');
          flag = false;
        } else {
          flag = true;
        }
      });
      return flag;
    },
    openDialog() {
      this.childInfo = JSON.parse(JSON.stringify(this.schemaChildInfo.children));
      Object.keys(this.childInfo).forEach((element, index) => {
        this.childInfo[index].multiField =
          this.childInfo[index].multiField || [];
      });
      this.initDynamic = this.schemaChildInfo.dynamic;
    },
    checkArrayNotEmpty(arr) {
      return arr && arr.length > 0;
    },
    viewSchemaMultiFields(row) {
      this.isSchemaMultiFieldsVisible = true;
      this.schemaExtInfo = row;
    },
    viewSchemaCopyTo(row) {
      this.schemaExtInfo = row;
      this.validCopyToFields = [];
      this.versionInfo.schema.forEach((el) => {
        if (el.dbFieldType === 'N/A') {
          if (el.fieldType === 'nested') {
            // skip current nested doc
            if (el.fieldName === this.schemaChildInfo.fieldName) return;
            this.getNestedFieldName(el, '', this.validCopyToFields);
            return;
          }
          this.validCopyToFields.push(el.fieldName);
        }
      });
      // get the tmp nested field
      this.childInfo.forEach((el) => {
        if (el.fieldType === 'nested') {
          this.getNestedFieldName(el, this.schemaChildInfo.fieldName, this.validCopyToFields);
          return;
        }
        this.validCopyToFields.push(`${this.schemaChildInfo.fieldName}.${el.fieldName}`);
      });
      this.copyToListFilter(this.validCopyToFields, `${this.schemaChildInfo.fieldName}.${row.fieldName}`);
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
    closeSchemaCopyToDialog() {
      this.isCopyToFieldsVisible = false;
    },
    closeSchemaMultiFieldsDialog() {
      this.isSchemaMultiFieldsVisible = false;
    },
    addSchemaCopyTo(array) {
      console.log(JSON.stringify(array));
      this.isCopyToFieldsVisible = false;
    },
    addSchemaMultiFields(array) {
      console.log(JSON.stringify(array));
      this.isSchemaMultiFieldsVisible = false;
    },
  },
  computed: {
    isEditable() {
      return this.versionOperation === 'view';
    },
    childTitle() {
      const title = `${this.schemaChildInfo.fieldName}子字段`;
      return title;
    },
  },
};

</script>

<style type="text/css">
.schema-content {
  margin-bottom: 10px;
}
.nested-input {
  width: 50%;
}
.schema-info-dialog .el-dialog__footer {
  padding: 10px 20px 15px;
}
</style>
