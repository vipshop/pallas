var path = require('path')

module.exports = {
  env: process.env.NODE_ENV,
  entry: './src/index.js',
  devPort: 8081,
  assetsSubDirectory: 'assets',
  assetsPublicPath: '/',
  proxyTable: {
    '/pallas': {
       target: 'http://127.0.0.1:8080',
       changeOrigin: true,
     },
  },
  buildIndex: path.resolve(__dirname, '../../dist/index.html'),
  buildRoot: path.resolve(__dirname, '../../dist'),
  // 关闭eslint
  eslintEnable: true,
  // 关闭babel(需要源码为非转换代码，并且eslint是正确配置才可以)
  babelEnable: true
};