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

package com.vip.pallas.processor.prop;

import com.vip.pallas.utils.ConfigReader;

/*
 * Order 
 * 	env 	     --> key
 *  env 	     --> key.replace(".", "_").toUpperCase()
 *  jvm args     --> key
 *  jvm args     --> key.replace(".", "_").toUpperCase()
 *  app props    --> key
 *  spring props --> key
 */
public class PallasConsolePropertyProcessor extends DefaultPropertyProcessor {

	@Override
	protected String getValue(String key) {
		String val = super.getValue(key);
		return null != val ? val : ConfigReader.getProperty(key);
	}

}