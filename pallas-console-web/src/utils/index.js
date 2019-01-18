import array from './array';
import http from './request';
import message from './message';
import option from './option';
import validate from './validate';
import routerMapper from './router_mapper';

const Utils = {
  array,
  http,
  message,
  option,
  validate,
  routerMapper,
};
/* eslint-disable no-param-reassign */
Utils.install = (Vue) => {
  Vue.prototype.$array = array;
  Vue.prototype.$http = http;
  Vue.prototype.$message = message;
  Vue.prototype.$option = option;
  Vue.prototype.$validate = validate;
  Vue.prototype.$routermapper = routerMapper;
};
export default Utils;
