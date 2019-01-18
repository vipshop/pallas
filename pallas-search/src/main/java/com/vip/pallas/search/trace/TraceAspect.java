package com.vip.pallas.search.trace;

import com.vip.pallas.search.filter.common.SessionContext;

import io.netty.handler.codec.http.HttpHeaders;

public abstract class TraceAspect {

	public abstract void beforeFilterStart(SessionContext sessionContext);

	public abstract void afterFilterEnd(SessionContext sessionContext);

	public abstract void beforeRestStart(SessionContext sessionContext, HttpHeaders httpHeaders);

	public abstract void afterRestEnd(Throwable throwable, int responseCode);

	public abstract String getTraceId();

}