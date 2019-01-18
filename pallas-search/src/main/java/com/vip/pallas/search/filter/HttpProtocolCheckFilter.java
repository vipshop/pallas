package com.vip.pallas.search.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.search.exception.HttpCodeErrorPallasException;
import com.vip.pallas.search.filter.base.AbstractFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.PallasRunner;
import com.vip.pallas.search.filter.common.SessionContext;
import com.vip.pallas.search.http.HttpCode;
import com.vip.pallas.search.http.PallasRequest;
import com.vip.pallas.search.utils.PallasSearchProperties;

import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.PrematureChannelClosureException;
import io.netty.handler.codec.TooLongFrameException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;

public class HttpProtocolCheckFilter extends AbstractFilter {

	private static Logger logger = LoggerFactory.getLogger(HttpProtocolCheckFilter.class);
	public static String DEFAULT_NAME = PRE_FILTER_NAME + HttpProtocolCheckFilter.class.getSimpleName().toUpperCase();
	public static Boolean validateCookie = PallasSearchProperties.VALID_COOKIE; // 是否需要进行cookie校验
	public static String className = HttpProtocolCheckFilter.class.getSimpleName();
	public static String classMethod = "run";

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) {
		PallasRequest request = sessionContext.getRequest();
		DecoderResult decoderResult = request.getDecoderResult();
		if (decoderResult != null) {
			if (decoderResult.isFailure()) {
				if (decoderResult.cause() instanceof TooLongFrameException) { // uri或者header,或者body的长度过长,message如下:"An
																				// HTTP line is larger than " +
																				// maxLength + " bytes."
					PallasRunner.errorProcess(sessionContext, new HttpCodeErrorPallasException(decoderResult.cause()
							.getMessage(), HttpCode.HTTP_REQUEST_URI_TOO_LONG, className, classMethod));
				} else if (decoderResult.cause() instanceof IllegalArgumentException) { // header校验失败(invalid escape
																						// sequence `%' at index 1
																						// ),uri校验失败( Header name cannot
																						// contain non-ASCII
																						// characters:)
					PallasRunner.errorProcess(sessionContext, new HttpCodeErrorPallasException(decoderResult.cause()
							.getMessage(), HttpCode.HTTP_PRECONDITION_FAILED, className, classMethod));
				} else if (decoderResult.cause() instanceof ErrorDataDecoderException) { // body存在非法字符校验失败Bad string:
					PallasRunner.errorProcess(sessionContext, new HttpCodeErrorPallasException("body invalid :"
							+ decoderResult.cause().getMessage(), HttpCode.HTTP_PRECONDITION_FAILED, className,
							classMethod));
				} else if (decoderResult.cause() instanceof PrematureChannelClosureException) { // http解析到一半，客户端又发送fin或者reset，导致该错误。
																								// 由于先对socket进行了关闭，所以该异常只会落盘日志，不会发送出去
					PallasRunner.errorProcess(sessionContext, new HttpCodeErrorPallasException("http parser failed:"
							+ decoderResult.cause().getMessage(), HttpCode.HTTP_PARSER_FAILED, className, classMethod));
				} else { // 不确定的异常，返回原始的错误信息
					PallasRunner.errorProcess(sessionContext, decoderResult.cause());
				}
				// 关闭掉channel
				if (sessionContext.getInBoundChannel() != null) {
					sessionContext.getInBoundChannel().close(); // 如果在解析的时候失败，则直接关闭掉该channel。
				}
				return;
			}

		}

		if (validateCookie) {
			// cookie包含无效字符时抛出412 name contains non-ascii character:
			try {
				request.getCookieMap();
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				PallasRunner.errorProcess(sessionContext, new HttpCodeErrorPallasException(e.getMessage(),
						HttpCode.HTTP_PRECONDITION_FAILED, className, classMethod));
				// 关闭掉channel
				if (sessionContext.getInBoundChannel() != null) {
					sessionContext.getInBoundChannel().close(); // 如果在解析的时候失败，则直接关闭掉该channel。
				}
				return;
			}
		}

		filterContext.fireNext(sessionContext);
	}

}
