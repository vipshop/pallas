package com.vip.pallas.search.utils;

import org.apache.commons.io.Charsets;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public abstract class JsonUtil {

	private static ObjectMapper mapper = MyObjectMapper.getInstance();
	static {
		mapper.getSerializationConfig().withDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
		mapper.getDeserializationConfig().withDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
	}

	public static ObjectMapper getObjectMapper() {
		return mapper;
	}

	/**
	 * 将java对象转换成json串
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static String toJson(Object value) throws Exception {
		return getObjectMapper().writeValueAsString(value);
	}

	/**
	 * 将java对象转换成json串
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public static byte[] toJsonByteArray(Object value) throws Exception {
		return getObjectMapper().writeValueAsBytes(value);
	}
	
	/**
	 * 将json串转换成java对象
	 * @param <T>
	 * @param content
	 * @param valueType
	 * @return
	 * @throws Exception
	 */
	public static <T> T readValue(String content, Class<T> valueType) throws Exception {
		return (T) mapper.readValue(content, valueType);
	}

	/**
	 * 将json串转换成java集合对象
	 * @param <T>
	 * @param content
	 * @param collectionClass
	 * @param elementClasses
	 * @return
	 * @throws Exception
	 */
	public static <T> T readValue(String content, Class<?> collectionClass, Class<?>... elementClasses) throws Exception {
		return (T) mapper.readValue(content, mapper.getTypeFactory().constructParametricType(collectionClass, elementClasses));
	}

	/**
	 * 将json串转换成java对象. 主要用于转换的java类中包含泛型的情况
	 * <pre>
	 * 用法:
	 * <code>
	 * String cont = "[{\"type\":1,\"uid\":123,\"username\":\"a\",\"password\":\"pd\"}]";
	 * ArrayList< User > us = readValue(cont, new TypeReference< ArrayList< User > >() { });
	 * </code>
	 * </pre>
	 * @param <T>
	 * @param content
	 * @param valueTypeRef
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static <T> T readValue(String content, TypeReference<T> valueTypeRef) throws Exception {
		return (T) mapper.readValue(content, valueTypeRef);
	}

	/**
	 * 将 json 字符串转成 map
	 * @param jsonStr
	 * @return 相对应 json 的 map 对象
	 * @throws Exception
	 */
	public static Map<String, Object> parseJsonObject(String jsonStr) throws Exception {
		return parseJsonObject(mapper.readTree(jsonStr.getBytes(Charsets.UTF_8)));
	}

	/**
	 * 将 json 字符串转成 map
	 * @param in 需要parse的InputStream
	 * @return 相对应 json 的 map 对象
	 * @throws Exception
	 */
	public static Map<String, Object> parseJsonObject(InputStream in) throws Exception {
		return parseJsonObject(mapper.readTree(in));
	}

	private static Map<String, Object> parseJsonObject(JsonNode node) throws Exception {
		Map<String, Object> inputMap = new HashMap<>();
		Iterator<String> fieldNames = node.getFieldNames();
		while (fieldNames.hasNext()) {
			String fieldName = fieldNames.next();
			JsonNode child = node.get(fieldName);
			if (child.isValueNode()) {
				inputMap.put(fieldName, child.asText());
			} else {
				inputMap.put(fieldName, child);
			}
		}
		return inputMap;
	}



}
