package com.vip.pallas.search.launch;

import com.vip.pallas.search.filter.HttpProtocolCheckFilter;
import com.vip.pallas.search.filter.base.DefaultFilterPipeLine;
import com.vip.pallas.search.filter.error.ErrorFilter;
import com.vip.pallas.search.filter.post.CommonResponseHeaderFilter;
import com.vip.pallas.search.filter.post.ResponseSendFilter;
import com.vip.pallas.search.filter.rest.RestInvokerFilter;
import com.vip.pallas.search.filter.rest.RestRequestBodyFilter;
import com.vip.pallas.search.filter.rest.RestRequestHeaderFilter;
import com.vip.pallas.search.filter.rest.RestRequestUriFilter;
import com.vip.pallas.search.filter.rest.RestResponseHeaderFilter;
import com.vip.pallas.search.filter.route.BalanceFilter;
import com.vip.pallas.search.filter.route.FlowRecordFilter;
import com.vip.pallas.search.filter.route.RouteFilter;
import com.vip.pallas.search.filter.route.UpStreamFilter;
import com.vip.pallas.search.utils.PallasSearchProperties;

public class BootStrap {
	
	public static void initZuul() throws Exception {
		initJavaFilters();
	}

	private static void initJavaFilters() {
		// 前置流程
		if(!PallasSearchProperties.SEARCH_SKIP_ROUTING){
			DefaultFilterPipeLine.getInstance().addLastSegment(new HttpProtocolCheckFilter(), new RouteFilter(), new BalanceFilter());
		}else{
			DefaultFilterPipeLine.getInstance().addLastSegment(new HttpProtocolCheckFilter(), new UpStreamFilter());
		}

		// rest流程
		DefaultFilterPipeLine.getInstance().addLastSegment(new RestRequestUriFilter(), new RestRequestBodyFilter(),
				new RestRequestHeaderFilter(), new FlowRecordFilter(), new RestInvokerFilter(), new RestResponseHeaderFilter());

		// 后置流程
		DefaultFilterPipeLine.getInstance().addLastSegment(new CommonResponseHeaderFilter(), new ResponseSendFilter());

		// 错误处理
		DefaultFilterPipeLine.getInstance().addLastSegment(new ErrorFilter());
	}
	
}
