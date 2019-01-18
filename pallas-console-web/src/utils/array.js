export default {
  removeByValue(arr, val) {
    arr.forEach((element, index) => {
      if (element === val) {
        arr.splice(index, 1);
      }
    });
  },
  isContainValue(arr, val) {
    let flag = false;
    if (arr.length > 0) {
      flag = arr.some((element) => {
        if (element === val) {
          return true;
        }
        return false;
      });
    }
    return flag;
  },
  strToArray(str) {
    if (str.indexOf(',') > 0) {
      const arr = str.split(',');
      return arr;
    }
    return [str];
  },
};
