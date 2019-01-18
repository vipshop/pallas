package com.vip.pallas.search.exception;

/**
 * 需要直接返回对应的httpcode
 * @author freeman.he
 *
 */
@SuppressWarnings("serial")
public class HttpCodeErrorPallasException extends NotPrintStackPallasException {
	private int httpCode;

	//通过设置className和method,可以减少堆栈信息的输出，提升性能
	public HttpCodeErrorPallasException(String message, int httpCode,String className,String method) {
		super(message, Integer.toString(httpCode), className, method);
		this.httpCode = httpCode;
	}
	
	public int getHttpCode() {
		return httpCode;
	}

	
}
