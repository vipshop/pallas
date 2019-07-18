package com.vip.pallas.test.client;

import com.vip.pallas.client.PallasRestClient;
import com.vip.pallas.client.PallasRestClientBuilder;
import com.vip.pallas.client.search.PallasScrollResponse;
import com.vip.pallas.client.search.ScrollIterator;
import com.vip.pallas.test.base.BaseSearchTest;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Response;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class PallasRestClientTest extends BaseSearchTest {
//    @Before
//    public void setUpPs() throws Exception {
//        String port  = System.getProperty("server.port");
//        if (StringUtils.isEmpty(port)) {
//            waitUntilServerStarted(8080);
//        } else {
//            waitUntilServerStarted(Integer.valueOf(port));
//        }
//        if (!started) {
//            startPS();
//            waitUntilServerStarted(SERVER_PORT);
//            started = true;
//        }
//    }
//    @Test
//    public void testScrollSearch() throws InvocationTargetException, InstantiationException, InterruptedException, IllegalAccessException, IOException {
//        final PallasRestClient buildClient = PallasRestClientBuilder.buildClient("XQx0dVPGB1dlPn3ZTDjaXw==", 1000);
//        final HttpEntity entity = new NStringEntity(
//                "{\n" + "    \"id\" : \"product_comment_product_comment_search\",\n" + "    \"params\" : {\"size\":1}\n" + "}",
//                ContentType.APPLICATION_JSON);
//        PallasScrollResponse response;
//        response = buildClient.scrollSearch(
//                "/product_comment/_search/template", Collections.EMPTY_MAP, "product_comment_product_comment_search",
//                entity);
//        assertThat(response.getResponse()).isNotNull();
//        assertThat(response.getIterator()).isNotNull();
//        assertThat(response.getResponse().getStatusLine().getStatusCode()).isEqualTo(200);
//        ScrollIterator iterator = response.getIterator();
//        try {
//            if (iterator.hasNext()){
//                Response res = iterator.next();
//                assertThat(res.getStatusLine().getStatusCode()).isEqualTo(200);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            iterator.close();
//        }
//    }
//
//    public static void shutdown() throws IOException {
//        if (SERVER_THREAD != null) {
//            System.out.println("start to shutdown the server...");
//            SERVER_THREAD.interrupt();
//            System.out.println("server interrupted.");
//            System.out.println("start to delete all the it-test cluster records in table search_server.");
//        }
//    }
}
