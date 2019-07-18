import { MessageBox, Message } from 'element-ui';

export default {
  errorMessage(text) {
    MessageBox.alert(text, '错误信息', {
      type: 'error',
      confirmButtonText: '确定',
    });
  },
  successMessage(text, callback) {
    MessageBox.alert(text, '成功信息', {
      type: 'success',
      confirmButtonText: '确定',
    }).then(() => {
      callback();
    }).catch(() => {
    });
  },
  confirmMessage(text, callback) {
    MessageBox.confirm(text, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }).then(() => {
      callback();
    }).catch(() => {
    });
  },
  success(text) {
    Message({
      showClose: true,
      message: text,
      type: 'success',
    });
  },
};
