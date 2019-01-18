<template>
    <div class="footer"><p>{{versionNumber}}</p></div>
</template>

<script>
export default {
  props: ['activeIndex'],
  data() {
    return {
      versionNum: '',
    };
  },
  methods: {
    getVersionNumber() {
      this.$http.get('/system/getVersion.json').then((data) => {
        this.versionNum = data;
      });
    },
  },
  created() {
    this.getVersionNumber();
  },
  computed: {
    versionNumber() {
      return `Pallas-Console-${this.versionNum} ©2017`;
    },
  },
};

</script>
<style>
.footer {
    position: fixed;
    bottom: 0;
    width: 100%;
}
.footer p {
    text-align: center;
    color: rgba(192,192,192,0.5);
    margin-top: 0px;
    margin-bottom: 0px;
    font-size: 13px;
}
</style>
