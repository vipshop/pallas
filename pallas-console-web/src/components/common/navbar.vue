<template>
    <div class="navbar">
        <div class="navbar-title">
            <span>
                <img src="../../image/pallas.png"/>
                Pallas一站式检索平台
            </span>
        </div>
        <div class="navbar-content">
            <el-menu theme="dark" :default-active="activeIndex" class="el-menu-demo" mode="horizontal" :router="true">
                <template v-for='item in serviceList'>
                  <el-menu-item :index="item.index" :route='item' :key="item.index"><i :class="item.icon"></i>{{item.title}}</el-menu-item>
                </template>
                <el-submenu index="3" class="pull-right">
                    <template slot="title"><i class="fa fa-user"></i>{{loginUser || 'null'}}</template>
                    <el-menu-item index=""><a style="display: block;" href="/pallas/logout"><i class="fa fa-sign-out"></i>注销</a></el-menu-item>
                </el-submenu>
            </el-menu>
        </div>
    </div>
</template>

<script>
export default {
  data() {
    return {
      serviceList: [
        { index: '/index', title: '索引管理', icon: 'fa fa-search', path: this.$routermapper.GetPath('indexManage') },
        { index: '/cluster', title: 'ES集群管理', icon: 'fa fa-sitemap', path: this.$routermapper.GetPath('clusterManage') },
        { index: '/authority', title: '模板变更', icon: 'fa fa-check-square-o', path: this.$routermapper.GetPath('authorityManage') },
        { index: '/agent', title: '代理管理', icon: 'fa fa-arrows', path: this.$routermapper.GetPath('agentManage') },
        { index: '/token', title: 'Token管理', icon: 'fa fa-key', path: this.$routermapper.GetPath('tokenManage') },
        { index: '/plugin', title: '插件管理', icon: 'fa fa-puzzle-piece', path: this.$routermapper.GetPath('pluginManage') },
        { index: '/permission', title: '权限管理', icon: 'fa fa-user-plus', path: this.$routermapper.GetPath('permissionManage') },
      ],
    };
  },
  computed: {
    activeIndex() {
      return this.$route.path.split('_')[0];
    },
    loginUser() {
      return this.$store.state.loginUser;
    },
  },
};

</script>

<style type="text/css">
.navbar {
    width: 100%;
    display: table;
}

.navbar-title {
    display: table-cell;
    background-color: #373a3c;
    color: white;
    height: 60px;
    line-height: 60px;
    font-weight: 700;
    width: 260px;
}

.navbar-title span img {
    float:left;
    padding: 10px 10px 0px 25px;
}

.navbar-content {
    display: table-cell;
    vertical-align: middle;
}

.navbar-content ul {
    background-color: #373a3c;
    border-radius: 0;
}

.logout-a-link {
    text-decoration: initial;
}

</style>
