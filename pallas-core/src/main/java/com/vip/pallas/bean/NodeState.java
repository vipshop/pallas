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

public enum NodeState {

	TO_BE_RESTART("TO_BE_RESTART", (byte)1),
	RESTARTING("RESTARTING", (byte)2),
	STARTED("STARTED", (byte)3),
	HEALTHY("HEALTHY", (byte)4);

	private String desc;
	private byte value;

	NodeState(String desc, byte value) {
		this.desc = desc;
		this.value = value;
	}

	public String getDesc() {
		return this.desc;
	}

	public byte getValue() {
		return value;
	}

	public static NodeState getNodeStateByValue(byte value){
		for(NodeState nodeState : NodeState.values()){
			if(nodeState.value == value){
				return nodeState;
			}
		}
		return null;
	}
}