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

package com.vip.pallas.search.rampup;

import com.vip.pallas.search.model.IndexRampup;
import com.vip.pallas.search.utils.LogUtils;
import com.vip.pallas.search.utils.SearchLogEvent;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class RampupCallback implements FutureCallback<HttpResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RampupCallback.class);

    private HttpHost targetHost;
    private String requestUrl;
    private String clusterId;
    private IndexRampup rampup;

    public RampupCallback(HttpHost targetHost, String requestUrl, String clusterId, IndexRampup rampup){
        this.targetHost = targetHost;
        this.requestUrl = requestUrl;
        this.clusterId = clusterId;
        this.rampup = rampup;
    }

    @Override
    public void completed(HttpResponse response) {
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();

        if(statusCode >= 200 && statusCode < 400){
            Map<Long, AtomicLong> rampupCounterMap = RampupCounter.getRampupCounterMap();
            Long versionId = rampup.getVersionId();
            if(!rampupCounterMap.containsKey(versionId)){
                rampupCounterMap.putIfAbsent(versionId, new AtomicLong());
            }
            rampupCounterMap.get(versionId).incrementAndGet();
        }else{
            LogUtils.error(LOGGER, SearchLogEvent.RAMPUP_EVENT,
					"index rampup error response with {}，targetHost: {}, requestUrl: {}, indexName: {}, clusterId: {}", statusLine.getReasonPhrase(), targetHost.toHostString(), requestUrl, rampup.getFullIndexName(), clusterId);
        }
    }

    @Override
    public void failed(Exception e) {
		LogUtils.error(LOGGER, SearchLogEvent.RAMPUP_EVENT, "index rampup request failed cause by {}，targetHost: {}, requestUrl: {}, indexName: {}, clusterId: {}", e.getMessage(), targetHost.toHostString(), requestUrl, rampup.getFullIndexName(), clusterId, e);
    }

    @Override
    public void cancelled() {
		LogUtils.error(LOGGER, SearchLogEvent.RAMPUP_EVENT, "index rampup request cancelled，targetHost: {}, requestUrl: {}, indexName: {}, clusterId: {}", targetHost.toHostString(), requestUrl, rampup.getFullIndexName(), clusterId);
    }
}
