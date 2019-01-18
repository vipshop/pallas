<template>
    <div class="login-box">
        <div class="login-header">
            用户登录
        </div>
        <div class="login-body">
            <div class="msg-error" v-if="loginErrorMsg"><i class="el-icon-remove"></i>{{loginErrorMsg}}</div> 
            <el-row>  
                <el-col>  
                    <el-input size="medium" v-model="loginInfo.username" placeholder="请输入帐号"></el-input>   
                </el-col>  
            </el-row>  
            <el-row>  
                <el-col>  
                    <el-input v-model="loginInfo.password" type="password" placeholder="请输入密码"></el-input>  
                </el-col>  
            </el-row>  
            <el-row>
                <el-col>  
                    <el-button @click="login()" style="width:100%" type="primary" v-loading="loading">登录</el-button>  
                </el-col>  
            </el-row>
        </div>
    </div>
</template>
<script>

export default {
  data() {
    return {
      loading: false,
      loginInfo: {
        username: '',
        password: '',
      },
      loginErrorMsg: '',
    };
  },
  methods: {
    login() {
      const params = new URLSearchParams();
      params.append('username', this.loginInfo.username);
      params.append('password', this.loginInfo.password);
      this.loading = true;
      this.$http.post('/authorization/login', params).then(() => {
        this.$router.push({ path: '/' });
        this.$emit('login-success');
      })
      .finally(() => {
        this.loading = false;
      });
    },
  },
  created() {
  },
};
</script>
<style>
.login-box {  
    margin: 15% auto;
    width: 400px;
    border: 1px solid #808080;
    border-radius: 6px;
}
.login-box .login-header {
    color: #eee;
    padding: 10px;
    border-bottom: 1px solid #808080;
}
.login-box .login-body {
    padding: 20px 40px;
}
.login-box .login-body .el-row {
    margin-bottom: 20px;
}
.login-box .login-body .el-row:last-child {  
    margin-bottom: 0;  
} 
.login-box .login-body .msg-error {
    position: relative;
    background: #ffebeb;
    color: #e4393c;
    border: 1px solid #faccc6;
    line-height: 15px;
    font-size: 13px;
    padding: 3px 10px;
    margin-bottom: 10px;
}
.login-box .login-body .msg-error i {
    margin-right: 5px;
}
</style>
