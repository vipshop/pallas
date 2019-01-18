package com.vip.pallas.search.service.impl;

import static java.util.Collections.emptyList;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.service.ElasticSearchService;
import com.vip.pallas.search.utils.ElasticRestClient;
import com.vip.pallas.search.utils.ElasticSearchStub;

public class ElasticSearchServiceImpl implements ElasticSearchService {
	
	private static final Logger logger = LoggerFactory.getLogger(ElasticSearchServiceImpl.class);

	@Override
	public List<String> getExcludeNodeList(String clusterAddress) {
		try {
			RestClient restClient = ElasticRestClient.build(clusterAddress);
			String responseStr = getClusterSettings(restClient);

			if (responseStr.indexOf("cluster.routing.allocation.exclude._ip") >= 0) {
				String ipList = StringUtils.substringBetween(responseStr,"\"cluster.routing.allocation.exclude._ip\":\"","\"");
				if (StringUtils.isNotEmpty(ipList)) {
					return Arrays.asList(ipList.split(","));
				}
			}
		} catch (Exception e) {
			logger.error("getExcludeNodeList by {} error: " + e, clusterAddress);
		}
		return emptyList();
	}

	//alias -> actual
	@Override
	public List<String[]> getActualIndexs(String clusterHttpAddress) {
		return ElasticSearchStub.performRequest(clusterHttpAddress, "/_cat/aliases/", (String line,List<String[]> list) -> {
			String[] infos = line.split("\\s+");
			if(infos.length > 2){
				list.add(new String[]{infos[0], infos[1]});
			}
		});
	}
	
	//aliasIndex -> shardNode
	@Override
	public List<String[]> getIndexAndNodes(String clusterHttpAddress) {
		return ElasticSearchStub.performRequest(clusterHttpAddress, "/_cat/shards/", (String line,List<String[]> list) ->{
			String[] infos = line.split("\\s+");
			if(infos.length > 6 && ("STARTED".equals(infos[3]) || "RELOCATING".equals(infos[3]))){
				list.add(new String[]{infos[0], infos[6]});
			}
		});
	}
	
	private String getClusterSettings(RestClient restClient) throws IOException {
		Response response = restClient.performRequest("GET", "/_cluster/settings?flat_settings=true",
		        Collections.singletonMap("pretty", "false"));
		return IOUtils.toString(response.getEntity().getContent(), Charset.forName("UTF-8"));
	}
}


