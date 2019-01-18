package com.vip.pallas.search.filter.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.exception.HttpCodeErrorPallasException;
import com.vip.pallas.search.exception.NotPrintStackPallasException;
import com.vip.pallas.search.filter.base.AbstractFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.filter.post.CommonResponseHeaderFilter;
import com.vip.pallas.search.http.HttpCode;

import io.netty.buffer.Unpooled;

/**
 * 错误统一处理
 * @author freeman.he
 *
 */
public class ErrorFilter extends AbstractFilter {
	private static Logger logger = LoggerFactory.getLogger(ErrorFilter.class);
	public static String DEFAULT_NAME = PRE_FILTER_NAME + ErrorFilter.class.getSimpleName().toUpperCase();

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws Exception {
		Throwable throwable = sessionContext.getThrowable();
		if (throwable != null) {
			// 是否打印堆栈信息
			sessionContext.setPrintExceStackInfo(printExceptionStackInfo(throwable));
			// 打印错误信息
			logThrowable(throwable);
			// 错误信息渲染，设置到body里面。返回状态码和mercury状态码处理
			processThrowable(sessionContext, throwable);

		}
		filterContext.fireFilter(sessionContext, CommonResponseHeaderFilter.DEFAULT_NAME);
	}

	public static void processThrowable(SessionContext sessionContext, Throwable t) {

		if (sessionContext.getHttpCode() == HttpCode.HTTP_RETRY_TIMEOUT) {
			// pallas search 手工抛出的重试失败错误，不需要处理
			return;
		}
		// 返回的httpcode，并且apiinfo为空(还不确定wrapper类型)
		if (t instanceof HttpCodeErrorPallasException) {
			sessionContext.setHttpCode(((HttpCodeErrorPallasException) t).getHttpCode());
		} else {
			sessionContext.setHttpCode(HttpCode.HTTP_INTERNAL_SERVER_ERROR);
		}

		if (sessionContext.getResponseBody() == null){
			sessionContext.setResponseBody(Unpooled.directBuffer().writeBytes(t.getMessage() != null
					? t.getMessage().getBytes() : "null".getBytes()));
		}
		return;
	}

	private static void logThrowable(Throwable t) {

		if (t instanceof NotPrintStackPallasException) {
			// 不需要打印日志
			return;
		}

		logger.error(t.getMessage(), t);

	}

	/**
	 * 判断是否需要打印堆栈信息
	 * 
	 * @param t
	 * @return
	 */
	private static Boolean printExceptionStackInfo(Throwable t) {
		if (t instanceof NotPrintStackPallasException) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

}
