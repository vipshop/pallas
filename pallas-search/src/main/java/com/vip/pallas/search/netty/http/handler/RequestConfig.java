package com.vip.pallas.search.netty.http.handler;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(RUNTIME)
@Target(METHOD)
public @interface RequestConfig {

	public String url() default "";
	
	public String method() default "get";
}
