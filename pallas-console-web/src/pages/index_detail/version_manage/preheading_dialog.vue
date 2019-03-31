<template>
  <div class="version-info-dialog">
    <el-dialog title="索引预热" v-model="visible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
        <el-form label-width="100px">
            <div class="label-title">
                <span class="span-title"><i class="fa fa-th-large"></i>最近预热情况</span>
            </div>
            <div class="label-content">
                <el-row :gutter="10">
                    <el-col :span="12">
                        <el-form-item label="总预热条数：">
                            <span>{{rampupInfo.rampupTarget}}</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="当前状态：">
                            <span>{{rampupInfo.state}}</span>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="10">
                    <el-col :span="12">
                        <el-form-item label="开始时间：">
                            <span>{{rampupInfo.beginTime}}</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="结束时间：">
                            <span>{{rampupInfo.endTime}}</span>
                        </el-form-item>
                    </el-col>
                </el-row>
            </div>
            <div class="label-title">
                <span class="span-title"><i class="fa fa-th-large"></i>开始预热</span>
            </div>
            <div class="label-content">
                <el-row :gutter="10">
                    <el-col :span="11">
                        <el-form-item label="预热条数" label-width="80px">
                            <el-input-number v-model="rampupTarget" :min="0"></el-input-number>
                        </el-form-item>
                    </el-col>
                    <el-col :span="11">
                        <el-form-item label="截止时间">
                            <el-date-picker
                                v-model="expireTime"
                                type="date"
                                placeholder="选择日期">
                            </el-date-picker>
                        </el-form-item>
                    </el-col>
                </el-row>
                <div style="margin-top: 20px;text-align: center;">
                    <el-button size="small" @click="startRampup()"><i class="fa fa-caret-square-o-right"></i>开始预热</el-button>
                </div>
            </div>
        </el-form>
    </el-dialog>
  </div>
</template>

<script>
export default {
  props: ['preheadingInfo'],
  data() {
    return {
      loading: false,
      visible: true,
      rampupTarget: 10000,
      expireTime: '',
      rampupInfo: {
          rampupTarget: '',
          state: '',
          beginTime: '',
          endTime: '',
      },
      versionId: this.preheadingInfo.versionId,
    };
  },
  created() {
    this.init();
  },
  methods: {
    closeDialog() {
      this.$emit('close-dialog');
    },
    init() {
      this.$http.get(`/version/rampup/id.json?versionId=${this.versionId}`).then((data) => {
        this.rampupInfo = data;
      });
    },
    startRampup() {
      const params = {
        versionId: this.preheadingInfo.versionId,
        rampupTarget: this.rampupTarget,
      };
      this.$http.get(`/version/rampup/start.json?versionId=${params.versionId}&rampupTarget=${params.rampupTarget}`).then((data) => {
        this.$message.successMessage('开启预热成功', () => {
          this.init();
        });
      });
    },
  },
};
</script>
<style>

</style>
