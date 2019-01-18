## Starter project using webpack2 with ES6 and Vue

基于Webpack2和ES6/Vue，用于构建单页应用的Starter Project。

这个Project使用了：webpack2 + Babel + ESLint + Vue + webpack-dev-middleware + webpack-hot-middleware + express

* [如何使用](#getting-started) – 怎么使用这个Project.
* [目录说明](#file-structure) – 建议目录说明.

#### 重要的前端框架

Vue：https://cn.vuejs.org/v2/guide/index.html

element（vue组件库，1.4.10版本）：http://element-cn.eleme.io/1.4/#/zh-CN/component/quickstart

vue-router（vue路由配置）：https://router.vuejs.org/zh-cn/

vuex（中心化状态管理）：https://vuex.vuejs.org/

momentjs (日期处理类)：http://momentjs.cn/

axios (http client)：https://www.npmjs.com/package/axios

### Getting started

#### 环境要求

Node4.0以上

建议使用Node>=6 / NPM>=3版本, 可以使用[nvm](https://github.com/creationix/nvm#usage)来切换不同Node版本

推荐使用[Yarn](https://yarnpkg.com/) 

#### 如何安装

* npm：`npm install` or `npm i`
* yarn： `yarn install`

#### 进入开发模式

* npm: `npm run dev`
* yarn: `yarn dev`

命令完成后会自动打开浏览器进入[http://localhost:8081](http://localhost:8081)

如果需要配置不同端口可以在/tasks/conf/app.conf.js的`devPort`里修改为其他端口

#### 构建生产可用文件

* npm: `npm run build`
* yarn: `yarn build`

#### 引入新的依赖

##### import devDependencies

* npm: `npm install --save-dev devDependency@version`
* yarn: `yarn add --dev devDependency@version`

##### import dependencies

* npm: `npm install --save dependency@version`
* yarn: `yarn add dependency@version`

#### 锁定依赖版本

yarn会自动更新yarn.lock文件

但是npm需要手动更新npm-shrinkwrap.json，执行命令：`npm shrinkwrap`即可，如果有报错，可以执行`npm prune`再尝试`npm shrinkwrap`

#### 屏蔽ESLint插件

由于ESLint默认在开发或者生产模式下都是打开的，设置/tasks/conf/app.conf.js里的`eslintEnable: true,`变量为false即可屏蔽ESLint插件

#### ESLint配置

当前使用了eslint-config-airbnb-base来配置ESLint规则

如果需要修改，那么首先增加新的config dependency`yarn add --dev eslint-config-***`

再修改.eslintrc.js文件里的`extends:'airbnb-base'`这一行来调整为新的配置

也可以在.eslintrc.js文件里的rules下来新增自己的自定义规则

#### Babel配置

当前使用es2015和stage-2的babel preset.如果需要调整，那么设置.babelrc文件既可

如果不需要使用Babel插件来转换代码，设置/tasks/conf/app.conf.js里的`babelEnable: true,`变量为false即可屏蔽Babel插件

#### 新增webpack loader

首先安装依赖`npm install --save-dev package` or `yarn add --dev package`

webpack配置放在/tasks/conf/webpack开头的配置文件里，当前这个版本有两个配置(dev/prod)，所以新增loader的话需要在这两个文件配置里的rules下新增


### File structure

建议目录结构如下

```
├── assets                        静态资源文件，会直接拷入到发布目录
├── src                           源码目录
│   ├── components                公共组件目录，pages里的UI一般由这里的组件和第三方组件拼装而成   
│   │   ├── DatePicker            公共DatePicker组件     
│   │   │   ├── DatePicker.js     DatePicker组件JS文件
│   │   │   ├── DatePicker.html   DatePicker组件模板
│   │   │   └── DatePicker.css    DatePicker组件样式
│   │   └── ...其他组件   
│   ├── pages                     应用业务界面目录，一般存放可路由到的业务UI，比如首页/索引管理/索引详情等
│   │   ├── index_manage          索引管理页面     
│   │   │   ├── index_manage.vue  索引管理页面vue文件
│   │   └── ...其他pages  
│   ├── directives                自定义指令目录
│   ├── image                     图片目录
│   ├── store                     存放vuex数据状态目录
│   ├── styles                    应用公共样式
│   ├── utils                     公用方法目录，包括http请求，校验等等
│   ├── App.vue                   入口vue组件，通过index.js渲染
│   ├── Login.vue                 登录组件
│   ├── routers.js                路由配置
│   └── index.js                  入口JS
├── tasks                         task相关文件存放目录
│   ├── conf                      task配置目录
│   │   ├── app.conf.js           一些全局设置和开关配置文件
│   │   ├── webpack.dev.conf.js   webpack开发模式配置文件
│   │   └── webpack.prod.conf.js  webpack生产模式配置文件
│   ├── utils                     task用到工具方法目录
│   ├── build.js                  yarn build执行文件，执行构建适合production的文件
│   └── dev.js                    yarn dev执行文件，进入开发模式方便开发
├── index.html	                  入口html
├── node_modules                  node_modules
├── package.json                  package.json
├── README.md                     README
├── npm-shrinkwrap.json           npm shirinkwrap文件
├── .babelrc                      babel配置
├── .eslintrc.js                  eslint配置
├── .eslintignore                 eslint忽略目录
└── yarn.lock                     yarn lock文件
```

#### 本地环境首次搭建详细步骤：

1、安装node（node --version查看node是否成功安装，新版本node已经集合了npm，如未安装npm请翻阅教程重新安装）

2、安装yarn  npm install -g yarn  ( yarn --version 查看yarn是否成功安装 )

3、前端开发目录如D:\project\pallas_web，运行yarn install或npm install(安装项目自动化工程所需插件)

4、键入yarn dev或npm run dev命令运行本地项目（如需打包请键入yarn build或npm run build），命令完成后会自动打开浏览器进入(http://localhost:8081)
