<template>
    <div>
        <div class="template-content" v-loading="loading" element-loading-text="请稍等···">
            <el-row>
                <div class="pull-left template-title">
                    当前正在编辑{{this.templateType}}：<span class="template-name">{{templateInfo.templateName}}</span>
                    <span v-show="templateInfo.approving && templateInfo.type === 1" class="template-approving">状态：<router-link tag="a" :to="{ name: 'authority_manage' }">待审核</router-link>，不可进行保存，删除等操作</span>
                    <span v-show="templateInfo.approving && templateInfo.type === 0" class="template-approving">引用该宏的模板处于待审核状态，不可进行保存、删除等操作</span>
                </div>
                <div class="pull-right" v-show="isAllPrivilege">
                    <el-select v-show="!isMacroVisible && isEditOperate" size="small" placeholder="请选择要插入的宏" v-model="selectedMacro" style="padding-right: 10px;" clearable @change="insertMacro">
                        <el-option v-for="item in macroList" :label="item.templateName" :value="item.templateName" :key="item.templateName"></el-option>
                    </el-select>
                    <el-button type="danger" @click="handleDelete" size="small" v-show="isEditOperate && !templateInfo.approving">删除</el-button>
                    <el-button type="primary" @click="handleSave" size="small" v-show="isEditOperate && !templateInfo.approving">保存</el-button>
                    <el-button type="primary" @click="handleApprove" size="small" v-show="isEditOperate && !templateInfo.approving && templateInfo.type === 1">提交</el-button>
                    <el-button type="primary" @click="handleHistoryVersion" size="small" v-show="templateInfo.hisCount > 0 && isEditOperate">{{historyVersionBtn}}</el-button>
                </div>
            </el-row>
            <div class="mrg-top-10">
                <el-row>
                    <el-tabs v-model="activeTab" @tab-click="tabClick">
                        <el-tab-pane label="编辑模板" name="edit">
                            <div :class="[isShowHistoryVersion ? 'template-edit-and-version-content' : 'template-edit-content']">
                                <editor ref="aceEditor1" :content="templateInfo.content" v-on:change-content="changeEditContent" :editor-id="eidtorId"></editor>
                            </div>
                            <div v-show="isShowHistoryVersion" class="template-history-version-content">
                                <div style="padding-left:10px;">
                                    <el-table :data="historyVersionList" border @row-click="handleVersionDialog">
                                        <el-table-column label="修改日期" width="150px">
                                              <template scope="scope">{{scope.row.createdTime | formatDate}}</template>
                                        </el-table-column>
                                        <el-table-column prop="description" label="描述">
                                        </el-table-column>
                                        <el-table-column prop="creator" label="修改者"></el-table-column>
                                    </el-table>
                                </div>
                            </div>
                        </el-tab-pane>
                        <el-tab-pane label="sql parser" name="sql" :disabled="isMacroVisible || !isAllPrivilege">
                              <el-row :gutter="2">
                                <el-col :span="11">
                                    <el-input :rows="paneHeight.height/21" class="result-content" type="textarea" v-model="sql" placeholder="请输入sql"></el-input>
                                </el-col>
                                <el-col :span="2">
                                  <div :style="sqlParseBtnStyle" align="center">
                                      <el-button size="small" title="结果仅供参考，需进一步加工" type="primary" @click="handleExplain">转 DSL</el-button></br>
                                      <el-button title="谨慎执行，别跑挂DB了" :disabled="!isAllPrivilege" style="margin-top: 5px;margin-left: 0px;" size="small" type="primary" @click="handleExecute">查询DB</el-button>
                                  </div>
                                </el-col>
                                <el-col :span="11">
                                  <div :style="paneHeight">
                                      <editor ref="aceEditor" :content="explainContent" editor-id="explainId"></editor>
                                  </div>
                                </el-col>
                              </el-row>
                        </el-tab-pane>
                        <el-tab-pane label="调试" name="debug" :disabled="isMacroVisible || !isAllPrivilege">
                              <div class="render-cluster" v-if="clusters.length > 1">
                                  <span>指定集群：</span>
                                  <el-select size="small" v-model="clusterId" placeholder="请选择集群" style="margin-left: 10px;">
                                      <el-option v-for="item in clusters" :key="item.id" :label="item.clusterId" :value="item.id"></el-option>
                                  </el-select>
                              </div>
                              <el-row>
                                  <fieldset class="no-border">
                                      <span class="edit-title">参数：</span>
                                      <div style="height: 300px;">
                                          <editor ref="aceEditor2" :content="templateInfo.params" v-on:change-content="changeDebugContent" :editor-id="debugId"></editor>
                                      </div>
                                  </fieldset>
                              </el-row>
                              <el-row>
                                  <div align="center" style="padding: 5px 0">
                                      <el-button size="small" type="primary" @click="handleRender">Render</el-button>
                                      <el-button size="small" type="primary" @click="handleDebug">运行</el-button>
                                  </div>
                              </el-row>
                              <el-row>
                                  <fieldset class="no-border">
                                      <span class="edit-title">结果：</span>
                                      <el-input type="textarea" class="result-content" readonly :autosize="{ minRows: 12}" v-model="resultContent"></el-input>
                                  </fieldset>
                              </el-row>
                        </el-tab-pane>
                        <el-tab-pane label="API" name="api" :disabled="isMacroVisible || !isAllPrivilege">
                              <el-row>
                                  <fieldset class="no-border">
                                      <div class="api-content">
                                          {{`Host: ${apiContent.http_address}`}}
                                          <br/><br/>
                                          <pre>{{`POST: ${apiContent.path}`}}</pre>
                                          <pre>{{apiContent.content}}</pre>
                                      </div>
                                  </fieldset>
                              </el-row>
                        </el-tab-pane>
                        <el-tab-pane label="性能测试" name="test" :disabled="isMacroVisible || !isAllPrivilege">
                            <template-test :index-id="indexId" :template-name="templateInfo.templateName" :params-info="paramsInfo"></template-test>
                        </el-tab-pane>
                        <el-tab-pane label="超时重试" name="timeoutRetry" :disabled="isMacroVisible || !isAllPrivilege">
                            <service-governance :index-id="indexId" :template-info="templateInfo"></service-governance>
                        </el-tab-pane>
                    </el-tabs>
                </el-row>
            </div>
        </div>
        <div v-if="isVersionContentVisible">
            <json-diff :is-overwrite="true" :json-diff-info="versionDiffInfo" @overwrite-operate="overwriteVersion" @close-dialog="closeVersionContentDialog"></json-diff>
        </div>
        <div v-if="isEditSaveVisible">
            <template-save-edit-dialog :index-id="indexId" :template-info="templateInfo" @close-edit-save-dialog="closeEditSaveDialog" @edit-save-success="editSaveSuccess"></template-save-edit-dialog>
        </div>
    </div>

</template>

<script>
import '../../../../components';
import TemplateTest from './template_test/template_test';
import ServiceGovernance from './service_governance/service_governance';
import TemplateSaveEditDialog from './template_save_edit_dialog/template_save_edit_dialog';

export default {
  props: ['indexId', 'indexName', 'clusters', 'isAllPrivilege', 'templateInfo', 'macroList'],
  data() {
    return {
      loading: false,
      clusterId: this.clusters[0].id,
      activeTab: 'edit',
      selectedMacro: '',
      resultContent: '',
      explainContent: '',
      apiContent: {},
      paramsInfo: [],
      historyVersionList: [],
      isShowHistoryVersion: false,
      isVersionContentVisible: false,
      versionDiffInfo: {},
      sql: '',
      isEditSaveVisible: false,
      paneHeight: {
        height: document.body.clientHeight - 298,
      },
    };
  },
  mounted() {
    this.paneHeight = { height: document.body.clientHeight - 298 };
    const that = this;
    window.onresize = function temp() {
      that.paneHeight = { height: document.body.clientHeight - 298 };
    };
  },
  methods: {
    tabClick() {
      if (this.activeTab === 'api') {
        const params = {
          indexId: this.indexId,
          templateName: this.templateInfo.templateName,
        };
        this.loading = true;
        this.$http.post('/index_template/genapi.json', params).then((data) => {
          this.apiContent = data;
        })
        .finally(() => {
          this.loading = false;
        });
      } else if (this.activeTab === 'test') {
        const params = {
          indexId: this.indexId,
          templateName: this.templateInfo.templateName,
        };
        this.loading = true;
        this.$http.post('/index_template/performance_script/param.json', params).then((data) => {
          this.paramsInfo = data.map((obj) => {
            const rObj = {};
            rObj.paramName = obj.paramName;
            rObj.include = false;
            rObj.valueType = '1';
            rObj.value = '';
            return rObj;
          });
        })
        .finally(() => {
          this.loading = false;
        });
      }
    },
    insertMacro() {
      if (this.selectedMacro) {
        this.$refs.aceEditor1.editor.insert(this.insertContent);
      }
    },
    changeEditContent(val) {
      if (this.templateInfo.content !== val) {
        this.templateInfo.content = val;
      }
    },
    changeDebugContent(val) {
      if (this.templateInfo.params !== val) {
        this.templateInfo.params = val;
      }
    },
    handleExecute() {
      const dataParams = {
        indexId: this.indexId,
        sql: this.sql,
      };
      this.loading = true;
      this.$http.post('/index_template/execute.json', dataParams).then((data) => {
        try {
          this.explainContent = JSON.stringify(data, undefined, 2);
        } catch (e) {
          this.explainContent = `解析错误: ${data.result}`;
        }
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleExplain() {
      const dataParams = {
        sql: this.sql,
        clusterId: this.clusterId,
      };
      this.loading = true;
      this.$http.post('/index_template/explain.json', dataParams).then((data) => {
        try {
          this.explainContent = JSON.stringify(JSON.parse(data), undefined, 2);
        } catch (e) {
          this.explainContent = `解析错误: ${data.result}`;
        }
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleDelete() {
      this.$message.confirmMessage(`确定删除模板${this.templateInfo.templateName}吗?`, () => {
        const params = {
          indexId: this.indexId,
          indexName: this.indexName,
          templateId: this.templateInfo.id,
          templateName: this.templateInfo.templateName,
        };
        this.loading = true;
        this.$http.post('/index_template/delete.json', params).then(() => {
          this.$message.successMessage('删除成功', () => {
            this.$emit('close-delete');
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    handleSave() {
      const params = {
        indexId: this.indexId,
        templateName: this.templateInfo.templateName,
        content: this.templateInfo.content,
        params: this.templateInfo.params,
      };
      this.loading = true;
      this.$http.post('/index_template/update.json', params).then(() => {
        this.$message.successMessage('保存成功', () => {
          this.$emit('close-edit');
        });
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleApprove() {
      this.isEditSaveVisible = true;
    },
    closeEditSaveDialog() {
      this.isEditSaveVisible = false;
    },
    editSaveSuccess() {
      this.isEditSaveVisible = false;
      this.$emit('close-edit');
      this.getHistoryList();
    },
    handleRender() {
      const dataParams = {
        indexId: this.indexId,
        templateName: this.templateInfo.templateName,
        params: this.templateInfo.params,
        clusterId: this.clusterId,
      };
      this.loading = true;
      this.$http.post('/index_template/render.json', dataParams).then((data) => {
        try {
          this.resultContent = JSON.stringify(JSON.parse(data), undefined, 2);
        } catch (e) {
          this.resultContent = `解析错误: ${data}`;
        }
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleDebug() {
      const dataParams = {
        indexId: this.indexId,
        templateName: this.templateInfo.templateName,
        params: this.templateInfo.params,
        clusterId: this.clusterId,
      };
      this.loading = true;
      this.$http.post('/index_template/debug.json', dataParams).then((data) => {
        this.resultContent = JSON.stringify(JSON.parse(data), undefined, 2);
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleHistoryVersion() {
      this.isShowHistoryVersion = !this.isShowHistoryVersion;
      if (this.isShowHistoryVersion) {
        this.getHistoryList();
      }
    },
    handleVersionDialog(row) {
      this.isVersionContentVisible = true;
      this.versionDiffInfo.left = row.content;
      this.versionDiffInfo.right = this.templateInfo.content;
    },
    closeVersionContentDialog() {
      this.isVersionContentVisible = false;
    },
    overwriteVersion() {
      this.templateInfo.content = this.versionDiffInfo.left;
      this.isVersionContentVisible = false;
    },
    getHistoryList() {
      this.loading = true;
      this.$http.get(`/index_template/hislist.json?templateId=${this.templateInfo.id}`).then((data) => {
        this.historyVersionList = data;
      })
      .finally(() => {
        this.loading = false;
      });
    },
  },
  components: {
    'template-test': TemplateTest,
    'template-save-edit-dialog': TemplateSaveEditDialog,
    'service-governance': ServiceGovernance,
  },
  computed: {
    insertContent() {
      return `##__${this.selectedMacro}__##`;
    },
    eidtorId() {
      return `${this.templateInfo.templateName}_edit`;
    },
    debugId() {
      return `${this.templateInfo.templateName}_debug`;
    },
    isMacroVisible() {
      return this.templateInfo.type === 0;
    },
    historyVersionBtn() {
      return this.isShowHistoryVersion ? '隐藏历史版本' : '历史版本';
    },
    isEditOperate() {
      return this.activeTab === 'edit';
    },
    templateType() {
      if (this.templateInfo.type === 0) {
        return '宏';
      }
      return '模板';
    },
    sqlParseBtnStyle() {
      return { 'margin-top': `${(this.paneHeight.height / 2) - 20}px` };
    },
  },
};

</script>

<style type="text/css">
.template-content .template-title {
  color: #fff;
  line-height: 30px;
}
.template-content .template-title .template-name{
  color: #32cd32;
  font-weight: bold;
}
.template-content .template-title .template-approving {
  color: red;
  margin-left: 10px;
  font-weight: initial;
}
.template-content .template-title .template-approving a {
  color: red;
}
.template-content .template-title .template-approving a:hover {
  color: rgba(255, 0, 0, 0.7);
}
.result-content .el-textarea__inner {
    background-color: #272822;
}
.api-content {
  padding: 5px;
  height: 600px;
  overflow: auto;
  background-color: #222;
  color: #fff
}
.edit-title {
  color: #fff;
}
.no-border {
  border: none;
}
.template-edit-content {
  height: 650px;
  width: 100%;
  overflow-y: auto; 
  float:left;
}
.template-edit-and-version-content {
  height: 650px;
  width: 62%;
  overflow-y: auto; 
  float:left;
}
.template-history-version-content {
  position: relative;
  max-height: 650px;
  width: 38%;
  overflow-y: auto;
  float:left;
}
.template-history-version-content table {
  font-size: 10px;
}
.template-history-version-content .el-table .cell {
  line-height: initial;
}
.template-history-version-content .el-table tr:hover {
  cursor: pointer;
}
.render-cluster {
  margin-left: 15px;
}
.render-cluster >span {
  font-size: 16px;
}
</style>
