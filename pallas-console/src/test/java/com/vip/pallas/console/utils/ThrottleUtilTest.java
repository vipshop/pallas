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

package com.vip.pallas.console.utils;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by owen on 10/7/2017.
 */
public class ThrottleUtilTest extends TestCase {

    @Test
    public void testEsClusterInc () {
        ThrottleUtil.esClusterInc("test_cluster");
        ThrottleUtil.esClusterInc("test_cluster");
        AtomicInteger c = ThrottleUtil.getCounter("cluster:test_cluster");
        assertEquals(2, c.get());
    }

    @Test
    public void testEsClusterDesc () {
        ThrottleUtil.esClusterInc("test_cluster2");
        ThrottleUtil.esClusterInc("test_cluster2");
        ThrottleUtil.esClusterDesc("test_cluster2");
        AtomicInteger c = ThrottleUtil.getCounter("cluster:test_cluster2");
        assertEquals(1, c.get());
    }
}