package com.vip.pallas.search.http;

public class HttpCode {
	public final static int HTTP_OK_CODE = 200;
	
	public final static int HTTP_NON_AUTHORITATIVE_INFORMATION = 203;
	
	public final static int HTTP_REQUEST_URI_TOO_LONG = 414;
	//调用osp服务不存在时返回460
	public final static int PROXY = 460;
	//调用osp方法不存在
	public final static int CALLEE = 550;
	public final static int HTTP_FORBIDDEN = 403;
	public final static int HTTP_NOT_FOUND = 404;
	public final static int HTTP_UNSUPPORTED_MEDIA_TYPE=415;
	public final static int HTTP_METHOD_NOT_ALLOWED =405;
	public final static int HTTP_PRECONDITION_FAILED = 412;
	public final static int HTTP_BAD_REQUEST = 400;
	public final static int HTTP_UNAUTHORIZED = 401;
	
	public final static int HTTP_UNPROCESSABLE_ENTITY = 422;
	
	public final static int HTTP_PARSER_FAILED = 499;
	
	public final static int HTTP_INTERNAL_SERVER_ERROR = 500;
	public final static int HTTP_BAD_GATEWAY = 502;
	public final static int HTTP_SERVICE_UNAVAILABLE = 503;
	public final static int HTTP_GATEWAY_TIMEOUT = 504;
	public final static int HTTP_GATEWAY_SHUTDOWN = 512;
	public final static int HTTP_RETRY_TIMEOUT = 520;
	
	public final static int HTTP_REQUEST_ENTITY_TOO_LARGE=413;

	public final static int HTTP_ANTI_BRUSH=911; //防刷拒绝状态码
	public final static int HTTP_WAF_FORBIDDEN = 912; //waf拒绝状态码
	public final static int HTTP_LIMITER = 914; // 限流拒绝状态码
	//字符串描述
	public final static String HTTP_BAD_GATEWAY_STR = "502";
}
