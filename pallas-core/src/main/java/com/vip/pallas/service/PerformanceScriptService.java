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

package com.vip.pallas.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.google.common.net.HostAndPort;
import com.vip.pallas.bean.PerformanceData;
import com.vip.pallas.bean.QueryParamSetting;
import com.vip.pallas.mybatis.entity.SearchTemplate;

/**
 * 性能测试脚本生成服务
 * 
 * @author timmy.hu
 */
public interface PerformanceScriptService {

	/**
	 * 根据请求查询模板，获取查询请求体中的参数名称列表。 一些特殊的非查询过滤字段的不返回，包括orderby等
	 * 
	 * 例如对于以下的请求体
	 * 
	 * <pre>
	 *   {
	 *	    "from": "10",
	 *	    "size": "100",
	 *	    "keyword": "${keyword}",
	 *	    "brand_warehouse_multi": "${brand_warehouse_multi}",
	 *	    "channel_id": {
	 *	      "list": "${channel_id}"
	 *	    },
	 *	    "orderby": {
	 *	      "sku_price": true,
	 *	      "asc": true
	 *	    }
	 *	  }
	 * </pre>
	 * 
	 * 返回的请求参数名称列表为["from","size","keyword","brand_warehouse_multi","channel_id"]
	 * 
	 * @param template
	 * @return
	 */
	List<Map<String, String>> getQueryParamNames(SearchTemplate template);

	/**
	 * 生成请求参数包体
	 * 
	 * 返回的map例子格式如下：
	 * 
	 * <pre>
	 * {
	 *	    "id":"msearch_merchandiseList",
	 *	    "params":{
	 *	        "from":"10",
	 *	        "size":"100",
	 *	        "keyword":"${keyword}",
	 *	        "channel_id":{
	 *	            "list":"${channel_id}"
	 *	        },
	 *	        "fields":{
	 *	            "list":"["text"]"
	 *	        }
	 *	    }
	 *	}
	 * </pre>
	 * 
	 * 
	 * 
	 * @param template
	 * @param paramSettings
	 * @return
	 */
	Map<String, Object> genSendQueryBody(SearchTemplate template, List<QueryParamSetting> paramSettings);

	/**
	 * 生成可进行性能测试的JMX脚本，该JMX脚本为完整的可以运行性能测试的脚本
	 * 
	 * 其中包含了JMX的测试计划定义、线程组定义、HTTP请求以及请求包体等
	 * 
	 * @param template
	 * @param paramSettings
	 * @return
	 */
	String genJmxScript(SearchTemplate template, List<PerformanceData> pds,
			List<List<QueryParamSetting>> paramSettingsList) throws Exception;

	/**
	 * 将测试脚本和所有的数据源文件进行压缩打包。其中添加jmx文件，文件名为testname变量+".jmx"，文件内容为jmxScript，数据源文件通过pds获取。
	 * 将zip压缩结果流写入到os输出流中
	 * 
	 * @param testname
	 * @param jmxScript
	 * @param pds
	 * @param os
	 * @throws IOException
	 */
	void zipFiles(String testname, String jmxScript, List<HostAndPort> hps,List<PerformanceData> pds, OutputStream os) throws Exception;

}