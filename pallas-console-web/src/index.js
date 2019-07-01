import Vue from 'vue';
import ElementUI from 'element-ui';
import VueClipboard from 'vue-clipboard2';
import 'element-ui/lib/theme-default/index.css';
import 'font-awesome/css/font-awesome.css';
import './components';
import './directives/';
import App from './App';
import Store from './store';
import mixins from './mixins';
import router from './routers';
import Utils from './utils';
import './styles/main.css';
import './styles/element.css';

Vue.mixin(mixins);
Vue.use(ElementUI);
Vue.use(Utils);
Vue.use(VueClipboard);

/* eslint-disable no-new */
new Vue({
  el: '#app',
  store: Store,
  router,
  render: h => h(App),
});
