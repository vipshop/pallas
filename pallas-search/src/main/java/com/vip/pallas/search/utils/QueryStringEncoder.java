package com.vip.pallas.search.utils;

import io.netty.handler.codec.http.HttpConstants;
import io.netty.util.internal.InternalThreadLocalMap;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Creates an URL-encoded URI from a path string and key-value parameter pairs. This encoder is for one time use only.
 * Create a new instance for each URI.
 *
 * <pre>
 * {@link QueryStringEncoder} encoder = new {@link QueryStringEncoder}("/hello");
 * encoder.addParam("recipient", "world");
 * assert encoder.toString().equals("/hello?recipient=world");
 * </pre>
 * 
 * @see QueryStringDecoder
 */
public class QueryStringEncoder {

	private final Charset charset;
	private final String uri;
	private final List<Param> params = new ArrayList<Param>();

	/**
	 * Creates a new encoder that encodes a URI that starts with the specified path string. The encoder will encode the
	 * URI in UTF-8.
	 */
	public QueryStringEncoder(String uri) {
		this(uri, HttpConstants.DEFAULT_CHARSET);
	}

	/**
	 * Creates a new encoder that encodes a URI that starts with the specified path string in the specified charset.
	 */
	public QueryStringEncoder(String uri, Charset charset) {
		if (charset == null) {
			throw new IllegalArgumentException("charset is null");
		}

		this.uri = uri;
		this.charset = charset;
	}

	/**
	 * Adds a parameter with the specified name and value to this encoder.
	 */
	public void addParam(String name, String value) {
		if (name == null) {
			throw new IllegalArgumentException("name is null");
		}
		params.add(new Param(name, value));
	}

	/**
	 * Returns the URL-encoded URI object which was created from the path string specified in the constructor and the
	 * parameters added by {@link #addParam(String, String)} getMethod.
	 */
	public URI toUri() throws URISyntaxException {
		return new URI(toString());
	}

	/**
	 * Returns the URL-encoded URI which was created from the path string specified in the constructor and the
	 * parameters added by {@link #addParam(String, String)} getMethod.
	 */
	@Override
	public String toString() {
		if (params.isEmpty()) {
			return uri;
		} else {
			StringBuilder sb = InternalThreadLocalMap.get().stringBuilder();
			if (uri != null) {
				sb.append(uri).append('?');
			}
			for (int i = 0; i < params.size(); i++) {
				Param param = params.get(i);
				sb.append(encodeComponent(param.name, charset));
				if (param.value != null) {
					sb.append('=');
					sb.append(encodeComponent(param.value, charset));
				}
				if (i != params.size() - 1) {
					sb.append('&');
				}
			}
			return sb.toString();
		}
	}

	private static String encodeComponent(String s, Charset charset) {
		// TODO: Optimize me.
		try {
			return URLEncoder.encode(s, charset.name()).replace("+", "%20");
		} catch (UnsupportedEncodingException ignored) {//NOSONAR
			throw new UnsupportedCharsetException(charset.name());
		}
	}

	private static final class Param {

		final String name;
		final String value;

		Param(String name, String value) {
			this.value = value;
			this.name = name;
		}
	}
}
