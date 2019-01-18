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

package com.vip.pallas.demo.test;

import com.vip.pallas.client.PallasRestClient;
import com.vip.pallas.client.PallasRestClientBuilder;
import com.vip.pallas.search.launch.Startup;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RestTest {

    public static void main(String[] args) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        System.setProperty("VIP_PALLAS_CONSOLE_QUERY_URL",
                "http://localhost:8080/pallas/ss/query_pslist_and_domain.json");

        System.setProperty("pallas.stdout", "true");

        ClassLoader classLoader = Startup.class.getClassLoader();
        Class<?> loadClass = classLoader.loadClass("com.vip.pallas.demo.test.ProductCommentTest");
        Method method = loadClass.getMethod("search");
        method.invoke(null);
    }
}


class ProductCommentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductCommentTest.class);

    public static void search() throws Exception {
        final PallasRestClient buildClient = PallasRestClientBuilder.buildClient("XQx0dVPGB1dlPn3ZTDjaXw==", 2000);
        final HttpEntity entity = new NStringEntity("{\n" +
                "    \"id\" : \"product_comment_product_comment_search\",\n" +
                "    \"params\" : {\n" +
                "        \"vendor_id\": 601000,\n" +
                "        \"contenet\": \"鞋\"\n" +
                "    }\n" +
                "}", ContentType.APPLICATION_JSON);

        Response response = buildClient.performRequest("POST",
                "/product_comment/_search/template", Collections.EMPTY_MAP, "product_comment_search",
                entity);

        LOGGER.info("search result: {}", EntityUtils.toString(response.getEntity(), UTF_8));
    }
}