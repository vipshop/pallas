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

package com.vip.pallas.processor.plugin;

import java.io.File;

import org.apache.commons.io.FileUtils;

import com.vip.pallas.processor.plugin.AbstractPluginFileProcessor;
import com.vip.pallas.utils.PallasConsoleProperties;

public class PluginDefaultFileProcessor extends AbstractPluginFileProcessor {

    private static final File PLUGIN_DIR = new File(System.getProperty("java.io.tmpdir") + "/pallas/plugin/");

    static {
        if(!PLUGIN_DIR.exists()){
            PLUGIN_DIR.mkdirs();
        }
    }

    @Override
    public String upload(String clusterId, String pluginName, String version, File file) throws Exception {
        FileUtils.copyFile(file, new File(PLUGIN_DIR.getPath() + "/" + clusterId + "/" + pluginName + "/" + version + "/" + pluginName + "-" + version + ".zip"));
        return PallasConsoleProperties.PALLAS_CONSOLE_REST_URL + "/plugin/upgrade/fileDownload.json?clusterId=" + clusterId + "&pluginName=" + pluginName + "&pluginVersion=" + version;
    }

    @Override
    public File download(String clusterId, String pluginName, String version) throws Exception {
        File tempFile = new File(PLUGIN_DIR.getPath() + "/" + pluginName + "-" + version + ".zip");
        if(tempFile.exists()){
            FileUtils.deleteQuietly(tempFile);
        }

        FileUtils.copyFile(new File(PLUGIN_DIR.getPath() + "/" + clusterId + "/" + pluginName + "/" + version + "/" + pluginName + "-" + version + ".zip"), tempFile);
        return tempFile;
    }
}