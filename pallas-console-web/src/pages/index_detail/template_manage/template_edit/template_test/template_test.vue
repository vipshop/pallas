<template>
    <div class="template-test">
        <div class="template-test-content">
            <div class="template-test-head">上传数据源</div>
            <div class="template-test-body">
                <template-test-data-source :index-id="indexId" :template-name="templateName" :data-source="dataSource"></template-test-data-source>
                <template-test-upload :add-data-source="addDataSource" :data-source="dataSource" @template-test-upload-success="templateTestUploadSuccess"></template-test-upload>
            </div>
            <div class="template-test-tips"><span>注意:若文件包含多列，可以定义多个参数名，参数名之间用分号隔开，例如k1;k2</span></div>
        </div>
        <template-test-params :index-id="indexId" :template-name="templateName" :params-info="paramsInfo" :data-source="dataSource"></template-test-params>
    </div>
</template>

<script>
import TemplateTestDataSource from './template_test_data_source/template_test_data_source';
import TemplateTestUpload from './template_test_upload/template_test_upload';
import TemplateTestParams from './template_test_params/template_test_params';

export default {
  props: ['indexId', 'templateName', 'paramsInfo'],
  data() {
    return {
      addDataSource: [{
        paramNameDef: '',
        indexId: this.indexId,
        templateName: this.templateName,
      }],
      dataSource: [],
    };
  },
  methods: {
    templateTestUploadSuccess(data) {
      this.dataSource.push(data);
    },
  },
  components: {
    'template-test-data-source': TemplateTestDataSource,
    'template-test-upload': TemplateTestUpload,
    'template-test-params': TemplateTestParams,
  },
};
</script>

<style type="text/css">
    .template-test {
        margin: 0 20px;
    }
    .template-test .template-test-content {
        margin: 0 0 20px 0;
    }
    .template-test .template-test-content .template-test-tips {
        margin-top: 5px;
        color: red;
        font-size: 13px;
    }
    .template-test .template-test-content .template-test-head {
        margin-bottom: 10px;
        background-color: #222;
        height: 25px;
        border-radius: 4px;
        padding-left: 5px;
        font-size: 15px;
    }
    .template-test-popper {
        background-color: #222;
        border: none;
    }
    .template-test-popper .popper-button {
        float: left;
        padding: 5px;
    }
</style>
