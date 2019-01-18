package com.vip.pallas.search.exception;

/**
 * 不需要打印堆栈信息，提升性能
 * @author freeman.he
 *
 */
@SuppressWarnings("serial")
public class NoStrackException extends PallasException{

	public NoStrackException(String message, String errorCode,String className,String method) {
		super(message, errorCode);
		setStackTrace(new StackTraceElement[] { new StackTraceElement(className, method, null, -1)});
	}

	final public void setStackTrace(StackTraceElement[] stackTrace) {//NOSONAR
		super.setStackTrace(stackTrace);
	}

}
