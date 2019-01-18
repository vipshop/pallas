<template>
    <el-dialog :title="tokenInfoTitle" :visible.sync="isVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="tokenInfo" :rules="rules" ref="tokenInfo" label-width="80px">
            <el-row>
                <el-col :span="16">
                    <el-form-item label="token" prop="clientToken">
                        <el-input v-model="tokenInfo.clientToken"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="6">
                    <el-form-item label="" prop="clientToken" label-width="10px">
                        <el-button size="small" @click="reGenerate">重新生成</el-button>
                        <el-popover placement="right" trigger="hover" content="Token的使用需在Pallas Rest Client发起Pallas Search查询时传入，重新生成后需点击更新按钮保存">
                            <i class="fa fa-info-circle" slot="reference"></i>
                        </el-popover>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22">
                    <el-form-item label="标题" prop="title">
                        <el-input v-model="tokenInfo.title"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row>
                <el-col :span="22">
                    <el-form-item label="启用状态" prop="enabled">
                        <div class="my-switch">
                            <el-switch v-model="tokenInfo.enabled"></el-switch>
                            <el-popover placement="right" trigger="hover" content="如果禁用，客户端就直连es，不经过pallas search">
                              <i class="fa fa-info-circle" slot="reference"></i>
                          </el-popover>
                        </div>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">    
           <el-button @click="closeDialog()">取消</el-button>
           <el-button type="confirm" @click="submitInfo()">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['tokenInfo', 'tokenInfoTitle', 'tokenInfoOperate'],
  data() {
    return {
      loading: false,
      isVisible: true,
      rules: {
        title: [{ required: true, message: '请输入Token标题', trigger: 'blur' }],
      },
    };
  },
  methods: {
    reGenerate() {
      this.loading = true;
      this.$http.post('/token/security/token.json').then((data) => {
        this.tokenInfo.clientToken = data;
      })
      .finally(() => {
        this.loading = false;
      });
    },
    submitInfo() {
      this.$refs.tokenInfo.validate((valid) => {
        if (valid) {
          this.tokenRequest('/token/insert.json');
        }
      });
    },
    tokenRequest(url) {
      this.loading = true;
      this.$http.post(url, this.tokenInfo).then(() => {
        const resp = {
          operation: this.tokenInfoOperate,
          clientToken: this.tokenInfo.clientToken,
          title: this.tokenInfo.title,
        };
        this.$emit('token-info-success', resp);
      })
      .finally(() => {
        this.loading = false;
      });
    },
    closeDialog() {
      this.$emit('close-dialog');
    },
  },
  computed: {
    isEditable() {
      return this.tokenInfoOperate === 'add';
    },
  },
};

</script>

<style type="text/css">
</style>
