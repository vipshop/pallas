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

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

public class ConfigReader extends PropertyPlaceholderConfigurer implements BeanPostProcessor, InitializingBean {

	private static Properties pros = new Properties();
	 
	public Object postProcessAfterInitialization(Object arg0, String arg1)
			throws BeansException {
		return arg0;
	}

	public Object postProcessBeforeInitialization(Object arg0, String arg1)
			throws BeansException {
		return arg0;
	}

	public void afterPropertiesSet() throws Exception {
		pros = mergeProperties(); // NOSONAR
	}

	public java.util.Properties getPros() {
		return pros;
	}

	public void setPros(java.util.Properties pros) {
		ConfigReader.pros = pros; // NOSONAR
	}

	public static String getProperty(String varname){
		return pros.getProperty(varname);
	}

	public static String getProperty(String varname, String defaultValue) {
		return StringUtils.defaultString(pros.getProperty(varname), defaultValue);
	}
	
	public static boolean getBooleanProperty(String varname){
		return Boolean.parseBoolean(pros.getProperty(varname));
	}
}