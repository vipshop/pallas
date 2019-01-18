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

package com.vip.pallas.test.core.open;

import com.google.common.net.HostAndPort;
import com.vip.pallas.bean.PerformanceData;
import com.vip.pallas.bean.QueryParamSetting;
import com.vip.pallas.mybatis.entity.SearchTemplate;
import com.vip.pallas.service.PerformanceScriptService;
import com.vip.pallas.service.SearchTemplateService;
import com.vip.pallas.test.base.BaseSpringEsTest;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class PerformanceScriptServiceImplTest extends BaseSpringEsTest {

	@Autowired
	private PerformanceScriptService performanceScriptService;

	@Autowired
	private SearchTemplateService searchTemplateService;

	private static String templateName = "product_comment_product_comment_search";

	private static Long indexId = 1l;
	private static String indexName = "product_comment";

	private static String fromParam = "from";

	private static String sizeParam = "size";

	private static String keywordParam = "keyword";

	private static String fieldsParam = "fields";

	private static String orderbyParam = "orderby";

	private static String listParam = "list";

	private static String channelIdParam = "channel_id";

	private static String notExistParam = "notExist";

	private static String paramsKeyName = "params";

	private static String[] serverNodes = new String[] { "127.0.0.1:9200"};

	@Before
	public void init() throws Exception {
		String content = getResourceContent("performancescript/performance_search_template.txt");
		searchTemplateService.delateByNameAndIndexId(templateName, indexId);
		SearchTemplate t = new SearchTemplate();
		t.setTemplateName(templateName);
		t.setIndexId(indexId);
		t.setType(SearchTemplate.TYPE_TEMPLATE);
		t.setContent(content);
		searchTemplateService.insert(t);
	}

	@Test
	public void testGetQueryParamNames() throws Exception {
		SearchTemplate template = searchTemplateService.findByNameAndIndexId(templateName, indexId);
		List<Map<String, String>> paramNameMaps = performanceScriptService.getQueryParamNames(template);
		assertThat(paramNameMaps).isNotEmpty();
		Map<String, String> paramNameMap = paramNameMaps.get(0);
		assertThat(paramNameMap).containsKey("paramName");
		assertThat(paramNameMap.size()).isEqualTo(1);

		List<String> paramNames = getParamNames(paramNameMaps);
		assertThat(paramNames).contains(fromParam, sizeParam);
		assertThat(paramNames.contains(orderbyParam)).isFalse();
	}

	private List<String> getParamNames(List<Map<String, String>> paramNameMaps) {
		List<String> result = new ArrayList<String>();
		for (Map<String, String> paramNameMap : paramNameMaps) {
			String paramName = paramNameMap.get("paramName");
			result.add(paramName);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGenSendQueryValues() {
		SearchTemplate template = searchTemplateService.findByNameAndIndexId(templateName, indexId);
		List<QueryParamSetting> paramSettings = initParamSettings();
		Map<String, Object> queryBody = performanceScriptService.genSendQueryBody(template, paramSettings);
		String idParamValue = (String) queryBody.get("id");
		assertThat(idParamValue).isEqualTo(indexName + "_" + template.getTemplateName());
		Map<String, Object> queryValues = (Map<String, Object>) queryBody.get(paramsKeyName);
		assertThat(queryValues.get(fromParam)).isEqualTo("10");
		assertThat(queryValues.get(sizeParam)).isEqualTo("100");
		Map<String, Object> fieldsMap = (Map<String, Object>) queryValues.get(fieldsParam);
		assertThat(fieldsMap.get(listParam)).isEqualTo("[\"text\"]");
		assertThat(fieldsMap.containsKey(notExistParam)).isFalse();
	}

	@Test
	public void testGenJmxScript() throws Exception {
		SearchTemplate template = searchTemplateService.findByNameAndIndexId(templateName, indexId);
		List<QueryParamSetting> paramSettings = initParamSettings();
		List<PerformanceData> pds = initPds();
		List<List<QueryParamSetting>> paramSettingsList = new ArrayList<List<QueryParamSetting>>();
		paramSettingsList.add(paramSettings);
		String jmxScript = performanceScriptService.genJmxScript(template, pds, paramSettingsList);
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document document = db.parse(new ByteArrayInputStream(jmxScript.getBytes("UTF-8")));
		Element rootEle = document.getDocumentElement();
		assertThat(rootEle.getTagName()).isEqualTo("jmeterTestPlan");
		NodeList csvDataChildNodes = rootEle.getElementsByTagName("CSVDataSet");
		assertThat(csvDataChildNodes.getLength()).isEqualTo(pds.size() + 1);
		NodeList httpSampleChildNodes = rootEle.getElementsByTagName("HTTPSamplerProxy");
		assertThat(httpSampleChildNodes.getLength()).isEqualTo(paramSettingsList.size());
	}

	@Test
	public void testZipFile() throws Exception {
		String testname = "testname";
		String zipFileName = "performance_test.zip";
		SearchTemplate template = searchTemplateService.findByNameAndIndexId(templateName, indexId);
		List<QueryParamSetting> paramSettings = initParamSettings();
		List<PerformanceData> pds = initPds();
		List<HostAndPort> hps = initHps();
		List<List<QueryParamSetting>> paramSettingsList = new ArrayList<List<QueryParamSetting>>();
		paramSettingsList.add(paramSettings);
		String jmxScript = performanceScriptService.genJmxScript(template, pds, paramSettingsList);
		String tmpDir = System.getProperty("java.io.tmpdir");
		File targetZipFile = new File(tmpDir, zipFileName);
		FileOutputStream os = new FileOutputStream(targetZipFile);
		try {
			performanceScriptService.zipFiles(testname, jmxScript, hps, pds, os);
			check(testname, pds, jmxScript, targetZipFile);
		} finally {
			FileUtils.deleteQuietly(targetZipFile);
		}
	}

	private List<HostAndPort> initHps() {
		List<HostAndPort> hps = new ArrayList<HostAndPort>();
		for (String serverNodeStr : serverNodes) {
			HostAndPort hp = HostAndPort.fromString(serverNodeStr);
			hps.add(hp);
		}
		return hps;
	}

	private void check(String testname, List<PerformanceData> pds, String jmxScript, File targetZipFile)
			throws FileNotFoundException, IOException {
		FileInputStream is = new FileInputStream(targetZipFile);
		ZipInputStream zis = new ZipInputStream(is);

		try {
			int fileSize = 0;
			ZipEntry entry = null;
			while ((entry = zis.getNextEntry()) != null) {
				fileSize++;
				String entryName = entry.getName();
				assertThat(entryName).isNotEmpty();
				if (entryName.equals(testname + ".jmx")) {
					String content = IOUtils.toString(zis);
					assertThat(content).isEqualTo(jmxScript);
				}
			}
			assertThat(fileSize).isEqualTo(pds.size() + 2);
		} finally {
			IOUtils.closeQuietly(zis);
		}
	}

	private List<QueryParamSetting> initParamSettings() {
		List<QueryParamSetting> result = new ArrayList<QueryParamSetting>();
		QueryParamSetting from = new QueryParamSetting(fromParam, QueryParamSetting.FIX_VALUE_TYPE, "10");
		QueryParamSetting size = new QueryParamSetting(sizeParam, QueryParamSetting.FIX_VALUE_TYPE, "100");
		QueryParamSetting keyword = new QueryParamSetting(keywordParam, QueryParamSetting.VAR_VALUE_TYPE, keywordParam);
		QueryParamSetting fields = new QueryParamSetting(fieldsParam, QueryParamSetting.FIX_VALUE_TYPE, "[\"text\"]");
		QueryParamSetting channelId = new QueryParamSetting(channelIdParam, QueryParamSetting.VAR_VALUE_TYPE,
				channelIdParam);
		QueryParamSetting notExist = new QueryParamSetting(notExistParam, QueryParamSetting.VAR_VALUE_TYPE,
				notExistParam);
		result.add(from);
		result.add(size);
		result.add(keyword);
		result.add(channelId);
		result.add(fields);
		result.add(notExist);
		return result;
	}

	private List<PerformanceData> initPds() {
		List<PerformanceData> result = new ArrayList<PerformanceData>();
		URL channelUrl = this.getClass().getClassLoader().getResource("performancescript/channel_id.csv");
		URL keywordUrl = this.getClass().getClassLoader().getResource("performancescript/keyword_warehouse.csv");
		String channelPath = channelUrl.getPath();
		if (channelPath.contains(":")) {
			channelPath = channelPath.substring(1, channelPath.length());
		}
		String keywordPath = keywordUrl.getPath();
		if (keywordPath.contains(":")) {
			keywordPath = keywordPath.substring(1, keywordPath.length());
		}
		PerformanceData pd1 = new PerformanceData("keyword", "keyword,warehouse", "keyword_warehouse.csv",
				keywordPath);
		PerformanceData pd2 = new PerformanceData("channel_id", "channel_id", "channel_id.csv", channelPath);
		result.add(pd1);
		result.add(pd2);
		return result;
	}

	
	public void close() throws Exception {
		searchTemplateService.delateByNameAndIndexId(templateName, indexId);
	}

    protected String getResourceContent(String resourcePath) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(resourcePath);
        try {
            return IOUtils.toString(is, Charsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(is);
        }
        return null;
    }
}