package com.vip.pallas.test.console.api.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.api.Assertions;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.http.HttpStatus;

import com.alibaba.fastjson.JSONObject;
import com.vip.pallas.bean.monitor.MonitorLevelModel;
import com.vip.pallas.console.vo.ClusterVO;
import com.vip.pallas.console.vo.PageResultVO;
import com.vip.pallas.console.vo.ResultVO;
import com.vip.pallas.console.vo.TokenPrivilegeVO;
import com.vip.pallas.mybatis.entity.Cluster;
import com.vip.pallas.mybatis.entity.SearchAuthorization;
import com.vip.pallas.mybatis.entity.SearchAuthorization.AuthorizationItem;
import com.vip.pallas.mybatis.entity.SearchAuthorization.Pool;
import com.vip.pallas.mybatis.entity.SearchServer;
import com.vip.pallas.test.base.BaseSpringEsTest;
import com.vip.pallas.test.base.ConstantValue;
import com.vip.pallas.utils.JsonUtil;
import com.vip.vjtools.vjkit.collection.ListUtil;
import com.vip.vjtools.vjkit.collection.SetUtil;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ServerApiControllerTest extends BaseSpringEsTest {

	private static final String QUERY_PS_PARAMS = "{\"token\":\"%s\", \"ip\":\"%s\"}";
	private static Cluster cluster = null;
	private static SearchAuthorization searchAuthorization = null;
	private static String psCluster1 = "pallas-search-test-pool-1";
	private static String psCluster2 = "pallas-search-test-pool-2";
	
	private static ClusterVO prepareMultiPsClusterVo() {
		ClusterVO clusterVO = new ClusterVO();
		clusterVO.setClusterId("es-cluster-test-pool");
		clusterVO.setDescription("es cluster test");
		clusterVO.setHttpAddress(ConstantValue.serverHttpAddress);
		clusterVO.setClientAddress(ConstantValue.clintHttpAddress);
		clusterVO.setRealClusters(null);
		clusterVO.setAccessiblePs(psCluster1 + "," + psCluster2);
		clusterVO.setMonitorLevelModel(MonitorLevelModel.getDefaultModel());
		return clusterVO;
	}
	
	private static void prepareMultiPsCluster() throws Exception {
		ClusterVO clusterVO = prepareMultiPsClusterVo();
		Assertions.assertThat(callRestApi("/cluster/add.json", JsonUtil.toJson(clusterVO))).isNull();
		
		ResultVO resultVO = callGetApi("/cluster/id.json?clusterId=" + clusterVO.getClusterId(), Cluster.class);
		Assertions.assertThat(resultVO.getStatus()).isEqualTo(HttpStatus.OK.value());
		cluster = (Cluster)resultVO.getData();
 		Assertions.assertThat(cluster.getId()).isNotNull();
	}
	
	private static void prepareToken() throws Exception {
		SearchAuthorization token = new SearchAuthorization();
		token.setClientToken("R7nzZl0tC0ioNR7d0JMAPA==");
		token.setTitle("this is pool test token");
		token.setEnabled(true);

		assertThat(callRestApi("/token/insert.json", JsonUtil.toJson(token))).isNull();

		Map<String, List<JSONObject>> resultMap = callGetApi("/token/list.json");
		assertThat(!resultMap.get("data").isEmpty());
		List<JSONObject> data = resultMap.get("data");
		Assert.assertTrue(CollectionUtils.isNotEmpty(data));
		List<SearchAuthorization> saList = JsonUtil.readValue(JsonUtil.toJson(data),
				new TypeReference<List<SearchAuthorization>>() {});
		for (SearchAuthorization sa : saList) {
			if (token.getClientToken().equals(sa.getClientToken())) {
				searchAuthorization = sa;
				break;
			}
		}
		Assert.assertNotNull(searchAuthorization);
	}
	
	private static void prepareAuth(Set<Pool> pools) throws Exception {
		TokenPrivilegeVO tokenPrivilege = new TokenPrivilegeVO();
		tokenPrivilege.setId(searchAuthorization.getId());
		AuthorizationItem clusterAuthItem = new AuthorizationItem();
		clusterAuthItem.setId(cluster.getId());
		clusterAuthItem.setName(cluster.getClusterId());
		clusterAuthItem.setPools(pools);
		clusterAuthItem.setIndexPrivileges(Collections.emptyList());
		tokenPrivilege.setAuthorizationItems(ListUtil.newArrayList(clusterAuthItem));
		callRestApi("/token/token_privilege/update.json", JsonUtil.toJson(tokenPrivilege));
	}
	
	@Before
	public synchronized void prepareData() throws Exception {
		if (null == cluster) {
			prepareMultiPsCluster();
			prepareToken();
		}
	}

	private String buildSearchServerJson(String cluster, String ipport, String pool, Boolean takeTraffic, Object info)
			throws Exception {
		com.vip.pallas.search.model.SearchServer searchServer = new com.vip.pallas.search.model.SearchServer(cluster,
				ipport, pool, takeTraffic, info);
		return JsonUtil.toJson(searchServer);
	}
	
	@Test
	public void test01QueryPs4RealES() throws Exception {
		prepareAuth(null);
		String param = String.format(QUERY_PS_PARAMS, searchAuthorization.getClientToken(), "127.0.0.1");
		String resutStr = callRestApiAndReturnString("/ss/query_pslist_and_domain.json", param);
		Assert.assertTrue(resutStr.contains(cluster.getClusterId() + ":9200"));
	}
	
	@Test
	public void test02QueryPs4DefaultSameDcPsServer() throws Exception {
		prepareAuth(null);
		String psIpprot1 = "129.0.0.1:8081";
		callRestApi("/ss/upsert.json", buildSearchServerJson(psCluster1, psIpprot1, "", null, new JSONObject()));
		
		String psIpprot2 = "129.1.0.1:8081";
		callRestApi("/ss/upsert.json", buildSearchServerJson(psCluster2, psIpprot2, "", null, new JSONObject()));
		
		String param = String.format(QUERY_PS_PARAMS, searchAuthorization.getClientToken(), psIpprot1);
		String resutStr = callRestApiAndReturnString("/ss/query_pslist_and_domain.json", param);
		Assert.assertTrue(resutStr.contains(psIpprot1));
		Assert.assertFalse(resutStr.contains(psIpprot2));
	}
	
	@Test
	public void test03SearchUpsert() throws Exception {
		String psIpprot = "127.0.0.1:8081";
		callRestApi("/ss/upsert.json", buildSearchServerJson(psCluster1, psIpprot, "", null, new JSONObject()));
		
		ResultVO<PageResultVO> resultVo = callGetApi("/ss/find.json?selectedCluster=" + psCluster1, PageResultVO.class);
		Assert.assertNotNull(resultVo.getData());
		List pageResultList = resultVo.getData().getList();
		Assert.assertTrue(CollectionUtils.isNotEmpty(pageResultList));
		List<SearchServer> result = JsonUtil.readValue(JsonUtil.toJson(pageResultList),
				new TypeReference<List<SearchServer>>() {});
		Assert.assertTrue(CollectionUtils.isNotEmpty(result));
	}
	
	@Test
	public void test04QueryPs4DefaultPsServer() throws Exception {
		prepareAuth(null);
		String psIpprot = "128.0.0.1:8081";
		callRestApi("/ss/upsert.json", buildSearchServerJson(psCluster1, psIpprot, "", null, new JSONObject()));
		
		String param = String.format(QUERY_PS_PARAMS, searchAuthorization.getClientToken(), psIpprot);
		String resutStr = callRestApiAndReturnString("/ss/query_pslist_and_domain.json", param);
		Assert.assertTrue(resutStr.contains(psIpprot));
	}
	
	@Test
	public void test05QueryPs4PoolPsServer() throws Exception {
		String psPool1 = "pool-130-1";
		prepareAuth(SetUtil.newHashSet(new Pool(psPool1, psCluster1)));
		String psIpprot1 = "130.0.0.1:8081";
		callRestApi("/ss/upsert.json", buildSearchServerJson(psCluster1, psIpprot1, psPool1, null, new JSONObject()));
		
		String psIpprot2 = "130.0.0.2:8081";
		callRestApi("/ss/upsert.json", buildSearchServerJson(psCluster2, psIpprot2, "", null, new JSONObject()));
		
		String param = String.format(QUERY_PS_PARAMS, searchAuthorization.getClientToken(), psIpprot1);
		String resutStr = callRestApiAndReturnString("/ss/query_pslist_and_domain.json", param);
		Assert.assertTrue(resutStr.contains(psIpprot1));
		Assert.assertFalse(resutStr.contains(psIpprot2));
	}
	
	@Test
	public void test06QueryPs4PoolSameDcPsServer() throws Exception {
		String psPool1 = "pool-131-1";
		String psPool2 = "pool-131-2";
		prepareAuth(SetUtil.newHashSet(new Pool(psPool1, psCluster1), new Pool(psPool2, psCluster1)));
		String psIpprot1 = "131.0.0.1:8081";
		callRestApi("/ss/upsert.json", buildSearchServerJson(psCluster1, psIpprot1, psPool1, null, new JSONObject()));
		
		String psIpprot2 = "131.1.0.1:8081";
		callRestApi("/ss/upsert.json", buildSearchServerJson(psCluster2, psIpprot2, "", null, new JSONObject()));
		
		String param = String.format(QUERY_PS_PARAMS, searchAuthorization.getClientToken(), psIpprot1);
		String resutStr = callRestApiAndReturnString("/ss/query_pslist_and_domain.json", param);
		Assert.assertTrue(resutStr.contains(psIpprot1));
		Assert.assertFalse(resutStr.contains(psIpprot2));
	}
	
	@Test
	public void test07QueryPs4KillPoolReturnDefault() throws Exception {
		String psPool1 = "pool-132-1";
		prepareAuth(SetUtil.newHashSet(new Pool(psPool1, psCluster1)));
		String psIpprot1 = "132.0.0.1:8081";
		// callRestApi("/ss/upsert.json", buildSearchServerJson(psCluster1, psIpprot1, psPool1, null, new JSONObject()));
		
		String psIpprot2 = "132.0.0.2:8081";
		callRestApi("/ss/upsert.json", buildSearchServerJson(psCluster1, psIpprot2, "", null, new JSONObject()));
		
		String param = String.format(QUERY_PS_PARAMS, searchAuthorization.getClientToken(), psIpprot1);
		String resutStr = callRestApiAndReturnString("/ss/query_pslist_and_domain.json", param);
		Assert.assertFalse(resutStr.contains(psIpprot1));
		Assert.assertTrue(resutStr.contains(psIpprot2));
	}
	
	@Test
	public void test08QueryPs4KillPoolReturnOtherCluster() throws Exception {
		String psPool1 = "pool-133-1";
		prepareAuth(SetUtil.newHashSet(new Pool(psPool1, psCluster1)));
		String psIpprot1 = "133.0.0.1:8081";
		// callRestApi("/ss/upsert.json", buildSearchServerJson(psCluster1, psIpprot1, psPool1, null, new JSONObject()));
		
		String psIpprot2 = "133.1.0.1:8081";
		callRestApi("/ss/upsert.json", buildSearchServerJson(psCluster2, psIpprot2, "", null, new JSONObject()));
		
		String param = String.format(QUERY_PS_PARAMS, searchAuthorization.getClientToken(), psIpprot1);
		String resutStr = callRestApiAndReturnString("/ss/query_pslist_and_domain.json", param);
		Assert.assertFalse(resutStr.contains(psIpprot1));
		Assert.assertTrue(resutStr.contains(psIpprot2));
	}
}
