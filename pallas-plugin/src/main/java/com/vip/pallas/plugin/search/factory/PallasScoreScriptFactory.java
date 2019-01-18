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

package com.vip.pallas.plugin.search.factory;

import com.vip.pallas.plugin.search.script.PallasSearchScript;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.script.ExecutableScript;
import org.elasticsearch.script.NativeScriptFactory;

import java.util.Map;

public class PallasScoreScriptFactory implements NativeScriptFactory {

	@Override
	public ExecutableScript newScript(@Nullable Map<String, Object> params) {
		return new PallasSearchScript(params);
	}

	@Override
	public boolean needsScores() {
		return true;
	}

	@Override
	public String getName() {
		return "pallas_score";
	}
}