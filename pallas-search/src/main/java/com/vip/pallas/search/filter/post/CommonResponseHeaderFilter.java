package com.vip.pallas.search.filter.post;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;

import org.apache.commons.lang3.StringUtils;

import com.vip.pallas.search.constant.Constant;
import com.vip.pallas.search.filter.base.AbstractFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpHeaders.Values;

/**
 * 来源主要有三部分：error,osp,rest
 * 
 * @author freeman.he
 *
 */
public class CommonResponseHeaderFilter extends AbstractFilter {
	public static String DEFAULT_NAME = PRE_FILTER_NAME + CommonResponseHeaderFilter.class.getSimpleName().toUpperCase();

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws Exception {
		// 如果出错，也必要要往下走
		HttpHeaders headers = sessionContext.getResponseHttpHeaders();
		// 添加traceid
		if (StringUtils.isNotEmpty(sessionContext.getTraceId())) {
			headers.set(Constant.HEAD_TRACEID, sessionContext.getTraceId());	
		}
		
		if (sessionContext.getRequest().getHttpVersion().isKeepAliveDefault()) {
			headers.remove(Names.CONNECTION);
		}
		else {
			headers.set(Names.CONNECTION, Values.CLOSE);
		}
		// 对content-type的添加,如果是jsonp则返回script，如果不是，则返回json。
		 if (headers.get(Names.CONTENT_TYPE) == null) {// 如果用户设置了，以用户为准
             headers.set(Names.CONTENT_TYPE, "application/json;charset=utf-8");
         }
		// 获取body
		ByteBuf byteBuf = sessionContext.getResponseBody();
		headers.set(CONTENT_LENGTH, byteBuf == null ? 0 : byteBuf.readableBytes());
		super.run(filterContext, sessionContext);

	}

}
