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

package com.vip.pallas.demo.es;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class PallasEmbeddedElastic {

    public static String withTemplate(String name, String templateBody) throws IOException {
        HttpPost httpPost = new HttpPost("http://localhost:9200/_search/template/" + name);

        templateBody = templateBody.replaceAll("\n"," ")
                .replaceAll("\r"," ")
                .replaceAll("\t"," ")
                //.replaceAll("\\{\\{\\{}", "{ {{")
                .replaceAll("\"", "\\\\\"");

        templateBody = "{\n\"template\":\"" + templateBody + "\"\n}";

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            httpPost.setEntity(new NStringEntity(templateBody, ContentType.APPLICATION_JSON));
            CloseableHttpResponse response = httpclient.execute(httpPost);
            return EntityUtils.toString(response.getEntity());
        }
    }
}