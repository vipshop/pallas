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

package com.vip.pallas.client.thread;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.vip.pallas.client.PallasRestClientBuilder;
import com.vip.pallas.client.env.LoadEnv;
import com.vip.pallas.client.util.HttpClient;
import com.vip.pallas.utils.IPUtils;

public class QueryConsoleTask implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(QueryConsoleTask.class);

	private static final String PARAMS = "{\"token\":\"%s\", \"ip\":\"" + IPUtils.localIp4Str() + "\"}";

	public static volatile Map<String, String> esDomainMap = new ConcurrentHashMap<>();

	public static volatile Map<String, List<String>> psListMap = new ConcurrentHashMap<>();

	private HashSet<String> tokenSet;

	public QueryConsoleTask(HashSet<String> tokenSet) {
		this.tokenSet = tokenSet;
	}

	@Override
	public void run() {
		Iterator<String> iterator = tokenSet.iterator();
		while (iterator.hasNext()) {
			try {
				String token = iterator.next();
				JSONObject jsonObject = JSON.parseObject(
						HttpClient.httpPost(LoadEnv.consoleQueryUrl,
								String.format(PARAMS, token)));
				if (jsonObject != null) {
					JSONObject data = jsonObject.getJSONObject("data");
					if (data != null) {
						String domain = data.getString("domain");
						if (domain != null && !domain.equals(esDomainMap.get(token))) { //NOSONAR
							log.warn("esDomain changed from {} to {}", esDomainMap.get(token), domain);
							esDomainMap.put(token, domain);
						}
						JSONArray ipArray = data.getJSONArray("psList");
						List<String> newPsList = ipArray.toJavaList(String.class);
						List<String> oldPsList = psListMap.get(token);

						if ((newPsList != null && oldPsList == null) || (newPsList != null && oldPsList != null
								&& (!newPsList.containsAll(oldPsList) || (!oldPsList.containsAll(newPsList))))) { //NOSONAR
							log.warn("psList changed from {} to {}", oldPsList, newPsList);
							psListMap.put(token, newPsList);
							PallasRestClientBuilder.rebuildInternalRestClient(token);
						}
					}
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public static String getEsDomainByToken(String token) {
		return esDomainMap.get(token);
	}

	public static List<String> getPsListByToken(String token) {
		return psListMap.get(token);
	}
}