REPLACE INTO `user` VALUES (1,'admin','admin','admin','','','System','Unkown Real User','1979-12-31 16:00:00','2018-11-15 05:19:58',0),(2,'guest','guest','guest','','','System','System','1979-12-31 16:00:00','2018-11-13 08:13:25',0);

REPLACE INTO `user_role` VALUES (1,1,1,'System','System','1979-12-31 16:00:00','2018-11-15 05:20:13',0),(2,2,2,'System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0);

REPLACE INTO `role` VALUES (1,'Admin','Admin','System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0),(2,'Guest','Guest','System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0);

REPLACE INTO `role_permission` VALUES (1,1,1,'System','System','1979-12-31 16:00:00','2018-11-14 17:18:53',0),(2,1,2,'System','System','1979-12-31 16:00:00','2018-11-14 17:18:55',0),(3,1,3,'System','System','1979-12-31 16:00:00','2018-11-14 17:18:57',0),(4,1,4,'System','System','1979-12-31 16:00:00','2018-11-14 17:19:08',0),(5,1,5,'System','System','1979-12-31 16:00:00','2018-11-14 17:19:03',0),(6,1,6,'System','System','1979-12-31 16:00:00','2018-11-14 17:19:02',0),(7,1,7,'System','System','1979-12-31 16:00:00','2018-11-14 17:19:01',0),(8,1,8,'System','System','1979-12-31 16:00:00','2018-11-19 03:58:49',0);

REPLACE INTO `permission` VALUES (1,'cluster.all','cluster.all.write','','System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0),(2,'index.all','index.all.write','','System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0),(3,'version.all','version.all.write','','System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0),(4,'template.all','template.all.write','','System','System','1979-12-31 16:00:00','1979-12-31 16:00:00',0),(5,'plugin.all','plugin.all.write','','System','System','1979-12-31 16:00:00','2018-11-14 17:17:14',0),(6,'pallas-search.all','pallas-search.all.write','','System','System','1979-12-31 16:00:00','2018-11-14 17:17:19',0),(7,'authorization.all','authorization.all.write','','System','System','1979-12-31 16:00:00','2018-11-14 17:17:25',0),(8,'user.all','user.all.write','','System','System','1979-12-31 16:00:00','2018-11-19 03:58:10',0);

CREATE ALIAS IF NOT EXISTS FIND_IN_SET FOR "com.vip.pallas.utils.H2DBFunctions.getClusterIds";

REPLACE INTO `cluster` VALUES (1,'pallas-test-cluster','开源测试演示','127.0.0.1:9200','127.0.0.1:9300',NULL,now(),0,'','SHARED-CLUSTER', '',null ,null);

REPLACE INTO `index` (`id`, `index_name`, `description`, `cluster_name`, `stat`, `create_time`, `update_time`, `is_deleted`, `timeout`, `retry`, `slower_than`) VALUES ('1', 'product_comment', '商品评价测试索引', 'pallas-test-cluster', 'inactive', now(), now(), '0', '0', '0', '200');

REPLACE INTO `index_version` (`id`,`index_id`,`version_name`,`is_used`,`sync_stat`,`num_of_shards`,`num_of_replication`,`vdp_queue`,`routing_key`,
`create_time`,`update_time`,`id_field`,`update_time_field`,`is_sync`,`is_deleted`,`vdp`,`filter_fields`,`prefer_executor`,
`check_sum`,`real_cluster_id`,`allocation_nodes`,`dynamic`,`index_slow_threshold`,`fetch_slow_threshold`,`query_slow_threshold`,
`refresh_interval`) VALUES(1,1,'versiontest',0,'full_ready',1,1,'q','id',now(),now(),'id','update_time',0,0,1,0,'',0,1,'',0,0,0,0,60);



REPLACE INTO `index_routing` (`index_id`, `index_name`, `routings_info`, `create_time`, `update_time`, `is_deleted`) values ('1', 'product_comment', '[{"name":"matchall","conditionRelation":"AND","conditions":[{"paramType":"header","paramName":"business_code","paramValue":"ittest","exprOp":"="}],"targetGroups":[{"id":1,"weight":1}],"enable":true}]
', now(), now(), '0');


REPLACE INTO `authorization` (`id`, `client_token`, `title`, `authorization_items`, `is_enabled`, `create_time`, `update_time`, `is_deleted`) VALUES ('1', 'XQx0dVPGB1dlPn3ZTDjaXw==', 'product_comment测试token', '[{"id":1,"name":"pallas-test-cluster","privileges":{},"indexPrivileges":[{"id":1,"name":"product_comment","privileges":{"IndexAll":["ReadOnly"]},"indexPrivileges":null}]}]', '1', now(), now(), '0');


REPLACE INTO `index_routing_target_group` (`id`, `index_id`, `index_name`, `type`, `name`, `nodes_info`, `clusters_info`, `state`, `cluster_level`, `create_time`, `update_time`, `is_deleted`) VALUES ('1', '1', 'product_comment', 'index', 'Default', '[]', '[{"cluster":"pallas-test-cluster","name":"pallas-test-cluster","address":"127.0.0.1:9200"}]', '0', '2', now(), now(), '0');


REPLACE INTO `search_template` (`id`, `index_id`, `template_name`, `content`, `params`, `type`, `create_time`, `update_time`, `is_deleted`, `timeout`, `retry`) VALUES ('1', '1', 'product_comment_search', '{
     "from": {{from}}{{^from}}0{{/from}},
     "size": {{size}}{{^size}}30{{/size}},
     "sort":[{"post_time": "desc"}],
     "query": {
         "bool": {
             "filter" : [
                 {"match_all": {}}
                 {{#audit_start_time}}
                   ,{"range":{"approval_time" : { "gte" : "{{audit_start_time}}" }}}
                 {{/audit_start_time}}
                 {{#audit_end_time}}
                   ,{"range":{"approval_time" : { "lte" : "{{audit_end_time}}" }}}
                 {{/audit_end_time}}
                 {{#comment_start_time}}
                   ,{"range":{"post_time" : { "gte" : "{{comment_start_time}}" }}}
                 {{/comment_start_time}}
                 {{#comment_end_time}}
                   ,{"range":{"post_time" : { "lte" : "{{comment_end_time}}" }}}
                 {{/comment_end_time}}
                 {{#is_satisfied}}
                     ,{"term": {  "is_satisfied": {{is_satisfied}}  }}
                 {{/is_satisfied}}
                 {{#spu_ids}}
                     ,{"terms": {  "spu_id": {{spu_ids}}  }}
                 {{/spu_ids}}
                 {{#sku_id}}
                     ,{"term": {  "sku_id": {{sku_id}}  }}
                 {{/sku_id}}
                 {{#id}}
                     ,{"term": {  "id": {{id}}  }}
                 {{/id}}
                 {{#vendor_id}}
                     ,{"term": {  "vendor_id": {{vendor_id}}  }}
                 {{/vendor_id}}
                 {{#approval_user}}
                     ,{"term": {  "approval_user": "{{approval_user}}"  }}
                 {{/approval_user}}
                 {{#cat3_ids}}
                     ,{"terms": {  "cat3_id": {{cat3_ids}}  }}
                 {{/cat3_ids}}
                 {{#status}}
                     ,{"terms": {  "status": {{status}}  }}
                 {{/status}}
                 {{#is_nlpmark}}
                     ,{"term": {  "is_nlpmark": {{is_nlpmark}}  }}
                 {{/is_nlpmark}}
                 {{#nlp_class}}
                     ,{"term": {  "nlp_class": "{{nlp_class}}"  }}
                 {{/nlp_class}}
                 {{#rep_source}}
                     ,{"term": {  "rep_source": {{rep_source}}  }}
                 {{/rep_source}}
                 {{#third_rep_source}}
                     ,{"term": {  "third_rep_source": {{third_rep_source}}  }}
                 {{/third_rep_source}}
                 {{#is_top}}
                     ,{"term": {  "is_top": {{is_top}}  }}
                 {{/is_top}}
                 {{#is_essence}}
                     ,{"term": {  "is_essence": {{is_essence}}  }}
                 {{/is_essence}}
                 {{#no_show_video}}
                     ,{"term": {  "video_url": "" }}
                 {{/no_show_video}}
                 {{#had_show_pic}}
                     ,{"range": {  "image_count": {"gt":0}  }}
                 {{/had_show_pic}}
                 {{#no_show_pic}}
                     ,{"range": {  "image_count": {"lte":0}  }}
                 {{/no_show_pic}}
                 {{#content}}
                     ,{ "match_phrase": { "content": "{{content}}" }}
                 {{/content}}
             ],
             "must_not":[
                 {{#had_show_video}}
                     {"term": {  "video_url": "" }}
                 {{/had_show_video}}
             ]
         }
     }
 }', '{
        "vendor_id": 601000,
        "contenet": "鞋"
    }', '1', now(), now(), '0', '0', '0');


 REPLACE INTO `search_template_h` (`id`, `template_id`, `description`, `creator`, `content`, `params`, `create_time`, `is_deleted`) VALUES ('1', '1', 'product_comment_search', 'admin', '{
     "from": {{from}}{{^from}}0{{/from}},
     "size": {{size}}{{^size}}30{{/size}},
     "sort":[{"post_time": "desc"}],
     "query": {
         "bool": {
             "filter" : [
                 {"match_all": {}}
                 {{#audit_start_time}}
                   ,{"range":{"approval_time" : { "gte" : "{{audit_start_time}}" }}}
                 {{/audit_start_time}}
                 {{#audit_end_time}}
                   ,{"range":{"approval_time" : { "lte" : "{{audit_end_time}}" }}}
                 {{/audit_end_time}}
                 {{#comment_start_time}}
                   ,{"range":{"post_time" : { "gte" : "{{comment_start_time}}" }}}
                 {{/comment_start_time}}
                 {{#comment_end_time}}
                   ,{"range":{"post_time" : { "lte" : "{{comment_end_time}}" }}}
                 {{/comment_end_time}}
                 {{#is_satisfied}}
                     ,{"term": {  "is_satisfied": {{is_satisfied}}  }}
                 {{/is_satisfied}}
                 {{#spu_ids}}
                     ,{"terms": {  "spu_id": {{spu_ids}}  }}
                 {{/spu_ids}}
                 {{#sku_id}}
                     ,{"term": {  "sku_id": {{sku_id}}  }}
                 {{/sku_id}}
                 {{#id}}
                     ,{"term": {  "id": {{id}}  }}
                 {{/id}}
                 {{#vendor_id}}
                     ,{"term": {  "vendor_id": {{vendor_id}}  }}
                 {{/vendor_id}}
                 {{#approval_user}}
                     ,{"term": {  "approval_user": "{{approval_user}}"  }}
                 {{/approval_user}}
                 {{#cat3_ids}}
                     ,{"terms": {  "cat3_id": {{cat3_ids}}  }}
                 {{/cat3_ids}}
                 {{#status}}
                     ,{"terms": {  "status": {{status}}  }}
                 {{/status}}
                 {{#is_nlpmark}}
                     ,{"term": {  "is_nlpmark": {{is_nlpmark}}  }}
                 {{/is_nlpmark}}
                 {{#nlp_class}}
                     ,{"term": {  "nlp_class": "{{nlp_class}}"  }}
                 {{/nlp_class}}
                 {{#rep_source}}
                     ,{"term": {  "rep_source": {{rep_source}}  }}
                 {{/rep_source}}
                 {{#third_rep_source}}
                     ,{"term": {  "third_rep_source": {{third_rep_source}}  }}
                 {{/third_rep_source}}
                 {{#is_top}}
                     ,{"term": {  "is_top": {{is_top}}  }}
                 {{/is_top}}
                 {{#is_essence}}
                     ,{"term": {  "is_essence": {{is_essence}}  }}
                 {{/is_essence}}
                 {{#no_show_video}}
                     ,{"term": {  "video_url": "" }}
                 {{/no_show_video}}
                 {{#had_show_pic}}
                     ,{"range": {  "image_count": {"gt":0}  }}
                 {{/had_show_pic}}
                 {{#no_show_pic}}
                     ,{"range": {  "image_count": {"lte":0}  }}
                 {{/no_show_pic}}
                 {{#content}}
                     ,{ "match_phrase": { "content": "{{content}}" }}
                 {{/content}}
             ],
             "must_not":[
                 {{#had_show_video}}
                     {"term": {  "video_url": "" }}
                 {{/had_show_video}}
             ]
         }
     }
 }', '{
        "vendor_id": 601000,
        "contenet": "鞋"
    }',now(), '0');