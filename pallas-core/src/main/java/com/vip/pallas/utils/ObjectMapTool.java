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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ObjectMapTool {

	private static Logger logger = LoggerFactory.getLogger(ObjectMapTool.class);

	public static Long parseLong(String input) {
		try {
			if (StringUtils.isNotBlank(input)) {
				return Long.valueOf(input);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static Double parseDouble(String input) {
		try {
			return Double.valueOf(input);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public static Integer parseInt(String input) {
		try {
			if (StringUtils.isNotBlank(input)) {
				return Integer.valueOf(input);
			}
			return null;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public static Float parseFloat(String input) {
		try {
			return Float.parseFloat(input);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public static boolean isEmpty(String input) {
		return input == null || input.length() < 1;
	}

	public static boolean isEmpty(Object input) {
		if (input instanceof String) {
			return isEmpty((String) input);
		}
		return input == null;
	}

	public static boolean isEmpty(Collection<?> c) {
		return (c == null || c.size() < 1);
	}

	public static boolean isNotEmpty(Collection<?> c) {
		return !isEmpty(c);
	}

	public static boolean isEmpty(Object[] objs) {
		return (objs == null || objs.length < 1);
	}

	public static boolean isNullString(String str) {
		return isEmpty(str) || "null".equals(str);
	}

	public static void removeNullItem(Map<? extends Object, ? extends Object> map) {
		Set<Object> keys = new HashSet<Object>(map.keySet());
		for (Object key : keys) {
			Object val = map.get(key);
			if (isEmpty(val)) {
				map.remove(key);
			}
		}
	}

	/**
	 * 从Map中提取出date对象
	 * 
	 * @param param
	 * @param key
	 * @return
	 */
	public static Date getDate(Map param, String key) {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (v == null) {
			return null;
		}
		Date d = DateUtil.getDateFromString(v.toString(), "yyyy-MM-dd HH:mm:ss");
		return d;
	}

	public static Long getDateMs(Map param, String key) {
		Date d = getDate(param, key);
		Long ret = null;
		if (d != null) {
			ret = d.getTime();
		}
		return ret;
	}

	public static Long getLong(Map param, String key) {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (v == null) {
			return null;
		}
		return parseLong(v.toString());
	}

	public static Set<String> getStringSet(Map param, String key) {
		List<String> list = getStringList(param, key);
		if (isEmpty(list)) {
			return Collections.emptySet();
		}
		return new HashSet<String>(list);
	}

	public static List<String> getStringList(Map param, String key) {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (isEmpty(v)) {
			return null;
		}
		List<String> ids = new ArrayList();
		String vs = v.toString();
		vs = vs.trim();
		if (vs.contains("[") || vs.contains("]")) {
			vs = vs.replace("[", "");
			vs = vs.replace("]", "");
		}
		if (isEmpty(vs)) {
			return ids;
		}
		if (!vs.contains(",")) {
			ids.add(vs);
			return ids;
		}
		String[] keyArr = vs.split(",");
		for (int i = 0; i < keyArr.length; i++) {
			String val = StringUtils.trimToNull(keyArr[i]);
			if (val != null) {
				ids.add(val);
			}
		}
		return ids;
	}

	public static List<Long> getLongList(Map param, String key) {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (isEmpty(v)) {
			return null;
		}
		List<Long> ids = new ArrayList();
		String vs = v.toString();
		vs = vs.trim();
		vs = vs.replace(" ", "");
		if (vs.contains("[") || vs.contains("]")) {
			vs = vs.replace("[", "");
			vs = vs.replace("]", "");
		}
		if (isEmpty(vs)) {
			return ids;
		}
		if (!vs.contains(",")) {
			ids.add(parseLong(vs));
			return ids;
		}
		String[] keyArr = vs.split(",");
		for (int i = 0; i < keyArr.length; i++) {
			if (!isEmpty(keyArr[i])) {
				ids.add(parseLong(keyArr[i]));
			}
		}
		return ids;
	}

	public static List<Double> getDoubleList(Map param, String key) {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (v == null) {
			return null;
		}
		if (isEmpty(v)) {
			return null;
		}
		List<Double> ids = new ArrayList();
		String vs = v.toString();
		if (vs.contains("[")) {
			vs = vs.replace("[", "");
			vs = vs.replace("]", "");
		}
		if (isEmpty(vs)) {
			return ids;
		}
		if (!vs.contains(",")) {
			ids.add(parseDouble(vs));
			return ids;
		}
		String[] keyArr = vs.split(",");
		for (int i = 0; i < keyArr.length; i++) {
			if (!isEmpty(keyArr[i])) {
				ids.add(parseDouble(keyArr[i]));
			}
		}
		return ids;
	}

	public static Double getDouble(Map param, String key) {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (v == null) {
			return null;
		}
		return parseDouble(v.toString());
	}

	public static Float getFloat(Map param, String key) {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (v == null) {
			return null;
		}
		return parseFloat(v.toString());
	}

	public static List<Integer> getIntegerList(Map param, String key) {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (v == null) {
			return null;
		}
		if (isEmpty(v)) {
			return null;
		}
		List<Integer> ids = new ArrayList();
		String vs = v.toString();
		if (vs.contains("[")) {
			vs = vs.replace("[", "");
			vs = vs.replace("]", "");
		}
		if (isEmpty(vs)) {
			return ids;
		}
		if (!vs.contains(",")) {
			ids.add(parseInt(vs));
			return ids;
		}
		String[] keyArr = vs.split(",");
		for (int i = 0; i < keyArr.length; i++) {
			if (!isEmpty(keyArr[i])) {
				ids.add(parseInt(keyArr[i]));
			}
		}
		return ids;
	}

	public static Integer getInteger(Map param, String key, int def) {
		Integer v = getInteger(param, key);
		if (v == null) {
			return def;
		}
		return v;
	}

	public static Integer getInteger(Map param, String key) {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (v == null) {
			return null;
		}
		return parseInt(v.toString());
	}

	public static String getString(Map param, String key) {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (v == null) {
			return null;
		}
		return v.toString();
	}

	public static boolean getBoolean(Map param, String key) {
		if (param == null || isEmpty(key)) {
			return false;
		}
		Object v = param.get(key);
		if (v == null) {
			return false;
		}
		String vb = v.toString().trim();
		if ("true".equalsIgnoreCase(vb) || "1".equalsIgnoreCase(vb)) {
			return true;
		}
		return false;
	}

	public static Byte getByte(Map param, String key) {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (v == null) {
			return null;
		}
		Integer xx = parseInt(v.toString());
		return xx.byteValue();
	}

	public static Short getShort(Map param, String key) {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (v == null) {
			return null;
		}
		Integer xx = parseInt(v.toString());
		return xx.shortValue();
	}

	public static <T> T getObj(Map param, String key, Class<T> t) {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (v == null) {
			return null;
		}
		return (T) v;
	}

	public static <T> T getObject(Map param, String key, Class<T> t) throws Exception {
		if (param == null || isEmpty(key)) {
			return null;
		}
		Object v = param.get(key);
		if (v == null) {
			return null;
		}
		return JsonUtil.readValue(JsonUtil.toJson(v), t);
	}
}