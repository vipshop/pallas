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

package com.vip.pallas.thread;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ExtendableThreadPoolExecutorTest {

	static class SleepTask implements Runnable{
		@Override
		public void run() {
			try {
				TimeUnit.SECONDS.sleep(3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};
    @Test
    public void testNormalUsage() {
    	TaskQueue workQueue = new TaskQueue(1);
		ExtendableThreadPoolExecutor executor = new ExtendableThreadPoolExecutor(1, 2, 1, TimeUnit.MINUTES, workQueue, new PallasThreadFactory(
				"test-ExtendableThreadPoolExecutor-thread"));
    	executor.submit(new SleepTask());
    	executor.submit(new SleepTask());
    	assertThat(executor.getActiveCount()).isEqualTo(2);
    	assertThat(workQueue.size()).isEqualTo(0);
    	assertThat(workQueue.remainingCapacity()).isEqualTo(1);
    	executor.submit(new SleepTask());
    	assertThat(workQueue.size()).isEqualTo(1);
    	assertThat(workQueue.remainingCapacity()).isEqualTo(0);
    	assertThat(executor.getSubmittedCount()).isEqualTo(3);
    }
    @Test(expected = RejectedExecutionException.class)
    public void testThrowException() {
    	TaskQueue workQueue = new TaskQueue(1);
		ExtendableThreadPoolExecutor executor = new ExtendableThreadPoolExecutor(1, 1, 1, TimeUnit.MINUTES, workQueue, new PallasThreadFactory(
				"test-ExtendableThreadPoolExecutor-thread"));
    	executor.submit(new SleepTask());
    	executor.submit(new SleepTask());
    	executor.submit(new SleepTask());
    }

		
}