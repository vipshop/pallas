import JSONbig from 'json-bigint';

export default {
  JSONbigParse(jsonString) {
    return JSONbig.parse(jsonString);
  },
  JSONbigStringify(obj, replacer, space) {
    return JSONbig.stringify(obj, replacer, space);
  },
  JSONbigStringifyFormat(obj) {
    return JSONbig.stringify(obj, undefined, 2);
  },
};
