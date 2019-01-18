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

package ${package}.demo.manager;

import ch.qos.logback.classic.LoggerContext;
import ${package}.demo.source.RunWithResSource;
import com.vip.pallas.plugin.search.script.PluginManager;
import net.openhft.chronicle.core.pool.ClassAliasPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchPluginManager implements PluginManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchPluginManager.class);

	@Override
	public void init() {
		RunWithResSource.POOL = RunWithResSource.getPool();
	}

	@Override
	public void destroy() {
		if(!RunWithResSource.POOL.isClosed()){
			RunWithResSource.POOL.destroy();
		}

		try {
			ClassAliasPool.CLASS_ALIASES.clean();
			LOGGER.info("ClassAliasPool cleaned");
		} catch (Throwable e) {
			LOGGER.error(e.toString(), e);
		}
		try {
			if (LoggerFactory.getILoggerFactory() instanceof LoggerContext) {
				((LoggerContext) LoggerFactory.getILoggerFactory()).stop();
			}
		} catch (Throwable e) {
			LOGGER.error(e.toString(), e);
		}

	}
}