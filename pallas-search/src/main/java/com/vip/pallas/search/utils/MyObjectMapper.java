package com.vip.pallas.search.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.codehaus.jackson.map.ObjectMapper;

public class MyObjectMapper extends ObjectMapper {

	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static MyObjectMapper mapper =  new MyObjectMapper();
	public static MyObjectMapper getInstance(){
		return mapper;
	}
	
	private MyObjectMapper(){
		super();
		DateFormat myDateFormat = new SimpleDateFormat(DATE_FORMAT); 
		super.getSerializationConfig().withDateFormat(myDateFormat);
		super.getDeserializationConfig().withDateFormat(myDateFormat);
	}
	
}
