<template>
    <div class="my-tab-content" :style="{ 'height': temPanelHeight }">
        <div class="template_content" v-loading="loading" element-loading-text="请稍等···">
            <div class="template_tree">
                <div v-show="isAllPrivilege">
                  <el-button type="primary" size="small" @click="addTemplate">新增</el-button>
                  <el-button type="primary" size="small" @click="exportTemplate">导出</el-button>
                　<el-button type="primary" size="small" @click="importTemplate">导入</el-button>
                　<el-button type="primary" size="small" @click="batchSubmitTemplate">批量提交</el-button>
                </div>
                <div class="mrg-top-10" :style="{ 'height': temPanelHeight - 35, 'width': '260px' }">
                  <el-tree style="overflow: auto;height: 100%;" node-key="id" :data="treeData" :props="defaultProps" default-expand-all :expand-on-click-node="false" highlight-current @node-click="handleNodeClick"></el-tree>
                </div>
            </div>
            <div class="template-warning" v-if="!isEditable"><i class="el-icon-warning"></i>请选择模板</div>
            <div v-for="template in templateList" :key="template.templateName" class="template-body">
                <template-edit v-if="templateInfo.templateName === template.templateName" :tem-panel-height="temPanelHeight" :metadata-list="metadataList" :clusters="clusters" :index-id="indexId" :index-name="indexName" :is-all-privilege="isAllPrivilege" :template-info="templateInfo" :macro-list="macroList" @close-delete="closeDelete" @close-edit="closeEdit"></template-edit>                    
            </div>
        </div>
    <div v-if="isTemplateAddVisible">
        <template-add-dialog :template-add-info="templateAddInfo" @close-dialog="closeDialog" @submit-close-dialog="submitCloseDialog"></template-add-dialog>
    </div>
    <div v-if="isTemplateImportVisible">
        <template-import-dialog :index-id="indexId" :template-import-title="templateImportTitle" :template-import-url="templateImportUrl" @close-dialog="closeImportDialog" @close-submit-dialog="closeSubmitImportDialog"></template-import-dialog>
    </div>
    <div v-if="isExprotTemplateVisible">
        <template-export-dialog :index-id="indexId" :template-list="templateList" @close-export-dialog="closeExportDialog"></template-export-dialog>
    </div>
    <div v-if="isBatchSubmitVisible">
        <template-batch-submit-dialog :index-id="indexId" :modified-template-list="modifiedTemplateList" @close-batch-submit-dialog="closeBatchSubmitDialog"></template-batch-submit-dialog>
    </div>
    </div>
</template>

<script>
import '../../../components';
import TemplateAddDialog from './template_add_dialog/template_add_dialog';
import TemplateImportDialog from './template_import_dialog/template_import_dialog';
import TemplateEdit from './template_edit/template_edit';
import TemplateExportDialog from './template_export_dialog/template_export_dialog';
import TemplateBatchSubmitDialog from './template_batch_submit_dialog/template_batch_submit_dialog';

export default {
  data() {
    return {
      loading: false,
      indexId: this.$route.query.indexId,
      indexName: this.$route.query.indexName,
      isAllPrivilege: false,
      templateInfo: {},
      templateList: [],
      modifiedTemplateList: [],
      isEditable: false,
      isTemplateAddVisible: false,
      isTemplateImportVisible: false,
      templateImportTitle: '',
      templateImportUrl: '',
      isExprotTemplateVisible: false,
      isBatchSubmitVisible: false,
      tempList: [],
      macroList: [],
      treeData: [{
        id: 'temp',
        label: '模板',
        children: [],
      }, {
        id: 'macro',
        label: '宏',
        children: [],
      }],
      templateAddInfo: {
        indexId: '',
        templateName: '',
        type: '1',
        description: '',
      },
      defaultProps: {
        children: 'children',
        label: 'label',
      },
      clusters: [],
      metadataList: [],
      temPanelHeight: {
        height: document.body.clientHeight - 210,
      },
    };
  },
  methods: {
    exportTemplate() {
      this.isExprotTemplateVisible = true;
    },
    batchSubmitTemplate() {
      this.isBatchSubmitVisible = true;
    },
    closeExportDialog() {
      this.isExprotTemplateVisible = false;
    },
    closeBatchSubmitDialog() {
      this.isBatchSubmitVisible = false;
    },
    importTemplate() {
      this.isTemplateImportVisible = true;
      this.templateImportTitle = '导入模板';
      this.templateImportUrl = `/pallas/index_template/import.json?indexId=${this.indexId}`;
    },
    addTemplate() {
      this.isTemplateAddVisible = true;
      this.templateAddInfo.indexId = this.indexId;
    },
    handleNodeClick(data) {
      if (!data.children) {
        this.isEditable = true;
        this.templateInfo = data;
        this.templateInfo.content = data.content || '';
        this.templateInfo.params = data.params || '';
      }
    },
    closeDelete() {
      this.init();
      this.isEditable = false;
    },
    closeEdit() {
      this.init();
    },
    closeDialog() {
      this.isTemplateAddVisible = false;
    },
    closeImportDialog() {
      this.isTemplateImportVisible = false;
    },
    closeSubmitImportDialog() {
      this.isTemplateImportVisible = false;
      this.init();
    },
    handleCallback(data) {
      this.templateList = data.list;
      const modifiedArr = [];
      data.list.forEach((element) => {
        if (element.newer && element.type === 1 && !element.approving) {
          modifiedArr.push(element);
        }
      });
      this.modifiedTemplateList = modifiedArr;
      this.isAllPrivilege = data.allPrivilege;
      const array = JSON.parse(JSON.stringify(data.list));
      const tempArr = [];
      const macroArr = [];
      array.forEach((element) => {
        if (element.newer && element.type === 1) {
          const approvedLabel = this.$createElement('span', null, [this.$createElement('b', { style: { color: 'red' } }, '新 '), this.$createElement('span', null, element.templateName)]);
          this.$set(element, 'label', approvedLabel);
        } else {
          this.$set(element, 'label', element.templateName);
        }
        if (element.type === 1) {
          tempArr.push(element);
        } else {
          macroArr.push(element);
        }
      });
      this.treeData[0].children = tempArr;
      this.treeData[1].children = macroArr;
      this.tempList = tempArr;
      this.macroList = macroArr;
    },
    getTemplateList() {
      return this.$http.get(`/index_template/list.json?indexId=${this.indexId}`).then((data) => {
        this.handleCallback(data);
      });
    },
    submitCloseDialog(val) {
      this.isTemplateAddVisible = false;
      this.loading = true;
      this.$http.get(`/index_template/list.json?indexId=${this.indexId}`).then((data) => {
        this.handleCallback(data);
        this.isEditable = true;
        Object.keys(this.templateList).forEach((element, index) => {
          if (this.templateList[index].templateName === val) {
            this.templateInfo = this.templateList[index];
            this.templateInfo.content = data.content || '';
            this.templateInfo.params = data.params || '';
            this.templateInfo.resultContent = '';
          }
        });
      })
      .finally(() => {
        this.loading = false;
      });
    },
    getClusters() {
      return this.$http.post('/index/version/metadata.json', { indexId: this.indexId }).then((data) => {
        this.clusters = data.clusters;
        this.metadataList = data.list;
      });
    },
    init() {
      this.loading = true;
      Promise.all([this.getTemplateList(), this.getClusters()]).then(() => {
        this.loading = false;
      });
    },
  },
  components: {
    'template-add-dialog': TemplateAddDialog,
    'template-import-dialog': TemplateImportDialog,
    'template-edit': TemplateEdit,
    'template-export-dialog': TemplateExportDialog,
    'template-batch-submit-dialog': TemplateBatchSubmitDialog,
  },
  mounted() {
    this.temPanelHeight = document.body.clientHeight - 210;
    const that = this;
    window.onresize = function temp() {
      that.temPanelHeight = document.body.clientHeight - 210;
    };
  },
  created() {
    this.init();
  },
};

</script>

<style type="text/css">
.template_content {
  display: table;
  width: 100%;
}
.template_tree {
  padding-right: 30px;
  display: table-cell;
  width: 245px;
}
.template-warning {
  text-align: center;
  color: red;
  font-size: larger;
  font-weight: bolder;
  display: table-cell;
  vertical-align: middle;
}
.template-warning i {
  padding-right: 10px;
}
.template-body {
  display: table-cell;
}
</style>
