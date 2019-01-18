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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemExitListener {

	private static AtomicBoolean exited = new AtomicBoolean(false);
	private static List<ExitHandler> handlers = new ArrayList<ExitHandler>();
	private static List<ExitHandler> terminateHandlers = new ArrayList<ExitHandler>();

	static {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				notifyExit();
			}
		});
	}

	public static synchronized void addTerminateListener(ExitHandler th) {
		if (terminateHandlers.contains(th)) {
			return;
		}
		terminateHandlers.add(th);
	}

	public static synchronized void addListener(ExitHandler th) {
		if (handlers.contains(th)) {
			return;
		}
		handlers.add(th);
	}

	public static boolean isOver() {
		return exited.get();
	}

	public static void notifyExit() {
		if (exited.getAndSet(true)) {
			return;
		}
		excuteHandler(handlers, () -> System.out.println("开始退出清理第一步..."), // NOSONAR
				() -> System.out.println("结束退出清理第一步...")); // NOSONAR

		excuteHandler(terminateHandlers, () -> System.out.println("开始退出清理最后一步..."), // NOSONAR
				() -> System.out.println("结束退出清理最后一步...")); // NOSONAR
	}

	private static void excuteHandler(List<ExitHandler> handlers, Runnable preDo, Runnable afterDo) {
		if (!handlers.isEmpty()) {
			final CountDownLatch doneSignal = new CountDownLatch(handlers.size());
			for (final ExitHandler t : handlers) {
				Thread tt = new Thread(() -> {
					t.execute();
					doneSignal.countDown();
				});
				tt.start();
			}
			preDo.run();
			try {
				doneSignal.await();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} finally {
				afterDo.run();
			}
		}
	}
}