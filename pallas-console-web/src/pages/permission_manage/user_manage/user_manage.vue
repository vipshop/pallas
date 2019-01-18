<template>
    <div class="my-tab-content">
        <div class="data-table-filter">
            <div class="pull-left">
                <el-form :inline="true" class="demo-form-inline">
                    <el-form-item label="">
                        <el-input placeholder="请搜索" v-model="formSearch.keywords" @keyup.enter.native="init"></el-input>
                    </el-form-item>
                    <el-form-item class="filter-search">
                        <el-button type="primary" icon="search" @click="init">查询</el-button>
                    </el-form-item>
                </el-form>
            </div>
            <div class="pull-right" v-if="allPrivilege">
                <el-button type="primary" icon="plus" @click="handleAdd">新增用户</el-button>
            </div>
        </div>
        <div class="content">
            <template>
                <el-table :data="userList" border style="width: 100%" v-loading="loading" element-loading-text="请稍等···">
                    <el-table-column label="登录名" prop="username"></el-table-column>
                    <el-table-column label="用户名" prop="realName"></el-table-column>
                    <el-table-column label="角色" prop="roles">
                        <template scope="scope">
                            <el-tag v-for="item in scope.row.roles" :key="item.id" style="margin-right: 3px;">{{item.description}}</el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column label="邮箱" prop="email">
                        <template scope="scope">{{scope.row.email || '-'}}</template>
                    </el-table-column>
                    <el-table-column label="创建时间" prop="createTime">
                        <template scope="scope">{{scope.row.createTime | formatDate}}</template>
                    </el-table-column>
                    <el-table-column label="操作" width="80" v-if="allPrivilege">
                        <template scope="scope">
                            <el-tooltip content="编辑" placement="top">
                                <el-button type="text" @click="handleEdit(scope.row)"><i class="fa fa-edit"></i></el-button>
                            </el-tooltip>
                            <el-tooltip content="删除" placement="top" v-if="scope.row.username !== loginUser">
                                <el-button type="text" @click="handleDelete(scope.row)"><i class="fa fa-trash"></i></el-button>
                            </el-tooltip>
                        </template>
                    </el-table-column>
                </el-table>
            </template>
            <div class="my-pagination" v-if="total != 0">
                <el-pagination layout="prev, pager, next" :total="total" :page-size="pageSize" :current-page="currentPage" @current-change="changePage"></el-pagination>
            </div>
        </div>
        <div v-if="isUserInfoVisible">
            <user-info-dialog
                :roles-list="rolesList"
                :user-operation="userOperation"
                :user-info="userInfo"
                @close-dialog="closeDialog"
                @user-info-success="userInfoSuccess">
            </user-info-dialog>
        </div>
    </div>
</template>

<script>
import UserInfoDialog from './user_info_dialog';

export default {
  components: {
    'user-info-dialog': UserInfoDialog,
  },
  data() {
    return {
      loading: false,
      userList: [],
      rolesList: [],
      pageSize: 10,
      currentPage: 1,
      total: 3,
      allPrivilege: true,
      formSearch: {
        keywords: '',
      },
      isUserInfoVisible: false,
      userInfo: {},
      userOperation: '',
    };
  },
  computed: {
    loginUser() {
      return this.$store.state.loginUser;
    },
  },
  methods: {
    getUserList() {
      const params = {
        currentPage: 1,
        pageSize: this.pageSize,
        keywords: this.formSearch.keywords,
      };
      return this.$http.get('/authorization/user/page.json', params).then((data) => {
        this.userList = data.list;
        this.allPrivilege = data.allPrivilege;
        this.total = data.total;
      });
    },
    handleAdd() {
      const userAddInfo = {
        username: '',
        realName: '',
        password: '',
        email: '',
        roleNames: [],
      };
      this.userOperation = 'add';
      this.userInfo = JSON.parse(JSON.stringify(userAddInfo));
      this.isUserInfoVisible = true;
    },
    handleEdit(row) {
      this.userOperation = 'edit';
      this.userInfo = {
        ...row,
        roleNames: row.roles.map(v => v.roleName),
      };
      this.isUserInfoVisible = true;
    },
    userInfoSuccess() {
      this.init();
      this.closeDialog();
    },
    closeDialog() {
      this.isUserInfoVisible = false;
    },
    handleDelete(row) {
      this.$message.confirmMessage(`确定删除用户 ${row.username} 吗?`, () => {
        this.loading = true;
        this.$http.get(`authorization/user/delete/${row.id}.json`).then(() => {
          this.$message.successMessage('删除用户成功', () => {
            this.init();
          });
        })
        .finally(() => {
          this.loading = false;
        });
      });
    },
    changePage(currentPage) {
      this.currentPage = currentPage;
      this.init();
    },
    getRolesList() {
      return this.$http.get('/authorization/role/page.json').then((data) => {
        this.rolesList = data.list;
      });
    },
    init() {
      this.loading = true;
      Promise.all([this.getUserList(), this.getRolesList()]).then(() => {})
      .finally(() => {
        this.loading = false;
      });
    },
  },
  created() {
    this.init();
  },
};
</script>
