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

package com.vip.pallas.plugin.upgrade;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.vip.pallas.utils.PallasBasicProperties;
import org.apache.commons.io.FileUtils;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import com.vip.pallas.bean.NodeState;
import com.vip.pallas.bean.PluginActionType;
import com.vip.pallas.bean.PluginCommands;
import com.vip.pallas.bean.PluginDictionary;
import com.vip.pallas.bean.PluginStates;
import com.vip.pallas.bean.PluginType;
import com.vip.pallas.plugin.PallasPlugin;
import com.vip.pallas.plugin.helper.ClassHelper;
import com.vip.pallas.plugin.helper.FileHelper;
import com.vip.pallas.utils.IPUtils;
import com.vip.pallas.plugin.utils.PluginInitializer;
import com.vip.pallas.plugin.utils.ScriptExecutor;
import com.vip.pallas.thread.ExtendableThreadPoolExecutor;
import com.vip.pallas.thread.PallasThreadFactory;
import com.vip.pallas.thread.TaskQueue;
import com.vip.pallas.utils.ZipUtil;

public class PluginKeepaliveJob implements Job {

    private static final org.apache.logging.log4j.Logger LOGGER = ESLoggerFactory.getLogger(PluginKeepaliveJob.class);

    private static final String PALLAS_ES_RESTART_COMMAND = System.getProperty("PALLAS_ES_RESTART_COMMAND", "/apps/svr/elasticsearch/bin/last-pallas-es.sh");

    public static ExtendableThreadPoolExecutor commandExecutorService = new ExtendableThreadPoolExecutor(
            3, 10, 2L, TimeUnit.MINUTES, new TaskQueue(
            20480), new PallasThreadFactory(
            "Pallas-Plugin-Command-Thread", Thread.MAX_PRIORITY));

    static {
        //检查目录
        File file = new File(PluginDictionary.DOWNLOAD_DIR);
        if (!file.exists()) {
            if(file.mkdirs()){
                LOGGER.info("Create dir: {} successed", PluginDictionary.DOWNLOAD_DIR);
            } else {
                LOGGER.info("Create dir: {} failed, please check it!", PluginDictionary.DOWNLOAD_DIR);
            }
        }

        file = new File(PluginDictionary.PALLAS_PLUGIN_DIR);
        if (!file.exists()) {
            if(file.mkdirs()){
                LOGGER.info("Create dir: {} successed", PluginDictionary.PALLAS_PLUGIN_DIR);
            } else {
                LOGGER.info("Create dir: {} failed, please check it!", PluginDictionary.PALLAS_PLUGIN_DIR);
            }
        }
    }

    public void execute(JobExecutionContext context) {
        try {
            if(PallasPlugin.clusterName != null){
                parseCommand(PallasAPIStub.keepalive(collectState()));
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private PluginStates collectState(){
        PluginStates pluginStates = new PluginStates();

        pluginStates.setClusterId(PallasPlugin.clusterName);
        pluginStates.setNodeIp(IPUtils.localIp4Str());
        pluginStates.setNodeHost(PallasPlugin.nodeName);

        List<String> pallasPluginDirList = listDirs(PluginDictionary.PALLAS_PLUGIN_DIR);
        List<String> esPluginDirList = listDirs(PluginDictionary.ES_PLUGIN_DIR);
        List<String> downloadDirList = listFiles(PluginDictionary.DOWNLOAD_DIR);

        collectWorkingState(pluginStates, PluginType.PALLAS, pallasPluginDirList);
        collectWorkingState(pluginStates, PluginType.ES, esPluginDirList);

        collectDownloadState(pluginStates, PluginType.PALLAS, downloadDirList, pallasPluginDirList);
        collectDownloadState(pluginStates, PluginType.ES, downloadDirList, esPluginDirList);

        return pluginStates;
    }

    private void collectWorkingState(PluginStates pluginStates, PluginType pluginType, List<String> pluginDirList) {
        if (pluginDirList != null) {
            for (String dirName : pluginDirList) {
                PluginStates.Plugin plugin = new PluginStates.Plugin();
                plugin.setType(pluginType);

                String[] nameAndVersion = parseNameAndVersion(dirName);

                if (nameAndVersion != null) {
                    plugin.setName(nameAndVersion[0]);
                    plugin.setVersion(nameAndVersion[1]);

                    pluginStates.addPlugin(plugin);
                }
            }
        }
    }

    private void collectDownloadState(PluginStates pluginStates, PluginType pluginType, List<String> downloadDirList, List<String> pluginDirList){
        if(downloadDirList != null){
            for(String dirName : downloadDirList){
                boolean isNew = true;
                String[] nameAndVersion = parseNameAndVersion(dirName);

                if(nameAndVersion != null){
                    if(pluginDirList != null){
                        for(String workDirName : pluginDirList){
                            String[] workNameAndVersion = parseNameAndVersion(workDirName);
                            if(workNameAndVersion == null){
                                continue;
                            }
                            String workName = workNameAndVersion[0];
                            if(nameAndVersion[0] == workName){
                                isNew = false;
                                break;
                            }
                        }
                    }

                    if(isNew){
                        PluginStates.Plugin plugin = new PluginStates.Plugin();
                        plugin.setType(pluginType);
                        plugin.setName(nameAndVersion[0]);
                        plugin.setAvailableVersions(nameAndVersion[1]);

                        pluginStates.addPlugin(plugin);
                    }
                }
            }
        }
    }

    private String[] parseNameAndVersion(String dirName){
        String name;
        String version;

        try{
            if(dirName.endsWith(".zip")){
                dirName = dirName.substring(0, dirName.indexOf(".zip"));
            }

            if(!dirName.endsWith("-SNAPSHOT")){
                int splitIndex = dirName.lastIndexOf("-");
                if(splitIndex < 0){
                    return null;
                }
                name = dirName.substring(0, splitIndex);
                version = dirName.substring(splitIndex + 1, dirName.length());
            }else{
                dirName = dirName.substring(0, dirName.lastIndexOf("-SNAPSHOT"));
                int splitIndex = dirName.lastIndexOf("-");
                if(splitIndex < 0){
                    return null;
                }
                name = dirName.substring(0, splitIndex);
                version = dirName.substring(splitIndex + 1, dirName.length()) + "-SNAPSHOT";
            }

            return new String[]{name, version};
        } catch (Exception e){
            LOGGER.error("parseNameAndVersion error", e);
            return null;
        }
    }

    private List<String> listDirs(String path){
        File[] files = new File(path).listFiles();
        if(files != null){
            List<String> list = new ArrayList<>();

            for (File f : files){
                if(f.isDirectory()){
                    list.add(f.getName());
                }
            }
            return list;
        }
        return null;
    }

    private List<String> listFiles(String path){
        File[] files = new File(path).listFiles();
        if(files != null){
            List<String> list = new ArrayList<>();

            for (File f : files){
                if(f.isFile()){
                    list.add(f.getName());
                }
            }
            return list;
        }
        return null;
    }

    private void deleteFilesByPrefix(String path, String prefix){
        File[] files = new File(path).listFiles();
        if(files != null){
            for (File f : files){
                if(f.isFile() && f.getName().contains(prefix)){
                    FileUtils.deleteQuietly(f);
                }
            }
        }
    }

    private boolean isExistsDownloadedPackage(PluginCommands.Plugin plugin){
        List<String> packages = listFiles(PluginDictionary.DOWNLOAD_DIR);

        if(packages != null){
            return packages.contains(plugin.getZipFileName());
        }

        return false;
    }

    private void parseCommand(PluginCommands pluginCommands) {
        if(pluginCommands != null){
            List<PluginCommands.Action> actionList = pluginCommands.getActions();

            if(actionList != null){
                for (PluginCommands.Action action : actionList){

                    PluginActionType actionType = action.getActionType();

                    if(actionType == PluginActionType.DOWNLOAD){
                        for (PluginCommands.Plugin plugin : action.getPlugins()){
                            commandExecutorService.submit(() -> {
                                try {
                                    downloadPackage(pluginCommands.getClusterId(), plugin);
									LOGGER.info("download plugin: {} successed", plugin.getName());
                                } catch (Exception e) {
									LOGGER.error("download plugin: {} failed", plugin.getName(), e);
                                }
                            });
                        }
                    } else if(actionType == PluginActionType.ENABLE){
                        for (PluginCommands.Plugin plugin : action.getPlugins()){
                            commandExecutorService.submit(() -> {
                                try {
                                    enablePlugin(plugin);
									LOGGER.info("enable plugin: {} successed", plugin.getName());
                                } catch (Exception e) {
									LOGGER.error("enable plugin: {} failed", plugin.getName(), e);
                                }
                            });
                        }
                    } else if(actionType == PluginActionType.REMOVE){
                        for (PluginCommands.Plugin plugin : action.getPlugins()){
                            commandExecutorService.submit(() -> {
                                try {
                                    removePlugin(plugin);
									LOGGER.info("remove plugin: {} successed", plugin.getName());
                                } catch (Exception e) {
                                    LOGGER.error("remove plugin: {} failed", plugin.getName(), e);
                                }
                            });
                        }
                    } else if(actionType == PluginActionType.RESTART){
                        commandExecutorService.submit(() -> {
                            try {
                                PluginInitializer.postNodeState(NodeState.RESTARTING);
                                restartNode();
                            } catch (Exception e) {
                                LOGGER.error(e.toString(), e);
                            }
                        });
                    }
                }
            }
        }
    }

    private void downloadPackage(String clusterId, PluginCommands.Plugin plugin) throws Exception {
        File file = new File(plugin.getFullZipFileName());
        String downloadUrl = PallasBasicProperties.PALLAS_CONSOLE_REST_URL + "/plugin/upgrade/fileDownload.json?clusterId=" + clusterId + "&pluginName=" + plugin.getName() + "&pluginVersion=" + plugin.getVersion();
        FileUtils.copyURLToFile(new URL(downloadUrl), file, 10_000, 10_000);
    }

    private void enablePlugin(PluginCommands.Plugin plugin) {
        String pluginName = plugin.getName();

        if(isExistsDownloadedPackage(plugin)){
            //先移除原来插件
            File[] files = new File(plugin.getWorkDir()).listFiles();
            if(files != null){
                for (File file : files){
                    if(file.isDirectory() && file.getName().contains(pluginName)){
                        FileUtils.deleteQuietly(file);
                    }
                }
            }

            //再释放新的插件包
            ZipUtil.unZip(plugin.getFullZipFileName(), plugin.getWorkDir());
        }
    }

    private void removePlugin(PluginCommands.Plugin plugin) throws Exception {
        String pluginName = plugin.getName();
        String version = plugin.getVersion();

        String pluginDir = version != null && !"".equals(version) ? pluginName + "-" + version : pluginName;
        File pluginRuntimePath = new File(plugin.getWorkDir() + pluginDir);

        if(pluginRuntimePath.exists()){
            removePlugin0(plugin, pluginRuntimePath);
        }

        //把download下同名的zip也移除
        deleteFilesByPrefix(PluginDictionary.DOWNLOAD_DIR, pluginName);
    }

    private void removePlugin0(PluginCommands.Plugin plugin, File file) throws Exception {
        if(plugin.isPallasPlugin()){
            List<String> list = FileHelper.listConfigPlugins(file.getPath());

            if(list != null){
                for (String scriptName : list){
                    ClassHelper.SCRIPT_CLASS_LOADED_MAP.remove(scriptName);
                }
            }
        }

        FileUtils.deleteQuietly(file);
    }

    private void restartNode() {
		ScriptExecutor.execute(PALLAS_ES_RESTART_COMMAND);
    }
}