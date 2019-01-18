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

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringContextUtil implements ApplicationContextAware { 

	  private static ApplicationContext applicationContext;     //Spring应用上下文环境 

	  /** 
	  * 实现ApplicationContextAware接口的回调方法，设置上下文环境 
	  * @param applicationContext 
	  * @throws BeansException 
	  */ 
	  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException { 
		  SpringContextUtil.applicationContext = applicationContext; // NOSONAR
	  } 

	  /** 
	  * @return ApplicationContext 
	  */ 
	  public static ApplicationContext getApplicationContext() { 
	    return applicationContext; 
	  } 

	  public static boolean containsBean(String beanName){
		  return  applicationContext.containsBean(beanName);
	  }
	  /** 
	  * 获取对象 
	  * @param name 
	  * @return Object 一个以所给名字注册的bean的实例 
	  * @throws BeansException 
	  */ 
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String name) throws BeansException {
		return (T) applicationContext.getBean(name);
	  } 
}