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

package com.vip.pallas.plugin.search.script;

import java.util.Map;

/**
 * Created by jamin.li on 18/06/2017.
 */
public abstract class AbstractPallasDoubleSearchScript extends AbstractPallasSearchScript {

	public AbstractPallasDoubleSearchScript(PallasSearchScript pallasSearchScript, Map<String, Object> params) {
		super(pallasSearchScript, params);
	}

	public Object run() {
		return Double.valueOf(this.runAsDouble());
	}

	public abstract double runAsDouble();
}