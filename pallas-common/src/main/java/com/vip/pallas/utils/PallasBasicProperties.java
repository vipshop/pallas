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

import com.vip.pallas.processor.prop.AbstractPropertyProcessor;
import com.vip.pallas.processor.prop.PropertyProcessor;

public class PallasBasicProperties {

	protected static PropertyProcessor processor = AbstractPropertyProcessor.getProcessor();
	
	public static final String NAME_PROCESSOR_PROPERTY = "pallas.processor.property";
	public static final String NAME_PROCESSOR_PROPERTY_KEY_PREFIX = "pallas.processor.property.key-prefix";
	
	private static final String NAME_CACHE_MAXIMUM_SIZE = "pallas.cache.maximum.size";
	public static final int CACHE_MAXIMUM_SIZE = processor.getInteger(NAME_CACHE_MAXIMUM_SIZE, Integer.MAX_VALUE);

	private static final String NAME_REFRESH_AFTER_WRITE_DURATION = "pallas.refresh.after.write.duration";
	public static int REFRESH_AFTER_WRITE_DURATION = processor.getInteger(NAME_REFRESH_AFTER_WRITE_DURATION, 30);

	private static final String NAME_EXPIRE_AFTER_WRITE_DURATION = "pallas.expire.after.write.duration";
	public static final int EXPIRE_AFTER_WRITE_DURATION = processor.getInteger(NAME_EXPIRE_AFTER_WRITE_DURATION, 24 * 60 * 60);

	private static final String NAME_EXPIRE_AFTER_ACCESS_DURATION = "pallas.expire.after.access.duration";
	public static final int EXPIRE_AFTER_ACCESS_DURATION = processor.getInteger(NAME_EXPIRE_AFTER_ACCESS_DURATION, 24 * 60 * 60);

	private static final String NAME_CONSOLE_REST_URL = "pallas.console.rest.url";
	public static String PALLAS_CONSOLE_REST_URL = processor.getString(NAME_CONSOLE_REST_URL, "http://localhost:8080/pallas");

	public static final String NAME_DEFAULT_INDEX_SLOWER_THAN = "pallas.default.index.slower.than";
	public static final int DEFAULT_INDEX_SLOWER_THAN = processor.getInteger(NAME_DEFAULT_INDEX_SLOWER_THAN, 200);

	public static final String NAME_DEFAULT_PS_SIDE_SLOW_THRESHOLD = "pallas.default.ps.side.slow.threshold";
	public static final int DEFAULT_PS_SIDE_THRESHOLD = processor.getInteger(NAME_DEFAULT_PS_SIDE_SLOW_THRESHOLD, 30);

	public static final String NAME_ES_HEARTBEAT_INTERVAL = "pallas.es.heartbeat.interval";
	public static final int PALLAS_ES_HEARTBEAT_INTERVAL = processor.getInteger(NAME_ES_HEARTBEAT_INTERVAL, 30);

	public static final String NAME_FLOW_RECORD_SAVE_CLUSTER_NAME = "pallas.flow.record.save.cluster.name";
	public static final String FLOW_RECORD_SAVE_CLUSTER_NAME = processor.getString(NAME_FLOW_RECORD_SAVE_CLUSTER_NAME, "");
	
	public static final String NAME_FLOW_RECORD_SAVE_CLUSTER_TRANSPORT_ADDRESS = "pallas.flow.record.save.cluster.transport.address";
	public static final String FLOW_RECORD_SAVE_CLUSTER_TRANSPORT_ADDRESS = processor.getString(NAME_FLOW_RECORD_SAVE_CLUSTER_TRANSPORT_ADDRESS, "");
	
}