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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vip.pallas.utils.JsonUtil;

public class EsAliases {
	
	private List<Map<String, Map<String, String>>> actions;

	/*
	public static void main(String[] args) throws Exception {
		EsAliases esAliases = new EsAliases(); 
		List<Map<String, Map<String, String>>> actions = new ArrayList<Map<String, Map<String, String>>>(2);
		
		Map<String, Map<String, String>> removeMap0 = new HashMap<String, Map<String, String>>();
		Map<String, Map<String, String>> addMap0 = new HashMap<String, Map<String, String>>();
		
		Map<String, String> removeMap = new HashMap<String, String>();
		Map<String, String> addMap = new HashMap<String, String>();
		
		removeMap.put("index", "vsearch11");
		removeMap.put("alias", "msearch");
		
		addMap.put("index", "vsearch12");
		addMap.put("alias", "msearch");
		
		removeMap0.put("remove", removeMap);
		addMap0.put("add", addMap);
		
		actions.add(removeMap0);
		actions.add(addMap0);
		
		esAliases.setActions(actions);
		System.out.println(JsonUtil.toJson(esAliases));
	}*/



	public List<Map<String, Map<String, String>>> getActions() {
		return actions;
	}



	public void setActions(List<Map<String, Map<String, String>>> actions) {
		this.actions = actions;
	}

}