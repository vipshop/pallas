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

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.eclipse.jetty.util.resource.Resource;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.List;


public class EmbeddedJettyServer {

	private List<Handler> handlers = new ArrayList<Handler>();

	private BrowseServer browseServer;

	public static void main(String[] args) throws Exception {
		new EmbeddedJettyServer().start();

	}

	public void start() throws Exception {
		browseServer = new BrowseServer(8081);
		HandlerList handlerList = new HandlerList();

		addServlet(ApiServlet.class, true, new String[] { "/pallas/*" });
		addResources();

		handlerList.setHandlers(handlers.toArray(new Handler[0]));
		browseServer.setHandler(handlerList);

		// set connector' s requestHeaderSize, for large cookies
		Connector[] connectors = browseServer.getConnectors();
		if (connectors != null) {
			for (Connector connector : connectors) {
				connector.setRequestHeaderSize(16384);
			}
		}

		browseServer.start();
		browseServer.join();
	}

	public void stop() throws Exception {
		if (browseServer != null) {
			browseServer.stop();
		}
	}

	public void addServlet(Class<? extends Servlet> servlet, boolean asyncSupported, String[] pathSpecs) {
		ServletHandler servletHandler = new ServletHandler();
		ServletHolder servletHolder = new ServletHolder(servlet);
		servletHolder.setName(servlet.getName());
		servletHolder.setAsyncSupported(asyncSupported);
		ServletMapping servletMapping = new ServletMapping();
		servletMapping.setServletName(servlet.getName());
		servletMapping.setPathSpecs(pathSpecs);
		servletHandler.addServlet(servletHolder);
		servletHandler.addServletMapping(servletMapping);
		handlers.add(servletHandler);
	}

	private void addResources() {
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(false);
		resourceHandler.setWelcomeFiles(new String[] {"index.html"});

		ContextHandler contextHandler = new ContextHandler();
		contextHandler.setContextPath("pallas");

		resourceHandler.setBaseResource(Resource.newClassPathResource("pallas-console"));
		handlers.add(resourceHandler);
		handlers.add(contextHandler);
	}
}