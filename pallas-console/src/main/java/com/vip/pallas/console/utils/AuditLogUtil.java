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

package com.vip.pallas.console.utils;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditLogUtil {
	private static Logger AUDITLOGGER = LoggerFactory.getLogger("AUDITLOG");
	
	public static Logger getAuditLog() {
		return AUDITLOGGER;
	}
	
	public static void log(String msg){
		StringBuilder logs = new StringBuilder();
		String info = MessageFormat.format("user {0}-{1} ,", SessionUtil.getLoginRealName(null), SessionUtil.getLoginUser(null) );
		logs.append(info);
		logs.append(msg);
		AUDITLOGGER.info(logs.toString());
	}
	
	public static void log(String format, Object ... arguments){
		String info = MessageFormat.format(format, arguments);
		log(info);
	}
}