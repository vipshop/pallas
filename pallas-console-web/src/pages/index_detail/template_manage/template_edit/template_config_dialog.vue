<template>
    <el-dialog title="新模板向导" size="large" v-model="visible" :before-close="handleClose" v-loading="loading" element-loading-text="请稍等···">
        <div class="template-config">
            <el-row :gutter="30">
                <el-col :span="12">
                    <div class="template-config-content">
                        <div class="title">
                          <span>模板选项</span><a style="margin-left: 10px;font-size: 13px; font-weight: normal;color: dodgerblue;" target="_blank" href="https://vipshop.github.io/pallas/#/zh-cn/1.x/Console%E6%A8%A1%E5%9D%97/%E6%A8%A1%E6%9D%BF%E7%AE%A1%E7%90%86/template">Mustache语法  <i class="fa fa-external-link" aria-hidden="true"></i></a>
                          <el-button class="pull-right" type="success" size="mini" @click="handleExecute"><i class="fa fa-caret-square-o-right"></i>调试</el-button>
                        </div>
                        <div style="margin-top: 15px;" class="">
                          <el-form label-width="0" label-position="left">
                              <el-row :gutter="20">
                                  <el-col :span="12">
                                      <el-form-item>
                                          <el-checkbox v-model="data.from.isChecked">from
                                              <el-input v-model="data.from.value"></el-input>
                                          </el-checkbox>
                                      </el-form-item>
                                  </el-col>
                                  <el-col :span="12">
                                      <el-form-item>
                                            <el-checkbox v-model="data.size.isChecked" style="margin-left: 5px;">size
                                                <el-input v-model="data.size.value"></el-input>
                                            </el-checkbox>
                                        </el-form-item>
                                  </el-col>
                              </el-row>
                              <el-row :gutter="30">
                                  <el-col :span="12">
                                      <el-form-item>
                                          <el-checkbox v-model="data.sort.isChecked">sort
                                              <el-switch v-model="data.sort.isVariable" on-text="变量" off-text="静态"></el-switch>
                                          </el-checkbox>
                                      </el-form-item>
                                  </el-col>
                                  <el-col :span="12">
                                      <el-form-item>
                                            <el-checkbox v-model="data.source.isChecked">source
                                                <el-switch v-model="data.source.isVariable" on-text="变量" off-text="静态"></el-switch>
                                            </el-checkbox>
                                        </el-form-item>
                                  </el-col>
                              </el-row>
                              <div style="margin-bottom: 5px">
                                  <span><i class="fa fa-th-large"></i>query配置</span>
                              </div>
                              <el-table border :data="metadatas" :max-height="320">
                                  <el-table-column prop="dbFieldName" label="字段名" width="180"></el-table-column>
                                  <el-table-column label="查询类型">
                                      <template scope="scope">
                                          <el-radio class="radio" v-model="scope.row.queryWay" label="">不选</el-radio>
                                          <el-radio class="radio" v-model="scope.row.queryWay" label="term">term</el-radio>
                                          <el-radio class="radio" v-model="scope.row.queryWay" label="multiTerm">terms</el-radio>
                                          <el-radio class="radio" v-model="scope.row.queryWay" label="range">range</el-radio>
                                          <el-radio class="radio" v-model="scope.row.queryWay" label="script">script</el-radio>
                                      </template>
                                  </el-table-column>
                              </el-table>
                          </el-form>
                        </div>
                    </div>
                </el-col>
                <el-col :span="12">
                    <div class="template-config-content">
                      <div class="title">模板展示</div>
                      <div style="height: 450px;">
                        <el-scrollbar>
                          <pre>{{templateContent}}</pre>
                        </el-scrollbar>
                      </div>
                    </div>
                </el-col>
            </el-row>
        </div>
        <div slot="footer" class="dialog-footer">    
            <el-button @click="handleClose()">取消</el-button>
            <el-button type="confirm" @click="handleSave" v-if="JSON.stringify(templateContent) !== '{}'">生成模板</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['metadataList'],
  data() {
    return {
      loading: false,
      visible: true,
      data: {
        from: { isChecked: false, value: '0' },
        size: { isChecked: false, value: '100' },
        sort: { isChecked: false, isVariable: true },
        source: { isChecked: false, isVariable: true },
      },
      templateContent: {},
      metadatas: [],
      queryBody: '',
    };
  },
  methods: {
    handleExecute() {
      this.queryBody = '';
      const obj = {};
      if (this.data.from.isChecked) {
        this.$set(obj, 'from', `<%{{from}}{{^from}}${this.data.from.value}{{/from}}%>`);
      }
      if (this.data.size.isChecked) {
        this.$set(obj, 'size', `<%{{size}}{{^size}}${this.data.size.value}{{/size}}%>`);
      }
      if (this.data.sort.isChecked) {
        if (this.data.sort.isVariable) {
          this.$set(obj, '<%{{#sort}}<--', 'TAG-->');
          this.$set(obj, 'sort', '<%{{#toJson}}sort.list{{/toJson}}%>');
          this.$set(obj, '<%{{/sort}}<--', 'TAG-->');
        } else {
          this.$set(obj, 'sort', [{ id: 'desc' }]);
        }
      }
      if (this.data.source.isChecked) {
        if (this.data.source.isVariable) {
          this.$set(obj, '<%{{#source_fields}}<--', 'TAG-->');
          this.$set(obj, '_source', '<%{{#toJson}}source_fields.list{{/toJson}}%>');
          this.$set(obj, '<%{{/source_fields}}<--', 'TAG-->');
        } else {
          this.$set(obj, '_source', ['id', 'update_time']);
        }
      }
      const isQuery = this.metadatas.some(ele => ele.queryWay);
      if (isQuery) {
        this.$set(obj, 'query', { bool: { filter: ['QUERY-BODY'] } });
        const resultArray = this.metadatas.filter(e => e.queryWay !== '');
        resultArray.forEach((ele, index) => {
          let frontSpace = '{}\n        ';
          let endNewline = '\n';
          if (index > 0) {
            frontSpace = '        ';
          }
          if (index === resultArray.length - 1) {
            endNewline = '';
          }
          switch (ele.queryWay) {
            case 'term':
              this.queryBody += `${frontSpace}{{#${ele.dbFieldName}}}\n`
                             + '        ,{\n'
                             + `          "term":{"${ele.dbFieldName}":"{{${ele.dbFieldName}}}"}\n`
                             + '        }\n'
                             + `        {{/${ele.dbFieldName}}}${endNewline}`;
              break;
            case 'multiTerm':
              this.queryBody += `${frontSpace}{{#${ele.dbFieldName}}}\n`
                             + '        ,{\n'
                             + `          "terms":{ "${ele.dbFieldName}":{{#toJson}}${ele.dbFieldName}.list{{/toJson}} }\n`
                             + '        }\n'
                             + `        {{/${ele.dbFieldName}}}${endNewline}`;
              break;
            case 'range':
              this.queryBody += `${frontSpace}{{#${ele.dbFieldName}_min}}\n`
                             + '        ,{\n'
                             + '          "range": {\n'
                             + `            "${ele.dbFieldName}": {\n`
                             + `              "from": "{{${ele.dbFieldName}_min}}",\n`
                             + `              "to": "{{${ele.dbFieldName}_max}}"\n`
                             + '            }\n'
                             + '          }\n'
                             + '        }\n'
                             + `        {{/${ele.dbFieldName}_min}}${endNewline}`;
              break;
            case 'script':
              this.queryBody += `${frontSpace}{{#${ele.dbFieldName}}}\n`
                             + '        ,{\n'
                             + '          "script": {\n'
                             + '            "script": {\n'
                             + '              "lang": "painless",\n'
                             + `              "inline": "return doc['${ele.dbFieldName}'].value > 0"\n`
                             + '            }\n'
                             + '          }\n'
                             + '        }\n'
                             + `        {{/${ele.dbFieldName}}${endNewline}`;
              break;
            default:
              break;
          }
        });
      }
      const expr1 = /"<%/g;
      const expr2 = /%>"/g;
      const expr3 = /<--": "TAG-->",|<--": "TAG-->"/g;
      const expr4 = /"QUERY-BODY"/g;
      this.templateContent = JSON.stringify(obj, undefined, 2);
      this.templateContent =
      this.templateContent.replace(expr1, '').replace(expr2, '').replace(expr3, '').replace(expr4, this.queryBody);
    },
    handleClose() {
      this.$emit('close-dialog');
    },
    handleSave() {
      this.$emit('cover-content', this.templateContent);
    },
  },
  created() {
    this.metadatas = this.metadataList.map((obj) => {
      const rObj = { ...obj };
      rObj.queryWay = '';
      return rObj;
    });
  },
};
</script>
<style type="text/css" scoped>
.template-config {
  height: 500px;
  margin: 15px;
  color: #fff;
}
.template-config-content .title {
  font-weight: bold;
  font-size: 16px;
}
</style>
