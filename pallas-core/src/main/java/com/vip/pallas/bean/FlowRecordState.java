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

public enum FlowRecordState {

	PENDING("PENDING", (byte)0), //就绪
	RECORDING("RECORDING", (byte)1), //正在记录
	FINISH("FINISH", (byte)2), //已完成
	STOP("STOP", (byte)3), //已终止
	END("END", (byte)4); //已结束，时间到期

	private String desc;
	private byte value;

	FlowRecordState(String desc, byte value) {
		this.desc = desc;
		this.value = value;
	}

	public String getDesc() {
		return this.desc;
	}

	public byte getValue() {
		return value;
	}
}