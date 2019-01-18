/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.vip.pallas.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.output.StringBuilderWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.service.impl.RequestLogServiceImpl;

public class IoUtil {
	private static final int EOF = -1;
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
	private static Logger logger = LoggerFactory.getLogger(IoUtil.class);

	public static String toString(URL url) throws IOException {
		InputStream inputStream = url.openStream();
		try {
			return toString(inputStream);
		} finally {
			inputStream.close();
		}
	}
	public static String loadFile(String path) {
		try {
			URL resource = RequestLogServiceImpl.class.getClassLoader().getResource(path);

			if (resource == null) {
				return null;
			}
			String res = toString(resource);
			
			if (res != null) {
				res = res.replace("\r\n", "");
				res = res.replace("\n", "");
			}
			return res;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}


	public static String toString(InputStream input) throws IOException {
		StringBuilderWriter sw = new StringBuilderWriter();
		copy(input, sw);
		return sw.toString();
	}
	public static void copy(InputStream input, Writer output) throws IOException {
		InputStreamReader in = new InputStreamReader(input, Charset.defaultCharset());
		copy(in, output);
	}

	public static int copy(Reader input, Writer output) throws IOException {

		char[] buffer = new char[DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while (EOF != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
			count += n;
		}
		return (int) count;
	}
}