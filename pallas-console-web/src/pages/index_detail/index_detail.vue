<template>
    <div class="page-content">
        <div class="my-breadcrumb">
            <el-breadcrumb separator="/" class="my-breadcrumb-content">
                <el-breadcrumb-item :to="{ name:'index_manage' }"><i class="fa fa-home"></i>索引管理</el-breadcrumb-item>
                <el-breadcrumb-item>{{indexName}}({{indexId}})</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <div class="page-tab">
            <el-tabs v-model="activeTab" @tab-click="onTabClick">
                <el-tab-pane name="version_manage">
                    <span slot="label"><i class="fa fa-share-alt"></i>版本管理</span>
                </el-tab-pane>
                <el-tab-pane name="template_manage">
                    <span slot="label"><i class="fa fa-th-large"></i>模板管理</span>
                </el-tab-pane>
                <el-tab-pane name="route_manage">
                    <span slot="label"><i class="fa fa-sitemap"></i>路由管理</span>
                </el-tab-pane>
                <el-tab-pane name="service_manage">
                    <span slot="label"><i class="fa fa-server"></i>服务治理</span>
                </el-tab-pane>
                <el-tab-pane name="dynamic_manage">
                    <span slot="label"><i class="fa fa-sort-amount-asc"></i>索引动态</span>
                </el-tab-pane>
                <el-tab-pane name="flow_record">
                    <span slot="label"><i class="fa fa-camera"></i>流量记录</span>
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
      indexId: this.$route.query.indexId,
      indexName: this.$route.query.indexName,
      activeTab: 'version_manage',
    };
  },
  methods: {
    onTabClick() {
      this.$router.push({
        name: this.activeTab,
        query: {
          indexId: this.$route.query.indexId,
          indexName: this.$route.query.indexName,
        },
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
<style>
.page-tab .el-tabs {
    background-color: #373a3c;
}
</style>
