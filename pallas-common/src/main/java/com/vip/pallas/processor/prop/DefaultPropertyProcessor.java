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

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.utils.PallasBasicProperties;

/*
 * Order 
 * 	env 	     --> key
 *  env 	     --> key.replace(".", "_").toUpperCase()
 *  jvm args     --> key
 *  jvm args     --> key.replace(".", "_").toUpperCase()
 *  app props    --> key
 */
public class DefaultPropertyProcessor extends AbstractPropertyProcessor {

	private static final Logger logger = LoggerFactory.getLogger(DefaultPropertyProcessor.class);
	
	protected String keyPrefix = "";
	
	protected DefaultPropertyProcessor() {
		keyPrefix = getKeyPrifix();
		logger.info("Use configure property processor key prefix {}", keyPrefix);
	};
	
	protected String getValue(String key) {
		Objects.requireNonNull(key);
        String val = getEnv(key);
        if (null != val) {
            return val;
        }
        val = getSystem(key);
        if (null != val) {
            return val;
        }
        return getProperty(key);
	}
	
	private static String getKeyPrifix() {
		return props.getProperty(PallasBasicProperties.NAME_PROCESSOR_PROPERTY_KEY_PREFIX, "");
	}
	
	protected String transform(String key) {
		return keyPrefix + key.replace('.', '_').toUpperCase();
	}
	
	protected String getEnv(String key){
		String val = System.getenv(key);
		if (val != null) {
			return val;
		}
		return System.getenv(transform(key));
	}
	
	protected String getSystem(String key){
		String val = System.getProperty(key);
		if (val != null) {
			return val;
		}
		return System.getProperty(transform(key));
	}
	
	protected String getProperty(String key){
		return props.getProperty(key);
	}
}