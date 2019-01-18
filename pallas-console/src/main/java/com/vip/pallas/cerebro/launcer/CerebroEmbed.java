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

package com.vip.pallas.cerebro.launcer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vip.pallas.exception.PallasException;
import com.vip.pallas.utils.PallasConsoleProperties;

public class CerebroEmbed {
	private static Logger logger = LoggerFactory.getLogger(CerebroEmbed.class);

	/**
	 * 递归删除目录下的所有文件及子目录下所有文件
	 * @param dir 将要删除的文件目录
	 * @return boolean Returns "true" if all deletions were successful. If a deletion fails, the method stops attempting
	 * to delete and returns "false".
	 */
	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			if(children != null){
				for (int i = 0; i < children.length; i++) {
					boolean success = deleteDir(new File(dir, children[i]));
					if (!success) {
						return false;
					}
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	public static void unzip(File zip, File directory) throws ZipException, IOException {
		ZipFile zipFile = new ZipFile(zip);
		try {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry zipEntry = entries.nextElement();
				if (zipEntry.isDirectory()) {
					File temp = new File(directory + File.separator + zipEntry.getName());
					temp.mkdirs();
					continue;
				}
				BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
				File f = new File(directory + File.separator + zipEntry.getName());
				File f_p = f.getParentFile();
				if (f_p != null && !f_p.exists()) {
					f_p.mkdirs();
				}
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
				int len = -1;
				byte[] bs = new byte[2048];
				while ((len = bis.read(bs, 0, 2048)) != -1) {
					bos.write(bs, 0, len);
				}
				bos.flush();
				bos.close();
				bis.close();
			}
		} finally {
			zipFile.close();
		}
	}

	public static void launch() throws PallasException, IOException {
		InputStream is = CerebroEmbed.class.getClassLoader().getResourceAsStream("cerebro.zip");

		File zipFile = new File(System.getProperty("user.home") + File.separator + "cerebro.zip");
		if (zipFile.exists()) {
			zipFile.delete();
		}
		FileOutputStream fs = null;
		
		try {
			fs = new FileOutputStream(zipFile);
			byte[] buf = new byte[64];
			int readed = is.read(buf);
			while (readed > 0) {
				fs.write(buf, 0, readed);
				readed = is.read(buf);
			}
		} catch (Exception e) {
			logger.error(e.toString(), e);
		} finally {
			if(fs != null){
				fs.close();
			}
			is.close();
		}

		File cerebroHome = null;
		if (zipFile.canRead()) {
			cerebroHome = new File(System.getProperty("user.home") + File.separator + "cerebro");
			deleteDir(cerebroHome);
			unzip(zipFile, cerebroHome);
			CerebroLauncher.launch(cerebroHome.getAbsolutePath(), PallasConsoleProperties.CEREBRO_PORT);
		} else {
			logger.error("{} not readable, cerebro can not be started! ", zipFile.getAbsolutePath());
		}
	}
}