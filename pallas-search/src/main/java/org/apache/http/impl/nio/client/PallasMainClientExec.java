/*
 * ==================================================================== Licensed to the Apache Software Foundation (ASF)
 * under one or more contributor license agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals on behalf of the Apache Software
 * Foundation. For more information on the Apache Software Foundation, please see <http://www.apache.org/>.
 *
 */
package org.apache.http.impl.nio.client;

import java.io.IOException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.protocol.HttpProcessor;

class PallasMainClientExec extends MainClientExec {

	public PallasMainClientExec(HttpProcessor httpProcessor, HttpRoutePlanner routePlanner,
			RedirectStrategy redirectStrategy, AuthenticationStrategy targetAuthStrategy,
			AuthenticationStrategy proxyAuthStrategy, UserTokenHandler userTokenHandler) {
		super(httpProcessor, routePlanner, redirectStrategy, targetAuthStrategy, proxyAuthStrategy, userTokenHandler);
	}

	@Override
	public void requestCompleted(final InternalState state, final AbstractClientExchangeHandler<?> handler) {
		final HttpClientContext localContext = state.getLocalContext();
		if (localContext.getAttribute("connected") == null) {
			localContext.setAttribute("connected", System.currentTimeMillis());
		}
		super.requestCompleted(state, handler);
	}

	@Override
	public void responseReceived(final HttpResponse response, final InternalState state,
			final AbstractClientExchangeHandler<?> handler) throws IOException, HttpException {
		final HttpClientContext context = state.getLocalContext();
		if (context.getAttribute("responseReceived") == null) {
			context.setAttribute("responseReceived", System.currentTimeMillis());
		}
		if (context.getAttribute("finalURI") == null && handler != null && handler.getCurrentRequest() != null) {
			context.setAttribute("finalURI", handler.getCurrentRequest().getURI().toString());
		}
		super.responseReceived(response, state, handler);
	}

}
