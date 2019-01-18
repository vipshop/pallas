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

package com.vip.pallas.plugin.upgrade;


import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.logging.ESLoggerFactory;

import com.vip.pallas.bean.PluginCommands;
import com.vip.pallas.bean.PluginStates;
import com.vip.pallas.utils.HttpClient;
import com.vip.pallas.utils.JsonUtil;
import com.vip.pallas.utils.PallasConsoleProperties;

public class PallasAPIStub {

	private static final org.apache.logging.log4j.Logger LOGGER = ESLoggerFactory.getLogger(PallasAPIStub.class);

    private static ThreadLocal<String> lastKeepaliveRequest = new ThreadLocal<>();
    private static ThreadLocal<String> lastKeepaliveResponse = new ThreadLocal<>();

	public static PluginCommands keepalive(PluginStates pluginStates) throws Exception {
		Map<String, Object> inputMap = new HashMap<>();
		String request = JsonUtil.toJson(pluginStates);

		inputMap.put("message", request);

		if(request != null && !request.equals(lastKeepaliveRequest.get())){
			LOGGER.info("keepalive request: " + request);
			lastKeepaliveRequest.set(request);
		}

		String commands = HttpClient.httpPost(PallasConsoleProperties.PALLAS_CONSOLE_REST_URL + "/rest/plugin/keepalive.json", JsonUtil.toJson(inputMap));

		Map<String, Object> resultMap = JsonUtil.readValue(commands, Map.class);

		String response = JsonUtil.toJson(resultMap);

		if(response != null && !response.equals(lastKeepaliveResponse.get())){
			LOGGER.info("keepalive response: " + response);
			lastKeepaliveResponse.set(response);
		}

		if((int)resultMap.get("status") == 0){
			Object data = resultMap.get("data");
			if(data != null){
				Object resp = ((Map) data).get("response");
				if(resp != null){
					return JsonUtil.readValue(JsonUtil.toJson(resp), PluginCommands.class);
				}
			}
		}

		return null;
	}
}