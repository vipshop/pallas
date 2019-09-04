package com.vip.pallas.client;

import com.vip.pallas.client.search.PallasScrollResponse;
import com.vip.pallas.client.search.ScrollIterator;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

public class PallasRestClientBuilderTest {

    @Test
    public void buildClient() throws InvocationTargetException, InstantiationException, InterruptedException, IllegalAccessException, IOException {
        final PallasRestClient buildClient = PallasRestClientBuilder.buildClient("VHDU8JiAJ69d6a8JDegndg==", 10000);
        final HttpEntity entity = new NStringEntity(
                "{\n" + "    \"id\" : \"yy_test_get1id1\",\n" + "    \"params\" : {\"size\":1}\n" + "}",
                ContentType.APPLICATION_JSON);
//        testMultiToken(buildClient3,entity);

        List<Future> result=new ArrayList<>();
        ExecutorService ex= Executors.newCachedThreadPool();
        for (int i=0;i<20;i++){
            result.add(ex.submit(() -> testMultiToken(buildClient,entity)));
//            result.add(ex.submit(() -> testMultiToken(buildClient2,entity)));
//            result.add(ex.submit(() -> testMultiToken(buildClient3,entity)));
        }
//        testMultiToken(buildClient,entity);
            final PallasRestClient buildClient2 = PallasRestClientBuilder.buildClient("xmelhcx1OWPFR41j1lGZjQ==", 10000);
//        testMultiToken(buildClient2,entity);
            final PallasRestClient buildClient3 = PallasRestClientBuilder.buildClient("zrP58UzmUAbDOouwAfsRZw==", 10000);
        for (int i=0;i<20;i++){
            result.add(ex.submit(() -> testMultiToken(buildClient2,entity)));
            result.add(ex.submit(() -> testMultiToken(buildClient3,entity)));
        }
        result.forEach(f-> {
            try {
                f.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }



    @Test
    public void buildClient1() {
    }

    @Test
    public void buildClient2() {
    }

    @Test
    public void rebuildInternalRestClient() {
    }

    @Test
    public void closeClientByToken() {
    }



    public void testMultiToken(PallasRestClient buildClient,HttpEntity entity) {
        for (int i =0;i< 100; i++){
            try {
                Thread.sleep(1);
                Response response=buildClient.performRequest("GET",
//                        "/yy_test/_search/template", Collections.EMPTY_MAP, "yy_test_get1id",
                        "_cluster/health", Collections.EMPTY_MAP, "",
                        entity);
//                System.out.println(EntityUtils.toString(response.getEntity()));
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}