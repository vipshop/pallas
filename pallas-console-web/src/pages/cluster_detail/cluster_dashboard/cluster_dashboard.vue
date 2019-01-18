<template>
    <iframe :src="clusterUrl" class="cluster_dashboard" width="100%" height="70%" scrolling=0 frameborder=0></iframe>
</template>

<script>

export default {
  props: ['clusterInfo'],
  computed: {
    clusterUrl() {
      let http = '';
      if (this.clusterInfo.httpAddress) {
        http = this.clusterInfo.httpAddress;
      }
      return `/cerebro/#/overview?cluster=${this.clusterInfo.clusterId}&host=${http}`;
    },
  },
};
</script>
