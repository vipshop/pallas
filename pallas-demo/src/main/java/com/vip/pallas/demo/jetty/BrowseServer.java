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

package com.vip.pallas.demo.jetty;

import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

public class BrowseServer extends Server {

	private static final Logger logger = LoggerFactory.getLogger(BrowseServer.class);

	public BrowseServer(int port) {
		super(port);
	}

	@Override
	protected void doStart() throws Exception {
		super.doStart();
	}

	public static void browseUri(Desktop dp, URI uri) {
		if (dp.isSupported(Desktop.Action.BROWSE)) {
			try {
				dp.browse(uri);
			} catch (IOException e) {
				logger.warn("[BrowseServer][can not get the default browser]", e);
			}
		} else {
			logger.warn("[BrowseServer][the \"browse\" action is not supported on the current platform]");
		}
	}
}