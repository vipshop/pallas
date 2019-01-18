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

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PallasTransportClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(PallasTransportClient.class);

	protected final static Map<String, TransportClient> TRANSPORT_CLIENT_MAP = new HashMap<>();
	private final static Object LOCK_OBJ = new Object();

	protected String clusterName;
	protected String clusterAddress;

	protected TransportClient transportClient;

	public PallasTransportClient(String clusterName, String clusterAddress){
		this.clusterName = clusterName;
		this.clusterAddress = clusterAddress;
		initClient();
	}

	private void initClient(){
		transportClient = TRANSPORT_CLIENT_MAP.get(clusterName);
		if(transportClient == null){
			synchronized (LOCK_OBJ) {
				if(!TRANSPORT_CLIENT_MAP.containsKey(clusterName)){
					Settings settings = Settings.builder()
							.put("cluster.name", clusterName)
							.put("client.transport.sniff", false).build();
					transportClient = new PreBuiltTransportClient(settings);
					String[] addressArray = clusterAddress.split(",");
					for (String address : addressArray) {
						try {
							String[] ipAndPortArray = address.split(":");
							transportClient.addTransportAddress(new InetSocketTransportAddress(
									InetAddress.getByName(ipAndPortArray[0]), Integer.parseInt(ipAndPortArray[1])));
						} catch (UnknownHostException e) {
							LOGGER.error(e.getMessage(), e);
						}
					}
					TRANSPORT_CLIENT_MAP.put(clusterName, transportClient);
				}
			}
		}
	}

	public void bulk(String index, String type, List<Map<String, ?>> dataList) throws Exception {
		BulkRequestBuilder bulkRequest = transportClient.prepareBulk();
		dataList.forEach(element -> bulkRequest.add(transportClient.prepareIndex(index, type).setSource(element)));

		BulkResponse bulkResponse = bulkRequest.get();
		if(bulkResponse != null && bulkResponse.hasFailures()){
			throw new Exception("bulk failed with " + bulkResponse.buildFailureMessage());
		}
	}

	public TransportClient getTransportClient() {
		return transportClient;
	}

	public void setTransportClient(TransportClient transportClient) {
		this.transportClient = transportClient;
	}
}