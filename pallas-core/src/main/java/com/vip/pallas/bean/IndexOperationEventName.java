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

public final class IndexOperationEventName {

	public final static String CREATE_INDEX = "创建索引";
	public final static String UPDATE_INDEX = "更新索引";
	public static final String DELETE_INDEX = "删除索引";
	
	public final static String CREATE_VERSION = "创建版本";
	public final static String UPDATE_VERSION = "更新版本";
	public final static String ENABLED_VERSION = "启用版本";
	public final static String DELETE_VERSION = "删除版本";

	public final static String BEGIN_SYN = "开始同步";
	public final static String STOP_SYN = "结束同步";
	public final static String FULL = "全量";
	public final static String VDP =  "增量";
	public final static String CHECKING = "对账";
	
	public final static String CREATE_TEMPLATE = "创建模板";
	public final static String UPDATE_TEMPLATE = "更新模板";
	public final static String IMPORT_TEMPLATE = "导入模板";
	public final static String DELETE_TEMPLATE = "删除模板";

}