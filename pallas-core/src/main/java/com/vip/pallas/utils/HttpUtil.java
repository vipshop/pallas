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

package com.vip.pallas.utils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.exception.BusinessLevelException;

public class HttpUtil {
	private static Logger logger = LoggerFactory.getLogger(HttpUtil.class);

	@SuppressWarnings("rawtypes")
	private ResponseHandler<Map> urlCallback = (HttpResponse response) -> {
        Map<String, String> ret = null;
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String result = EntityUtils.toString(response.getEntity(), "utf-8");
            ret = new HashMap<String, String>();
            String[] keypairs = result.split("&");
            for (String keypair : keypairs) {
                if (ObjectMapTool.isEmpty(keypair)) {
                    continue;
                }
                String[] kv = keypair.split("=");
                if (kv.length != 2) {
                    continue;
                }
                ret.put(kv[0], kv[1]);
            }
        } else {
            String result = EntityUtils.toString(response.getEntity(), "utf-8");
            logger.info("response error = " + result + ",response code" + response.getStatusLine().getStatusCode());
            throw new BusinessLevelException("when parse response, response=" + response);
        }
        return ret;
    };

	@SuppressWarnings("rawtypes")
	private ResponseHandler<Map> jsonCallback = (HttpResponse response) -> {
        Map<String, Object> ret = null;
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String result = EntityUtils.toString(response.getEntity(), "utf-8");
            try {
                ObjectMapper objectMapper = JsonUtil.getObjectMapper();
                ret = objectMapper.readValue(result, Map.class);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                ret = new HashMap<>();
                ret.put("error", result);
            }
        } else {
            String result = EntityUtils.toString(response.getEntity(), "utf-8");
            logger.info("response error = " + result + ",response code" + response.getStatusLine().getStatusCode());
            throw new BusinessLevelException("when parse response, response=" + response);
        }
        return ret;
    };

	public Map<String, Object> httpGet(String url, Map<String, Object> params, boolean json) throws Exception {
		Set<String> keySet = params.keySet();

		String getUrl = url;
		if (url.lastIndexOf('?') <= -1) {
			getUrl += "?";
		}

		boolean flag = false;
		for (String key : keySet) {
			if (flag) {
				getUrl += "&";
			}
			getUrl += key + "=";
			getUrl += ObjectMapTool.getString(params, key);
			flag = true;
		}
		HttpGet request = new HttpGet(new URI(getUrl));
		HttpClient httpClient = HttpClientUtil.getHttpClient();
		return httpClient.execute(request, json ? jsonCallback : urlCallback);
	}
}