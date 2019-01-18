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
import org.junit.Test;

/**
 * Created by owen on 22/11/2017.
 */
public class IoUtilTest extends TestCase {

    @Test
    public void test() {
        String s = IoUtil.loadFile("performancescript/performance_search_template.txt");
        assertTrue(s.contains("orderby"));
    }
}