package com.vip.pallas.search.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by jamin.li on 10/12/2017.
 */
public class ElasticSearchStub {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchStub.class);

    public static RestClient getElasticRestClient(String clusterHttpAddress){
        return ElasticRestClient.build(clusterHttpAddress);
    }

    public static List<String[]> performRequest(String clusterHttpAddress, String endpoint, BiConsumer<String, List<String[]>> comsumer) {
        if(clusterHttpAddress == null || clusterHttpAddress.isEmpty()){
            return Collections.EMPTY_LIST;
        }

        RestClient client = getElasticRestClient(clusterHttpAddress);
        Response response;
        try {
            response = client.performRequest("GET", endpoint);
        } catch (IOException e) {
			LOGGER.error("invoker error by clusterHttpAddress: {}, cause: " + e, clusterHttpAddress);
            return Collections.EMPTY_LIST;
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))) {
            String line;
            List<String[]> aliasesList = new LinkedList<>();
            while ((line = br.readLine()) != null) {
                comsumer.accept(line, aliasesList);
            }
            return aliasesList;
        } catch (Exception e) {
			LOGGER.error("invoker error by clusterHttpAddress: {}, cause: " + e, clusterHttpAddress);
            return Collections.EMPTY_LIST;
        }
    }
}
