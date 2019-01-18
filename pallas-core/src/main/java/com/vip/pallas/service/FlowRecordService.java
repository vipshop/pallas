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

package com.vip.pallas.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import com.vip.pallas.mybatis.entity.FlowRecord;
import com.vip.pallas.mybatis.entity.FlowRecordConfig;
import com.vip.pallas.mybatis.entity.Page;

public interface FlowRecordService {

	List<FlowRecord> findAllFlowRecord();

	List<FlowRecordConfig> findAllFlowRecordConfig();

	void recordFinish(Long recordId);

	List<FlowRecordConfig> findFlowRecordConfigByClusterAndIndex(String clusterName, String indexName);

	void downloadRecord(OutputStream outputStream, Long recordId) throws InterruptedException, IOException;

	//返回关联实体
	FlowRecord getFlowRecordById(Long recordId);

	//返回简单实体
	FlowRecord findFlowRecordById(Long recordId);
	
	FlowRecordConfig findFlowRecordConfigById(Long configId);

	List<FlowRecord> findFlowRecordByPage(Page<FlowRecord> page, Long indexId);

	List<FlowRecord> findFlowRecordByConfig(Page<FlowRecord> page, Long indexId, Long configId);

	List<FlowRecordConfig> findFlowRecordConfigByPage(Page<FlowRecordConfig> page, Long indexId);

	void insert(FlowRecordConfig config);

	void insert(FlowRecord record);

	void update(FlowRecordConfig config);

	void update(FlowRecord record);

	List<FlowRecord> getAllRecording();

	List<FlowRecord> getAvailableRecording();

	List<FlowRecord> getRecordingByConfigId(Long configId);

	void increRecordTotal(Long id, Integer increment);

	void flowRecordSchedule();

	void disableConfigIfNecessary(Long recordId);

}