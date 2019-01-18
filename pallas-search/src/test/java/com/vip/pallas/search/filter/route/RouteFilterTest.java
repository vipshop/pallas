package com.vip.pallas.search.filter.route;

import static com.vip.pallas.search.model.SearchAuthorization.AUTHORIZATION_CAT_CLUSTERALL;
import static com.vip.pallas.search.model.SearchAuthorization.AUTHORIZATION_CAT_INDEXALL;
import static com.vip.pallas.search.model.SearchAuthorization.AUTHORIZATION_PRIVILEGE_READONLY;
import static com.vip.pallas.search.model.SearchAuthorization.AUTHORIZATION_PRIVILEGE_WRITE;
import static java.util.stream.Collectors.toList;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.vip.pallas.search.model.FlowRecord;
import com.vip.pallas.search.model.Index;
import com.vip.pallas.search.model.IndexRouting;
import com.vip.pallas.search.model.IndexRoutingTargetGroup;
import com.vip.pallas.search.model.SearchAuthorization;
import com.vip.pallas.search.model.ServiceInfo;
import com.vip.pallas.search.model.TemplateWithTimeoutRetry;
import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.service.MockPallasCacheServiceImpl;
import com.vip.pallas.search.service.PallasCacheService;
import com.vip.pallas.search.service.impl.PallasCacheServiceImpl;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import junit.framework.TestCase;

/**
 * Created by owen on 28/11/2017.
 */
public class RouteFilterTest extends TestCase {

    private RouteFilter filter = new RouteFilter();

    @Test
    public void testRandomTargetGroupByWeight() {

        IndexRouting.ConditionTarget t1 = new IndexRouting.ConditionTarget();
        t1.setId(1L);
        t1.setWeight(7);

        IndexRouting.ConditionTarget t2 = new IndexRouting.ConditionTarget();
        t2.setId(2L);
        t2.setWeight(2);

        IndexRouting.ConditionTarget t3 = new IndexRouting.ConditionTarget();
        t3.setId(3L);
        t3.setWeight(1);

        List<IndexRouting.ConditionTarget> collect = Stream.of(t1).collect(toList());
        IndexRouting.ConditionTarget result = filter.randomTargetGroupByWeight(collect);
        assertTrue(result.getId().equals(1L));

        collect = Stream.of(t1, t2, t3).collect(toList());
        int c1 = 0, c2 = 0, c3 = 0;
        for(int i = 0; i< 100; i++) {
            result = filter.randomTargetGroupByWeight(collect);
            if (result.getId().equals(1L)) {
                c1++;
            } else if (result.getId().equals(2L)) {
                c2++;
            } else if (result.getId().equals(3L)) {
                c3++;
            }
        }
        System.out.println(c1);
        System.out.println(c2);
        System.out.println(c3);
        assertTrue(c1 > 0);
        assertTrue(c2 > 0);
        assertTrue(c3 > 0);
    }

    @Test
    public void testCheckOperation() {

        RouteFilter.OperationType op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/sales/_search/template?1=1", "GET");
        assertEquals(op, RouteFilter.OperationType.INDEX_SERACH);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/sales?1=1", "GET");
        assertEquals(op, RouteFilter.OperationType.INDEX_SERACH);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/sales/_search?1=1", "GET");
        assertEquals(op, RouteFilter.OperationType.INDEX_SERACH);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/sales/item/123456?1=1", "GET");
        assertEquals(op, RouteFilter.OperationType.INDEX_SERACH);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/sales/_settings?1=1", "GET");
        assertEquals(op, RouteFilter.OperationType.INDEX_SERACH);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/sales/item/_mapping?1=1", "GET");
        assertEquals(op, RouteFilter.OperationType.INDEX_SERACH);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/_search/analysis?1=1", "POST");
        assertEquals(op, RouteFilter.OperationType.INDEX_SERACH);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/_render/template?1=1", "POST");
        assertEquals(op, RouteFilter.OperationType.INDEX_SERACH);

        op = filter.checkOperation("sales", "pallas-qa", "/_bulk?1=1", "POST");
        assertEquals(op, RouteFilter.OperationType.INDEX_UPDATE);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/sales/_update?1=1", "POST");
        assertEquals(op, RouteFilter.OperationType.INDEX_UPDATE);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/sales/item/123456?1=1", "PUT");
        assertEquals(op, RouteFilter.OperationType.INDEX_UPDATE);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/sales/_create?1=1", "PUT");
        assertEquals(op, RouteFilter.OperationType.INDEX_UPDATE);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/sales/_delete_by_query?1=1", "POST");
        assertEquals(op, RouteFilter.OperationType.INDEX_UPDATE);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/sales/item/123456?1=1", "DELETE");
        assertEquals(op, RouteFilter.OperationType.INDEX_UPDATE);

        op = filter.checkOperation("", "pallas-qa", "/pallas-qa/_cluster/settings?1=1", "GET");
        assertEquals(op, RouteFilter.OperationType.CLUSTER_SEARCH);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/_cat/shards?1=1", "GET");
        assertEquals(op, RouteFilter.OperationType.CLUSTER_SEARCH);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/_flush/sync?1=1", "GET");
        assertEquals(op, RouteFilter.OperationType.CLUSTER_SEARCH);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/_nodes/stats?1=1", "GET");
        assertEquals(op, RouteFilter.OperationType.CLUSTER_SEARCH);

        op = filter.checkOperation("sales", "pallas-qa", "/pallas-qa/_cluster/settings?1=1", "POST");
        assertEquals(op, RouteFilter.OperationType.CLUSTER_UPDTE);

    }

    @Test
    public void testValidateAuthorization() throws Exception {
        MockPallasCacheServiceImpl.mockPallasCacheFactory(mockService);

        indexName = "sales";
        cluster = "pallas-qa";
        method = "GET";
        url = "/pallas-qa/sales/_search?1=1";

        auth = new SearchAuthorization();
        auth.setEnabled(true);
        auth.setClientToken("AAAAAAABBBBBBBCCCCCCCDDDDDDEEEEEE====");
        auth.setTitle("ForUT");

        //cluster privilege
        SearchAuthorization.AuthorizationItem item = new SearchAuthorization.AuthorizationItem();
        item.setId(1L);
        item.setName("pallas-qa");
        Map<String, List<String>> clusterPrivileges = new HashMap<>();
        clusterPrivileges.put(AUTHORIZATION_CAT_CLUSTERALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY, AUTHORIZATION_PRIVILEGE_WRITE));
        item.setPrivileges(clusterPrivileges);


        //index1 privilege
        SearchAuthorization.AuthorizationItem item1 = new SearchAuthorization.AuthorizationItem();
        item1.setId(10001L);
        item1.setName("sales");
        Map<String, List<String>> indexPrivileges = new HashMap<>();
        indexPrivileges.put(AUTHORIZATION_CAT_INDEXALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY, AUTHORIZATION_PRIVILEGE_WRITE));
        item1.setPrivileges(indexPrivileges);
        item.setIndexPrivileges(tToList(item1));

        auth.setAuthorizationItems(SearchAuthorization.toXContent(tToList(item)));

        //check Search
        filter.validateAuthorization(testReq);
        indexPrivileges.put(AUTHORIZATION_CAT_INDEXALL, Collections.emptyList());
        auth.setAuthorizationItems(SearchAuthorization.toXContent(tToList(item)));
        auth.setAuthorizationItemList(null);
        filter.validateAuthorization(testReq);

        clusterPrivileges.put(AUTHORIZATION_CAT_CLUSTERALL, Collections.emptyList());
        auth.setAuthorizationItems(SearchAuthorization.toXContent(tToList(item)));
        auth.setAuthorizationItemList(null);
        Exception ex = null;
        try {
            filter.validateAuthorization(testReq);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        ex = null;

        //check Update
        method = "POST";
        url = "/_bulk";

        indexPrivileges.put(AUTHORIZATION_CAT_INDEXALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY, AUTHORIZATION_PRIVILEGE_WRITE));
        clusterPrivileges.put(AUTHORIZATION_CAT_CLUSTERALL, Collections.emptyList());
        auth.setAuthorizationItems(SearchAuthorization.toXContent(tToList(item)));
        auth.setAuthorizationItemList(null);
        filter.validateAuthorization(testReq);

        indexPrivileges.put(AUTHORIZATION_CAT_INDEXALL, Collections.emptyList());
        clusterPrivileges.put(AUTHORIZATION_CAT_CLUSTERALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY, AUTHORIZATION_PRIVILEGE_WRITE));
        auth.setAuthorizationItems(SearchAuthorization.toXContent(tToList(item)));
        auth.setAuthorizationItemList(null);
        filter.validateAuthorization(testReq);

        indexPrivileges.put(AUTHORIZATION_CAT_INDEXALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY));
        clusterPrivileges.put(AUTHORIZATION_CAT_CLUSTERALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY));
        auth.setAuthorizationItems(SearchAuthorization.toXContent(tToList(item)));
        auth.setAuthorizationItemList(null);
        try {
            filter.validateAuthorization(testReq);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        ex = null;

        //check Cluster Search
        method = "GET";
        url = "/pallas-qa/_cluster/settings";

        indexPrivileges.put(AUTHORIZATION_CAT_INDEXALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY, AUTHORIZATION_PRIVILEGE_WRITE));
        clusterPrivileges.put(AUTHORIZATION_CAT_CLUSTERALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY, AUTHORIZATION_PRIVILEGE_WRITE));
        auth.setAuthorizationItems(SearchAuthorization.toXContent(tToList(item)));
        auth.setAuthorizationItemList(null);
        filter.validateAuthorization(testReq);

        indexPrivileges.put(AUTHORIZATION_CAT_INDEXALL, Collections.emptyList());
        clusterPrivileges.put(AUTHORIZATION_CAT_CLUSTERALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY));
        auth.setAuthorizationItems(SearchAuthorization.toXContent(tToList(item)));
        auth.setAuthorizationItemList(null);
        filter.validateAuthorization(testReq);

        indexPrivileges.put(AUTHORIZATION_CAT_INDEXALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY));
        clusterPrivileges.put(AUTHORIZATION_CAT_CLUSTERALL, Collections.emptyList());
        auth.setAuthorizationItems(SearchAuthorization.toXContent(tToList(item)));
        auth.setAuthorizationItemList(null);
        try {
            filter.validateAuthorization(testReq);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        ex = null;

        //check Cluster Update
        method = "POST";
        url = "/pallas-qa/_cluster/settings";

        indexPrivileges.put(AUTHORIZATION_CAT_INDEXALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY, AUTHORIZATION_PRIVILEGE_WRITE));
        clusterPrivileges.put(AUTHORIZATION_CAT_CLUSTERALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY, AUTHORIZATION_PRIVILEGE_WRITE));
        auth.setAuthorizationItems(SearchAuthorization.toXContent(tToList(item)));
        auth.setAuthorizationItemList(null);
        filter.validateAuthorization(testReq);

        indexPrivileges.put(AUTHORIZATION_CAT_INDEXALL, Collections.emptyList());
        clusterPrivileges.put(AUTHORIZATION_CAT_CLUSTERALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY, AUTHORIZATION_PRIVILEGE_WRITE));
        auth.setAuthorizationItems(SearchAuthorization.toXContent(tToList(item)));
        auth.setAuthorizationItemList(null);
        filter.validateAuthorization(testReq);

        indexPrivileges.put(AUTHORIZATION_CAT_INDEXALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY));
        clusterPrivileges.put(AUTHORIZATION_CAT_CLUSTERALL, tToList(AUTHORIZATION_PRIVILEGE_READONLY));
        auth.setAuthorizationItems(SearchAuthorization.toXContent(tToList(item)));
        auth.setAuthorizationItemList(null);
        try {
            filter.validateAuthorization(testReq);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        ex = null;


        //check Default Privilege change to ReadOnly
        RouteFilter.DEFAULT_AUTHORIZATION_PRIVILEGE = AUTHORIZATION_PRIVILEGE_READONLY;
        method = "GET";
        url = "/pallas-qa/_cluster/settings";

        indexPrivileges.put(AUTHORIZATION_CAT_INDEXALL, Collections.emptyList());
        clusterPrivileges.put(AUTHORIZATION_CAT_CLUSTERALL, Collections.emptyList());
        auth = null;
        token = null;
        try {
            filter.validateAuthorization(testReq);
        } catch (Exception e) {
            ex = e;
        }
        assertNull(ex);
        ex = null;

        //check with WRONG token
        RouteFilter.DEFAULT_AUTHORIZATION_PRIVILEGE = AUTHORIZATION_PRIVILEGE_READONLY;
        method = "GET";
        url = "/pallas-qa/_cluster/settings";

        indexPrivileges.put(AUTHORIZATION_CAT_INDEXALL, Collections.emptyList());
        clusterPrivileges.put(AUTHORIZATION_CAT_CLUSTERALL, Collections.emptyList());
        auth = null;
        token = "AAAABBBBCCCC==";
        try {
            filter.validateAuthorization(testReq);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        ex = null;

        //reset to default service
        MockPallasCacheServiceImpl.mockPallasCacheFactory(PallasCacheServiceImpl.getInstance());
    }

    private <T> List<T> tToList (T... t) {
        return Stream.of(t).collect(Collectors.toList());
    }


    @Test
    public void testExtractTargetGroupFromScrollId() throws ExecutionException {
        fakeBodyStrForPost = "";
        List<ServiceInfo> serviceInfos = filter.extractTargetGroupFromScrollId(testReq);
        assertEquals(serviceInfos.size(), 0);

        fakeBodyStrForPost = "not_exist_scroll_id";
        serviceInfos = filter.extractTargetGroupFromScrollId(testReq);
        assertEquals(serviceInfos.size(), 0);

        fakeBodyStrForPost = "{\n" +
                "\t\"scroll_id\"    :     \"aaaaabbbbbcccccddddd[1]\",\n" +
                "\t\"scoll\":\"1m\"\n" +
                "}";
        Exception ex = null;
        try {
            serviceInfos = filter.extractTargetGroupFromScrollId(testReq);
        }catch (Exception e) {
            e.printStackTrace();
            ex = e;
        }
        assertNotNull(ex);


        //FIXME 由于没有mock PallasSearcCache 所以拿不到真实的TargetGroup
        fakeBodyStrForPost = "{\n" +
                "\t\"scroll_id\"    :     \"aaaaabbbbbcccccddddd[1]\",\n" +
                "\t\"scoll\":\"1m\"\n" +
                "}";
        ex = null;
        try {
            serviceInfos = filter.extractTargetGroupFromScrollId(testReq);
        }catch (Exception e) {
            ex = e;
        }
    }

    private volatile static String indexName = "";

    private volatile static String cluster = "";

    private volatile static String token = "AAAABBBBCCCC==";

    private volatile static String method = "";

    private volatile static String url = "";

    private volatile static String fakeBodyStrForPost = "";

    private volatile static SearchAuthorization auth = null;

    private PallasRequest testReq = new PallasRequest() {

        @Override
        public void setPreference(String preference) {

        }

        @Override
        public String getPreference() {
            return null;
        }

        @Override
        public void setRoutePrimaryFirst(boolean routePrimaryFirst) {

        }

        @Override
        public boolean isRoutePrimaryFirst() {
            return false;
        }

        @Override
        public void setRouteReplicaFirst(boolean routeReplicaFirst) {

        }

        @Override
        public boolean isRouteReplicaFirst() {
            return false;
        }

        @Override
        public void setIsIndexSearch(boolean isIndexSearch) {

        }

        @Override
        public boolean isIndexSearch() {
            return true;
        }

        @Override
        public String getClientToken() {
            return token;
        }

        @Override
        public Map<String, List<String>> getParameterMap() {
            return null;
        }

        @Override
        public boolean isPostFormBody() {
            return false;
        }

        @Override
        public String getBodyStrForPost() {
            return fakeBodyStrForPost;
        }

        @Override
        public Map<String, String> getCookieMap() {
            return null;
        }

        @Override
        public String getCookie(String name) {
            return null;
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public Iterator<String> getHeaderNames() {
            return null;
        }

        @Override
        public Set<String> getNewHeaderNames() {
            return null;
        }

        @Override
        public String getMethod() {
            return method;
        }

        @Override
        public String getPathInfo() {
            return null;
        }

        @Override
        public String getUri() {
            return url;
        }

        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public String getParameter(String name) {
            return null;
        }

        @Override
        public List<String> getParameters(String name) {
            return null;
        }

        @Override
        public String remoteAddress() {
            return null;
        }

        @Override
        public HttpVersion getHttpVersion() {
            return null;
        }

        @Override
        public void writeAndFlush(FullHttpResponse fullHttpResponse) {

        }

        @Override
        public void closeChannle() {

        }

        @Override
        public void addParamterByUrlAndkey(String params, String key) throws IllegalArgumentException {

        }

        @Override
        public void removeParamter(String name) {

        }

        @Override
        public void removeHeader(String name) {

        }

        @Override
        public void setHeader(String name, String value) {

        }

        @Override
        public String getModifiedUri() {
            return url;
        }

        @Override
        public HttpMethod getHttpMethod() {
            return null;
        }

        @Override
        public HttpHeaders getHttpHeader() {
            return null;
        }

        @Override
        public DecoderResult getDecoderResult() {
            return null;
        }

        @Override
        public ByteBuf getModifyBodyContent() throws UnsupportedEncodingException {
            return null;
        }

        @Override
        public ByteBuf getContent() {
            return null;
        }

        @Override
        public String getIndexName() {
            return indexName;
        }

        @Override
        public String getClientIp() {
            return null;
        }

        @Override
        public String getTemplateId() {
            return null;
        }

        @Override
        public void setScrollFirst(boolean b) {

        }

        @Override
        public boolean isScrollFirst() {
            return false;
        }

        @Override
        public void setScrollContinue(boolean scroll) {

        }

        @Override
        public boolean isScrollContinue() {
            return false;
        }

        @Override
        public void setTargetGroupId(Long id) {

        }

        @Override
        public Long getTargetGroupId() {
            return null;
        }

        @Override
        public void setFixScrollRequestContent(byte[] fixContent) {

        }

        @Override
        public byte[] getFixScrollRequestContent() {
            return new byte[0];
        }

        @Override
        public void setLogicClusterId(String cluster) {

        }

        @Override
        public String getLogicClusterId() {
            return cluster;
        }
    };

    private PallasCacheService mockService = new PallasCacheService() {
        @Override
        public void initCache() throws ExecutionException {

        }

        @Override
        public String getClusterPortByClusterName(String clusterName) {
            return "9200";
        }

		@Override
		public List<FlowRecord> getFlowRecord(String clusterName, String indexName, String templateName) {
			return null;
		}

        @Override
        public FlowRecord getFlowRecordById(Long recordId) {
            return null;
        }

        @Override
        public void refreshRouting() {

        }

        @Override
        public void invalidateCache() {

        }

        @Override
        public List<String> getAvailableNodesByIndex(String indexName) throws ExecutionException {
            return null;
        }

        @Override
        public List<String> getAvailableNodesByCluster(String clusterName) throws ExecutionException {
            return null;
        }

        @Override
        public List<IndexRouting> getIndexLevelRoutingByIndexNameAndCluster(String indexName, String clusterId) throws ExecutionException {
            return null;
        }

        @Override
        public List<IndexRouting> getClusterLevelRoutingByIndexNameAndCluster(String clusterId) throws ExecutionException {
            return null;
        }

        @Override
        public List<IndexRoutingTargetGroup> getTargetGroupByIndexId(Long indexId) throws ExecutionException {
            return null;
        }

        @Override
        public IndexRoutingTargetGroup getTargetGroupById(Long targetGroupId) throws ExecutionException {
            return null;
        }

        @Override
        public SearchAuthorization getSearchAuthorization(String clientToken) throws ExecutionException {
            return auth;
        }

        @Override
        public String getClusterPortByIndexAndCluster(String indexName, String clusterName) {
            return null;
        }

        @Override
        public List<String> getAllNodeListByClusterName(String clusterName) throws ExecutionException {
            return null;
        }

        @Override
        public List<String> getShardNodeListByIndexAndCluster(String indexName, String clusterName) throws ExecutionException {
            return null;
        }

        @Override
        public List<String> getSourceIndexByIndexAndCluster(String indexName, String clusterName) throws ExecutionException {
            return null;
        }

        @Override
        public Index getIndexByIndexAndCluster(String indexName, String clusterName) {
            return null;
        }

        @Override
        public String tryToExtractClusterId(String indexName, String providedClusterId) throws ExecutionException {
            return null;
        }

		@Override
		public TemplateWithTimeoutRetry getConfigByTemplateIdAndCluster(String templateId, String clusterName,
				String indexName) throws ExecutionException {
			// TODO Auto-generated method stub
			return null;
		}
    };
}
