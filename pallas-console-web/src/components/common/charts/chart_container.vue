<template>
    <div class="panel-container">
        <div class="panel-header">
            <span class="pull-left">{{title}}</span>
            <a class="pull-right" @click="togglePanel">
                <i v-if="isExpand" class="fa fa-minus"></i>
                <i v-if="!isExpand" class="fa fa-plus"></i>
            </a>
        </div>
        <div class="panel-body" v-show="isExpand">
            <slot name="chart"></slot>
        </div>
    </div>
</template>

<script>
export default {
  props: ['title'],
  data() {
    return {
      isExpand: true,
    };
  },
  methods: {
    togglePanel() {
      this.isExpand = !this.isExpand;
    },
  },
};
</script>
<style scoped>
.panel-container {
    border: 1px solid gray;
    margin-bottom: 20px;
}
.panel-container .panel-header {
    color: #eee;
    background-color: #373a3c;
    border-bottom: 1px solid gray;
    height: 35px;
    line-height: 35px;
    padding: 0 10px;
}
.panel-container .panel-header i {
    height: 35px;
    line-height: 35px;
}
.panel-container .panel-body {
    background-color: #373a3c;
}
</style>
