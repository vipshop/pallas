package com.vip.pallas.search.filter.throttling;

import com.google.common.util.concurrent.RateLimiter;
import com.vip.pallas.search.exception.HttpCodeErrorPallasException;
import com.vip.pallas.search.filter.base.AbstractFilter;
import com.vip.pallas.search.filter.base.AbstractFilterContext;
import com.vip.pallas.search.filter.common.SessionContext;

import static com.vip.pallas.search.http.HttpCode.HTTP_TOO_MANY_REQUESTS;

public class ThrottlingFilter extends AbstractFilter {

	public static String className = ThrottlingFilter.class.getSimpleName();
	public static String classMethod = "run";
	public static String DEFAULT_NAME = PRE_FILTER_NAME + ThrottlingFilter.class.getSimpleName().toUpperCase();

	@Override
	public String name() {
		return DEFAULT_NAME;
	}

	@Override
	public void run(AbstractFilterContext filterContext, SessionContext sessionContext) throws Exception {
		String restTemplateId = sessionContext.getRequest().getTemplateId();

		if (null != restTemplateId) {
			RateLimiter rateLimiter = ThrottlingPolicyHelper.getRateLimiterByRestTemplateId(restTemplateId);
			if (null != rateLimiter && !rateLimiter.tryAcquire()) {
				throw new HttpCodeErrorPallasException(
						"the request of [" + restTemplateId + "] be throttled, the limit is " + rateLimiter.getRate()
								+ " per seconds", HTTP_TOO_MANY_REQUESTS, className, classMethod);
			}
		}
		super.run(filterContext, sessionContext);
	}

}
