package com.vip.pallas.search.thread;

import com.vip.pallas.search.model.FlowRecord;
import com.vip.pallas.search.filter.route.FlowRecordFilter;
import com.vip.pallas.search.service.PallasCacheFactory;
import com.vip.pallas.thread.ExtendableThreadPoolExecutor;
import com.vip.pallas.thread.PallasThreadFactory;
import com.vip.pallas.thread.TaskQueue;
import com.vip.pallas.search.utils.HttpClient;
import com.vip.pallas.search.utils.JsonUtil;
import com.vip.pallas.utils.PallasBasicProperties;
import com.vip.pallas.search.utils.PallasTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FlowRecordSaveThread implements Runnable  {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlowRecordSaveThread.class);

	private Long recordId;
	private PallasTransportClient pallasTransportClient;

	private final static int BULK_THREAD_COUNT_PER_RECORD = Integer.parseInt(System.getProperty("flow.record.bulk.thread.count", "2")); //每个流量记录任务分配的Bulk线程数
	private final static int BULK_SIZE = Integer.parseInt(System.getProperty("flow.record.bulk.size", "1000"));//每次ES批量插入的大小

	private final static String FLOW_RECORD_INDEX_NAME_PERFIX = ".pallas_search_record_";
	private final static String FLOW_RECORD_INDEX_TYPE_NAME = "item";

	private static final ThreadPoolExecutor bulkExecutorService = new ExtendableThreadPoolExecutor(16, 30, 20, TimeUnit.SECONDS,
			new TaskQueue(300), new PallasThreadFactory("pallas-search-flow-record-bulk-pool"));

	public FlowRecordSaveThread(Long recordId) {
		this.recordId = recordId;
		this.pallasTransportClient = new PallasTransportClient(PallasBasicProperties.FLOW_RECORD_SAVE_CLUSTER_NAME,
				PallasBasicProperties.FLOW_RECORD_SAVE_CLUSTER_TRANSPORT_ADDRESS);
	}

	@Override
	public void run() {
		try{
			Map<Long, SoftReference<ArrayBlockingQueue<Map<String, ?>>>> flowRecordQueueMap = FlowRecordFilter.getFlowRecordQueue();
			for (;;) {
				FlowRecord flowRecord = PallasCacheFactory.getCacheService().getFlowRecordById(recordId);
				if (flowRecord != null) {
					if (flowRecord.getTotal() >= flowRecord.getFlowRecordConfig().getLimit()) {
						break;
					}

					if(flowRecordQueueMap.containsKey(recordId)) {
						SoftReference<ArrayBlockingQueue<Map<String, ?>>> refFlowRecordQueue = flowRecordQueueMap.get(recordId);
						if (refFlowRecordQueue != null) {
							ArrayBlockingQueue<Map<String, ?>> flowRecordQueue = refFlowRecordQueue.get();
							if(flowRecordQueue != null){
								if (flowRecordQueue.size() >= BULK_SIZE * BULK_THREAD_COUNT_PER_RECORD) {
									List<Map<String, ?>> list = new ArrayList<>(BULK_SIZE);
									flowRecordQueue.drainTo(list, BULK_SIZE); //NOSONAR
									pallasTransportClient.bulk(FLOW_RECORD_INDEX_NAME_PERFIX + recordId, FLOW_RECORD_INDEX_TYPE_NAME, list);
									updateRecordNum(list.size());
								} else {
									Thread.sleep(3000);
								}
							}
						}
					}else{
						break;
					}
				}else{
					break;
				}
			}
		}catch(Exception e){
			LOGGER.error(e.toString(), e);
		}finally {
			cleanQueueWhenExit();
		}
	}

	private void cleanQueueWhenExit(){
		Map<Long, SoftReference<ArrayBlockingQueue<Map<String, ?>>>> flowRecordQueueMap = FlowRecordFilter.getFlowRecordQueue();
		if(flowRecordQueueMap != null && flowRecordQueueMap.containsKey(recordId)){
			SoftReference<ArrayBlockingQueue<Map<String, ?>>> refQueue = flowRecordQueueMap.remove(recordId);
			if(refQueue != null){
				ArrayBlockingQueue<Map<String, ?>> queue = refQueue.get();
				if(queue != null){
					queue.clear();
				}
				refQueue.clear();
			}
		}
	}

	private void updateRecordNum(int num) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("recordId", recordId);
		params.put("recordNum", num);

		HttpClient.httpPost(PallasBasicProperties.PALLAS_CONSOLE_REST_URL + "/record/flow_record/update_num.json", JsonUtil.toJson(params));
	}

	public static void submitTask(Long recordId) {
		for (int i = 0; i < BULK_THREAD_COUNT_PER_RECORD; i++) {
 			bulkExecutorService.submit(new FlowRecordSaveThread(recordId));
		}
	}
}
