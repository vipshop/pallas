package com.vip.pallas.test.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.io.IOUtils;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.IndexSettings;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static java.lang.ClassLoader.getSystemResourceAsStream;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;

/**
 *
 * 基础ES测试类，继承即获得内嵌ES前置启动
 *
 *
 */
public class BaseEsTest{

	protected static final String EMBEDDED_CLUTER_ID = "pallas-test-cluster";
	
    static {
        try {
            System.setProperty("file.encoding", "UTF-8");
            System.setProperty("embedded.elasticsearch.download.directory", getEsZipDir());
            launchES();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void launchES() throws IOException, InterruptedException {
        IndexSettings indexSettings = IndexSettings.builder()
                .withType("item", getSystemResourceAsStream("product_comment-mapping.json"))
                .withSettings(getSystemResourceAsStream("product_comment-settings.json"))
                .build();

        EmbeddedElastic embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion("5.5.2")
                .withSetting(PopularProperties.CLUSTER_NAME, EMBEDDED_CLUTER_ID)
                .withSetting(PopularProperties.HTTP_PORT, 9200)
                .withEsJavaOpts("-Xms256m -Xmx256m -Dfile.encoding=UTF-8")
                .withSetting(PopularProperties.TRANSPORT_TCP_PORT, 9300)
                .withIndex("product_comment", indexSettings)
                .withStartTimeout(120, TimeUnit.SECONDS)
                .withDownloadDirectory(new File(System.getProperty("embedded.elasticsearch.download.directory")))
                .withInstallationDirectory(new File(System.getProperty("java.io.tmpdir"), "embedded-elasticsearch-dir1"))
                .build()
                .start();

        PallasEmbeddedElastic.withTemplate("product_comment_product_comment_search", IOUtils.toString(getSystemResourceAsStream("product_comment_search")));

        JSONArray objects = JSON.parseArray(IOUtils.toString(getSystemResourceAsStream("product_comment-data.json"), UTF_8));

        embeddedElastic.index("product_comment", "item", objects.stream().map(object -> object.toString()).collect(toList()));
    }

    private static String getEsZipDir() throws IOException{
        File directory = new File("");
        String courseFile = directory.getCanonicalPath();
        Path path = Paths.get(courseFile, "../elasticsearch");
        if((new File(path + "/elasticsearch-5.5.2.zip").exists())){
            return path.toString();
        }else{
            return System.getProperty("java.io.tmpdir");
        }
    }
}




