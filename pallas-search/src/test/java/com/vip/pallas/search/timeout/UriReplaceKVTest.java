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

package com.vip.pallas.search.timeout;

import org.junit.Test;

import junit.framework.TestCase;


public class UriReplaceKVTest extends TestCase {

    @Test
    public void test() throws Exception {
		String u1 = "yy_test/_search/template?&timeout=100ms&preference=_prefer_nodes:6oWh7dybRG6zfRriYR94Qw,efaef23fafee9";
		String newU1 = AsyncCall.replaceUriValue(u1, "preference=_prefer_nodes:", "node1,node2");
		System.out.println(newU1);
		assertEquals(newU1, "yy_test/_search/template?&timeout=100ms&preference=_prefer_nodes:node1,node2");
		String u2 = "yy_test/_search/template?preference=_prefer_nodes:6oWh7dybRG6zfRriYR94Qw,efaef23fafee9&timeout=100ms";
		String newU2 = AsyncCall.replaceUriValue(u2, "preference=_prefer_nodes:", "node1,node2");
		assertEquals(newU2, "yy_test/_search/template?preference=_prefer_nodes:node1,node2&timeout=100ms");
		String u3 = "yy_test/_search/template";
		String newU3 = AsyncCall.replaceUriValue(u3, "preference=_prefer_nodes:", "node1,node2");
		assertEquals(newU3, "yy_test/_search/template?preference=_prefer_nodes:node1,node2");
		String u4 = "yy_test/_search/template?&timeout=100ms";
		String newU4 = AsyncCall.replaceUriValue(u4, "preference=_prefer_nodes:", "node1,node2");
		assertEquals(newU4, "yy_test/_search/template?&timeout=100ms&preference=_prefer_nodes:node1,node2");
		String u5 = "yy_test/_search/template?&timeout=100ms";
		String newU5 = AsyncCall.replaceUriValue(u5, "timeout=", "444ms");
		assertEquals(newU5, "yy_test/_search/template?&timeout=444ms");
    }
}
