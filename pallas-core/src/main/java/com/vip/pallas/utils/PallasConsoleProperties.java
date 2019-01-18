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

public class PallasConsoleProperties extends PallasBasicProperties {

	public static final String NAME_AUTHROIZE_APP = "pallas.authroize.app";
	public static final String PALLAS_AUTHROIZE_APP = processor.getString(NAME_AUTHROIZE_APP,  "pallas-console");
	
	private static final String NAME_CEREBRO_PORT = "pallas.cerebro.port";	
	public static final String CEREBRO_PORT = processor.getString(NAME_CEREBRO_PORT,"9001");
	
	public static final String NAME_PROXY_THROTTLE =  "pallas.proxy.throttle";
	public static final int PROXY_THROTTLE = processor.getInteger(NAME_PROXY_THROTTLE, 100);

	public static final String NAME_CACHE_LOADDING_CONSUME_TIME = "pallas.cache.loadding.consume.time";
	public static final int CACHE_LOADDING_CONSUME_TIME = processor.getInteger(NAME_CACHE_LOADDING_CONSUME_TIME, 15000);

	public static final String NAME_FLOW_RECORD_SAVE_CLUSTER_REST_ADDRESS = "pallas.flow.record.save.cluster.rest.address";
	public static final String FLOW_RECORD_SAVE_CLUSTER_REST_ADDRESS = processor.getString(NAME_FLOW_RECORD_SAVE_CLUSTER_REST_ADDRESS, "");
	
	public static final String NAME_SECURITY_ENABLE = "pallas.security.enable";
    public static final boolean PALLAS_SECURITY_ENABLE = processor.getBoolean(NAME_SECURITY_ENABLE, false);
    
    public static final String NAME_SECURITY_AUTHENTICATION = "pallas.security.authentication";
    public static final String PALLAS_SECURITY_AUTHENTICATION = processor.getString(NAME_SECURITY_AUTHENTICATION, "simple");
    
    public static final String NAME_AUTHORIZATION_ENABLE = "pallas.authorization.enable";
    public static final boolean PALLAS_AUTHORIZATION_ENABLE = processor.getBoolean(NAME_AUTHORIZATION_ENABLE, false);
    
    public static final String NAME_LOGIN_URL = "pallas.login.url";
    public static final String PALLAS_LOGIN_URL = processor.getString(NAME_LOGIN_URL, "http://localhost/#/login");
    
    public static final String NAME_PROCESSOR_FILE_PLUGIN = "pallas.processor.file-plugin";
    public static final String PROCESSOR_FILE_PLUGIN = processor.getString(NAME_PROCESSOR_FILE_PLUGIN, "com.vip.pallas.processor.plugin.PluginDefaultFileProcessor");
}