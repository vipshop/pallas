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

import com.vip.pallas.plugin.utils.PluginLoader;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.elasticsearch.common.logging.ESLoggerFactory;

import java.io.File;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;

/**
 * 监控插件目录变化，有插件jar更新时触发
 * Created by jamin.li on 13/06/2017.
 */
public class ScriptListener extends AbstractFileListener {

    private static final org.apache.logging.log4j.Logger LOGGER = ESLoggerFactory.getLogger(ScriptListener.class);

    private static final ScheduledExecutorService PLUGIN_INIT_EXECUTOR = Executors.newScheduledThreadPool(2);

    private final static long PLUGIN_SCAN_INTERVAL = Integer.getInteger("PLUGIN_SCAN_INTERVAL",
            5000);

    public FileAlterationMonitor monitor(String path) throws Exception {
        FileAlterationObserver observer = new FileAlterationObserver(new File(path),
                FileFilterUtils.and(FileFilterUtils.fileFileFilter()), null);
        observer.addListener(this);
        FileAlterationMonitor monitor = new FileAlterationMonitor(PLUGIN_SCAN_INTERVAL, observer);
        monitor.start();
        LOGGER.info("start monitor file: {} successed", path);

        return monitor;
    }

    @Override
    public void onInit(File file) {
        LOGGER.info("onInit with file: {}", file);

        PLUGIN_INIT_EXECUTOR.schedule(()->{
            LOGGER.info("begin to load plugin with path: {}", file);

            PluginLoader.loadPlugin(file);

            LOGGER.info("loaded plugin with path: {}", file);
        }, PLUGIN_SCAN_INTERVAL, TimeUnit.MILLISECONDS);
    }

    @Override
    public void onDirectoryCreate(File file) {
    }

    @Override
    public void onDirectoryChange(File file) {
    }

    @Override
    public void onDirectoryDelete(File file) {
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
}