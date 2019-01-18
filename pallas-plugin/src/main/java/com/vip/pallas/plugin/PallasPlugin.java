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

package com.vip.pallas.plugin;

import com.vip.pallas.plugin.search.factory.PallasScoreScriptFactory;
import com.vip.pallas.plugin.search.factory.PallasScriptFactory;
import com.vip.pallas.plugin.search.script.similarity.VipSearchSimilarityProvider;
import com.vip.pallas.plugin.utils.PluginInitializer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexModule;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.ScriptPlugin;
import org.elasticsearch.script.NativeScriptFactory;
import org.elasticsearch.script.ScriptEngineService;

import java.util.ArrayList;
import java.util.List;

public class PallasPlugin extends Plugin implements ScriptPlugin {

	public static String clusterName;
	public static String nodeName;
	public static String logsPath;

	@Override
	public List<NativeScriptFactory> getNativeScripts() {
		ArrayList<NativeScriptFactory> list = new ArrayList<>();

		list.add(new PallasScriptFactory());
		list.add(new PallasScoreScriptFactory());

		return list;
	}

	@Override
	public ScriptEngineService getScriptEngineService(Settings settings) {
		clusterName = settings.get("cluster.name");
		nodeName = settings.get("node.name");
		logsPath = settings.get("path.logs");

		PluginInitializer.init();

		return null;
	}

	@Override
	public void onIndexModule(IndexModule indexModule) {
		indexModule.addSimilarity("vip-search-similarity", VipSearchSimilarityProvider::new);
	}
}