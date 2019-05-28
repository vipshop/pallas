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

package com.vip.pallas.client.util;

import com.vip.pallas.utils.PallasBasicProperties;

public class PallasRestClientProperties extends PallasBasicProperties {

	public static final String NAME_CONSOLE_QUERY_URL = "pallas.console.query.url";
	public static final String PALLAS_CONSOLE_QUERY_URL = processor.getString(NAME_CONSOLE_QUERY_URL,
			"http://localhost:8080");
	
	public static final String NAME_QUERY_INTERVAL_SECONDS = "pallas.query.interval.seconds";
	public static final int PALLAS_QUERY_INTERVAL_SECONDS = processor.getInteger(NAME_QUERY_INTERVAL_SECONDS, 10);

	public static final String NAME_CLIENT_FATAL_ERROR_KEY = "pallas.client.fatal.error.key";
	public static final String PALLAS_CLIENT_FATAL_ERROR_KEY = processor.getString(NAME_CLIENT_FATAL_ERROR_KEY, "PALLAS_FATAL_ERROR");

	public static final String NAME_CLIENT_FATAL_ERROR_MSG = "pallas.client.fatal.error.msg";
	public static final String PALLAS_CLIENT_FATAL_ERROR_MSG = processor.getString(NAME_CLIENT_FATAL_ERROR_MSG,
			"pls check your token and pallas console domain. make sure you get a valid pallas-search to connect to.");
}