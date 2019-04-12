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

package com.vip.pallas.search.netty.http.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationResolver {

	private static Logger logger = LoggerFactory.getLogger(AnnotationResolver.class);
	
	public static final Map<String/**url**/, Method/**method**/> URL_MAPPING = new HashMap<>();
	
	public static void parseClass(Class clazz) {
		try {
	        Method[] methods=clazz.getDeclaredMethods();
	        for(Method m:methods){
	            RequestConfig cfg = m.getAnnotation(RequestConfig.class);
	            if (cfg != null) {
	            	URL_MAPPING.put(cfg.url(), m);
	    	        logger.info("export rest url: {} , method: {}.{}", cfg.url(), clazz.getSimpleName(), m.getName());
	            }
	        }
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
}
