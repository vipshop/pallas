<template>
    <el-dialog :title="dynamicInfoTitle" size="small" v-model="isDynamicInfoVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="dynamicInfo" :rules="rules" ref="dynamicInfo" label-width="140px">
            <el-row>
                <el-col :span="22">
                    <el-form-item prop="indexName" label="索引" required>
                        <el-input v-model="dynamicInfo.indexName" :disabled="true"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22">
                    <el-form-item prop="filterVersion" label="版本">
                        <el-select v-model="dynamicInfo.filterVersion" filterable allow-create style="width:100%;">
                            <el-option label="全部版本" value=""></el-option>
                            <el-option v-for="item in versionIdList" :label="item.value" :value="item.value" :key="item.value"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22">
                    <el-form-item label="事件" prop="selectedType" required>
                        <el-select v-model="dynamicInfo.selectedType" style="width:100%;">
                            <el-option v-for="item in typeList" :label="item.value" :value="item.value" :key="item.value"></el-option>
                            <el-option label="全部事件" value="全部事件"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22" v-if="dynamicInfo.selectedType=='全部事件'">
                    <el-form-item label="操作" prop="eventName" required>
                        <el-select v-model="dynamicInfo.eventName" style="width:100%;">
                            <el-option label="全部操作" value="全部操作"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
                <el-col :span="22" v-else-if="dynamicInfo.selectedType=='索引事件'">
                    <el-form-item label="操作" prop="eventName" required>
                        <el-select v-model="dynamicInfo.eventName" style="width:100%;">
                            <el-option v-for="item in indexEventList" :label="item.value" :value="item.value" :key="item.value"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
                <el-col :span="22" v-else-if="dynamicInfo.selectedType=='版本事件'">
                    <el-form-item label="操作" prop="eventName" required>
                        <el-select v-model="dynamicInfo.eventName" style="width:100%;">
                            <el-option v-for="item in versionEventList" :label="item.value" :value="item.value" :key="item.value"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
                <el-col :span="22" v-else-if="dynamicInfo.selectedType=='同步事件'">
                    <el-form-item label="操作" prop="eventName" required>
                        <el-select v-model="dynamicInfo.eventName" style="width:100%;">
                            <el-option v-for="item in versionSyncEventList" :label="item.value" :value="item.value" :key="item.value"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
                <el-col :span="22" v-else-if="dynamicInfo.selectedType=='模板事件'">
                    <el-form-item label="操作" prop="eventName" required>
                        <el-select v-model="dynamicInfo.eventName" style="width:100%;">
                            <el-option v-for="item in templateEventList" :label="item.value" :value="item.value" :key="item.value"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
             <el-row>
                 <el-col :span="22">
                     <el-form-item prop="timeRange" label="天数" required>
                         <el-input v-model="dynamicInfo.timeRange" :disabled="true"></el-input>
                     </el-form-item>
                 </el-col>
             </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">
           <el-button @click="closeDialog()">取消</el-button>
           <el-button type="confirm" @click="deleteDynamicInfo()">删除</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['dynamicOperation', 'dynamicInfo', 'dynamicInfoTitle', 'typeList', 'versionIdList'],
  data() {
    return {
      indexEventList: [{
        value: '创建索引',
      }, {
        value: '更新索引',
      }],
      versionEventList: [{
        value: '创建版本',
      }, {
        value: '更新版本',
      }, {
        value: '启用版本',
      }, {
        value: '删除版本',
      }],
      versionSyncEventList: [{
        value: '开始同步',
      }, {
        value: '结束同步',
      }, {
        value: '全量',
      }, {
        value: '增量',
      }, {
        value: '对账',
      }],
      templateEventList: [{
        value: '新建模板',
      }, {
        value: '编辑模板',
      }, {
        value: '导入模板',
      }, {
        value: '删除模板',
      }],
      loading: false,
      isDynamicInfoVisible: true,
      rules: {

      },
    };
  },
  methods: {
    deleteDynamicInfo() {
      this.$refs.dynamicInfo.validate((valid) => {
        if (valid) {
          if (this.dynamicOperation === 'delete') {
            this.loading = true;
            this.$http.post('/index/dynamic/delete.json', this.dynamicInfo).then(() => {
              this.$message.successMessage('删除成功', () => {
                this.$emit('operate-close-dialog');
              });
            })
            .finally(() => {
              this.loading = false;
            });
          }
        } else {
          console.log('error submit!!');
        }
      });
    },
    closeDialog() {
      this.$emit('close-dialog');
    },
  },

};

</script>
