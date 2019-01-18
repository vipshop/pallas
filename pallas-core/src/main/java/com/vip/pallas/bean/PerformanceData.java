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

import java.io.Serializable;

/**
 * 性能测试脚本生成中的数据源
 * 
 * @author timmy.hu
 *
 */
public class PerformanceData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 405150565144939572L;

	/**
	 * 数据源名称
	 */
	private String dataName;

	/**
	 * 参数名称定义，多个参数之间用逗号隔开
	 */
	private String paramNameDef;

	/**
	 * 文件名，不包含路径信息
	 */
	private String fileName;

	/**
	 * 真实的文件路径名称，其中包含文件所在的绝对路径
	 */
	private String realFileName;

	public PerformanceData(String dataName, String paramNameDef, String fileName, String realFileName) {
		this.dataName = dataName;
		this.paramNameDef = paramNameDef;
		this.fileName = fileName;
		this.realFileName = realFileName;
	}

	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	public String getParamNameDef() {
		return paramNameDef;
	}

	public void setParamNameDef(String paramNameDef) {
		this.paramNameDef = paramNameDef;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getRealFileName() {
		return realFileName;
	}

	public void setRealFileName(String realFileName) {
		this.realFileName = realFileName;
	}

}