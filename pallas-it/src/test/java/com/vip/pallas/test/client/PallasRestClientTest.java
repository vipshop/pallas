package com.vip.pallas.test.client;

import com.vip.pallas.client.PallasRestClient;
import com.vip.pallas.client.PallasRestClientBuilder;
import com.vip.pallas.client.search.PallasScrollResponse;
import com.vip.pallas.client.search.ScrollIterator;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class PallasRestClientTest {
    @Test
    public void testScrollSearch() throws InvocationTargetException, InstantiationException, InterruptedException, IllegalAccessException, IOException {
        final PallasRestClient buildClient = PallasRestClientBuilder.buildClient("cGKJojsqaOFLPMZJHP0Dsg==", 1000);
        final HttpEntity entity = new NStringEntity(
                "{\n" + "    \"id\" : \"yy_test_get1id1\",\n" + "    \"params\" : {\"size\":1}\n" + "}",
                ContentType.APPLICATION_JSON);
        PallasScrollResponse response;
        response = buildClient.scrollSearch(
                "/yy_test/_search/template", Collections.EMPTY_MAP, "yy_test_get1id",
                entity);
        assertThat(response.getResponse()).isNotNull();
        assertThat(response.getIterator()).isNotNull();
        assertThat(response.getResponse().getStatusLine().getStatusCode()).isEqualTo(200);
        ScrollIterator iterator = response.getIterator();
        try {
            if (iterator.hasNext()){
                Response res = iterator.next();
                assertThat(res.getStatusLine().getStatusCode()).isEqualTo(200);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            iterator.close();
        }
    }
}
