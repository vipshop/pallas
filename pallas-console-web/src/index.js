import Vue from 'vue';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-default/index.css';
import 'font-awesome/css/font-awesome.css';
import './components';
import './directives/';
import App from './App';
import Store from './store';
import router from './routers';
import Utils from './utils';
import './styles/main.css';
import './styles/element.css';

Vue.use(ElementUI);
Vue.use(Utils);

/* eslint-disable no-new */
new Vue({
  el: '#app',
  store: Store,
  router,
  render: h => h(App),
});
