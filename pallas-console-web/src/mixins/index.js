import Moment from 'moment';

export default {
  methods: {
    bytesToSize(bytes) {
      if (bytes === 0) return '0 B';
      const k = 1024;
      const sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
      const i = Math.floor(Math.log(bytes) / Math.log(k));
      const j = (bytes / (k ** i)).toFixed(2);
      return `${j} ${sizes[i]}`;
    },
    formatDate(time, format) {
      const date = new Date(time);
      const formatTime = Moment(date).format(format);
      return formatTime;
    },
  },
};
