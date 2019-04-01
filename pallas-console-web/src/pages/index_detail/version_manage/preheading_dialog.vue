<template>
  <div class="version-info-dialog">
    <el-dialog title="索引预热" v-model="visible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="formInfo" ref="formInfo" label-width="100px">
            <div class="label-title">
                <span class="span-title"><i class="fa fa-th-large"></i>最近预热情况</span>
            </div>
            <div class="label-content">
                <div style="margin-bottom: 10px;">
                    <el-button size="small" @click="init()"><i class="fa fa-refresh"></i>刷新</el-button>
                    <el-button size="small" @click="handleStop()" v-if="rampupInfo.state === 'doing'"><i class="fa fa-stop-circle"></i>停止</el-button>
                    <el-button size="small" @click="toMercury()" class="pull-right"><i class="fa fa-area-chart"></i>Mercury监控</el-button>
                </div>
                <el-row :gutter="10">
                    <el-col :span="12">
                        <el-form-item label="总预热条数：">
                            <span>{{rampupInfo.rampupTarget}}</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="当前状态：">
                            <span>{{rampupStatusMap[rampupInfo.state]}}</span>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="10">
                    <el-col :span="12">
                        <el-form-item label="开始时间：">
                            <span>{{rampupInfo.beginTime | formatDate}}</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="结束时间：">
                            <span>{{rampupInfo.endTime | formatDate}}</span>
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
                        <el-form-item label="预热条数" prop="rampupTarget" label-width="80px">
                            <el-input v-model.number="formInfo.rampupTarget" :disabled="rampupInfo.state === 'doing'"></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="11">
                        <el-form-item label="截止时间" prop="endTime">
                            <el-date-picker
                                clearable
                                v-model="formInfo.endTime"
                                :disabled="rampupInfo.state === 'doing'"
                                type="datetime"
                                placeholder="选择日期时间">
                            </el-date-picker>
                        </el-form-item>
                    </el-col>
                </el-row>
                <div style="margin-top: 20px;text-align: center;">
                    <el-button v-if="rampupInfo.state !== 'doing'" :disabled="preheadingInfo.isUsed" size="small" @click="startRampup()" :title="preheadingInfo.isUsed ? '该版本已启用' : ''"><i class="fa fa-caret-square-o-right"></i>开始预热</el-button>
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
      formInfo: {
        rampupTarget: '',
        endTime: '',
      },
      rampupInfo: {
        rampupTarget: '',
        state: '',
        beginTime: '',
        endTime: '',
      },
      rampupStatusMap: {
        doing: '正在预热',
        finish: '预热完成',
        stop: '预热终止',
      },
    };
  },
  created() {
    this.init();
  },
  methods: {
    toMercury() {
      window.open('http://trace.vip.vip.com/#/group/aaa/gd15-pes-ebs.api.vip.com/endpointDetails');
    },
    closeDialog() {
      this.$emit('close-dialog');
    },
    init() {
      this.loading = true;
      this.$http.get(`/version/rampup/id.json?versionId=${this.preheadingInfo.versionId}`).then((data) => {
        this.rampupInfo = data;
      })
      .finally(() => {
        this.loading = false;
      });
    },
    handleStop() {
      this.loading = true;
      this.$http.get(`/version/rampup/stop.json?versionId=${this.versionId}`).then(() => {
        this.$message.successMessage('停止预热成功', () => {
          this.init();
        });
      })
      .finally(() => {
        this.loading = false;
      });
    },
    startRampup() {
      if (!this.formInfo.rampupTarget && !this.formInfo.endTime) {
        this.$message.errorMessage('请填写预热条数或截止时间！');
      } else if (this.formInfo.rampupTarget && this.formInfo.endTime) {
        this.$message.errorMessage('预热条数与截止时间二选一！');
      } else {
        const params = {
          versionId: this.preheadingInfo.versionId,
          ...this.formInfo,
        };
        this.loading = true;
        this.$http.get('/version/rampup/start.json?', params).then(() => {
          this.$message.successMessage('开启预热成功', () => {
            this.init();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      }
    },
  },
};
</script>
<style>
</style>
