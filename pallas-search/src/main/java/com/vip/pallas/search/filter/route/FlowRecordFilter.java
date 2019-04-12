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

package com.vip.pallas.search.filter.route;

import com.vip.pallas.search.model.FlowRecord;
import com.vip.pallas.search.filter.base.AbstractFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.service.PallasCacheFactory;
import com.vip.pallas.search.thread.FlowRecordSaveThread;
import io.netty.util.internal.InternalThreadLocalMap;
import io.netty.util.internal.ThreadLocalRandom;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class FlowRecordFilter extends AbstractFilter {

	public static String DEFAULT_NAME = PRE_FILTER_NAME + FlowRecordFilter.class.getSimpleName().toUpperCase();

	private static final Map<Long, SoftReference<ArrayBlockingQueue<Map<String, ?>>>> FLOW_RECORD_QUEUE = new ConcurrentHashMap<>();
	private final static Object lockObj = new Object();

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws Exception {
		PallasRequest pallasRequest = sessionContext.getRequest();

		List<FlowRecord> flowRecord = PallasCacheFactory.getCacheService()
				.getFlowRecord(pallasRequest.getLogicClusterId(), pallasRequest.getIndexName(), pallasRequest.getTemplateId());

		if(flowRecord != null){
			flowRecord.stream().forEach(v -> {
				if(threadLocalRandom().nextDouble() <= v.getFlowRecordConfig().getSampleRate()){
					Map<String, Object> sourceMap = new HashMap<>();
					sourceMap.put("index_name", pallasRequest.getIndexName());
					sourceMap.put("template_name", pallasRequest.getTemplateId());
					sourceMap.put("request_body", pallasRequest.getBodyStrForPost());
					sourceMap.put("request_time", System.currentTimeMillis());
					getFlowRecordQueueByRecordId(v.getId()).offer(sourceMap);
				}
			});
		}

		super.run(filterContext, sessionContext);
	}

	private ArrayBlockingQueue<Map<String, ?>> getFlowRecordQueueByRecordId(Long recordId){
		if(!FLOW_RECORD_QUEUE.containsKey(recordId)){
			synchronized(lockObj) {
				if(!FLOW_RECORD_QUEUE.containsKey(recordId)){
					FlowRecordSaveThread.submitTask(recordId);
					FLOW_RECORD_QUEUE.put(recordId, new SoftReference<>(new ArrayBlockingQueue(10000)));
				}
			}
		}
		return FLOW_RECORD_QUEUE.get(recordId).get();
	}

	public static Map<Long, SoftReference<ArrayBlockingQueue<Map<String, ?>>>> getFlowRecordQueue(){
		return FLOW_RECORD_QUEUE;
	}

	private static ThreadLocalRandom threadLocalRandom() {
		return InternalThreadLocalMap.get().random();
	}
}
