//package com.vip.pallas.search.timeout;
//
//import static com.vip.pallas.search.MockLocalHttpServerService.MOCKES_HOST_PORT;
//import static org.assertj.core.api.Assertions.assertThat;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.http.client.methods.CloseableHttpResponse;
//import org.apache.http.util.EntityUtils;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//
//import com.vip.pallas.test.BaseSearchTest;
//import com.vip.pallas.search.filter.base.AbstractFilterContext;
//import com.vip.pallas.search.filter.base.DefaultFilterPipeLine;
//import com.vip.pallas.search.launch.BootStrap;
//import com.vip.pallas.search.utils.ModifierUtils;
//import com.vip.pallas.search.utils.PallasSearchProperties;
//import org.junit.Test;
//
//public class TimeoutTest extends BaseSearchTest {
//	public static final String TEMPLATE_ID_HEADER_NAME  = "X-PALLAS-SEARCH-TEMPLATE-ID";
//	public static final String DOMAIN_HEADER_NAME = "X-PALLAS-SEARCH-ES-DOMAIN";
//
//	public static void main(String[] args) throws NoSuchFieldException, SecurityException, Exception {
//		ModifierUtils.setFinalStatic(DefaultFilterPipeLine.class.getDeclaredField("name2ctx"), new HashMap<String, AbstractFilterContext>(16), DefaultFilterPipeLine.getInstance());
//	}
//
//	@BeforeClass
//	public static void skipRoutingFilter() throws Exception {
//		ModifierUtils.setFinalStatic(PallasSearchProperties.class.getField("SEARCH_SKIP_ROUTING"), true, null);
//		ModifierUtils.setFinalStatic(DefaultFilterPipeLine.class.getDeclaredField("name2ctx"), new HashMap<String, AbstractFilterContext>(16), DefaultFilterPipeLine.getInstance());
//		BootStrap.initZuul();
//	}
//
//	@AfterClass
//	public static void setBackFilter() throws NoSuchFieldException, SecurityException, Exception {
//		ModifierUtils.setFinalStatic(PallasSearchProperties.class.getField("SEARCH_SKIP_ROUTING"), false, DefaultFilterPipeLine.getInstance());
//		ModifierUtils.setFinalStatic(DefaultFilterPipeLine.class.getDeclaredField("name2ctx"), new HashMap<String, AbstractFilterContext>(16), DefaultFilterPipeLine.getInstance());
//		BootStrap.initZuul();
//	}
//
//	@Test
//	public void test() throws IOException {
////		startServer();
//		Map<String, String> header = new HashMap<>();
//		header.put("X-PALLAS-SEARCH-TEMPLATE-ID", "vfeature_it_timeout_test");
//		header.put("X_PALLAS_SEARCH_UP_STREAM_URL", MOCKES_HOST_PORT);
//		header.put("X-PALLAS-SEARCH-ES-DOMAIN", "test-rpm.api.vip.com");
//
//		String requestBody = "{\n" +
//                "  \"id\": \"vfeature_it_timeout_test\",\n" +
//                "  \"params\": {}" +
//                "}";
//		CloseableHttpResponse callRestApiAndReturnResponse = callRestApiAndReturnResponse("localhost", SERVER_PORT, "/vfeature/_search/template", header, requestBody);
//		String result = EntityUtils.toString(callRestApiAndReturnResponse.getEntity());
//		assertThat(callRestApiAndReturnResponse.getStatusLine().getStatusCode()).isEqualTo(520);
//		System.out.println(result);
//	}
//
//
//}
