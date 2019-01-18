import axios from 'axios';
import message from './message';

axios.defaults.baseURL = '/pallas';

let showError = false;

export default {
  get(url, data) {
    return this.request(url, data, 'GET');
  },

  post(url, data) {
    return this.request(url, data, 'POST');
  },

  put(url, data) {
    return this.request(url, data, 'PUT');
  },

  delete(url, data) {
    return this.request(url, data, 'DELETE');
  },

  buildErrorHandler(msg, callback) {
    if (!showError) {
      message.errorMessage(msg);
      showError = false;
    }
    if (callback) {
      callback();
    }
  },

  request(url, dataObj, methodType) {
    return new Promise((resolve, reject) => {
      const config = {
        url,
        method: methodType,
        cache: false,
      };
      /* eslint-disable no-case-declarations */
      switch (methodType) {
        case 'GET':
        case 'DELETE':
          config.params = dataObj;
          break;
        default:
          config.data = dataObj;
          break;
      }
      axios.request(config).then((response) => {
        if (response.status === 200) {
          if (response.data !== '') {
            if (response.data.status === 200) {
              showError = false;
              resolve(response.data.data);
            } else if (response.data.status === 401) {
              top.location.href = response.data.message;
            } else {
              message.errorMessage(response.data.message || '请求失败！');
              showError = true;
              reject();
            }
          } else {
            showError = false;
            resolve(response.data.data);
          }
        }
      })
      .catch((error) => {
        const errMsg = error.response.data ? error.response.data.message || '请求失败' : '请求失败';
        message.errorMessage(errMsg);
        showError = true;
        reject();
      });
    });
  },
};
