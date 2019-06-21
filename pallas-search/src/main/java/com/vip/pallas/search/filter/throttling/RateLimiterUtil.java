package com.vip.pallas.search.filter.throttling;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

public class RateLimiterUtil {

	private static final Logger logger = LoggerFactory.getLogger(RateLimiterUtil.class);

	/**
	 * a util method to create RateLimiter with customized maxBurstSeconds
	 */
	public static RateLimiter create(double permitsPerSecond, double maxBurstSeconds) {
		try {
			Class<?> sleepingStopwatchClass = Class
					.forName("com.google.common.util.concurrent.RateLimiter$SleepingStopwatch");
			Method createStopwatchMethod = sleepingStopwatchClass.getDeclaredMethod("createFromSystemTimer");
			createStopwatchMethod.setAccessible(true);
			Object stopwatch = createStopwatchMethod.invoke(null);

			Class<?> burstyRateLimiterClass = Class
					.forName("com.google.common.util.concurrent.SmoothRateLimiter$SmoothBursty");
			Constructor<?> burstyRateLimiterConstructor = burstyRateLimiterClass.getDeclaredConstructors()[0];
			burstyRateLimiterConstructor.setAccessible(true);

			RateLimiter rateLimiter = (RateLimiter) burstyRateLimiterConstructor.newInstance(stopwatch, maxBurstSeconds);
			rateLimiter.setRate(permitsPerSecond);
			// set the init size to the maxPermits, to prevent the request from being rejected while reCreating the RateLimiter
			setField(rateLimiter, "storedPermits", permitsPerSecond * maxBurstSeconds);
			return rateLimiter;
		} catch (Throwable e) {
			logger.error("Failed to customize the RateLimiter, fallback to default creation method", e);
			return RateLimiter.create(permitsPerSecond);
		}
	}

	private static boolean setField(Object targetObject, String fieldName, Object fieldValue) {
		Field field;
		try {
			field = targetObject.getClass().getDeclaredField(fieldName);
		} catch (NoSuchFieldException e) {
			field = null;
		}
		Class superClass = targetObject.getClass().getSuperclass();
		while (field == null && superClass != null) {
			try {
				field = superClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				superClass = superClass.getSuperclass();
			}
		}
		if (field == null) {
			return false;
		}
		field.setAccessible(true);
		try {
			field.set(targetObject, fieldValue);
			return true;
		} catch (IllegalAccessException e) {
			return false;
		}
	}
}