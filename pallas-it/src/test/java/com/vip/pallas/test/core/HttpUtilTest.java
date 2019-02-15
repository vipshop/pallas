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

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.vip.pallas.test.base.BaseEsTest;
import com.vip.pallas.utils.HttpUtil;

/**
 * Created by owen on 12/7/2017.
 */
public class HttpUtilTest extends BaseEsTest {

    @Test
    public void testHttpGet() throws Exception {
        Map<String, Object> params = new HashMap<String, Object>();

		Map<String, Object> res = HttpUtil.httpGet("http://127.0.0.1:9200/_cat/indices", params, false);
        Assert.assertEquals("{}", res.toString());

		res = HttpUtil.httpGet("http://127.0.0.1:9200/_cat/indices", params, true);
        Assert.assertTrue(res.size() > 0);
    }
}