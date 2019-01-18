package com.vip.pallas.search.netty.http.handler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AnnotationResolver {

	private static Logger logger = LoggerFactory.getLogger(AnnotationResolver.class);
	
	public static final Map<String/**url**/, Method/**method**/> URL_MAPPING = new HashMap<>();
	
	public static void parseClass(Class clazz) {
		try {
	        Method[] methods=clazz.getDeclaredMethods();
	        for(Method m:methods){
	            RequestConfig cfg = m.getAnnotation(RequestConfig.class);
	            if (cfg != null) {
	            	URL_MAPPING.put(cfg.url(), m);
	    	        logger.info("export rest url: {} , method: {}.{}", cfg.url(), clazz.getSimpleName(), m.getName());
	            }
	        }
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
}
