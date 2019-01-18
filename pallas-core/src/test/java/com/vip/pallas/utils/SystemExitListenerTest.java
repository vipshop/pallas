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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by owen on 12/7/2017.
 */
public class SystemExitListenerTest extends TestCase {

    @Test
    public void testListener() throws InterruptedException {

        CountDownLatch l1 = new CountDownLatch(1);
        CountDownLatch l2 = new CountDownLatch(1);

        SystemExitListener.addListener(new ExitHandler() {
            @Override
            public void run() {
                l1.countDown();
            }
        });

        SystemExitListener.addTerminateListener(new ExitHandler() {
            @Override
            public void run() {
                l2.countDown();
            }
        });

        SystemExitListener.notifyExit();
        l1.await();
        l2.await();
        assertTrue(SystemExitListener.isOver());
    }
}