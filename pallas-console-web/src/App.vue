<template>
    <div>
        <template v-if="initialized || initComponent === 'Login'">
            <component :is="initComponent" @login-success="loginSuccess"></component>
        </template>
        <template v-else>
            <div v-loading.fullscreen="loading" element-loading-text="Pallas正在初始化应用，请稍等···"></div>
        </template>
    </div>
</template>
<script>
import Login from './Login';
import { SET_LOGIN_USER } from './store/types';

export default {
  data() {
    return {
      loading: false,
      initialized: false,
      interval: 0,
    };
  },
  methods: {
    loginSuccess() {
      this.init();
    },
    // 隔十分钟请求一次心跳
    heartbeat() {
      this.interval = setInterval(() => {
        this.$http.get('/heartbeat.json').then(() => {
        });
      }, 600000);
    },
    getLoginUser() {
      return this.$http.get('/system/loginUser.json').then((data) => {
        this.$store.dispatch(SET_LOGIN_USER, data);
      });
    },
    init() {
      this.loading = true;
      Promise.all([this.getLoginUser()]).then(() => {
        this.heartbeat();
      })
      .finally(() => {
        this.initialized = true;
        this.loading = false;
      });
    },
  },
  computed: {
    initComponent() {
      let resultComponent = '';
      if (this.$route.path === '/login') {
        resultComponent = 'Login';
      } else {
        resultComponent = 'Container';
      }
      return resultComponent;
    },
  },
  created() {
    if (this.$route.path !== '/login') {
      this.init();
    }
  },
  destroyed() {
    clearInterval(this.interval);
  },
  components: {
    Login,
  },
};
</script>
