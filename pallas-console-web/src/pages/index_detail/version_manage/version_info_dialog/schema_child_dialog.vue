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
                        <el-input v-model="scope.row.fieldName" :disabled="isEditable"></el-input>
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
                    </template>
                </el-table-column>
            </el-table>
            <div slot="footer" class="dialog-footer">    
                     
              <el-button @click="cancelBtn()">取消</el-button>
              <el-button v-if="!isEditable" type="confirm" @click="confirmBtn()">确定</el-button>     
            </div>
        </el-dialog>
    </div>
</template>

<script>
export default {
  props: ['isSchemaChildVisible', 'schemaChildInfo', 'versionOperation', 'schemaParentFieldName'],
  data() {
    return {
      childInfo: [],
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
      this.initDynamic = this.schemaChildInfo.dynamic;
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
.schema-info-dialog .el-dialog__footer {
  padding: 10px 20px 15px;
}
</style>
