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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.vip.pallas.search.filter.rest.RestInvokerFilter;
import com.vip.pallas.search.model.IndexRampup;
import com.vip.pallas.search.service.PallasCacheFactory;
import com.vip.pallas.search.utils.HttpClientUtil;
import com.vip.pallas.utils.LogUtils;
import com.vip.pallas.search.utils.PallasSearchProperties;
import com.vip.pallas.search.utils.SearchLogEvent;
import com.vip.pallas.thread.ExtendableThreadPoolExecutor;
import com.vip.pallas.thread.PallasThreadFactory;
import com.vip.pallas.thread.TaskQueue;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.util.internal.InternalThreadLocalMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.PallasHttpAsyncClientBuilder;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RampupHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(RampupHandler.class);

    public static CloseableHttpAsyncClient httpClient;

    private static RestInvokerFilter.IdleConnectionEvictor connEvictor = null;

    private static final ExtendableThreadPoolExecutor RAMPUP_EXECUTOR = new ExtendableThreadPoolExecutor(
            3 , 20, 2L, TimeUnit.MINUTES, new TaskQueue(
            20480), new PallasThreadFactory("pallas-index-rampup-pool", Thread.MAX_PRIORITY));

    static {
        //独立预热HttpClient
        ConnectionKeepAliveStrategy keepAliveStrategy = (response, context) -> PallasSearchProperties.HTTP_SERVER_KEEPALIVE_TIMEOUT;

        ConnectingIOReactor ioReactor;
        try {
            IOReactorConfig config = IOReactorConfig.custom().setSelectInterval(40)
                    .setIoThreadCount(PallasSearchProperties.CONNECTION_IO_THREAD_NUM).build();
            ioReactor = new DefaultConnectingIOReactor(config);
        } catch (IOReactorException e) {
            LogUtils.error(LOGGER, SearchLogEvent.RAMPUP_EVENT,e.getMessage(), e);
            throw new RuntimeException(e);// Noncompliant
        }

        PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(ioReactor);
        cm.setDefaultMaxPerRoute(PallasSearchProperties.CONNECTION_MAX_PER_ROUTE);
        cm.setMaxTotal(PallasSearchProperties.PALLAS_CONNECTION_MAX);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(PallasSearchProperties.HTTP_POOL_AQUIRE_TIMEOUT)
                .setConnectTimeout(PallasSearchProperties.HTTP_CONNECTION_TIMEOUT)
                .setSocketTimeout(PallasSearchProperties.HTTP_SOCKET_TIMEOUT)
                .build();

        if (httpClient == null) {
            httpClient = PallasHttpAsyncClientBuilder.create().setConnectionManager(cm).setKeepAliveStrategy(keepAliveStrategy)
                    .setDefaultRequestConfig(requestConfig).setThreadFactory(
                            new ThreadFactoryBuilder().setNameFormat("Pallas-Search-Rampup-Http-Rest-Client").build())
                    .build();
            httpClient.start();
        }

        if (connEvictor == null) {
            connEvictor = new RestInvokerFilter.IdleConnectionEvictor(cm);
            connEvictor.start();
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown()));
    }

    public static void rampupIfNecessary(HttpHost targetHost, String requestUrl, DefaultFullHttpRequest outBoundRequest, HttpRequestBase httpRequest, final HttpEntity entity, String indexName, String clusterId) {
        RAMPUP_EXECUTOR.submit(() -> {
            try{
                List<IndexRampup> rampupList = PallasCacheFactory.getCacheService().getRampupByIndexNameAndCluster(indexName, clusterId);

                if(rampupList != null && !rampupList.isEmpty()){
                    //开启预热的话，找出指定索引下的全部要预热的版本，逐个预热

                    HttpEntity httpEntity = entity;

                    if(httpEntity == null){
                        httpEntity = ((HttpPost) httpRequest).getEntity();
                    }

                    for (IndexRampup rampup : rampupList) {
                        //预热脚本增加采样率计算
                        if (rampup != null && isSample(rampup.getSampleRate())) {
                            HttpRequestBase request = HttpClientUtil.getHttpUriRequest(targetHost, outBoundRequest, httpEntity);
                            request.setURI(URI.create(requestUrl.replace(indexName, rampup.getFullIndexName())));
                            HttpClientContext httpContext = HttpClientContext.create();

                            httpClient.execute(targetHost, request, httpContext, new RampupCallback(targetHost, requestUrl, clusterId, rampup));
                        }
                    }
                }
            }catch (Exception e){
                LogUtils.error(LOGGER, SearchLogEvent.RAMPUP_EVENT,"index rampup error cause by {}，targetHost: {}, requestUrl: {}, indexName: {}, clusterId: {}", e.getMessage(), targetHost, requestUrl, indexName, clusterId, e);
            }
        });
    }

    /**
     * 预热脚本的采样率值范围是 1~100，最大值表示100%采样，1表示1% 的采样率
     * @param sampleRate
     * @return
     */
    private static boolean isSample(Integer sampleRate) {
        //可能需要向前兼容，前面版本没有值的时候需要采样
        if (sampleRate == null) {
            return true;
        }
        int selected = InternalThreadLocalMap.get().random().nextInt(100) + 1;
        return selected <= sampleRate;
    }

    public static void shutdown() {
        if (connEvictor != null) {
            connEvictor.shutdown();
        }

        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                LogUtils.error(LOGGER, SearchLogEvent.RAMPUP_EVENT,e.getMessage(), e);
            }
        }
    }
}
