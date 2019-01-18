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

import com.vip.pallas.plugin.helper.ClassHelper;
import com.vip.pallas.plugin.threadlocal.ThreadMarker;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.elasticsearch.script.AbstractSearchScript;
import org.elasticsearch.search.lookup.*;

import java.io.IOException;
import java.util.Map;

public class PallasSearchScript extends AbstractSearchScript {

	private static final org.apache.logging.log4j.Logger LOGGER = ESLoggerFactory.getLogger(PallasSearchScript.class);

	private PallasExecutableScript pluginInstance;

	public PallasSearchScript(Map<String, Object> params) {
		Object _plugin = params.get("_plugin");

		if (_plugin == null) {
			throw new IllegalArgumentException("could not found param named by '_plugin' from params");
		}

		String scriptName = String.valueOf(_plugin);

		try {
			this.pluginInstance = ClassHelper.newInstance(scriptName, this, params);

			if(this.pluginInstance == null){
				throw new IllegalStateException("pluginInstance is null");
			}
		} catch (Throwable e){
			LOGGER.error("fail to new plugin instance by name: {}", scriptName, e);
			throw e;
		}
	}

	@Override
	public Object run() {
		if(this.pluginInstance != null){
			try {
				ThreadMarker.markThreadIfNecessary(Thread.currentThread());
				return pluginInstance.run();
			} catch (Throwable e) {
				LOGGER.error("execute script failed, cause: {}", e.toString(), e);
				return null;
			}
		}

		return null;
	}

	public ScriptDocValues.Strings docFieldStrings(String field) {
		return super.docFieldStrings(field);
	}

	public ScriptDocValues.Longs docFieldLongs(String field) {
		return super.docFieldLongs(field);
	}

	public ScriptDocValues.Doubles docFieldDoubles(String field) {
		return super.docFieldDoubles(field);
	}

	public IndexField getIndexField(Object key) {
		return super.indexLookup().get(key);
	}

	public float getScore() throws IOException {
		return super.score();
	}

	public LeafDocLookup getDoc() {
		return super.doc();
	}

	public SourceLookup getSource() {
		return super.source();
	}

	public LeafIndexLookup getIndexLookup() {
		return super.indexLookup();
	}

	public LeafFieldsLookup getFields() {
		return super.fields();
	}
}