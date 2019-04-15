## 概述

为了更好的了解Elasticsearch集群的状态、指标等信息，Pallas Console提供可视化界面展示集群信息，状态，索引分布情况，节点监控指标等各种信息，并提供在线重启集群等一系列功能，方便开发，运维等相关人员更好的了解和管理集群。

## 1 集群总览

点击集群管理，可以看到集群的列表信息，并提供了操作集群的快捷入口。

![](image/clusteroverview_open.png)

## 2 添加集群

  点击添加集群按钮：

  ![](image/addphycluster_open.png)

  - 域名：集群的域名

  - 集群类型：物理集群；逻辑集群

  - HTTP地址：ES集群地址，如有多个，以逗号分隔，如：10.0.0.1:9200,10.0.0.2:9200,10.0.0.3:9200

  - ES client地址 ：ES client地址，如有多个，以逗号分隔，10.0.0.1:9300,10.0.0.2:9300,10.0.0.3:9300

  - 绑定代理集群：pallas-search地址

  如果是添加逻辑集群，则无须填HTTP地址和ES client地址,如图：

![](image/addlogicluster_open.png)

## 3 路由管理

旨在为不同的请求定制不同的路由规则，详细设计见Pallas-search。

- 3.1 默认路由规则: 无
 
- 3.2 创建路由规则:

  同索引中的路由管理
  
- 3.3 节点集

  当前只有普通级别,用户可以编辑指定所要路由到的集群节点


## 4 集群管理

提供一系列集群的指标信息，帮助开发，运维更好的了解集群的状态。

> 为了加强Pallas Console对集群的管理，集成了两个比较重要的ES集群管理工具，`bigdesk`和`cerebro`，并且集成到同一个视图来管理，如下：

![](image/coverview_open.png)

### 4.1 Bigdesk

如图所示，相关人员可以直接操作所有bigdesk的所有操作和查看所有节点的即时状态，更多详细的操作请参考[bigdesk](https://github.com/hlstudio/bigdesk)的官网。

![](image/bigdesk.png)

### 4.2 Cerebro

Pallas Console同时集成了ES集群管理管理工具`Cerebro`供用户操作，用户可以自行进入`索引信息，节点信息，命令行工具`等tab进行操作。更详细的`Cerebro`的操作可以参考[官方文档](https://github.com/lmenezes/elasticsearch-kopf)(Cerebro的前身是kopf)。

![](image/coverview_open.png)

### 4.3 监控

#### 4.3.1 概述

> 提供集群、节点、索引三个级别的监控

> 监控信息默认保存7天

> 提供相对时间（最近30分钟等）和指定查询时间段两种方式查询

#### 4.3.2 设计

![](image/monitor.png)

##### 4.3.2.1 定时采集

- 采集数据来源：/_nodes/stats、/_cluster/stats、/stats、/_cat、/_cluster/health

- 数据清洗：过滤掉不需要的指标

- 数据存储：保存到ES中，索引名：.pallas-es-metrics,mapping保存在pallas-index模块(待开源)下的resources/templates/monitor_index.ftl

- 采集周期：每10s一次

- 定时删除：每天凌晨1点定时删除7天的前的数据

#### 4.3.2.2 查询

- 借助ES的Aggs实现聚合查询

- 借助freemarker，将查询语句模板化，模板保存在pallas-core模块下的resources/templates

- 前端效果图借助[Echarts](https://echarts.baidu.com/)来展示

- 具体实现，参照：com.vip.pallas.service.impl.MonitorServiceImpl.java

#### 4.3.3 效果

![](image/monitor_cluster.png)

![](image/monitor_index_open.jpg)

## 5 重启

可通过pallas-console重启ES集群

集群总览页，操作栏点击对应集群的重启按钮，进入如下页面：

![](image/clusterRestart_open.png)

点击重启按钮，等待完成重启

## 6 编辑

可以修改Cluster Address，Pallas Search Address

## 7 删除

> 注意：当此集群上存在索引时，不能删除 