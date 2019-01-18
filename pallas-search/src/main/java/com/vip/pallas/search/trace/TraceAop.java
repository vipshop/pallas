package com.vip.pallas.search.trace;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TraceAop {
	private static final Logger logger = LoggerFactory.getLogger(TraceAop.class);
	private static TraceAop instance;

	static {
		initAopImpl();
	}

	public static TraceAop instance() {
		return instance;
	}

	public abstract void start();

	public abstract TraceAspect newTrace();

	private static void initAopImpl() {
		try {
			// discover TraceAop
			ServiceLoader<TraceAop> loader = ServiceLoader.load(TraceAop.class);
			Iterator<TraceAop> iter = loader.iterator();
			while (iter.hasNext()) {
				instance = iter.next();
				logger.info("TraceAop implementation found: {}", instance.getClass().getName());
				return;
			}
			// log error, fallback
			instance = new DefaultTraceAop();
			logger.error("TraceAop implementation not found, fallback to: {}", instance.getClass().getName());
		} catch (Throwable e) {
			// log error, fallback
			instance = new DefaultTraceAop();
			logger.error("TraceAop implementation not found, fallback to: {}", instance.getClass().getName());
		}

	}

	protected TraceAop() {

	}



}

