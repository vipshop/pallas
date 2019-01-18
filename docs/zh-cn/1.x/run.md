# Pallas代码运行指引

## 概述
基于[Maven](http://maven.apache.org/)构建。

项目整体划分为以下模块：

  - 前后端分离的Pallas-console模块：pallas-console；pallas-console-web
  - 封装Dao/Service层：pallas-core；pallas-core-open
  - 封装公共类：pallas-common
  - 基于Netty的ES代理模块：pallas-search
  - 客户端：pallas-rest-client
  - 插件：pallas-plugin

## 0 导入schema

  - 安装Mysql，导入数据库schema及初始数据
  
  - schema信息保存在pallas-core模块下的resources/db/ddl/mysql/schema.sql中
  
  - 初始数据保存在pallas-core模块下的resources/db/dml/mysql/data.sql中
  
## 1 Pallas core

  - 封装Dao/Service层
  
  - pallas-console,pallas-search都依赖它，因此需先构建pallas-core,pallas-core-open
  
  - 构建命令：mvn clean package

## 2 Pallas console

  - 依赖pallas-core,pallas-core-open
  
  - 以maven方式运行：-Dspring.profiles.active=integratetest -Dpallas.stdout=true -Dpallas.db.type=h2
  
    - 数据源：支持mysql，h2
    
    - spring.profiles.active:指定运行环境，可以通过环境变量设置mysql的url，username，password
    
        - PALLAS_DB_ADDRESS：数据库地址
        
        - PALLAS_DB_USERNAME：用户名
        
        - PALLAS_DB_PASSWORD：密码
    
    - pallas.db.type：如果不指定，默认为mysql（本机需先安装mysql并导入schmea）；如若指定为h2，默认为内存模式，可在properties文件中修改为文件或其他模式
            
    - 运行：借助Eclipse，IDEA等开发集成环境
    
  - 启动会监听8080端口
  
## 3 Pallas console web

  - 基于[vue](https://cn.vuejs.org/)的前端代码

  - 环境搭建
  
    - 安装Node（node --version查看node是否成功安装，npm --version 查看npm是否成功安装，新版本node已经集合了npm，如未安装npm请翻阅教程重新安装）
    
    - 安装yarn( yarn --version 查看yarn是否成功安装 )
    
    - 前端开发目录如D:\project\pallas_web，运行npm install或yarn install(安装项目自动化工程所需插件)
    
  - 运行
  
    - 键入npm run dev或yarn dev命令运行本地项目（如需打包请键入npm run build 或 yarn build），命令完成后会自动打开浏览器进入(http://localhost:8081)
    
## 4 Pallas search

  - 依赖pallas-core，pallas-common
  
    - 运行前需先构建pallas-core，构建命令：mvn clean package
   
  - 启动
    
    - Eclipse，IDEA或者其他集成环境：
      
      - 以main方式启动com.vip.pallas.search.launch.Startup
      
      - 添加-Dpallas.search.cluster=pppp 所属集群，随便填
      
      - 添加-Dpallas.stdout=true -Dpallas.search.port=9225  监听端口
      
      - 添加-DVIP_PALLAS_CONSOLE_REST_URL=http://localhost:8080/pallas 
      
      - 添加-Dpallas.console.upload_url=http://localhost:8080/pallas/ss/upsert.json ，向console上报search信息
      
  - 启动后登录到Pallas Console，便可在代理管理面板找到你的机器并且是上线状态
  
## 5 pallas rest client

  - 详细使用方式见SDK设计
  
  - 使用前切记配置需要上报的Pallas Console的正确Host或者域名

## 6 pallas plugin

  - 详细使用方式见插件管理中的pallas-plugin开发与使用模块。


  
  
  