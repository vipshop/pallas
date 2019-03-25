/* eslint-disable no-param-reassign */
import Vue from 'vue';
import Vuex from 'vuex';
import {
  SET_LOGIN_USER,
  SET_MONITOR_TIME_INTERVAL,
} from './types';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    loginUser: '',
    monitorTimeInterval: '30',
  },
  getters: {
  },
  mutations: {
    [SET_LOGIN_USER](state, loginUser) {
      state.loginUser = loginUser;
    },
    [SET_MONITOR_TIME_INTERVAL](state, timeInterval) {
      state.monitorTimeInterval = timeInterval;
    },
  },
  actions: {
    [SET_LOGIN_USER]({ commit }, loginUser) {
      commit(SET_LOGIN_USER, loginUser);
    },
    [SET_MONITOR_TIME_INTERVAL]({ commit }, timeInterval) {
      commit(SET_MONITOR_TIME_INTERVAL, timeInterval);
    },
  },
});
