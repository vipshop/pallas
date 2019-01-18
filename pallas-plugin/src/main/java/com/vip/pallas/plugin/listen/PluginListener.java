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

package com.vip.pallas.plugin.listen;

import com.vip.pallas.bean.PluginDictionary;
import com.vip.pallas.plugin.helper.FileHelper;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.elasticsearch.common.logging.ESLoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 监控插件根目录变化，有插件变动（增删）时触发 Created by jamin.li on 13/06/2017.
 */
public class PluginListener extends AbstractFileListener {

	private static final org.apache.logging.log4j.Logger LOGGER = ESLoggerFactory.getLogger(PluginListener.class);

	private final static int PLUGIN_SCAN_INTERVAL = Integer.getInteger("PLUGIN_SCAN_INTERVAL",
			15000);

	// pluginPath e.g /apps/dat/web/working/pallas-es-public-plugins/goods-rank-es-2.0.8
	private static final Map<String /** pluginPath **/, FileAlterationMonitor> PLUGIN_MONITOR_MAP = new ConcurrentHashMap<>();

	static {
		if (!PluginDictionary.PALLAS_PLUGIN_DIR.endsWith("/")) {
			PluginDictionary.PALLAS_PLUGIN_DIR += "/";
		}
	}

	public FileAlterationMonitor monitor() {
		FileAlterationObserver observer = new FileAlterationObserver(new File(PluginDictionary.PALLAS_PLUGIN_DIR),
				FileFilterUtils.and(FileFilterUtils.directoryFileFilter()), null);
		observer.addListener(this);
		FileAlterationMonitor monitor = new FileAlterationMonitor(PLUGIN_SCAN_INTERVAL, observer);
		try {
			monitor.start();
			LOGGER.info("start monitor file: {} successed", PluginDictionary.PALLAS_PLUGIN_DIR);
		} catch (Exception e) {
			LOGGER.error("start monitor file: {} failed", PluginDictionary.PALLAS_PLUGIN_DIR, e);
		}

		return monitor;
	}

	@Override
	public void onInit(File file) {
		LOGGER.info("onInit with file: {}", file);
		try {
			List<File> files = FileHelper.listSubDir(file.getPath());
			if (files != null && files.size() > 0) {
				for (File f : files) {
					String path = f.getPath();
					PLUGIN_MONITOR_MAP.put(path, new ScriptListener().monitor(path));
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
		}
	}

	@Override
	public void onDirectoryCreate(File file) {
		if (!isRootPath(file)) {
			return;
		}

		LOGGER.info("onDirectoryCreate with file: {}", file);

		String path = file.getPath();
		try {
			PLUGIN_MONITOR_MAP.put(path, new ScriptListener().monitor(path));
		} catch (Exception e) {
			LOGGER.error(e.toString(), e);
		}
	}

	@Override
	public void onDirectoryChange(File file) {
	}

	@Override
	public void onDirectoryDelete(File file) {
		if (!isRootPath(file)) {
			return;
		}

		LOGGER.info("onDirectoryDelete with file: {}", file);

		String path = file.getPath();
		FileAlterationMonitor monitor = PLUGIN_MONITOR_MAP.get(path);
		if (monitor != null) {
			try {
				monitor.stop();
				PLUGIN_MONITOR_MAP.remove(path);
				LOGGER.info("FileAlterationMonitor with path: {} had stopped", path);
			} catch (Exception e) {
				LOGGER.error(e.toString(), e);
			}
		}
	}

	@Override
	public void onFileCreate(File file) {
	}

	@Override
	public void onFileChange(File file) {
	}

	@Override
	public void onFileDelete(File file) {
	}

	@Override
	public void onStop(FileAlterationObserver fileAlterationObserver) {
	}

	private boolean isRootPath(File file) {
		return !file.getPath().substring(PluginDictionary.PALLAS_PLUGIN_DIR.length() + 1, file.getPath().length()).contains("/");
	}
}