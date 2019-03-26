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
    monitorTimeInterval: {
      command: '30',
      from: new Date().getTime() - (30 * 60 * 1000),
      to: new Date().getTime(),
    },
  },
  getters: {
  },
  mutations: {
    [SET_LOGIN_USER](state, loginUser) {
      state.loginUser = loginUser;
    },
    [SET_MONITOR_TIME_INTERVAL](state, timeInterinfo) {
      state.monitorTimeInterval = { ...timeInterinfo };
    },
  },
  actions: {
    [SET_LOGIN_USER]({ commit }, loginUser) {
      commit(SET_LOGIN_USER, loginUser);
    },
    [SET_MONITOR_TIME_INTERVAL]({ commit }, timeInterinfo) {
      commit(SET_MONITOR_TIME_INTERVAL, timeInterinfo);
    },
  },
});
