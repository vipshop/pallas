package com.vip.pallas.search.rampup;

import com.vip.pallas.search.model.IndexRampup;
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
            LOGGER.error("index rampup error response with {}，targetHost: {}, requestUrl: {}, indexName: {}, clusterId: {}", statusLine.getReasonPhrase(), targetHost.toHostString(), requestUrl, rampup.getFullIndexName(), clusterId);
        }
    }

    @Override
    public void failed(Exception e) {
        LOGGER.error("index rampup request failed cause by {}，targetHost: {}, requestUrl: {}, indexName: {}, clusterId: {}", e.getMessage(), targetHost.toHostString(), requestUrl, rampup.getFullIndexName(), clusterId, e);
    }

    @Override
    public void cancelled() {
        LOGGER.error("index rampup request cancelled，targetHost: {}, requestUrl: {}, indexName: {}, clusterId: {}", targetHost.toHostString(), requestUrl, rampup.getFullIndexName(), clusterId);
    }
}
