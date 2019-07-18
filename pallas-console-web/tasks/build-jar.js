process.env.NODE_ENV = 'springboot_package'

var rm = require('rimraf')
var path = require('path')
var chalk = require('chalk')
var webpack = require('webpack')
var appConf = require('./conf/jar.app.conf')
var webpackConfig = require('./conf/webpack.jar.conf')
var utils = require('./utils')

rm(path.join(appConf.buildRoot, appConf.assetsSubDirectory), err => {
  if (err) throw err

  utils.checkLoaderEnable(webpackConfig, 'eslintEnable', 'eslint-loader')
  utils.checkLoaderEnable(webpackConfig, 'babelEnable', 'babel-loader')

  webpack(webpackConfig, function(err, stats) {
    if (err) throw err
    process.stdout.write(stats.toString({
        colors: true,
        modules: false,
        children: false,
        chunks: false,
        chunkModules: false
      }) + '\n\n')

    console.log(chalk.cyan('  Build complete.\n'))
  })
})
