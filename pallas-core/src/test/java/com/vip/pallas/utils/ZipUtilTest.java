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

import junit.framework.TestCase;
import org.assertj.core.util.Files;
import org.junit.Test;

import java.io.File;

/**
 * Created by owen on 20/11/2017.
 */
public class ZipUtilTest extends TestCase {

	@Test
	public void testNormal() {

		File file = new File(System.getProperty("java.io.tmpdir") + "/test_zip/");
		ZipUtil.unZip(ZipUtilTest.class.getClassLoader().getResource("utils/test.zip").getPath(),
				file.getAbsolutePath() + "/");

		assertTrue(file.listFiles().length > 0);

		Files.delete(file);

	}

}