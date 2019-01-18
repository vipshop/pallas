
CREATE DATABASE IF NOT EXISTS pallas_console DEFAULT CHARSET utf8;
USE pallas_console;

CREATE TABLE IF NOT EXISTS `approve` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `title` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '标题',
  `relate_id` bigint(15) NOT NULL DEFAULT '0' COMMENT '关联ID',
  `approve_type` tinyint(2) NOT NULL DEFAULT '1' COMMENT '1:模板审批，2:其他审批',
  `approve_state` tinyint(2) NOT NULL DEFAULT '0' COMMENT '0:待审核；1:已上线；2:审核不通过；3:未提交',
  `apply_user` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '申请人',
  `apply_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '申请时间',
  `approve_user` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '审批人',
  `approve_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '审批时间',
  `approve_opinion` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '审批意见',
  `note` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '备注',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='审批表';


CREATE TABLE IF NOT EXISTS `authorization` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `client_token` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Client-Token 值',
  `title` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '备注',
  `authorization_items` varchar(20000) COLLATE utf8_bin NOT NULL DEFAULT '[]' COMMENT '权限配置组合',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '启用状态',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除，0:未删除；1:已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_client_token` (`client_token`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='授权管理表';


CREATE TABLE IF NOT EXISTS `cluster` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `cluster_id` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '集群ID',
  `description` text COLLATE utf8_bin COMMENT '集群描述',
  `http_address` varchar(512) COLLATE utf8_bin DEFAULT NULL COMMENT 'HTTP地址',
  `client_address` varchar(512) COLLATE utf8_bin DEFAULT NULL COMMENT 'CLIENT地址',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除',
  `real_clusters` varchar(512) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '当为逻辑集群时，此项不为空，值为集群id集合，逗号分开，如：3,5',
  `accessible_ps` varchar(512) COLLATE utf8_bin DEFAULT '' COMMENT 'ࠉӔ؃ϊ֢ٶܯȺքpallas-searchܯȺ',
  PRIMARY KEY (`id`),
  UNIQUE KEY `cluster_id_unqiue` (`cluster_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;



CREATE TABLE IF NOT EXISTS `cron_delete` (
  `id` bigint(15) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `version_id` bigint(15) NOT NULL COMMENT '对应的版本id',
  `cron` varchar(128) NOT NULL COMMENT 'saturn cron 表达式',
  `scroll_size` int(11) NOT NULL COMMENT 'Scroll size, default to 1000',
  `is_syn` tinyint(1) NOT NULL COMMENT '是否已经同步',
  `dsl` text NOT NULL COMMENT 'delete_by_query表达式',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `data_source` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `index_id` bigint(15) DEFAULT NULL,
  `ip` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT 'IP',
  `port` varchar(64) COLLATE utf8_bin DEFAULT NULL COMMENT '端口',
  `dbname` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT '表名',
  `username` varchar(64) COLLATE utf8_bin DEFAULT NULL COMMENT '用户名',
  `password` varchar(512) COLLATE utf8_bin DEFAULT NULL,
  `table_name` varchar(64) COLLATE utf8_bin DEFAULT NULL COMMENT '表名',
  `description` text COLLATE utf8_bin COMMENT '描述',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_idxid_dbname_tbname` (`dbname`,`table_name`,`index_id`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


CREATE TABLE IF NOT EXISTS `flow_record` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `index_id` bigint(15) NOT NULL DEFAULT '0' COMMENT '索引ID',
  `template_id` bigint(15) NOT NULL DEFAULT '0' COMMENT '模板ID',
  `sample_rate` decimal(15,10) NOT NULL DEFAULT '0.0000000000' COMMENT '抽样率',
  `limit` bigint(15) NOT NULL DEFAULT '0' COMMENT '流量记录条数',
  `start_time` datetime NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '流量记录开始时间',
  `end_time` datetime NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '流量记录结束时间',
  `config_id` bigint(15) NOT NULL DEFAULT '0' COMMENT '配置ID',
  `total` bigint(15) NOT NULL DEFAULT '0' COMMENT '流量记录总条数',
  `state` int(11) NOT NULL DEFAULT '0' COMMENT '1:就绪；2:正在采集；3:已完成；4:已终止',
  `note` varchar(256) COLLATE utf8_bin DEFAULT '' COMMENT '备注',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除，0:未删除；1:已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='流量记录表';


CREATE TABLE IF NOT EXISTS `flow_record_config` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `index_id` bigint(15) NOT NULL DEFAULT '0' COMMENT '索引ID',
  `template_id` bigint(15) DEFAULT '0' COMMENT '模板ID',
  `sample_rate` decimal(15,10) NOT NULL DEFAULT '0.0000000000' COMMENT '抽样率',
  `limit` bigint(15) NOT NULL DEFAULT '0' COMMENT '流量记录条数',
  `start_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '流量记录开始时间',
  `end_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '流量记录结束时间',
  `is_enable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用，0:未启用；1:已启用',
  `note` varchar(256) COLLATE utf8_bin DEFAULT '' COMMENT '备注',
  `create_user` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '提交人',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除，0:未删除；1:已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='流量记录配置表';

CREATE TABLE IF NOT EXISTS `flow_replay` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `record_id` bigint(15) NOT NULL DEFAULT '0' COMMENT '流量记录ID',
  `thread_count` int(11) NOT NULL DEFAULT '0' COMMENT '回放线程数',
  `connection_count` int(11) NOT NULL DEFAULT '0' COMMENT '回放连接数',
  `executor_count` int(11) DEFAULT '0' COMMENT '回放executor数',
  `timeout` int(11) NOT NULL DEFAULT '0' COMMENT '回放超时时间',
  `qps` int(11) NOT NULL DEFAULT '0' COMMENT '回放qps',
  `job_name` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '回放作业名',
  `target_template_id` bigint(15) DEFAULT '0' COMMENT '重放目标模板',
  `used_local_template` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否使用本地模板调试',
  `token` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '回放token',
  `loop` int(11) NOT NULL DEFAULT '-1' COMMENT '回放循环次数，-1为不限制次数forever',
  `duration` int(11) NOT NULL DEFAULT '-1' COMMENT '回放持续时间，单位秒，-1为不限制时间forever',
  `state` int(11) NOT NULL DEFAULT '0' COMMENT '1:就绪；2:正在回放；3:已完成；4:已终止',
  `note` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '备注',
  `create_user` varchar(256) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '提交人',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除，0:未删除；1:已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT='流量回放表';


CREATE TABLE IF NOT EXISTS `index` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '索引ID',
  `index_name` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT '索引名称（唯一）',
  `description` text COLLATE utf8_bin COMMENT '索引描述',
  `cluster_name` varchar(128) COLLATE utf8_bin DEFAULT NULL COMMENT '所属集群',
  `stat` varchar(64) COLLATE utf8_bin DEFAULT NULL COMMENT '索引状态(active:己启用，inactive:未启用)',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  `create_user` varchar(256) COLLATE utf8_bin DEFAULT NULL COMMENT '创建人',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除',
  `timeout` int(11) NOT NULL DEFAULT '0' COMMENT '超时时间，毫秒单位',
  `retry` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数',
  `slower_than` int(11) NOT NULL DEFAULT '0' COMMENT '慢查询的预设阀值，毫秒单位',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_index_cluster` (`index_name`,`cluster_name`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


CREATE TABLE IF NOT EXISTS `index_operation` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `index_id` bigint(15) DEFAULT NULL,
  `event_type` varchar(32) DEFAULT NULL COMMENT '事件类型',
  `event_detail` text COMMENT '事件内容',
  `event_name` varchar(128) DEFAULT NULL COMMENT '事件名称',
  `operator` varchar(128) DEFAULT NULL COMMENT '操作人',
  `operation_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '操作时间',
  `version_id` int(11) DEFAULT NULL COMMENT '版本',
  `end_time` timestamp NULL DEFAULT NULL COMMENT '结束时间',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除',
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `index_routing` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `index_id` bigint(15) NOT NULL DEFAULT '0',
  `index_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '索引逻辑名',
  `type` varchar(45) COLLATE utf8_bin NOT NULL DEFAULT 'index' COMMENT '路由所属类型，index是索引级别路由的节点集，cluster是集群路由级别的节点集',
  `routings_info` varchar(20000) CHARACTER SET utf8 NOT NULL DEFAULT '' COMMENT '路由信息',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除，0:未删除；1:已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_index_routing_index` (`index_id`,`type`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='索引路由表';


CREATE TABLE IF NOT EXISTS `index_routing_security` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `index_id` bigint(15) NOT NULL DEFAULT '0' COMMENT '索引ID',
  `index_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '索引逻辑名',
  `criteria` varchar(10000) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '路由协议应用的限制条件',
  `protocol_controls` varchar(10000) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '路由协议具体配置',
  `state` int(11) NOT NULL DEFAULT '0' COMMENT '状态',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除，0:未删除；1:已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_protocol_control_index_id` (`index_id`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='路由协议管理表';


CREATE TABLE IF NOT EXISTS `index_routing_target_group` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `index_id` bigint(15) NOT NULL DEFAULT '0',
  `index_name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '索引逻辑名',
  `type` varchar(45) COLLATE utf8_bin NOT NULL DEFAULT 'index' COMMENT '节点集所属类型，index是索引级别路由的节点集，cluster是集群路由级别的节点集',
  `name` varchar(255) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'Target Group 名字',
  `nodes_info` varchar(10000) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '选择集群信息',
  `clusters_info` varchar(10000) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '选择集群信息',
  `state` int(11) NOT NULL DEFAULT '0' COMMENT '状态',
  `cluster_level` int(12) NOT NULL DEFAULT '0' COMMENT '集群级别',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除，0:未删除；1:已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_target_group_index_id` (`name`,`index_id`,`type`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin COMMENT='路由目标集表';


CREATE TABLE IF NOT EXISTS `index_version` (
  `id` bigint(15) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `index_id` bigint(15) DEFAULT NULL,
  `version_name` varchar(256) COLLATE utf8_bin DEFAULT NULL COMMENT '版本标识，随机生成（不可重复）',
  `is_used` tinyint(1) DEFAULT NULL COMMENT '是否启用（1：启用，0：未启用）',
  `sync_stat` varchar(30) COLLATE utf8_bin DEFAULT NULL COMMENT '同步状态(full_sync:全量；delta_sync:增量)',
  `num_of_shards` tinyint(3) DEFAULT NULL COMMENT '分片数量',
  `num_of_replication` tinyint(3) DEFAULT NULL COMMENT '复制数量',
  `vdp_queue` tinytext CHARACTER SET utf8 COMMENT 'vdp queue',
  `routing_key` varchar(256) COLLATE utf8_bin DEFAULT NULL COMMENT 'routing key',
  `create_time` timestamp NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `id_field` varchar(256) COLLATE utf8_bin DEFAULT NULL COMMENT 'ID字段',
  `update_time_field` varchar(256) COLLATE utf8_bin DEFAULT NULL COMMENT 'update_time字段',
  `is_sync` tinyint(1) DEFAULT NULL COMMENT '是否启用了同步',
  `is_deleted` tinyint(4) DEFAULT NULL COMMENT '是否己删除',
  `vdp` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否vdp,false为rdp',
  `filter_fields` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否过滤表字段，即只用部分字段',
  `prefer_executor` varchar(256) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '优先executor',
  `check_sum` tinyint(1) NOT NULL DEFAULT '0' COMMENT '遍历db前是否先检查条数',
  `real_cluster_id` bigint(15) NOT NULL DEFAULT '0' COMMENT '所属ES集群ID',
  `allocation_nodes` varchar(1024) COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '该索引版本创建在集群的哪些机器',
  `dynamic` tinyint(1) NOT NULL DEFAULT '0' COMMENT '配置dynamic属性是否为true',
  `index_slow_threshold` bigint(15) NOT NULL DEFAULT '0' COMMENT 'index slowlog threshold',
  `fetch_slow_threshold` bigint(15) NOT NULL DEFAULT '0' COMMENT 'fetch slowlog threshold',
  `query_slow_threshold` bigint(15) NOT NULL DEFAULT '0' COMMENT 'query slowlog threshold',
  `refresh_interval` tinyint(3) NOT NULL DEFAULT '60' COMMENT '索引刷新周期，单位秒',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


CREATE TABLE IF NOT EXISTS `mapping` (
  `id` bigint(16) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `parent_id` bigint(16) DEFAULT NULL COMMENT '父模板ID',
  `version_id` bigint(15) DEFAULT NULL COMMENT '索引版本ID',
  `field_name` varchar(256) COLLATE utf8_bin DEFAULT NULL COMMENT '字段',
  `field_type` varchar(256) COLLATE utf8_bin DEFAULT NULL COMMENT '索引类型',
  `multi` tinyint(1) DEFAULT NULL COMMENT '是否多值',
  `search` tinyint(1) DEFAULT NULL COMMENT '是否建索引',
  `doc_value` tinyint(1) DEFAULT NULL COMMENT '是否生成doc_value',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `dynamic` tinyint(1) NOT NULL DEFAULT '0' COMMENT '配置dynamic属性是否为true',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


CREATE TABLE IF NOT EXISTS `node` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `cluster_name` varchar(255) NOT NULL DEFAULT '' COMMENT '集群名',
  `node_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '节点名',
  `node_ip` varchar(255) NOT NULL DEFAULT '' COMMENT '节点IP',
  `state` tinyint(2) NOT NULL DEFAULT '0' COMMENT '1:即将重启；2:正在重启；3:已启动；4:正常',
  `state_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '状态时间',
  `last_startup_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '最后启动时间',
  `note` varchar(1000) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '备注',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除',
  PRIMARY KEY (`id`),
  KEY `idx_node_name` (`node_name`) USING BTREE,
  KEY `idx_cluster_name` (`cluster_name`) USING BTREE,
  KEY `idx_cluster_node` (`cluster_name`,`node_name`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT='节点信息表';


CREATE TABLE IF NOT EXISTS `permission` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `permission_code` varchar(255) NOT NULL DEFAULT '' COMMENT '权限值',
  `permission_name` varchar(255) NOT NULL DEFAULT '' COMMENT '权限名称 permission_code+operation',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '权限描述',
  `created_by` varchar(255) NOT NULL DEFAULT '' COMMENT '创建用户',
  `last_updated_by` varchar(255) DEFAULT '' COMMENT '最近一次变更用户',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '1980-01-01 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT='权限表';


CREATE TABLE IF NOT EXISTS `plugin_command` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `cluster_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '集群ID',
  `node_ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '节点IP',
  `plugin_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '插件名字',
  `plugin_version` varchar(255) NOT NULL DEFAULT '' COMMENT '需要升级的版本',
  `plugin_type` tinyint(2) NOT NULL DEFAULT '0' COMMENT '插件类型：0:PALLAS, 1:ES',
  `command_exec` varchar(1000) NOT NULL DEFAULT '' COMMENT '下发命令',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `plugin_runtime` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `cluster_id` varchar(255) NOT NULL DEFAULT '' COMMENT '集群ID',
  `node_ip` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '节点IP',
  `node_host` varchar(255) NOT NULL DEFAULT '' COMMENT '节点主机名',
  `plugin_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '插件名字',
  `plugin_version` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '插件版本',
  `available_versions` varchar(1000) NOT NULL DEFAULT '' COMMENT '当前插件存在的版本',
  `plugin_type` tinyint(2) NOT NULL DEFAULT '0' COMMENT '0:script；1:analyzer；2:similarity；3:other',
  `es_version` varchar(50) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT 'ES版本号',
  `note` varchar(1000) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '备注',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT='审批表';


CREATE TABLE IF NOT EXISTS `plugin_upgrade` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `cluster_id` varchar(255) NOT NULL DEFAULT '' COMMENT '集群id',
  `plugin_name` varchar(255) NOT NULL DEFAULT '' COMMENT '插件名称',
  `plugin_version` varchar(255) NOT NULL DEFAULT '' COMMENT '插件升级版本',
  `plugin_type` tinyint(2) NOT NULL DEFAULT '0' COMMENT '插件类型',
  `package_path` varchar(512) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '插件包存储路径（VOS）',
  `state` tinyint(2) NOT NULL DEFAULT '0' COMMENT '0:待审核；1:待升级；2:升级完成；3:审核不通过；4:已取消',
  `note` varchar(1000) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '备注',
  `apply_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '申请人',
  `apply_time` timestamp NULL DEFAULT NULL COMMENT '申请时间',
  `approve_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL DEFAULT '' COMMENT '审批人',
  `approve_time` timestamp NULL DEFAULT NULL COMMENT '审批时间',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) unsigned NOT NULL DEFAULT '0' COMMENT '是否己删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT='审批表';


CREATE TABLE IF NOT EXISTS `role` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_name` varchar(255) NOT NULL DEFAULT '' COMMENT '角色名称',
  `description` varchar(255) NOT NULL DEFAULT '' COMMENT '角色描述',
  `created_by` varchar(255) NOT NULL DEFAULT '' COMMENT '创建用户',
  `last_updated_by` varchar(255) NOT NULL DEFAULT '' COMMENT '最近一次变更用户',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '1980-01-01 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_role_name` (`role_name`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='角色表';


CREATE TABLE IF NOT EXISTS `role_permission` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '角色id',
  `permission_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '权限id',
  `created_by` varchar(255) NOT NULL DEFAULT '' COMMENT '创建用户',
  `last_updated_by` varchar(255) DEFAULT '' COMMENT '最近一次变更用户',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '1980-01-01 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_deleted_role_permission` (`is_deleted`,`role_id`,`permission_id`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT='角色权限关联表';


CREATE TABLE IF NOT EXISTS `search_server` (
  `id` bigint(15) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `ipport` varchar(32) NOT NULL DEFAULT '' COMMENT 'pallas search节点ip与port',
  `cluster` varchar(128) NOT NULL DEFAULT '' COMMENT '所属集群',
  `info` varchar(20000) NOT NULL DEFAULT '' COMMENT '节点上报信息，json格式',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '记录生成时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除，0:未删除；1:已删除',
  `take_traffic` tinyint(1) NOT NULL DEFAULT '1' COMMENT 'ˇرԐ՘·ì0úһԐ՘   1: Ԑ՘',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT='Search服务器表';


CREATE TABLE IF NOT EXISTS `search_template` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `index_id` bigint(15) DEFAULT NULL,
  `template_name` varchar(64) COLLATE utf8_bin DEFAULT NULL COMMENT '模板名',
  `description` varchar(255) COLLATE utf8_bin DEFAULT NULL COMMENT '模板描述 ',
  `content` text CHARACTER SET utf8 COMMENT '模板内容',
  `params` text CHARACTER SET utf8 COMMENT '查询参数',
  `type` tinyint(1) NOT NULL DEFAULT '1' COMMENT '1:模板；0：宏',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否己删除',
  `timeout` int(11) NOT NULL DEFAULT '0' COMMENT '超时时间，毫秒单位',
  `retry` int(11) NOT NULL DEFAULT '0' COMMENT '重试次数',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `unique_indexid_and_templatename` (`index_id`,`template_name`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


CREATE TABLE IF NOT EXISTS `search_template_h` (
  `id` bigint(15) unsigned NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `template_id` bigint(15) NOT NULL DEFAULT '0' COMMENT '模板ID',
  `description` varchar(256) DEFAULT NULL COMMENT '模板描述',
  `creator` varchar(64) DEFAULT NULL COMMENT '创建人',
  `content` text COMMENT '模板内容',
  `params` text COMMENT '模板参数',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 01:01:01' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '是否己删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `template_FK_idx` (`template_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(255) NOT NULL DEFAULT '' COMMENT '用户名',
  `password` varchar(255) NOT NULL DEFAULT '' COMMENT '密码串',
  `real_name` varchar(255) NOT NULL DEFAULT '' COMMENT '真实名称',
  `employee_id` varchar(255) NOT NULL DEFAULT '' COMMENT '员工号',
  `email` varchar(255) NOT NULL DEFAULT '' COMMENT '邮箱',
  `created_by` varchar(255) NOT NULL DEFAULT '' COMMENT '创建用户',
  `last_updated_by` varchar(255) NOT NULL DEFAULT '' COMMENT '最近一次变更用户',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '1980-01-01 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_deleted_username` (`is_deleted`,`username`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT='用户表';


CREATE TABLE IF NOT EXISTS `user_role` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '用户id',
  `role_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '角色id',
  `created_by` varchar(255) NOT NULL DEFAULT '' COMMENT '创建用户',
  `last_updated_by` varchar(255) NOT NULL DEFAULT '' COMMENT '最后更新用户',
  `create_time` timestamp NOT NULL DEFAULT '1980-01-01 00:00:00' COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT '1980-01-01 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  KEY `idx_deleted_user_role` (`is_deleted`,`user_id`,`role_id`) USING BTREE
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COMMENT='用户角色关联表';

