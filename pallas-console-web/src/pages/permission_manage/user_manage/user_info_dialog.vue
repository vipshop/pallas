<template>
    <el-dialog :title="userInfoTitle" :visible.sync="isVisible" :before-close="closeDialog" v-loading="loading" element-loading-text="请稍等···">
        <el-form :model="userInfo" :rules="rules" ref="userInfo" label-width="100px">
            <el-form-item label="登录名" prop="username">
                <el-col :span="22">
                    <el-input v-model="userInfo.username" :disabled="!isEditable"></el-input>
                </el-col>
            </el-form-item>
            <el-form-item label="用户名" prop="realName">
                <el-col :span="22">
                    <el-input v-model="userInfo.realName" :disabled="!isEditable"></el-input>
                </el-col>
            </el-form-item>
            <el-form-item label="密码" prop="password">
                <el-col :span="22">
                    <el-input v-model="userInfo.password"></el-input>
                </el-col>
            </el-form-item>
            <el-form-item
              label="邮箱"
              prop="email"
              :rules="[
                { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur,change' }
              ]"
            >
                <el-col :span="22">
                    <el-input v-model="userInfo.email"></el-input>
                </el-col>
            </el-form-item>
            <el-form-item label="角色" prop="roleNames">
                <el-col :span="22">
                    <el-select v-model="userInfo.roleNames" multiple placeholder="请选择" style="width: 100%;">
                      <el-option
                        v-for="item in rolesList"
                        :key="item.id"
                        :label="item.roleName"
                        :value="item.roleName">
                      </el-option>
                    </el-select>
                </el-col>
            </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">    
            <el-button @click="closeDialog()">取消</el-button>
            <el-button type="confirm" @click="handleSubmit()">确定</el-button>
        </div>
    </el-dialog>
</template>

<script>
export default {
  props: ['userInfo', 'userOperation', 'rolesList'],
  data() {
    return {
      isVisible: true,
      loading: false,
      rules: {
        username: [{ required: true, message: '请输入登录名', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
      },
    };
  },
  methods: {
    handleSubmit() {
      this.$refs.userInfo.validate((valid) => {
        if (valid) {
          this.userInfoRequest();
        }
      });
    },
    userInfoRequest() {
      this.loading = true;
      this.$http.post('/authorization/user/update.json', this.userInfo).then(() => {
        this.$message.successMessage(`${this.userInfoTitle}成功`, () => {
          this.$emit('user-info-success');
        });
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
      return this.userOperation === 'add';
    },
    userInfoTitle() {
      return this.isEditable ? '新增用户' : '编辑用户';
    },
  },
};
</script>
