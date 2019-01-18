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

import java.io.*;
import java.util.Enumeration;

import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.exception.BusinessLevelException;

import static java.nio.file.Files.newOutputStream;

public class ZipUtil {
	private static final int BUFFER_SIZE = 2048;
	private static final char FILE_SEPERATOR = '/';

	private static final Logger LOGGER = LoggerFactory.getLogger(ZipUtil.class);

	private ZipUtil() {
		throw new IllegalStateException("Utility class");
	}

	private static void mkdirIfNotExists(File dir) {
		if (dir.exists()) {
			return;
		}
		if (!dir.mkdirs()) {// 创建目录
			throw new BusinessLevelException("创建目录失败：" + dir.getAbsolutePath());
		}
	}

	/**
	 * 解压Zip文件
	 * @param zipFilePath 文件目录
	 */
	public static void unZip(String zipFilePath, String targetPath) {
		String thisTargetPath = targetPath;
		if (!thisTargetPath.endsWith("/")) {
			thisTargetPath += "/";
		}

		int count = -1;
		File folder = new File(thisTargetPath);
		mkdirIfNotExists(folder);
		try (ZipFile zipFile = new ZipFile(zipFilePath, "gbk")) {
			Enumeration<?> entries = zipFile.getEntries();

			while (entries.hasMoreElements()) {
				byte buf[] = new byte[BUFFER_SIZE];

				ZipEntry entry = (ZipEntry) entries.nextElement();

				String filename = entry.getName();

				filename = thisTargetPath + filename;
				File file = new File(filename);

				if (entry.isDirectory()) { // 如果是文件夹先创建
					mkdirIfNotExists(file);
					continue;
				}

				if (filename.lastIndexOf(FILE_SEPERATOR) != -1) { // 检查此文件是否带有文件夹
					File dir = new File(filename.substring(0, filename.lastIndexOf('/')));
					mkdirIfNotExists(dir);
				}

				if (!file.createNewFile()) {
					throw new BusinessLevelException("创建文件失败：" + file.getAbsolutePath());
				}

				try (InputStream is = zipFile.getInputStream(entry);
					 OutputStream fos = newOutputStream(file.toPath()); // NOSONAR
					 BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER_SIZE);) {
					while ((count = is.read(buf)) > -1) {
						bos.write(buf, 0, count);
					}
					bos.flush();
				}

			}
		} catch (IOException ioe) {
			LOGGER.error(ioe.toString(), ioe);
		}
	}
}