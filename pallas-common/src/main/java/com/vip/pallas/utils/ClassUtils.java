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

import java.util.Iterator;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassUtils {

	private static Logger logger = LoggerFactory.getLogger(ClassUtils.class);
	
	public static ClassLoader getDefaultClassLoader() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null) {
			cl = ClassUtils.class.getClassLoader();
			if (cl == null) {
				cl = ClassLoader.getSystemClassLoader();
			}
		}
		logger.info("Use ClassLoader {}", cl.getClass().getName());
		return cl;
	}
	
	/*
	 * 获取最后一个加载的迭代器，以期spi配置可以进行覆盖替换
	 */
	public static <T> T loadFromServiceLoader(Class<T> c) {
        ServiceLoader<T> loader = ServiceLoader.load(c, getDefaultClassLoader());
        Iterator<T> it = loader.iterator();
        if (it.hasNext())
            return it.next();
        return null;
    }
}