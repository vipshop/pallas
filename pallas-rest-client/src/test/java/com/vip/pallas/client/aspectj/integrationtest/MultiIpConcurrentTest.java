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

package com.vip.pallas.client.aspectj.integrationtest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpHost;
import org.apache.http.client.AuthCache;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;

public class MultiIpConcurrentTest {

	private final AtomicInteger lastHostIndex = new AtomicInteger(0);
	private volatile HostTuple<Set<HttpHost>> hostTuple;
	public static void main(String[] args) {
		final MultiIpConcurrentTest client = new MultiIpConcurrentTest();
		client.setHost();
		for (int i = 0; i < 30; i++) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					System.out.println(client.nextHost().hosts.next());
				}
			}).start();
		}

	}

	private void setHost() {
		Set<HttpHost> httpHosts = new HashSet<>();
		AuthCache authCache = new BasicAuthCache();

		HttpHost[] hosts = { HttpHost.create("127.0.0.1")};

		for (HttpHost host : hosts) {
			Objects.requireNonNull(host, "host cannot be null");
			httpHosts.add(host);
			authCache.put(host, new BasicScheme());
		}
		hostTuple = new HostTuple<>(Collections.unmodifiableSet(httpHosts), authCache);
	}

	private HostTuple<Iterator<HttpHost>> nextHost() {
		final HostTuple<Set<HttpHost>> hostTuple = this.hostTuple;
		Collection<HttpHost> nextHosts = Collections.emptySet();
		do {
			Set<HttpHost> filteredHosts = new HashSet<>(hostTuple.hosts);
			List<HttpHost> rotatedHosts = new ArrayList<>(filteredHosts);
			Collections.rotate(rotatedHosts, rotatedHosts.size() - lastHostIndex.getAndIncrement());
			nextHosts = rotatedHosts;
		} while (nextHosts.isEmpty());
		return new HostTuple<>(nextHosts.iterator(), hostTuple.authCache);
	}

	private static class HostTuple<T> {
		public final T hosts;
		public final AuthCache authCache;

		HostTuple(final T hosts, final AuthCache authCache) {
			this.hosts = hosts;
			this.authCache = authCache;
		}
	}
}