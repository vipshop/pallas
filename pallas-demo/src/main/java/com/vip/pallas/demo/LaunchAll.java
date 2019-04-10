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

package com.vip.pallas.demo;

import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.vip.pallas.demo.es.PallasEmbeddedElastic;
import com.vip.pallas.demo.jetty.BrowseServer;
import com.vip.pallas.demo.jetty.EmbeddedJettyServer;

import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.IndexSettings;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

public class LaunchAll {

    public static void main(String[] args) throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("pallas.stdout", "true");
        System.setProperty("pallas.db.type", "h2");
        System.setProperty("spring.profiles.active", "demo");
        System.setProperty("pallas.console.rest.url", "http://localhost:8080/pallas");
        System.setProperty("pallas.console.upload_url", "http://localhost:8080/pallas/ss/upsert.json");
        System.setProperty("embedded.elasticsearch.download.directory", getDownloadDir());
        System.setProperty("pallas.flow.record.save.cluster.name", "localhost");
        System.setProperty("pallas.flow.record.save.cluster.rest.address", "localhost:9200,localhost:9210,localhost:9220");
        System.setProperty("pallas.flow.record.save.cluster.transport.address", "localhost:9300,localhost:9310,localhost:9320");
        System.setProperty("pool.name", "poo1,poo2,pool3");
        ClassLoader classLoader = LaunchAll.class.getClassLoader();
        Class<?> loadClass = classLoader.loadClass("com.vip.pallas.demo.Launcher");
        Method method = loadClass.getMethod("main", String[].class);
        method.invoke(null, new Object[] { args });
    }

    private static String getDownloadDir(){
        String elasticFilePath = LaunchAll.class.getClassLoader().getResource("elasticsearch").getPath();
        File file = new File(elasticFilePath + "/elasticsearch-5.5.2.zip");

        if((file.exists())){
            return elasticFilePath;
        }else{
            Pattern pattern = Pattern.compile("file:(.*)/pallas-demo.jar!/elasticsearch");
            Matcher matcher = pattern.matcher(elasticFilePath);

            if (matcher.find()) {
                return matcher.group(1) + "/elasticsearch";
            }
        }
        return System.getProperty("java.io.tmpdir");
    }
}

class Launcher{
    private static final Logger LOGGER = LoggerFactory.getLogger(LaunchAll.class);

    private static Desktop desktop = Desktop.getDesktop();

    public static void main(String[] args) throws Exception {
        launchES();
		LOGGER.info("start es successfully.");

        launchConsole(args);
		LOGGER.info("start pallas-console successfully.");

        browse();

        launchSearch(args);
		LOGGER.info("start pallas-search successfully.");
    }

    private static void browse(){
        URI uri = URI.create("http://127.0.0.1:8081/");
        LOGGER.info("[Browsing][" + uri + "]");
        BrowseServer.browseUri(desktop, uri);
        LOGGER.info("[Browsed]");
    }

    public static void launchES() throws IOException, InterruptedException {
        IndexSettings indexSettings = IndexSettings.builder()
                .withType("item", getSystemResourceAsStream("product_comment-mapping.json"))
                .withSettings(getSystemResourceAsStream("product_comment-settings.json"))
                .build();

        new Thread(() -> {
            try{
                EmbeddedElastic.builder()
                    .withElasticVersion("5.5.2")
                    .withSetting(PopularProperties.CLUSTER_NAME, "pallas-test-cluster")
                    .withSetting(PopularProperties.HTTP_PORT, 9200)
                    .withSetting(PopularProperties.TRANSPORT_TCP_PORT, 9300)
                    .withSetting("discovery.zen.ping.unicast.hosts", "127.0.0.1:9300, 127.0.0.1:9310, 127.0.0.1:9320")
                    .withSetting("discovery.zen.minimum_master_nodes", "2")
                    .withSetting("cluster.routing.allocation.disk.threshold_enabled", false)
                    .withEsJavaOpts("-Xms256m -Xmx256m -Dfile.encoding=UTF-8")
                    .withStartTimeout(30, TimeUnit.SECONDS)
                    .withDownloadDirectory(new File(System.getProperty("embedded.elasticsearch.download.directory")))
                    .withInstallationDirectory(new File(System.getProperty("java.io.tmpdir"), "embedded-elasticsearch-dir2"))
                    .build()
                    .start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try{
                EmbeddedElastic.builder()
                    .withElasticVersion("5.5.2")
                    .withSetting(PopularProperties.CLUSTER_NAME, "pallas-test-cluster")
                    .withSetting(PopularProperties.HTTP_PORT, 9210)
                    .withSetting(PopularProperties.TRANSPORT_TCP_PORT, 9310)
                    .withSetting("discovery.zen.ping.unicast.hosts", "127.0.0.1:9300, 127.0.0.1:9310, 127.0.0.1:9320")
                    .withSetting("discovery.zen.minimum_master_nodes", "2")
                    .withSetting("cluster.routing.allocation.disk.threshold_enabled", false)
                    .withEsJavaOpts("-Xms256m -Xmx256m -Dfile.encoding=UTF-8")
                    .withStartTimeout(30, TimeUnit.SECONDS)
                    .withDownloadDirectory(new File(System.getProperty("embedded.elasticsearch.download.directory")))
                    .withInstallationDirectory(new File(System.getProperty("java.io.tmpdir"), "embedded-elasticsearch-dir3"))
                    .build()
                    .start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }).start();

        EmbeddedElastic embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion("5.5.2")
                .withSetting(PopularProperties.CLUSTER_NAME, "pallas-test-cluster")
                .withSetting(PopularProperties.HTTP_PORT, 9220)
                .withSetting(PopularProperties.TRANSPORT_TCP_PORT, 9320)
                .withSetting("discovery.zen.ping.unicast.hosts", "127.0.0.1:9300, 127.0.0.1:9310, 127.0.0.1:9320")
                .withSetting("discovery.zen.minimum_master_nodes", "2")
                .withSetting("cluster.routing.allocation.disk.threshold_enabled", false)
                .withEsJavaOpts("-Xms256m -Xmx256m -Dfile.encoding=UTF-8")
                .withIndex("product_comment", indexSettings)
                .withStartTimeout(30, TimeUnit.SECONDS)
                .withDownloadDirectory(new File(System.getProperty("embedded.elasticsearch.download.directory")))
                .withInstallationDirectory(new File(System.getProperty("java.io.tmpdir"), "embedded-elasticsearch-dir1"))
                .build()
                .start();

        PallasEmbeddedElastic.withTemplate("product_comment_product_comment_search", IOUtils.toString(getSystemResourceAsStream("product_comment_search")));

        JSONArray objects = JSON.parseArray(IOUtils.toString(getSystemResourceAsStream("product_comment-data.json"), UTF_8));
        embeddedElastic.index("product_comment", "item", objects.stream().map(object -> object.toString()).collect(toList()));
    }

    private static void launchSearch(String[] args) throws NoSuchMethodException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        ClassLoader classLoader = LaunchAll.class.getClassLoader();
        Class<?> loadClass = classLoader.loadClass("com.vip.pallas.search.launch.Startup");
        Method method = loadClass.getMethod("main", String[].class);
        method.invoke(null, new Object[] { args });
    }

    private static void launchConsole(String[] args) throws InvocationTargetException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException {
        ClassLoader classLoader = LaunchAll.class.getClassLoader();
        Class<?> loadClass = classLoader.loadClass("com.vip.pallas.console.ConsoleApplication");
        Method method = loadClass.getMethod("main", String[].class);
        method.invoke(null, new Object[] { args });

        launchConsoleUi();
    }

    private static void launchConsoleUi() {
        new Thread(() -> {
            try {
                new EmbeddedJettyServer().start();
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }).start();
    }
}