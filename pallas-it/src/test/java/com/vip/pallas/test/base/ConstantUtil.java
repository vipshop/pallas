/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.vip.pallas.test.base;

public interface ConstantUtil {
    //H2
    String h2_url = "jdbc:h2:mem:pallas_console";
    String h2_username = "root";
    String h2_password = "123456";

    //init db data in order
    String insert_cluster = "REPLACE INTO `cluster` VALUES (1,'pallas-test-cluster','开源测试演示','127.0.0.1:9200','127.0.0.1:9300',NULL,now(),0,'','SHARED-CLUSTER');";

    String insert_index = "REPLACE INTO `index` (`id`, `index_name`, `description`, `cluster_name`, `stat`, `create_time`, `update_time`, `is_deleted`, `timeout`, `retry`, `slower_than`) VALUES ('1', 'product_comment', '商品评价测试索引', 'pallas-test-cluster', 'inactive', now(), now(), '0', '0', '0', '200');";

    String insert_index_routing = "REPLACE INTO `index_routing` (`id`, `index_id`, `index_name`, `routings_info`, `create_time`, `update_time`, `is_deleted`) values ('1', '1', 'product_comment', '[{\"name\":\"matchall\",\"conditionRelation\":\"AND\",\"conditions\":[{\"paramType\":\"header\",\"paramName\":\"business_code\",\"paramValue\":\"ittest\",\"exprOp\":\"=\"}],\"targetGroups\":[{\"id\":1,\"weight\":1}],\"enable\":true}]\n" +
            "', now(), now(), '0');";

    String insert_authorization = "REPLACE INTO `authorization` (`id`, `client_token`, `title`, `authorization_items`, `is_enabled`, `create_time`, `update_time`, `is_deleted`) VALUES ('1', 'XQx0dVPGB1dlPn3ZTDjaXw==', 'product_comment测试token', '[{\"id\":1,\"name\":\"pallas-test-cluster\",\"privileges\":{},\"indexPrivileges\":[{\"id\":1,\"name\":\"product_comment\",\"privileges\":{\"IndexAll\":[\"ReadOnly\"]},\"indexPrivileges\":null}]}]', '1', now(), now(), '0');";

    String insert_index_routing_target_group = "REPLACE INTO `index_routing_target_group` (`id`, `index_id`, `index_name`, `type`, `name`, `nodes_info`, `clusters_info`, `state`, `cluster_level`, `create_time`, `update_time`, `is_deleted`) VALUES ('1', '1', 'product_comment', 'index', 'Default', '[]', '[{\"cluster\":\"pallas-test-cluster\",\"name\":\"pallas-test-cluster\",\"address\":\"127.0.0.1:9200\"}]', '0', '2', now(), now(), '0');";

    String insert_search_template = "REPLACE INTO `search_template` (`id`, `index_id`, `template_name`, `content`, `params`, `type`, `create_time`, `update_time`, `is_deleted`, `timeout`, `retry`) VALUES ('1', '1', 'product_comment_search', '{\n" +
            "     \"from\": {{from}}{{^from}}0{{/from}},\n" +
            "     \"size\": {{size}}{{^size}}30{{/size}},\n" +
            "     \"sort\":[{\"post_time\": \"desc\"}],\n" +
            "     \"query\": {\n" +
            "         \"bool\": {\n" +
            "             \"filter\" : [\n" +
            "                 {\"match_all\": {}}\n" +
            "                 {{#audit_start_time}}\n" +
            "                   ,{\"range\":{\"approval_time\" : { \"gte\" : \"{{audit_start_time}}\" }}}\n" +
            "                 {{/audit_start_time}}\n" +
            "                 {{#audit_end_time}}\n" +
            "                   ,{\"range\":{\"approval_time\" : { \"lte\" : \"{{audit_end_time}}\" }}}\n" +
            "                 {{/audit_end_time}}\n" +
            "                 {{#comment_start_time}}\n" +
            "                   ,{\"range\":{\"post_time\" : { \"gte\" : \"{{comment_start_time}}\" }}}\n" +
            "                 {{/comment_start_time}}\n" +
            "                 {{#comment_end_time}}\n" +
            "                   ,{\"range\":{\"post_time\" : { \"lte\" : \"{{comment_end_time}}\" }}}\n" +
            "                 {{/comment_end_time}}\n" +
            "                 {{#is_satisfied}}\n" +
            "                     ,{\"term\": {  \"is_satisfied\": {{is_satisfied}}  }}\n" +
            "                 {{/is_satisfied}}\n" +
            "                 {{#spu_ids}}\n" +
            "                     ,{\"terms\": {  \"spu_id\": {{spu_ids}}  }}\n" +
            "                 {{/spu_ids}}\n" +
            "                 {{#sku_id}}\n" +
            "                     ,{\"term\": {  \"sku_id\": {{sku_id}}  }}\n" +
            "                 {{/sku_id}}\n" +
            "                 {{#id}}\n" +
            "                     ,{\"term\": {  \"id\": {{id}}  }}\n" +
            "                 {{/id}}\n" +
            "                 {{#vendor_id}}\n" +
            "                     ,{\"term\": {  \"vendor_id\": {{vendor_id}}  }}\n" +
            "                 {{/vendor_id}}\n" +
            "                 {{#approval_user}}\n" +
            "                     ,{\"term\": {  \"approval_user\": \"{{approval_user}}\"  }}\n" +
            "                 {{/approval_user}}\n" +
            "                 {{#cat3_ids}}\n" +
            "                     ,{\"terms\": {  \"cat3_id\": {{cat3_ids}}  }}\n" +
            "                 {{/cat3_ids}}\n" +
            "                 {{#status}}\n" +
            "                     ,{\"terms\": {  \"status\": {{status}}  }}\n" +
            "                 {{/status}}\n" +
            "                 {{#is_nlpmark}}\n" +
            "                     ,{\"term\": {  \"is_nlpmark\": {{is_nlpmark}}  }}\n" +
            "                 {{/is_nlpmark}}\n" +
            "                 {{#nlp_class}}\n" +
            "                     ,{\"term\": {  \"nlp_class\": \"{{nlp_class}}\"  }}\n" +
            "                 {{/nlp_class}}\n" +
            "                 {{#rep_source}}\n" +
            "                     ,{\"term\": {  \"rep_source\": {{rep_source}}  }}\n" +
            "                 {{/rep_source}}\n" +
            "                 {{#third_rep_source}}\n" +
            "                     ,{\"term\": {  \"third_rep_source\": {{third_rep_source}}  }}\n" +
            "                 {{/third_rep_source}}\n" +
            "                 {{#is_top}}\n" +
            "                     ,{\"term\": {  \"is_top\": {{is_top}}  }}\n" +
            "                 {{/is_top}}\n" +
            "                 {{#is_essence}}\n" +
            "                     ,{\"term\": {  \"is_essence\": {{is_essence}}  }}\n" +
            "                 {{/is_essence}}\n" +
            "                 {{#no_show_video}}\n" +
            "                     ,{\"term\": {  \"video_url\": \"\" }}\n" +
            "                 {{/no_show_video}}\n" +
            "                 {{#had_show_pic}}\n" +
            "                     ,{\"range\": {  \"image_count\": {\"gt\":0}  }}\n" +
            "                 {{/had_show_pic}}\n" +
            "                 {{#no_show_pic}}\n" +
            "                     ,{\"range\": {  \"image_count\": {\"lte\":0}  }}\n" +
            "                 {{/no_show_pic}}\n" +
            "                 {{#content}}\n" +
            "                     ,{ \"match_phrase\": { \"content\": \"{{content}}\" }}\n" +
            "                 {{/content}}\n" +
            "             ],\n" +
            "             \"must_not\":[\n" +
            "                 {{#had_show_video}}\n" +
            "                     {\"term\": {  \"video_url\": \"\" }}\n" +
            "                 {{/had_show_video}}\n" +
            "             ]\n" +
            "         }\n" +
            "     }\n" +
            " }', '{\n" +
            "        \"vendor_id\": 601000,\n" +
            "        \"contenet\": \"鞋\"\n" +
            "    }', '1', now(), now(), '0', '0', '0');";

    String insert_search_template_h = " REPLACE INTO `search_template_h` (`id`, `template_id`, `description`, `creator`, `content`, `params`, `create_time`, `is_deleted`) VALUES ('1', '1', 'product_comment_search', 'admin', '{\n" +
            "     \"from\": {{from}}{{^from}}0{{/from}},\n" +
            "     \"size\": {{size}}{{^size}}30{{/size}},\n" +
            "     \"sort\":[{\"post_time\": \"desc\"}],\n" +
            "     \"query\": {\n" +
            "         \"bool\": {\n" +
            "             \"filter\" : [\n" +
            "                 {\"match_all\": {}}\n" +
            "                 {{#audit_start_time}}\n" +
            "                   ,{\"range\":{\"approval_time\" : { \"gte\" : \"{{audit_start_time}}\" }}}\n" +
            "                 {{/audit_start_time}}\n" +
            "                 {{#audit_end_time}}\n" +
            "                   ,{\"range\":{\"approval_time\" : { \"lte\" : \"{{audit_end_time}}\" }}}\n" +
            "                 {{/audit_end_time}}\n" +
            "                 {{#comment_start_time}}\n" +
            "                   ,{\"range\":{\"post_time\" : { \"gte\" : \"{{comment_start_time}}\" }}}\n" +
            "                 {{/comment_start_time}}\n" +
            "                 {{#comment_end_time}}\n" +
            "                   ,{\"range\":{\"post_time\" : { \"lte\" : \"{{comment_end_time}}\" }}}\n" +
            "                 {{/comment_end_time}}\n" +
            "                 {{#is_satisfied}}\n" +
            "                     ,{\"term\": {  \"is_satisfied\": {{is_satisfied}}  }}\n" +
            "                 {{/is_satisfied}}\n" +
            "                 {{#spu_ids}}\n" +
            "                     ,{\"terms\": {  \"spu_id\": {{spu_ids}}  }}\n" +
            "                 {{/spu_ids}}\n" +
            "                 {{#sku_id}}\n" +
            "                     ,{\"term\": {  \"sku_id\": {{sku_id}}  }}\n" +
            "                 {{/sku_id}}\n" +
            "                 {{#id}}\n" +
            "                     ,{\"term\": {  \"id\": {{id}}  }}\n" +
            "                 {{/id}}\n" +
            "                 {{#vendor_id}}\n" +
            "                     ,{\"term\": {  \"vendor_id\": {{vendor_id}}  }}\n" +
            "                 {{/vendor_id}}\n" +
            "                 {{#approval_user}}\n" +
            "                     ,{\"term\": {  \"approval_user\": \"{{approval_user}}\"  }}\n" +
            "                 {{/approval_user}}\n" +
            "                 {{#cat3_ids}}\n" +
            "                     ,{\"terms\": {  \"cat3_id\": {{cat3_ids}}  }}\n" +
            "                 {{/cat3_ids}}\n" +
            "                 {{#status}}\n" +
            "                     ,{\"terms\": {  \"status\": {{status}}  }}\n" +
            "                 {{/status}}\n" +
            "                 {{#is_nlpmark}}\n" +
            "                     ,{\"term\": {  \"is_nlpmark\": {{is_nlpmark}}  }}\n" +
            "                 {{/is_nlpmark}}\n" +
            "                 {{#nlp_class}}\n" +
            "                     ,{\"term\": {  \"nlp_class\": \"{{nlp_class}}\"  }}\n" +
            "                 {{/nlp_class}}\n" +
            "                 {{#rep_source}}\n" +
            "                     ,{\"term\": {  \"rep_source\": {{rep_source}}  }}\n" +
            "                 {{/rep_source}}\n" +
            "                 {{#third_rep_source}}\n" +
            "                     ,{\"term\": {  \"third_rep_source\": {{third_rep_source}}  }}\n" +
            "                 {{/third_rep_source}}\n" +
            "                 {{#is_top}}\n" +
            "                     ,{\"term\": {  \"is_top\": {{is_top}}  }}\n" +
            "                 {{/is_top}}\n" +
            "                 {{#is_essence}}\n" +
            "                     ,{\"term\": {  \"is_essence\": {{is_essence}}  }}\n" +
            "                 {{/is_essence}}\n" +
            "                 {{#no_show_video}}\n" +
            "                     ,{\"term\": {  \"video_url\": \"\" }}\n" +
            "                 {{/no_show_video}}\n" +
            "                 {{#had_show_pic}}\n" +
            "                     ,{\"range\": {  \"image_count\": {\"gt\":0}  }}\n" +
            "                 {{/had_show_pic}}\n" +
            "                 {{#no_show_pic}}\n" +
            "                     ,{\"range\": {  \"image_count\": {\"lte\":0}  }}\n" +
            "                 {{/no_show_pic}}\n" +
            "                 {{#content}}\n" +
            "                     ,{ \"match_phrase\": { \"content\": \"{{content}}\" }}\n" +
            "                 {{/content}}\n" +
            "             ],\n" +
            "             \"must_not\":[\n" +
            "                 {{#had_show_video}}\n" +
            "                     {\"term\": {  \"video_url\": \"\" }}\n" +
            "                 {{/had_show_video}}\n" +
            "             ]\n" +
            "         }\n" +
            "     }\n" +
            " }', '{\n" +
            "        \"vendor_id\": 601000,\n" +
            "        \"contenet\": \"鞋\"\n" +
            "    }',now(), '0');";
}
