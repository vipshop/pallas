<template>
    <el-dialog title="选择复制的域" v-model="isCopyToFieldsVisible" @open="initSelectList"  :before-close="closeDialog">
        <div class="copy-to-field">
            <el-transfer
            filterable
            v-model="copyToFieldSelected"
            :data="copyToFieldList"
            :titles="['可选的域', '已选的域']"></el-transfer>
        </div>
        <div slot="footer" class="dialog-footer">    
            <el-button @click="closeDialog()">取 消</el-button>
            <el-button type="confirm" @click="sumbitCopyToField">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['isCopyToFieldsVisible', 'schemaCopyToInfo', 'copyToList', 'schemaParentFieldName'],
  data() {
    return {
      copyToFieldList: [],
      copyToFieldSelected: [],
    };
  },
  methods: {
    closeDialog() {
      this.$emit('close-schema-dialog');
    },
    sumbitCopyToField() {
      if (this.copyToFieldSelected.length > 0) {
        this.schemaCopyToInfo.copyTo = [];
        this.copyToFieldSelected.forEach((element) => {
          this.schemaCopyToInfo.copyTo.push(element);
        });
        this.$emit('add-schema-copy-to', this.schemaCopyToInfo.copyTo);
      } else {
        this.$message.errorMessage('请选择要复制的域！');
      }
    },
    initSelectList() {
      this.copyToFieldSelected = [];
      const arr = JSON.parse(JSON.stringify(this.copyToList));
      this.copyToFieldList = arr.map((obj) => {
        const rObj = {};
        rObj.key = obj;
        rObj.label = obj;
        return rObj;
      });
    },
  },
};
</script>

<style>
.copy-to-field {
    margin: 10px;
}
.copy-to-field .el-transfer-panel {
    width: 250px;
}
</style>
