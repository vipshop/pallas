/* eslint-disable no-param-reassign */
import Vue from 'vue';
import Vuex from 'vuex';
import {
  SET_LOGIN_USER,
} from './types';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {
    loginUser: '',
  },
  getters: {
  },
  mutations: {
    [SET_LOGIN_USER](state, loginUser) {
      state.loginUser = loginUser;
    },
  },
  actions: {
    [SET_LOGIN_USER]({ commit }, loginUser) {
      commit(SET_LOGIN_USER, loginUser);
    },
  },
});
