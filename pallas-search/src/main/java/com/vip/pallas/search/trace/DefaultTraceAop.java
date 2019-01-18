package com.vip.pallas.search.trace;

import com.vip.pallas.search.filter.common.SessionContext;

import io.netty.handler.codec.http.HttpHeaders;

public class DefaultTraceAop extends TraceAop {

	@Override
	public TraceAspect newTrace() {
		return new DefaultTraceAspect();
	}

	class DefaultTraceAspect extends TraceAspect {

		@Override
		public void beforeFilterStart(SessionContext sessionContext) {

		}

		@Override
		public void afterFilterEnd(SessionContext sessionContext) {

		}

		@Override
		public void afterRestEnd(Throwable throwable, int responseCode) {

		}

		@Override
		public String getTraceId() {
			return null;
		}

		@Override
		public void beforeRestStart(SessionContext sessionContext, HttpHeaders httpHeaders) {

		}

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}
}


