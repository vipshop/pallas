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

package com.vip.pallas.test.core;

import com.vip.pallas.test.base.BaseEsTest;
import com.vip.pallas.utils.HttpClient;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by owen on 12/7/2017.
 */
public class HttpClientTest extends BaseEsTest {

    @Test
    public void testGet() throws Exception {

        String res = HttpClient.httpGet("http://127.0.0.1:9200/_cat");
        Assert.assertTrue(res.contains("/_cat/nodes"));
    }

    @Test
    public void testPost() throws Exception {
        String res = HttpClient.httpPost("http://127.0.0.1:9200/sales/item/aaa", "{}");
        System.out.println(res);
        Assert.assertTrue(res.contains("\"created\":true"));
    }

    @Test
    public void testDelete() throws Exception {
        HttpClient.httpPost("http://127.0.0.1:9200/sales/item/aaa", "{}");
        String res = HttpClient.httpDelete("http://127.0.0.1:9200/sales/item/aaa");
        Assert.assertTrue(res.contains("{\"found\":true"));
    }
}