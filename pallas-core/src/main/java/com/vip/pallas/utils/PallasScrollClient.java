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

package com.vip.pallas.utils;

import com.vip.pallas.thread.ExtendableThreadPoolExecutor;
import com.vip.pallas.thread.PallasThreadFactory;
import com.vip.pallas.thread.TaskQueue;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.slice.SliceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PallasScrollClient extends PallasTransportClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(PallasScrollClient.class);

    private final static int SLICE = 4; // 切片数
    private final static int SCROLL_KEEP_ALIVE = 300000; //scroll查询窗口，单位毫秒
    private final static int SCROLL_SIZE = 2000; //run size

    private String indexName;

    private AtomicInteger sliceCounter = new AtomicInteger(SLICE);
    private transient boolean isScrollEnd = false;

    private static final ThreadPoolExecutor scrollExecutorService = new ExtendableThreadPoolExecutor(10, 30, 20, TimeUnit.SECONDS,
            new TaskQueue(300), new PallasThreadFactory("pallas-console-flow-record-scroll-pool"));

    public PallasScrollClient(String clusterName, String clusterAddress, String indexName) {
        super(clusterName, clusterAddress);
        this.indexName = indexName;
    }

    public void run(){
        for (int i = 0; i < SLICE; i++) {
            initScroll(i);
        }
    }

    private void initScroll(int sliceId){
        scrollExecutorService.submit(() -> {
            try{
                SearchResponse searchResponse = transportClient.prepareSearch(indexName)
                        .slice(new SliceBuilder(sliceId, SLICE))
                        .setQuery(new MatchAllQueryBuilder())
                        .setScroll(new TimeValue(SCROLL_KEEP_ALIVE))
                        .setSize(SCROLL_SIZE)
                        .get();

                if(!checkEnd(searchResponse)){
                    scroll(searchResponse.getScrollId());
                    hits(searchResponse.getHits().getHits());
                }
            }catch(Exception e){
                LOGGER.error(e.toString(), e);
            }
        });
    }

    private void scroll(String scrollId){
        scrollExecutorService.submit(() -> {
            try{
                while(true){
                    SearchResponse searchResponse = transportClient
                            .prepareSearchScroll(scrollId)
                            .setScroll(new TimeValue(SCROLL_KEEP_ALIVE))
                            .get();

                    if(!checkEnd(searchResponse)){
                        hits(searchResponse.getHits().getHits());
                    }else{
                        break;
                    }
                }
            }catch(Exception e){
                LOGGER.error(e.toString(), e);
            }
        });
    }

    private boolean checkEnd(SearchResponse searchResponse){
        if(searchResponse.getHits().getHits().length != 0){
            return false;
        }else {
            if(sliceCounter.decrementAndGet() == 0){
                if(!isScrollEnd){
                    synchronized (this) {
                        if (!isScrollEnd) {
                            scrollEnd();
                            isScrollEnd = true;
                        }
                    }
                }
            }
            return true;
        }
    }

    protected abstract void hits(SearchHit[] hits);

    protected abstract void scrollEnd();
}