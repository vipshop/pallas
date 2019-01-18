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

package com.vip.pallas.orika;

import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import ma.glasnost.orika.Converter;
import ma.glasnost.orika.Mapper;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.impl.ConfigurableMapper;
import ma.glasnost.orika.impl.DefaultMapperFactory;

public class OrikaBeanMapper extends ConfigurableMapper implements ApplicationContextAware{
	 
    private MapperFactory factory;
    private ApplicationContext applicationContext;
 
    public OrikaBeanMapper() {
        super(false);
    }
 
    @Override
    protected void configure(MapperFactory factory) {
        this.factory = factory;
        this.mapperRegistration();
        this.converterRegistration();
    }
 
    protected void configureFactoryBuilder(DefaultMapperFactory.Builder factoryBuilder) {
    }
 
 
    private void addMapper(Mapper<?, ?> mapper) {
        this.factory.registerMapper(mapper);
    }
 
    public void addConverter(Converter<?, ?> converter) {
        this.factory.getConverterFactory().registerConverter(converter);
    }
 
    @SuppressWarnings("rawtypes")
    private void mapperRegistration() {
    	Map<String, Mapper> mappers = applicationContext.getBeansOfType(Mapper.class);
        if (CollectionUtils.isNotEmpty(mappers.values())) {
        	for (Mapper mapper : mappers.values()) {
        		this.addMapper(mapper);
			}
        }
    }
    
    @SuppressWarnings("rawtypes")
    private void converterRegistration() {
		Map<String, Converter> converters = applicationContext.getBeansOfType(Converter.class);
        if (CollectionUtils.isNotEmpty(converters.values())) {
        	for (Converter converter : converters.values()) {
        		this.addConverter(converter);
			}
        }
    }
    
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        this.init();
        ClassMapRegistry.init(this.factory);
    }



}