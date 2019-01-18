package com.vip.pallas.search.filter.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.filter.HttpProtocolCheckFilter;
import com.vip.pallas.search.filter.base.DefaultFilterPipeLine;
import com.vip.pallas.search.filter.error.ErrorFilter;
import com.vip.pallas.search.filter.post.ResponseSendFilter;
import com.vip.pallas.search.http.HttpCode;

/**
 * @author freeman.he
 *
 */
public class PallasRunner {
	private static Logger logger = LoggerFactory.getLogger(PallasRunner.class);

	public static void run(SessionContext context) {
		try {

			DefaultFilterPipeLine.getInstance().get(HttpProtocolCheckFilter.DEFAULT_NAME).fireSelf(context); // 开始执行
		} catch (Throwable e) {
			errorProcess(context, e);
		}
	}

	// 错误处理的统一入口。由于异步的原因，在调用filter出错，会catch住，然后调用该接口
	public static void errorProcess(SessionContext sessionContext, Throwable t) {

		try {
			Throwable throwable = sessionContext.getThrowable();
			// 说明在处理错误当中，又发生了错误(该方法被调用了两次)，则直接到发送数据的地方
			if (throwable != null) {
				if (t != null) {
					logger.error("", t);
				}
				DefaultFilterPipeLine.getInstance().get(ResponseSendFilter.DEFAULT_NAME).fireSelf(sessionContext);
			} else { // 有错误，则将指向errorFilter进行处理
				sessionContext.setThrowable(t);
				DefaultFilterPipeLine.getInstance().get(ErrorFilter.DEFAULT_NAME).fireSelf(sessionContext);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			sessionContext.setHttpCode(HttpCode.HTTP_INTERNAL_SERVER_ERROR);
			// 直接打印错误
			DefaultFilterPipeLine.getInstance().get(ResponseSendFilter.DEFAULT_NAME).fireSelf(sessionContext);
		}

	}

}