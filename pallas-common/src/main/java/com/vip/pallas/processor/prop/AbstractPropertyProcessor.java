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

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ServiceConfigurationError;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Resources;
import com.vip.pallas.utils.PallasBasicProperties;

public abstract class AbstractPropertyProcessor implements PropertyProcessor {

	private static final Logger logger = LoggerFactory.getLogger(AbstractPropertyProcessor.class);
	
	protected static Properties props;

	protected AbstractPropertyProcessor() {};
	
	static {
		props = getProperties(NAME_PALLAS_APPLICATION_RESOURCES);
	}
	
	private static Properties getProperties(String resource) {
		Properties properties = new Properties();
		try {
			ClassLoader cl = AbstractPropertyProcessor.class.getClassLoader();
			Enumeration<URL> urls = cl.getResources(resource);
			List<URL> urlList = Collections.list(urls);
			Collections.reverse(urlList);
			for (URL url : urlList) {
				logger.info("Get Properties: url=" + url);
				byte[] bytes = Resources.toByteArray(url);
				properties.load(new ByteArrayInputStream(bytes));
			}
		} catch (Exception e) {
			logger.error("Error load: " + resource, e);
		}
		return properties;
	}
	
	private static class PropertyProcessorHolder {
		private static PropertyProcessor processor = initProcessor();
		
		private static PropertyProcessor initProcessor() {
			String propertyProcessorClassName = props.getProperty(PallasBasicProperties.NAME_PROCESSOR_PROPERTY);
			if (StringUtils.isEmpty(propertyProcessorClassName)) {
				propertyProcessorClassName = DefaultPropertyProcessor.class.getName();
				logger.info("Couldn't found configure property by key 'pallas.processor.property' Use Default {}", propertyProcessorClassName);
			}
			try {
				Class<?> PropertyProcessorClass = Class.forName(propertyProcessorClassName);
				Constructor<?> constructor=  PropertyProcessorClass.getDeclaredConstructor();
				constructor.setAccessible(true);
				processor = (PropertyProcessor) constructor.newInstance();
			} catch (Exception e) {
				logger.error(e.toString());
				throw new ServiceConfigurationError("Provider " + propertyProcessorClassName + " could not be instantiated :" + e);
			}
			logger.info("Use configure property processor {}", propertyProcessorClassName);
			return processor;
		}
	}
	
	public static PropertyProcessor getProcessor() {
		return PropertyProcessorHolder.processor;
	}
	
	protected abstract String getValue(String key);
	
	@Override
	public String getString(String key, String def) {
		String val = getValue(key);
		return null != val ? val : def;
	}

	@Override
	public int getInteger(String key, int def) {
		String val = getValue(key);
		return null != val ? Integer.parseInt(val) : def;
	}

	@Override
	public boolean getBoolean(String key, boolean def) {
		String val = getValue(key);
		return  null != val ? Boolean.parseBoolean(val) : def;
	}

}