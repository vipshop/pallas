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

package com.vip.pallas.service.impl;

import static java.nio.charset.Charset.forName;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Resource;

import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vip.pallas.bean.FlowRecordState;
import com.vip.pallas.bean.SourceQueue;
import com.vip.pallas.mybatis.entity.FlowRecord;
import com.vip.pallas.mybatis.entity.FlowRecordConfig;
import com.vip.pallas.mybatis.entity.Page;
import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.mybatis.repository.FlowRecordConfigRepository;
import com.vip.pallas.mybatis.repository.FlowRecordRepository;
import com.vip.pallas.service.ElasticSearchService;
import com.vip.pallas.service.FlowRecordService;
import com.vip.pallas.service.SearchTemplateService;
import com.vip.pallas.thread.PallasThreadFactory;
import com.vip.pallas.utils.PallasConsoleProperties;
import com.vip.pallas.utils.PallasScrollClient;

@Service
@Transactional(rollbackFor=Exception.class)
public class FlowRecordServiceImpl implements FlowRecordService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlowRecordServiceImpl.class);

	@Resource
	private FlowRecordRepository recordRepository;

	@Resource
	private FlowRecordConfigRepository configRepository;

	@Resource
	private ElasticSearchService elasticSearchService;

	@Resource
	private SearchTemplateService searchTemplateService;

	public final static String RECORD_INDEX_PERFIX = ".pallas_search_record_";

	@Override
	public List<FlowRecord> findAllFlowRecord() {
		return recordRepository.selectAll();
	}

	@Override
	public List<FlowRecordConfig> findAllFlowRecordConfig() {
		return configRepository.selectAll();
	}

	private ScheduledExecutorService FLOW_RECORD_STATE_SCHEDULE = Executors.newScheduledThreadPool(2
			, new PallasThreadFactory("pallas-flow-record-schedule-pool"));

	{
		FLOW_RECORD_STATE_SCHEDULE.scheduleWithFixedDelay(() -> {
			try {
				this.flowRecordSchedule();// 定时异步处理记录状态
			} catch (Exception e) {
				LOGGER.error(e.toString(), e);
			}
		}, 0, 10, TimeUnit.SECONDS);
	}

	@Override
	public void recordFinish(Long recordId) {
		FlowRecord flowRecord = recordRepository.selectByPrimaryKey(recordId);

		if(flowRecord != null && flowRecord.getState() != FlowRecordState.FINISH.getValue()){
			flowRecord.setState((int) FlowRecordState.FINISH.getValue());
			recordRepository.updateByPrimaryKeySelective(flowRecord);

			//同步禁用规则
			FlowRecordConfig flowRecordConfig = configRepository.selectByPrimaryKey(flowRecord.getConfigId());
			if(flowRecordConfig != null){
				flowRecordConfig.setIsEnable(Boolean.FALSE);
				configRepository.updateByPrimaryKeySelective(flowRecordConfig);
			}
		}
	}

    @Override
	public List<FlowRecordConfig> findFlowRecordConfigByClusterAndIndex(String clusterName, String indexName) {
		return configRepository.selectByClusterAndIndex(clusterName, indexName);
	}

	@Override
	public void downloadRecord(OutputStream outputStream, Long recordId) throws InterruptedException, IOException {
		SourceQueue<String[]> queue = new SourceQueue<String[]>();
		AtomicBoolean isEnd = new AtomicBoolean(false);

		new PallasScrollClient(PallasConsoleProperties.FLOW_RECORD_SAVE_CLUSTER_NAME,
				PallasConsoleProperties.FLOW_RECORD_SAVE_CLUSTER_TRANSPORT_ADDRESS,
				RECORD_INDEX_PERFIX + recordId) {

			@Override
			protected void hits(SearchHit[] hits) {
				try {
					queue.put(Arrays.stream(hits).map(hit -> {
						String[] request = new String[4];
						request[0] = String.valueOf(hit.getSource().get("index_name"));
						request[1] = String.valueOf(hit.getSource().get("template_name"));

						String requestBody = String.valueOf(hit.getSource().get("request_body")).replaceAll("\\r\\n", "");
						if(requestBody.contains(",")){
							if(requestBody.contains("\"")){
								requestBody = requestBody.replace("\"", "\"\"");
							}
							requestBody = "\"" + requestBody + "\"";
						}

						request[2] = requestBody;
						request[3] = String.valueOf(hit.getSource().get("request_time"));
						return request;
					}).collect(toList()));
				} catch (InterruptedException e) {
					LOGGER.error(e.toString(), e);
					Thread.currentThread().interrupt();
				}
			}

			@Override
			protected void scrollEnd() {
				isEnd.getAndSet(true);
			}
		}.run();

		//write header
		byte[] bytes = ("index_name,template_name,request_body,request_time\n").getBytes(forName("UTF-8"));
		outputStream.write(bytes, 0, bytes.length);

		while (!isEnd.get()){
			pollAndWrite(queue, outputStream);
			Thread.sleep(100);
		}

		pollAndWrite(queue, outputStream);
		outputStream.flush();
		outputStream.close();
	}

	private void pollAndWrite(SourceQueue<String[]> queue, OutputStream outputStream) throws IOException {
		while (queue.size() > 0){
			String[] request = queue.poll();
			byte[] bytes = (request[0] + "," + request[1] + "," + request[2] + "," + request[3] + "\n").getBytes(forName("UTF-8"));
			outputStream.write(bytes, 0, bytes.length);
		}
	}

	@Override
	public FlowRecord getFlowRecordById(Long recordId) {
		return recordRepository.selectFlowRecordById(recordId);
	}

	@Override
	public FlowRecord findFlowRecordById(Long recordId) {
		return recordRepository.selectByPrimaryKey(recordId);
	}

	@Override
	public FlowRecordConfig findFlowRecordConfigById(Long configId) {
		return configRepository.selectByPrimaryKey(configId);
	}

	@Override
	public List<FlowRecord> findFlowRecordByPage(Page<FlowRecord> page, Long indexId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("indexId", indexId);
		page.setParams(params);

		return recordRepository.selectFlowRecordByPage(page);
	}

	@Override
	public List<FlowRecord> findFlowRecordByConfig(Page<FlowRecord> page, Long indexId, Long configId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("indexId", indexId);
		params.put("configId", configId);
		page.setParams(params);

		return recordRepository.selectFlowRecordByConfig(page);
	}

	@Override
	public List<FlowRecordConfig> findFlowRecordConfigByPage(Page<FlowRecordConfig> page, Long indexId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("indexId", indexId);
		page.setParams(params);

		return configRepository.selectFlowRecordConfigByPage(page);
	}

	@Override
	public void insert(FlowRecordConfig config) {
		config.setIsDeleted(Boolean.FALSE);
		configRepository.insert(config);
	}

	@Override
	public void insert(FlowRecord record) {
		record.setIsDeleted(Boolean.FALSE);
		recordRepository.insert(record);
	}

	@Override
	public void update(FlowRecordConfig config) {
		config.setUpdateTime(new Date());
		configRepository.updateByPrimaryKeySelective(config);
	}

	@Override
	public void update(FlowRecord record) {
		record.setUpdateTime(new Date());
		recordRepository.updateByPrimaryKeySelective(record);
	}

    @Override
	public List<FlowRecord> getAllRecording() {
		if(recordRepository != null){
			List<FlowRecord> recordList = recordRepository.selectAllRecording();
			if(recordList != null){
				return recordList.stream()
					.filter(record -> record.getFlowRecordConfig().getStartTime().getTime() <= System.currentTimeMillis())
					.collect(toList());
			}
		}
		return null;
	}

	@Override
	public List<FlowRecord> getAvailableRecording() {
		if(recordRepository != null){
			List<FlowRecord> recordList = recordRepository.selectAllRecording();
			if(recordList != null){
				return recordList.stream()
						.filter(record -> record.getFlowRecordConfig().getStartTime().getTime() <= System.currentTimeMillis())
						.filter(record -> record.getFlowRecordConfig().getEndTime().getTime() >= System.currentTimeMillis())
						.map(record -> {
							if(record.getFlowRecordConfig().getTemplateId() == -1){
								SearchTemplate template = new SearchTemplate();
								template.setId(-1L);
								template.setTemplateName("-");
								record.getFlowRecordConfig().setTemplate(template);
							}
							return record;
						})
						.collect(toList());
			}
		}
		return null;
	}

	@Override
	public List<FlowRecord> getRecordingByConfigId(Long configId) {
		List<FlowRecord> recordList = recordRepository.selectRecordingByConfigId(configId);
		if(recordList != null){
			long currentTimeMillis = System.currentTimeMillis();
			return recordList.stream()
					.filter(record -> record.getFlowRecordConfig().getStartTime().getTime() <= currentTimeMillis)
					.filter(record -> record.getFlowRecordConfig().getEndTime().getTime() >= currentTimeMillis)
					.collect(toList());
		}
		return null;
	}

	@Override
	public void increRecordTotal(Long id, Integer increment) {
		recordRepository.increRecordTotal(id, increment);
	}

	@Override
	public void flowRecordSchedule() {
		List<FlowRecord> recordList = this.getAllRecording();
		if(recordList != null && recordList.size() > 0){
			recordList.stream().forEach(record -> {
				FlowRecordConfig config = record.getFlowRecordConfig();
				if(record.getTotal() >= config.getLimit()
						|| System.currentTimeMillis() > config.getEndTime().getTime()){
					if(record.getTotal() >= config.getLimit()){
						record.setState((int) FlowRecordState.FINISH.getValue());
					}else if(System.currentTimeMillis() > config.getEndTime().getTime()){
						record.setState((int) FlowRecordState.END.getValue());
					}

					this.update(record);
					//同时禁用规则
					FlowRecordConfig recordConfig = configRepository.selectByPrimaryKey(record.getConfigId());
					recordConfig.setIsEnable(Boolean.FALSE);
					this.update(recordConfig);
				}
			});
		}
	}

	@Override
	public void disableConfigIfNecessary(Long recordId) {
		FlowRecord flowRecord = recordRepository.selectByPrimaryKey(recordId);
		Long configId = flowRecord.getConfigId();
		List<FlowRecord> flowRecordList = recordRepository.selectRecordingByConfigId(configId);

		boolean couldBeDisable = true;

		if(flowRecordList != null){
			for(FlowRecord record : flowRecordList){
				if(record.getId() != recordId){
					couldBeDisable = false;
				}
			}
		}

		if(couldBeDisable){
			FlowRecordConfig config = configRepository.selectByPrimaryKey(configId);
			config.setIsEnable(Boolean.FALSE);
			this.update(config);
		}
	}
}