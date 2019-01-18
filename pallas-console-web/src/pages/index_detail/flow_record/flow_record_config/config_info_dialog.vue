<template>
    <el-dialog :title="configInfoTitle" size="small" v-model="isconfigInfoVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="configInfo" :rules="rules" ref="configInfo" label-width="140px">
            <el-row>
                <el-col :span="22">
                    <el-form-item label="目标模板" prop="templateId" required>
                        <el-select v-model="configInfo.templateId" style="width:100%;">
                            <el-option v-for="item in templates" :label="item.templateName" :value="item.id" :key="item.id"></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22">
                    <el-form-item label="采集开始时间" prop="startTime" required>
                        <el-date-picker
                                v-model="configInfo.startTime"
                                type="datetime"
                                placeholder="选择开始时间"
                                style="width:100%">
                        </el-date-picker>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22">
                    <el-form-item label="采集结束时间" prop="endTime" required>
                        <el-date-picker
                                v-model="configInfo.endTime"
                                type="datetime"
                                placeholder="选择结束时间"
                                style="width:100%">
                        </el-date-picker>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22">
                    <el-form-item label="抽样系数" prop="sampleRate" required>
                        <el-input v-model="configInfo.sampleRate" placeholder="范围0到1"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22">
                    <el-form-item label="采集数量" prop="limit">
                        <el-input v-model="configInfo.limit"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22">
                    <el-form-item label="描述" prop="note">
                        <el-input type="textarea" v-model="configInfo.note"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="closeDialog()">取消</el-button>
            <el-button type="confirm" @click="submitconfigInfo()">保存</el-button>
        </div>
    </el-dialog>
</template>

<script>
  export default {
    props: ['configOperation', 'configInfo', 'configInfoTitle', 'allPhysicals', 'templates'],
    data() {
      return {
        loading: false,
        isconfigInfoVisible: true,
        rules: {
          templateId: [{ validator: this.$validate.validateSelect, trigger: 'change' }],
          startTime: [{ validator: this.$validate.validateTime, trigger: 'change' }],
          endTime: [{ validator: this.$validate.validateTimeExpire, trigger: 'change' }],
          sampleRate: [{ required: true, message: '抽样系数不能为空', trigger: 'blur' }],
          limit: [{ required: true, message: '采集数量不能为空', trigger: 'blur' }],
        },
      };
    },
    methods: {
      submitconfigInfo() {
        this.$refs.configInfo.validate((valid) => {
          if (valid) {
            if (this.configOperation === 'add') {
              this.loading = true;
              this.$http.post('/record/flow_record_config/add.json', this.configInfo).then(() => {
                this.$message.successMessage('新增规则成功', () => {
                  this.$emit('operate-close-dialog');
                });
              })
              .finally(() => {
                this.loading = false;
              });
            } else {
              this.loading = true;
              this.$http.post('/record/flow_record_config/edit.json', this.configInfo).then(() => {
                this.$message.successMessage('修改规则成功', () => {
                  this.$emit('operate-close-dialog');
                });
              })
              .finally(() => {
                this.loading = false;
              });
            }
          }
        });
      },
      closeDialog() {
        this.$emit('close-dialog');
      },
    },
    computed: {
      isEditable() {
        return this.configOperation === 'edit';
      },
    },
  };

</script>
