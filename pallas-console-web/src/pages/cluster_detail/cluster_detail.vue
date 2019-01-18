<template>
    <div class="page-content" v-loading="loading" element-loading-text="请稍等···">
        <div class="my-breadcrumb">
            <el-breadcrumb separator="/" class="my-breadcrumb-content">
                <el-breadcrumb-item :to="{ name:'cluster_manage' }"><i class="fa fa-home"></i>ES集群管理</el-breadcrumb-item>
                <el-breadcrumb-item>{{clusterId}}</el-breadcrumb-item>
            </el-breadcrumb>
        </div>
        <dashboard-component :cluster-info="clusterInfo"></dashboard-component>
    </div>
</template>

<script>
import ClusterDashboard from './cluster_dashboard/cluster_dashboard';

export default {
  data() {
    return {
      loading: false,
      clusterInfo: {},
      clusterId: this.$route.query.clusterId,
    };
  },
  methods: {
    getClusterInfo() {
      this.loading = true;
      this.$http.get(`/cluster/id.json?clusterId=${this.$route.query.clusterId}`).then((data) => {
        this.clusterInfo = data;
      })
      .finally(() => {
        this.loading = false;
      });
    },
  },
  components: {
    'dashboard-component': ClusterDashboard,
  },
  created() {
    this.getClusterInfo();
  },
};
</script>
