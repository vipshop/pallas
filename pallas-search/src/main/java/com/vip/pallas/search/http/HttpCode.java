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

package com.vip.pallas.search.http;

public class HttpCode {
	public static final int HTTP_OK_CODE = 200;
	
	public static final int HTTP_NON_AUTHORITATIVE_INFORMATION = 203;

	public static final int HTTP_REDIRECT_CODE = 300;
	
	public static final int HTTP_REQUEST_URI_TOO_LONG = 414;
	//调用osp服务不存在时返回460
	public static final int PROXY = 460;
	//调用osp方法不存在
	public static final int CALLEE = 550;
	public static final int HTTP_FORBIDDEN = 403;
	public static final int HTTP_NOT_FOUND = 404;
	public static final int HTTP_UNSUPPORTED_MEDIA_TYPE=415;
	public static final int HTTP_METHOD_NOT_ALLOWED =405;
	public static final int HTTP_PRECONDITION_FAILED = 412;
	public static final int HTTP_BAD_REQUEST = 400;
	public static final int HTTP_UNAUTHORIZED = 401;
	
	public static final int HTTP_UNPROCESSABLE_ENTITY = 422;
	public static final int HTTP_TOO_MANY_REQUESTS = 429;
	
	public static final int HTTP_PARSER_FAILED = 499;
	
	public static final int HTTP_INTERNAL_SERVER_ERROR = 500;
	public static final int HTTP_BAD_GATEWAY = 502;
	public static final int HTTP_SERVICE_UNAVAILABLE = 503;
	public static final int HTTP_GATEWAY_TIMEOUT = 504;
	public static final int HTTP_GATEWAY_SHUTDOWN = 512;
	public static final int HTTP_RETRY_TIMEOUT = 520;
	
	public static final int HTTP_REQUEST_ENTITY_TOO_LARGE=413;

	public static final int HTTP_ANTI_BRUSH=911; //防刷拒绝状态码
	public static final int HTTP_WAF_FORBIDDEN = 912; //waf拒绝状态码
	public static final int HTTP_LIMITER = 914; // 限流拒绝状态码
	//字符串描述
	public static final String HTTP_BAD_GATEWAY_STR = "502";
}
