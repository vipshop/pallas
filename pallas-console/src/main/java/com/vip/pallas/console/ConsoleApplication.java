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

package com.vip.pallas.console;

import java.io.IOException;
import java.util.ServiceConfigurationError;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;

import com.vip.pallas.cerebro.launcer.CerebroEmbed;
import com.vip.pallas.console.filter.AbstractAuthProcessor;
import com.vip.pallas.console.filter.AuthenticationProcessor;
import com.vip.pallas.exception.PallasException;
import com.vip.pallas.processor.plugin.AbstractPluginFileProcessor;
import com.vip.pallas.utils.PallasConsoleProperties;
import com.vip.vjtools.vjkit.reflect.ReflectionUtil;

@SpringBootApplication
@ImportResource("classpath:applicationContext.xml")
public class ConsoleApplication {

	private static final Logger logger = LoggerFactory.getLogger(ConsoleApplication.class);
	
	public static void main(String[] args) throws PallasException, IOException {
		SpringApplication.run(ConsoleApplication.class, args);
		CerebroEmbed.launch();
	}

	@Bean
	public AbstractAuthProcessor registerAuthProcessor() {
		return new AuthenticationProcessor();
	}

	@Bean
	public AbstractPluginFileProcessor registerPluginFileProcessor() {
		Object processor = null;
		try {
			Class<?> pluginFileProcessorClass = Class.forName(PallasConsoleProperties.PROCESSOR_FILE_PLUGIN);
			processor = ReflectionUtil.invokeConstructor(pluginFileProcessorClass, ArrayUtils.EMPTY_OBJECT_ARRAY);
		} catch (Exception e) {
			throw new ServiceConfigurationError("Provider " + PallasConsoleProperties.PROCESSOR_FILE_PLUGIN + " could not be instantiated :" + e.toString()); // NOSONAR
		}
		if (!(processor instanceof AbstractPluginFileProcessor)) {
			throw new ServiceConfigurationError(processor.getClass().getName() + "cannot be cast to" + AbstractPluginFileProcessor.class.getName());
		}
		logger.info("Use configure file plugin processor {}", processor.getClass().getName());
		return (AbstractPluginFileProcessor) processor;
	}
}