<template>
    <el-dialog title="查询变量插入" class="template-insert-dialog" style="min-width: 700px" v-model="visible" :before-close="handleClose" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="info" ref="info" label-width="100px">
            <el-row>
                <el-col :span="23">
                    <el-form-item prop="field" label="查询变量">
                        <el-select v-model="info.field" style="width: 100%;">
                            <el-option label="from" value="from"></el-option>
                            <el-option label="size" value="size"></el-option>
                            <el-option label="sort" value="sort"></el-option>
                            <el-option label="source" value="source"></el-option>
                            <el-option label="query" value="query"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="23">
                    <el-form-item prop="from" label="from" v-if="info.field === 'from'">
                        <el-input-number v-model="info.from" style="width: 100%;"></el-input-number>
                    </el-form-item>
                    <el-form-item prop="size" label="size" v-if="info.field === 'size'">
                        <el-input-number v-model="info.size" style="width: 100%;"></el-input-number>
                    </el-form-item>
                    <el-form-item prop="sort" label="sort" v-if="info.field === 'sort'">
                        <div class="my-switch">
                            <el-switch v-model="info.sort" on-text="变量" off-text="静态"></el-switch>
                        </div>
                    </el-form-item>
                    <el-form-item prop="source" label="source" v-if="info.field === 'source'">
                        <div class="my-switch">
                            <el-switch v-model="info.source" on-text="变量" off-text="静态"></el-switch>
                        </div>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
              <el-col :span="23">
                  <el-form-item label-width="30px">
                      <el-table border :data="metadatas" :max-height="320" v-if="info.field === 'query'">
                          <el-table-column prop="field" label="字段名" width="130" show-overflow-tooltip></el-table-column>
                          <el-table-column label="查询方式">
                          <template slot-scope="scope">
                              <el-radio class="radio" v-model="scope.row.queryWay" label="term">term</el-radio>
                              <el-radio class="radio" v-model="scope.row.queryWay" label="multiTerm">多值term</el-radio>
                              <el-radio class="radio" v-model="scope.row.queryWay" label="range">range</el-radio>
                              <el-radio class="radio" v-model="scope.row.queryWay" label="script">script</el-radio>
                          </template>
                          </el-table-column>
                      </el-table>
                    </el-form-item>
              </el-col>
            </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">    
           <el-button @click="handleClose()">取消</el-button>
           <el-button type="confirm" @click="handleInsert()">插入查询变量</el-button> 
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['fieldList'],
  data() {
    return {
      loading: false,
      visible: true,
      info: {
        field: 'from',
        from: 0,
        size: 100,
        sort: true,
        source: true,
      },
      metadatas: [],
      resultContent: '',
    };
  },
  methods: {
    handleInsert() {
      this.$refs.info.validate((valid) => {
        if (valid) {
          if (this.info.field === 'from') {
            this.resultContent = `"from": {{from}}{{^from}}${this.info.from}{{/from}},`;
            this.insertContent();
          } else if (this.info.field === 'size') {
            this.resultContent = `"size": {{size}}{{^size}}${this.info.size}{{/size}},`;
            this.insertContent();
          } else if (this.info.field === 'sort') {
            if (this.info.sort) {
              this.resultContent = '{{#sort}}\n  "sort": {{#toJson}}sort.list{{/toJson}},\n  {{/sort}}';
            } else {
              this.resultContent = '"sort": [{"id":"desc"}],';
            }
            this.insertContent();
          } else if (this.info.field === 'source') {
            if (this.info.source) {
              this.resultContent = '{{#source_fields}}\n  "_source": {{#toJson}}source_fields.list{{/toJson}},\n  {{/source_fields}}';
            } else {
              this.resultContent = '"_source": [{"id","update_time"}],';
            }
            this.insertContent();
          } else {
            const isQuery = this.metadatas.some(ele => ele.queryWay);
            if (isQuery) {
              const resultArray = this.metadatas.filter(e => e.queryWay !== '');
              resultArray.forEach((ele, index) => {
                let frontSpace = '';
                let endNewline = '\n';
                if (index > 0) {
                  frontSpace = '        ';
                }
                if (index === resultArray.length - 1) {
                  endNewline = '';
                }
                switch (ele.queryWay) {
                  case 'term':
                    this.resultContent += `${frontSpace}{{#${ele.field}}}\n`
                                  + '        ,{\n'
                                  + `          "term":{"${ele.field}":"{{${ele.field}}}"}\n`
                                  + '        }\n'
                                  + `        {{/${ele.field}}}${endNewline}`;
                    break;
                  case 'multiTerm':
                    this.resultContent += `${frontSpace}{{#${ele.field}}}\n`
                                  + '        ,{\n'
                                  + `          "terms":{ "${ele.field}":{{#toJson}}${ele.field}.list{{/toJson}} }\n`
                                  + '        }\n'
                                  + `        {{/${ele.field}}}${endNewline}`;
                    break;
                  case 'range':
                    this.resultContent += `${frontSpace}{{#${ele.field}_min}}\n`
                                  + '        ,{\n'
                                  + '          "range": {\n'
                                  + `            "${ele.field}": {\n`
                                  + `              "from": "{{${ele.field}_min}}",\n`
                                  + `              "to": "{{${ele.field}_max}}"\n`
                                  + '            }\n'
                                  + '          }\n'
                                  + '        }\n'
                                  + `        {{/${ele.field}_min}}${endNewline}`;
                    break;
                  case 'script':
                    this.resultContent += `${frontSpace}{{#${ele.field}}}\n`
                                  + '        ,{\n'
                                  + '          "script": {\n'
                                  + '            "script": {\n'
                                  + '              "lang": "painless",\n'
                                  + `              "inline": "return doc['${ele.field}'].value > 0"\n`
                                  + '            }\n'
                                  + '          }\n'
                                  + '        }\n'
                                  + `        {{/${ele.field}}${endNewline}`;
                    break;
                  default:
                    break;
                }
              });
              this.insertContent();
            } else {
              this.$message.errorMessage('还未选择任何query字段，请勾选！');
            }
          }
        }
      });
    },
    insertContent() {
      this.$emit('insert-template-content', this.resultContent);
    },
    handleClose() {
      this.$emit('close-dialog');
    },
  },
  created() {
    this.metadatas = this.fieldList.map((obj) => {
      const rObj = { ...obj };
      rObj.queryWay = '';
      return rObj;
    });
  },
};
</script>
<style type="text/css">
.template-insert-dialog .el-dialog--small {
    width: auto;
    min-width: 700px;
}
.template-insert {
  margin: 15px;
  color: #fff;
}
</style>
