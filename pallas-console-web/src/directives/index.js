import Vue from 'vue';
import dmp from './diffMatchPatch';

Vue.directive('diff', dmp.diff);
Vue.directive('processingDiff', dmp.processingDiff);
Vue.directive('semanticDiff', dmp.semanticDiff);
Vue.directive('lineDiff', dmp.lineDiff);
