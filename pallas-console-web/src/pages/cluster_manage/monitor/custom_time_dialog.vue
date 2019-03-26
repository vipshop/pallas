<template>
    <el-dialog title="自定义时间" v-model="visible" :before-close="closeDialog">
        <el-form :model="formInfo" :rules="rules" ref="formInfo" label-width="40px" style="margin-top: 20px;">
            <el-row>
                <el-col :span="22">
                    <el-form-item prop="timeRange">
                        <el-date-picker
                            v-model="formInfo.timeRange"
                            type="datetimerange"
                            placeholder="选择时间范围"
                            style="width: 100%"
                            align="right">
                        </el-date-picker>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">
           <el-button @click="closeDialog()">取消</el-button>
           <el-button type="confirm" @click="handleSubmit()">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: [],
  data() {
    return {
      visible: true,
      formInfo: {
        timeRange: '',
      },
      rules: {
        timeRange: [{ validator: this.validateTimeRange, trigger: 'blur' }],
      },
    };
  },
  methods: {
    validateTimeRange(rule, value, callback) {
      if (!value || JSON.stringify(value) === '[null,null]') {
        callback(new Error('请选择时间范围'));
      } else {
        if ((Date.parse(value[1]) - Date.parse(value[0])) > 7 * 24 * 60 * 60 * 1000) {
          callback(new Error('时间范围不能大于7天'));
        }
        callback();
      }
    },
    closeDialog() {
      this.$emit('close-dialog');
    },
    handleSubmit() {
      this.$refs.formInfo.validate((valid) => {
        if (valid) {
          const params = {
            command: 'custom',
            from: Date.parse(this.formInfo.timeRange[0]),
            to: Date.parse(this.formInfo.timeRange[1]),
          };
          this.$emit('set-custom-time', params);
        }
      });
    },
  },
};
</script>
