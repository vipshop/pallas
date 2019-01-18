/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.http.impl.nio.client;

import java.net.ProxySelector;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.RequestAddCookies;
import org.apache.http.client.protocol.RequestAuthCache;
import org.apache.http.client.protocol.RequestClientConnControl;
import org.apache.http.client.protocol.RequestDefaultHeaders;
import org.apache.http.client.protocol.RequestExpectContinue;
import org.apache.http.client.protocol.ResponseProcessCookies;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.SchemePortResolver;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.conn.util.PublicSuffixMatcher;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.auth.DigestSchemeFactory;
import org.apache.http.impl.auth.KerberosSchemeFactory;
import org.apache.http.impl.auth.NTLMSchemeFactory;
import org.apache.http.impl.auth.SPNegoSchemeFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.NoopUserTokenHandler;
import org.apache.http.impl.client.ProxyAuthenticationStrategy;
import org.apache.http.impl.client.TargetAuthenticationStrategy;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.impl.conn.DefaultSchemePortResolver;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.impl.cookie.DefaultCookieSpecProvider;
import org.apache.http.impl.cookie.IgnoreSpecProvider;
import org.apache.http.impl.cookie.NetscapeDraftSpecProvider;
import org.apache.http.impl.cookie.RFC6265CookieSpecProvider;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.NHttpClientEventHandler;
import org.apache.http.nio.conn.NHttpClientConnectionManager;
import org.apache.http.nio.conn.NoopIOSessionStrategy;
import org.apache.http.nio.conn.SchemeIOSessionStrategy;
import org.apache.http.nio.conn.ssl.SSLIOSessionStrategy;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpProcessorBuilder;
import org.apache.http.protocol.RequestContent;
import org.apache.http.protocol.RequestTargetHost;
import org.apache.http.protocol.RequestUserAgent;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.TextUtils;
import org.apache.http.util.VersionInfo;

/**
 * Builder for {@link CloseableHttpAsyncClient} instances.
 * <p>
 * When a particular component is not explicitly this class will
 * use its default implementation. System properties will be taken
 * into account when configuring the default implementations when
 * {@link #useSystemProperties()} method is called prior to calling
 * {@link #build()}.
 * <ul>
 *  <li>ssl.TrustManagerFactory.algorithm</li>
 *  <li>javax.net.ssl.trustStoreType</li>
 *  <li>javax.net.ssl.trustStore</li>
 *  <li>javax.net.ssl.trustStoreProvider</li>
 *  <li>javax.net.ssl.trustStorePassword</li>
 *  <li>ssl.KeyManagerFactory.algorithm</li>
 *  <li>javax.net.ssl.keyStoreType</li>
 *  <li>javax.net.ssl.keyStore</li>
 *  <li>javax.net.ssl.keyStoreProvider</li>
 *  <li>javax.net.ssl.keyStorePassword</li>
 *  <li>https.protocols</li>
 *  <li>https.cipherSuites</li>
 *  <li>http.proxyHost</li>
 *  <li>http.proxyPort</li>
 *  <li>http.keepAlive</li>
 *  <li>http.maxConnections</li>
 *  <li>http.agent</li>
 * </ul>
 * <p>
 * Please note that some settings used by this class can be mutually
 * exclusive and may not apply when building {@link CloseableHttpAsyncClient}
 * instances.
 * <p>
 * Pallas team modified line 849, changed MainClientExec to PallasMainClientExec.
 * 
 * @since 4.0
 */
public class PallasHttpAsyncClientBuilder {

    private NHttpClientConnectionManager connManager;
	private boolean connManagerShared;
	private SchemePortResolver schemePortResolver;
	private SchemeIOSessionStrategy sslStrategy;
	private HostnameVerifier hostnameVerifier;
	private SSLContext sslcontext;
	private ConnectionReuseStrategy reuseStrategy;
	private ConnectionKeepAliveStrategy keepAliveStrategy;
	private AuthenticationStrategy targetAuthStrategy;
	private AuthenticationStrategy proxyAuthStrategy;
	private UserTokenHandler userTokenHandler;
	private HttpProcessor httpprocessor;

	private LinkedList<HttpRequestInterceptor> requestFirst;
	private LinkedList<HttpRequestInterceptor> requestLast;
	private LinkedList<HttpResponseInterceptor> responseFirst;
	private LinkedList<HttpResponseInterceptor> responseLast;

	private HttpRoutePlanner routePlanner;
	private RedirectStrategy redirectStrategy;
	private Lookup<AuthSchemeProvider> authSchemeRegistry;
	private Lookup<CookieSpecProvider> cookieSpecRegistry;
	private CookieStore cookieStore;
	private CredentialsProvider credentialsProvider;
	private String userAgent;
	private HttpHost proxy;
	private Collection<? extends Header> defaultHeaders;
	private IOReactorConfig defaultIOReactorConfig;
	private ConnectionConfig defaultConnectionConfig;
	private RequestConfig defaultRequestConfig;

	private ThreadFactory threadFactory;
	private NHttpClientEventHandler eventHandler;

	private PublicSuffixMatcher publicSuffixMatcher;

	private boolean systemProperties;
	private boolean cookieManagementDisabled;
	private boolean authCachingDisabled;
	private boolean connectionStateDisabled;

	private int maxConnTotal = 0;
	private int maxConnPerRoute = 0;

	public static PallasHttpAsyncClientBuilder create() {
		return new PallasHttpAsyncClientBuilder();
    }

	protected PallasHttpAsyncClientBuilder() {
        super();
    }

    /**
     * Assigns file containing public suffix matcher. Instances of this class can be created
     * with {@link org.apache.http.conn.util.PublicSuffixMatcherLoader}.
     *
     * @see org.apache.http.conn.util.PublicSuffixMatcher
     * @see org.apache.http.conn.util.PublicSuffixMatcherLoader
     *
     *   @since 4.1
     */
	public final PallasHttpAsyncClientBuilder setPublicSuffixMatcher(final PublicSuffixMatcher publicSuffixMatcher) {
        this.publicSuffixMatcher = publicSuffixMatcher;
        return this;
    }

    /**
     * Assigns {@link NHttpClientConnectionManager} instance.
     */
	public final PallasHttpAsyncClientBuilder setConnectionManager(
            final NHttpClientConnectionManager connManager) {
        this.connManager = connManager;
        return this;
    }

    /**
     * Defines the connection manager is to be shared by multiple
     * client instances.
     * <p>
     * If the connection manager is shared its life-cycle is expected
     * to be managed by the caller and it will not be shut down
     * if the client is closed.
     *
     * @param shared defines whether or not the connection manager can be shared
     *  by multiple clients.
     *
     * @since 4.1
     */
	public final PallasHttpAsyncClientBuilder setConnectionManagerShared(
            final boolean shared) {
        this.connManagerShared = shared;
        return this;
    }

    /**
     * Assigns {@link SchemePortResolver} instance.
     */
	public final PallasHttpAsyncClientBuilder setSchemePortResolver(
            final SchemePortResolver schemePortResolver) {
        this.schemePortResolver = schemePortResolver;
        return this;
    }

    /**
     * Assigns maximum total connection value.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   org.apache.http.nio.conn.NHttpClientConnectionManager)} method.
     */
	public final PallasHttpAsyncClientBuilder setMaxConnTotal(final int maxConnTotal) {
        this.maxConnTotal = maxConnTotal;
        return this;
    }

    /**
     * Assigns maximum connection per route value.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   org.apache.http.nio.conn.NHttpClientConnectionManager)} method.
     */
	public final PallasHttpAsyncClientBuilder setMaxConnPerRoute(final int maxConnPerRoute) {
        this.maxConnPerRoute = maxConnPerRoute;
        return this;
    }

    /**
     * Assigns {@link ConnectionReuseStrategy} instance.
     */
	public final PallasHttpAsyncClientBuilder setConnectionReuseStrategy(
            final ConnectionReuseStrategy reuseStrategy) {
        this.reuseStrategy = reuseStrategy;
        return this;
    }

    /**
     * Assigns {@link ConnectionKeepAliveStrategy} instance.
     */
	public final PallasHttpAsyncClientBuilder setKeepAliveStrategy(
            final ConnectionKeepAliveStrategy keepAliveStrategy) {
        this.keepAliveStrategy = keepAliveStrategy;
        return this;
    }

    /**
     * Assigns {@link UserTokenHandler} instance.
     * <p>
     * Please note this value can be overridden by the {@link #disableConnectionState()}
     * method.
     */
	public final PallasHttpAsyncClientBuilder setUserTokenHandler(final UserTokenHandler userTokenHandler) {
        this.userTokenHandler = userTokenHandler;
        return this;
    }

    /**
     * Assigns {@link AuthenticationStrategy} instance for proxy
     * authentication.
     */
	public final PallasHttpAsyncClientBuilder setTargetAuthenticationStrategy(
            final AuthenticationStrategy targetAuthStrategy) {
        this.targetAuthStrategy = targetAuthStrategy;
        return this;
    }

    /**
     * Assigns {@link AuthenticationStrategy} instance for target
     * host authentication.
     */
	public final PallasHttpAsyncClientBuilder setProxyAuthenticationStrategy(
            final AuthenticationStrategy proxyAuthStrategy) {
        this.proxyAuthStrategy = proxyAuthStrategy;
        return this;
    }

    /**
     * Assigns {@link HttpProcessor} instance.
     */
	public final PallasHttpAsyncClientBuilder setHttpProcessor(final HttpProcessor httpprocessor) {
        this.httpprocessor = httpprocessor;
        return this;
    }

    /**
     * Adds this protocol interceptor to the head of the protocol processing list.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * org.apache.http.protocol.HttpProcessor)} method.
     */
	public final PallasHttpAsyncClientBuilder addInterceptorFirst(final HttpResponseInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (responseFirst == null) {
            responseFirst = new LinkedList<HttpResponseInterceptor>();
        }
        responseFirst.addFirst(itcp);
        return this;
    }

    /**
     * Adds this protocol interceptor to the tail of the protocol processing list.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * org.apache.http.protocol.HttpProcessor)} method.
     */
	public final PallasHttpAsyncClientBuilder addInterceptorLast(final HttpResponseInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (responseLast == null) {
            responseLast = new LinkedList<HttpResponseInterceptor>();
        }
        responseLast.addLast(itcp);
        return this;
    }

    /**
     * Adds this protocol interceptor to the head of the protocol processing list.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * org.apache.http.protocol.HttpProcessor)} method.
     */
	public final PallasHttpAsyncClientBuilder addInterceptorFirst(final HttpRequestInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (requestFirst == null) {
            requestFirst = new LinkedList<HttpRequestInterceptor>();
        }
        requestFirst.addFirst(itcp);
        return this;
    }

    /**
     * Adds this protocol interceptor to the tail of the protocol processing list.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * org.apache.http.protocol.HttpProcessor)} method.
     */
	public final PallasHttpAsyncClientBuilder addInterceptorLast(final HttpRequestInterceptor itcp) {
        if (itcp == null) {
            return this;
        }
        if (requestLast == null) {
            requestLast = new LinkedList<HttpRequestInterceptor>();
        }
        requestLast.addLast(itcp);
        return this;
    }

    /**
     * Assigns {@link HttpRoutePlanner} instance.
     */
	public final PallasHttpAsyncClientBuilder setRoutePlanner(final HttpRoutePlanner routePlanner) {
        this.routePlanner = routePlanner;
        return this;
    }

    /**
     * Assigns {@link RedirectStrategy} instance.
     */
	public final PallasHttpAsyncClientBuilder setRedirectStrategy(final RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
        return this;
    }

    /**
     * Assigns default {@link CookieStore} instance which will be used for
     * request execution if not explicitly set in the client execution context.
     */
	public final PallasHttpAsyncClientBuilder setDefaultCookieStore(final CookieStore cookieStore) {
        this.cookieStore = cookieStore;
        return this;
    }

    /**
     * Assigns default {@link CredentialsProvider} instance which will be used
     * for request execution if not explicitly set in the client execution
     * context.
     */
	public final PallasHttpAsyncClientBuilder setDefaultCredentialsProvider(
            final CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }


    /**
     * Assigns default {@link org.apache.http.auth.AuthScheme} registry which will
     * be used for request execution if not explicitly set in the client execution
     * context.
     */
	public final PallasHttpAsyncClientBuilder setDefaultAuthSchemeRegistry(
            final Lookup<AuthSchemeProvider> authSchemeRegistry) {
        this.authSchemeRegistry = authSchemeRegistry;
        return this;
    }

    /**
     * Assigns default {@link org.apache.http.cookie.CookieSpec} registry which will
     * be used for request execution if not explicitly set in the client execution
     * context.
     */
	public final PallasHttpAsyncClientBuilder setDefaultCookieSpecRegistry(
            final Lookup<CookieSpecProvider> cookieSpecRegistry) {
        this.cookieSpecRegistry = cookieSpecRegistry;
        return this;
    }

    /**
     * Assigns {@code User-Agent} value.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * org.apache.http.protocol.HttpProcessor)} method.
     */
	public final PallasHttpAsyncClientBuilder setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Assigns default proxy value.
     * <p>
     * Please note this value can be overridden by the {@link #setRoutePlanner(
     *   org.apache.http.conn.routing.HttpRoutePlanner)} method.
     */
	public final PallasHttpAsyncClientBuilder setProxy(final HttpHost proxy) {
        this.proxy = proxy;
        return this;
    }

    /**
     * Assigns {@link SchemeIOSessionStrategy} instance.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   org.apache.http.nio.conn.NHttpClientConnectionManager)} method.
     */
	public final PallasHttpAsyncClientBuilder setSSLStrategy(final SchemeIOSessionStrategy strategy) {
        this.sslStrategy = strategy;
        return this;
    }

    /**
     * Assigns {@link SSLContext} instance.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   org.apache.http.nio.conn.NHttpClientConnectionManager)} and the {@link #setSSLStrategy(
     *   org.apache.http.nio.conn.SchemeIOSessionStrategy)} methods.
     */
	public final PallasHttpAsyncClientBuilder setSSLContext(final SSLContext sslcontext) {
        this.sslcontext = sslcontext;
        return this;
    }

    /**
     * Assigns {@link X509HostnameVerifier} instance.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   org.apache.http.nio.conn.NHttpClientConnectionManager)} and the {@link #setSSLStrategy(
     *   org.apache.http.nio.conn.SchemeIOSessionStrategy)} methods.
     *
     * @deprecated (4.1) use {@link #setSSLHostnameVerifier(javax.net.ssl.HostnameVerifier)}
     */
    @Deprecated
	public final PallasHttpAsyncClientBuilder setHostnameVerifier(final X509HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    /**
     * Assigns {@link javax.net.ssl.HostnameVerifier} instance.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   org.apache.http.nio.conn.NHttpClientConnectionManager)} and the {@link #setSSLStrategy(
     *   org.apache.http.nio.conn.SchemeIOSessionStrategy)} methods.
     *
     * @since 4.1
     */
	public final PallasHttpAsyncClientBuilder setSSLHostnameVerifier(final HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    /**
     * Assigns default request header values.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * org.apache.http.protocol.HttpProcessor)} method.
     */
	public final PallasHttpAsyncClientBuilder setDefaultHeaders(final Collection<? extends Header> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
        return this;
    }

    /**
     * Assigns default {@link IOReactorConfig}.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   org.apache.http.nio.conn.NHttpClientConnectionManager)} method.
     */
	public final PallasHttpAsyncClientBuilder setDefaultIOReactorConfig(final IOReactorConfig config) {
        this.defaultIOReactorConfig = config;
        return this;
    }

    /**
     * Assigns default {@link ConnectionConfig}.
     * <p>
     * Please note this value can be overridden by the {@link #setConnectionManager(
     *   org.apache.http.nio.conn.NHttpClientConnectionManager)} method.
     */
	public final PallasHttpAsyncClientBuilder setDefaultConnectionConfig(final ConnectionConfig config) {
        this.defaultConnectionConfig = config;
        return this;
    }

    /**
     * Assigns default {@link RequestConfig} instance which will be used
     * for request execution if not explicitly set in the client execution
     * context.
     */
	public final PallasHttpAsyncClientBuilder setDefaultRequestConfig(final RequestConfig config) {
        this.defaultRequestConfig = config;
        return this;
    }

    /**
     * Assigns {@link ThreadFactory} instance.
     */
	public final PallasHttpAsyncClientBuilder setThreadFactory(final ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
        return this;
    }

    /**
     * Assigns {@link NHttpClientEventHandler} instance.
     *
     * @since 4.1
     */
	public final PallasHttpAsyncClientBuilder setEventHandler(final NHttpClientEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        return this;
    }

    /**
     * Disables connection state tracking.
     */
	public final PallasHttpAsyncClientBuilder disableConnectionState() {
        connectionStateDisabled = true;
        return this;
    }

    /**
     * Disables state (cookie) management.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * org.apache.http.protocol.HttpProcessor)} method.
     */
	public final PallasHttpAsyncClientBuilder disableCookieManagement() {
        cookieManagementDisabled = true;
        return this;
    }

    /**
     * Disables authentication scheme caching.
     * <p>
     * Please note this value can be overridden by the {@link #setHttpProcessor(
     * org.apache.http.protocol.HttpProcessor)} method.
     */
	public final PallasHttpAsyncClientBuilder disableAuthCaching() {
        authCachingDisabled = true;
        return this;
    }

    /**
     * Use system properties when creating and configuring default
     * implementations.
     */
	public final PallasHttpAsyncClientBuilder useSystemProperties() {
        systemProperties = true;
        return this;
    }

    private static String[] split(final String s) {
        if (TextUtils.isBlank(s)) {
            return null;
        }
        return s.split(" *, *");
    }

    public CloseableHttpAsyncClient build() {

        PublicSuffixMatcher publicSuffixMatcher = this.publicSuffixMatcher;
        if (publicSuffixMatcher == null) {
            publicSuffixMatcher = PublicSuffixMatcherLoader.getDefault();
        }

        NHttpClientConnectionManager connManager = this.connManager;
        if (connManager == null) {
            SchemeIOSessionStrategy sslStrategy = this.sslStrategy;
            if (sslStrategy == null) {
                SSLContext sslcontext = this.sslcontext;
                if (sslcontext == null) {
                    if (systemProperties) {
                        sslcontext = SSLContexts.createSystemDefault();
                    } else {
                        sslcontext = SSLContexts.createDefault();
                    }
                }
                final String[] supportedProtocols = systemProperties ? split(
                        System.getProperty("https.protocols")) : null;
                final String[] supportedCipherSuites = systemProperties ? split(
                        System.getProperty("https.cipherSuites")) : null;
                HostnameVerifier hostnameVerifier = this.hostnameVerifier;
                if (hostnameVerifier == null) {
                    hostnameVerifier = new DefaultHostnameVerifier(publicSuffixMatcher);
                }
                sslStrategy = new SSLIOSessionStrategy(
                        sslcontext, supportedProtocols, supportedCipherSuites, hostnameVerifier);
            }
            final ConnectingIOReactor ioreactor = IOReactorUtils.create(
                defaultIOReactorConfig != null ? defaultIOReactorConfig : IOReactorConfig.DEFAULT, threadFactory);
            final PoolingNHttpClientConnectionManager poolingmgr = new PoolingNHttpClientConnectionManager(
                    ioreactor,
                    RegistryBuilder.<SchemeIOSessionStrategy>create()
                        .register("http", NoopIOSessionStrategy.INSTANCE)
                        .register("https", sslStrategy)
                        .build());
            if (defaultConnectionConfig != null) {
                poolingmgr.setDefaultConnectionConfig(defaultConnectionConfig);
            }
            if (systemProperties) {
                String s = System.getProperty("http.keepAlive", "true");
                if ("true".equalsIgnoreCase(s)) {
                    s = System.getProperty("http.maxConnections", "5");
                    final int max = Integer.parseInt(s);
                    poolingmgr.setDefaultMaxPerRoute(max);
                    poolingmgr.setMaxTotal(2 * max);
                }
            } else {
                if (maxConnTotal > 0) {
                    poolingmgr.setMaxTotal(maxConnTotal);
                }
                if (maxConnPerRoute > 0) {
                    poolingmgr.setDefaultMaxPerRoute(maxConnPerRoute);
                }
            }
            connManager = poolingmgr;
        }
        ConnectionReuseStrategy reuseStrategy = this.reuseStrategy;
        if (reuseStrategy == null) {
            if (systemProperties) {
                final String s = System.getProperty("http.keepAlive", "true");
                if ("true".equalsIgnoreCase(s)) {
                    reuseStrategy = DefaultConnectionReuseStrategy.INSTANCE;
                } else {
                    reuseStrategy = NoConnectionReuseStrategy.INSTANCE;
                }
            } else {
                reuseStrategy = DefaultConnectionReuseStrategy.INSTANCE;
            }
        }
        ConnectionKeepAliveStrategy keepAliveStrategy = this.keepAliveStrategy;
        if (keepAliveStrategy == null) {
            keepAliveStrategy = DefaultConnectionKeepAliveStrategy.INSTANCE;
        }
        AuthenticationStrategy targetAuthStrategy = this.targetAuthStrategy;
        if (targetAuthStrategy == null) {
            targetAuthStrategy = TargetAuthenticationStrategy.INSTANCE;
        }
        AuthenticationStrategy proxyAuthStrategy = this.proxyAuthStrategy;
        if (proxyAuthStrategy == null) {
            proxyAuthStrategy = ProxyAuthenticationStrategy.INSTANCE;
        }
        UserTokenHandler userTokenHandler = this.userTokenHandler;
        if (userTokenHandler == null) {
            if (!connectionStateDisabled) {
                userTokenHandler = DefaultAsyncUserTokenHandler.INSTANCE;
            } else {
                userTokenHandler = NoopUserTokenHandler.INSTANCE;
            }
        }
        SchemePortResolver schemePortResolver = this.schemePortResolver;
        if (schemePortResolver == null) {
            schemePortResolver = DefaultSchemePortResolver.INSTANCE;
        }

        HttpProcessor httpprocessor = this.httpprocessor;
        if (httpprocessor == null) {

            String userAgent = this.userAgent;
            if (userAgent == null) {
                if (systemProperties) {
                    userAgent = System.getProperty("http.agent");
                }
                if (userAgent == null) {
                    userAgent = VersionInfo.getUserAgent(
                            "Apache-HttpAsyncClient",
                            "org.apache.http.nio.client", getClass());
                }
            }

            final HttpProcessorBuilder b = HttpProcessorBuilder.create();
            if (requestFirst != null) {
                for (final HttpRequestInterceptor i: requestFirst) {
                    b.addFirst(i);
                }
            }
            if (responseFirst != null) {
                for (final HttpResponseInterceptor i: responseFirst) {
                    b.addFirst(i);
                }
            }
            b.addAll(
                    new RequestDefaultHeaders(defaultHeaders),
                    new RequestContent(),
                    new RequestTargetHost(),
                    new RequestClientConnControl(),
                    new RequestUserAgent(userAgent),
                    new RequestExpectContinue());
            if (!cookieManagementDisabled) {
                b.add(new RequestAddCookies());
            }
            if (!authCachingDisabled) {
                b.add(new RequestAuthCache());
            }
            if (!cookieManagementDisabled) {
                b.add(new ResponseProcessCookies());
            }
            if (requestLast != null) {
                for (final HttpRequestInterceptor i: requestLast) {
                    b.addLast(i);
                }
            }
            if (responseLast != null) {
                for (final HttpResponseInterceptor i: responseLast) {
                    b.addLast(i);
                }
            }
            httpprocessor = b.build();
        }
        // Add redirect executor, if not disabled
        HttpRoutePlanner routePlanner = this.routePlanner;
        if (routePlanner == null) {
            if (proxy != null) {
                routePlanner = new DefaultProxyRoutePlanner(proxy, schemePortResolver);
            } else if (systemProperties) {
                routePlanner = new SystemDefaultRoutePlanner(
                        schemePortResolver, ProxySelector.getDefault());
            } else {
                routePlanner = new DefaultRoutePlanner(schemePortResolver);
            }
        }
        Lookup<AuthSchemeProvider> authSchemeRegistry = this.authSchemeRegistry;
        if (authSchemeRegistry == null) {
            authSchemeRegistry = RegistryBuilder.<AuthSchemeProvider>create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactory())
                .register(AuthSchemes.DIGEST, new DigestSchemeFactory())
                .register(AuthSchemes.NTLM, new NTLMSchemeFactory())
                .register(AuthSchemes.SPNEGO, new SPNegoSchemeFactory())
                .register(AuthSchemes.KERBEROS, new KerberosSchemeFactory())
                .build();
        }
        Lookup<CookieSpecProvider> cookieSpecRegistry = this.cookieSpecRegistry;
        if (cookieSpecRegistry == null) {
            final CookieSpecProvider defaultProvider = new DefaultCookieSpecProvider(publicSuffixMatcher);
            final CookieSpecProvider laxStandardProvider = new RFC6265CookieSpecProvider(
                    RFC6265CookieSpecProvider.CompatibilityLevel.RELAXED, publicSuffixMatcher);
            final CookieSpecProvider strictStandardProvider = new RFC6265CookieSpecProvider(
                    RFC6265CookieSpecProvider.CompatibilityLevel.STRICT, publicSuffixMatcher);
            cookieSpecRegistry = RegistryBuilder.<CookieSpecProvider>create()
                    .register(CookieSpecs.DEFAULT, defaultProvider)
                    .register("best-match", defaultProvider)
                    .register("compatibility", defaultProvider)
                    .register(CookieSpecs.STANDARD, laxStandardProvider)
                    .register(CookieSpecs.STANDARD_STRICT, strictStandardProvider)
                    .register(CookieSpecs.NETSCAPE, new NetscapeDraftSpecProvider())
                    .register(CookieSpecs.IGNORE_COOKIES, new IgnoreSpecProvider())
                    .build();
        }

        CookieStore defaultCookieStore = this.cookieStore;
        if (defaultCookieStore == null) {
            defaultCookieStore = new BasicCookieStore();
        }

        CredentialsProvider defaultCredentialsProvider = this.credentialsProvider;
        if (defaultCredentialsProvider == null) {
            defaultCredentialsProvider = new BasicCredentialsProvider();
        }

        RedirectStrategy redirectStrategy = this.redirectStrategy;
        if (redirectStrategy == null) {
            redirectStrategy = DefaultRedirectStrategy.INSTANCE;
        }

        RequestConfig defaultRequestConfig = this.defaultRequestConfig;
        if (defaultRequestConfig == null) {
            defaultRequestConfig = RequestConfig.DEFAULT;
        }

        final PallasMainClientExec exec = new PallasMainClientExec(
            httpprocessor,
            routePlanner,
            redirectStrategy,
            targetAuthStrategy,
            proxyAuthStrategy,
            userTokenHandler);

        ThreadFactory threadFactory = null;
        NHttpClientEventHandler eventHandler = null;
        if (!this.connManagerShared) {
            threadFactory = this.threadFactory;
            if (threadFactory == null) {
                threadFactory = Executors.defaultThreadFactory();
            }
            eventHandler = this.eventHandler;
            if (eventHandler == null) {
                eventHandler = new HttpAsyncRequestExecutor();
            }
        }
        return new InternalHttpAsyncClient(
            connManager,
            reuseStrategy,
            keepAliveStrategy,
            threadFactory,
            eventHandler,
            exec,
            cookieSpecRegistry,
            authSchemeRegistry,
            defaultCookieStore,
            defaultCredentialsProvider,
            defaultRequestConfig);
    }

}
