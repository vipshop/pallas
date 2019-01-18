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

public class PluginDictionary {

    public static String DOWNLOAD_DIR = System.getProperty("PLUGIN_DOWNLOAD_DIR","/apps/dat/web/working/pallas-es-public-plugins-download");

    public static String PALLAS_PLUGIN_DIR = System.getProperty("PALLAS_PLUGIN_DIR","/apps/dat/web/working/pallas-es-public-plugins");

    public static String ES_PLUGIN_DIR = System.getProperty("ES_PLUGIN_DIR","/apps/svr/elasticsearch/plugins");
}