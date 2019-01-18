<template>
    <div>
        <el-form :model="dataSource" :rules="rules" ref="dataSource" label-width="120px">
            <el-row :gutter="20">
                <el-col :span="7">
                    <el-form-item label="数据库地址" prop="ip">
                        <el-input v-model="dataSource.ip" placeholder="请输入ip或域名"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="7">
                    <el-form-item label="数据库PORT" prop="port" required>
                        <el-input v-model="dataSource.port"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="7">
                    <el-form-item label="数据库表" prop="tableName">
                        <el-input v-model="dataSource.tableName"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
            <el-row :gutter="20">
                <el-col :span="7">
                    <el-form-item label="数据库名" prop="dbname">
                        <el-input v-model="dataSource.dbname"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="7">
                    <el-form-item label="用户名" prop="username">
                        <el-input v-model="dataSource.username"></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="7">
                    <el-form-item label="密码" prop="password">
                        <el-input v-model="dataSource.password" type="password"></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>
    </div>
</template>

<script>
export default {
  props: ['dataSource', 'indexOperation'],
  data() {
    return {
      rules: {
        ip: [{ required: true, message: '请输入数据库地址', trigger: 'blur' }],
        port: [{ validator: this.$validate.validatePort, trigger: 'blur' }],
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
        dbname: [{ required: true, message: '请输入数据库名', trigger: 'blur' }, { validator: this.dbnameValidate, trigger: 'blur' }],
        tableName: [{ required: true, message: '请输入数据库表', trigger: 'blur' }, { validator: this.dbnameValidate, trigger: 'blur' }],
      },
    };
  },
  methods: {
    dbnameValidate(rule, value, callback) {
      this.$emit('db-validate', this.dataSource, callback.bind(this));
    },
  },
};
</script>
<style>
</style>

