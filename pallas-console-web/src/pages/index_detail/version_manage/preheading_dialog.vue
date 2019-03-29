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
                            <span >10000</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="当前状态：">
                            <span>正在预热</span>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="10">
                    <el-col :span="12">
                        <el-form-item label="开始时间：">
                            <span>2019-03-28 13:00:00</span>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="结束时间：">
                            <span>2019-03-28 14:00:00</span>
                        </el-form-item>
                    </el-col>
                </el-row>
            </div>
            <div class="label-title">
                <span class="span-title"><i class="fa fa-th-large"></i>开始预热</span>
            </div>
            <div class="label-content">
                <el-button size="small" style="margin-bottom: 10px;"><i class="fa fa-caret-square-o-right"></i>开始预热</el-button>
                <el-row :gutter="10">
                    <el-col :span="11">
                        <el-form-item label="预热条数" label-width="80px">
                            <el-input-number v-model="preheadNum" :min="0"></el-input-number>
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
            </div>
        </el-form>
    </el-dialog>
  </div>
</template>

<script>
export default {
  props: [],
  data() {
    return {
      loading: false,
      visible: true,
      preheadNum: 1000,
      expireTime: '',
    };
  },
  methods: {
    closeDialog() {
      this.$emit('close-dialog');
    },
  },
};
</script>
<style>

</style>
