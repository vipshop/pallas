import Vue from 'vue';
import moment from 'moment';

Vue.filter('formatDate', (time) => {
  let formatTime;
  if (time !== null) {
    const date = new Date(time);
    formatTime = moment(date).format('YYYY-MM-DD HH:mm:ss');
  } else {
    formatTime = '-';
  }
  return formatTime;
});
Vue.filter('formatOnlyDate', (time) => {
  const date = new Date(time);
  return moment(date).format('MM-DD');
});
Vue.filter('formatOnlyTime', (time) => {
  const date = new Date(time);
  return moment(date).format('HH:mm:ss');
});
