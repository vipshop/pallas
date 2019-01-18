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

package com.vip.pallas.bean;

/**
 * 查询请求参数的配置信息
 * 
 * @author timmy.hu
 *
 */
public class QueryParamSetting {

	/**
	 * 参数对应的请求值类型为固定的字符串形式，例如"from":"10", "size":"100"
	 */
	public static final int FIX_VALUE_TYPE = 1;

	/**
	 * 参数对应的请求值类型为传递变量的形式，例如
	 */
	public static final int VAR_VALUE_TYPE = 2;

	/**
	 * 请求参数
	 */
	private String paramName;

	/**
	 * 请求值类型
	 */
	private int valueType;

	/**
	 * 请求值。对于valueType为固定值的情况，该value为普通的查询字符串；
	 * 对于valueType为变量的形式，该值为查询关键字，不包含${}符号
	 */
	private String value;
	
	private boolean include;
	
	public QueryParamSetting() {
		
	}

	public QueryParamSetting(String paramName, int valueType, String value) {
		this.paramName = paramName;
		this.valueType = valueType;
		this.value = value;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public int getValueType() {
		return valueType;
	}

	public void setValueType(int valueType) {
		this.valueType = valueType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * 判断是否为固定请求值类型
	 * 
	 * @return
	 */
	public boolean isFixValueType() {
		return this.valueType == QueryParamSetting.FIX_VALUE_TYPE;
	}

	/**
	 * 判断是否为变量请求值类型
	 * 
	 * @return
	 */
	public boolean isVarValueType() {
		return this.valueType == QueryParamSetting.VAR_VALUE_TYPE;
	}
	
	

	public boolean isInclude() {
		return include;
	}

	public void setInclude(boolean include) {
		this.include = include;
	}

	/**
	 * 获取需要在脚本中设置的值
	 * 
	 * 对于FIX_VALUE_TYPE类型，直接返回字面值； 对于VAR_VALUE_TYPE类型，通过${}符号将其value包起来
	 * 
	 * @return
	 */
	public String getScriptValue() {
		if (valueType == QueryParamSetting.FIX_VALUE_TYPE) {
			return value;
		}
		if (valueType == QueryParamSetting.VAR_VALUE_TYPE) {
			return String.format("${%s}", value);
		}
		return null;
	}
}