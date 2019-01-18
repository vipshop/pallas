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

package com.vip.pallas.commando;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.exception.BusinessLevelException;

/**
 * Web层异常处理器
 * 
 */
public class ExceptionHandler extends AbstractExceptionHandler {
	private static Logger logger = LoggerFactory.getLogger(ExceptionHandler.class);

	@Override
	public StatusDescription handle(Exception e) {
		StatusDescription sm = null;
		if (e instanceof BusinessLevelException) {
			sm = new StatusDescription(((BusinessLevelException) e).getErrorCode(), e.getMessage());
		} else {
			sm = StatusDescription.unknown(e.getMessage());
		}
		logger.error(e.getMessage(), e);
		return sm;
	}
}