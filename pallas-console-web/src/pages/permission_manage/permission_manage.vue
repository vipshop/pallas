<template>
    <div class="page-content">
        <div class="my-breadcrumb">
            <el-breadcrumb separator="/" class="my-breadcrumb-content">
                <el-breadcrumb-item :to="{ name:'permission_manage' }"><i class="fa fa-home"></i>权限管理</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="page-tab">
            <el-tabs v-model="activeTab" @tab-click="onTabClick">
                <el-tab-pane name="user_manage">
                    <span slot="label"><i class="fa fa-user"></i>用户管理</span>
                </el-tab-pane>
            </el-tabs>
            <router-view></router-view>
        </div>
    </div>
</template>

<script>

export default {
  data() {
    return {
      activeTab: 'user_manage',
    };
  },
  methods: {
    onTabClick() {
      this.$router.push({
        name: this.activeTab,
      });
    },
    getActiveTab() {
      const str = this.$route.name;
      if (str) {
        this.activeTab = str;
      }
    },
  },
  created() {
    this.activeTab = this.$route.name;
  },
  watch: {
    $route: 'getActiveTab',
  },
};
</script>
