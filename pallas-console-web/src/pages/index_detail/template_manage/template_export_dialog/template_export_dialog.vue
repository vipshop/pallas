<template>
    <el-dialog title="导出模板" v-model="isTemplateExportVisible" :before-close="closeDialog">
        <div class="export-template">
            <el-transfer
            filterable
            v-model="templateSelected"
            :data="exportTemplateList"
            :titles="['可选模板', '已选模板']"></el-transfer>
        </div>
        <div slot="footer" class="dialog-footer">    
            <el-button @click="closeDialog()">取 消</el-button>
            <el-button type="confirm" @click="sumbitExportTemplate">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['indexId', 'templateList'],
  data() {
    return {
      isTemplateExportVisible: true,
      templateSelected: [],
      exportTemplateList: [],
    };
  },
  methods: {
    closeDialog() {
      this.$emit('close-export-dialog');
    },
    sumbitExportTemplate() {
      if (this.templateSelected.length > 0) {
        const templateIds = this.templateSelected.join(',');
        window.location.href = `/pallas/index_template/export.json?indexId=${this.indexId}&templateIds=${templateIds}`;
      } else {
        this.$message.errorMessage('请选择要导出的模板！');
      }
    },
  },
  created() {
    const arr = JSON.parse(JSON.stringify(this.templateList));
    this.exportTemplateList = arr.map((obj) => {
      const rObj = {};
      rObj.key = obj.id;
      rObj.label = obj.templateName;
      return rObj;
    });
  },
};
</script>

<style>
.export-template {
    margin: 10px;
}
.export-template .el-transfer-panel {
    width: 250px;
}
</style>
