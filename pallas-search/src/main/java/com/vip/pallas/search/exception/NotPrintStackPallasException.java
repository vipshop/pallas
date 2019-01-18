package com.vip.pallas.search.exception;

@SuppressWarnings("serial")
public class NotPrintStackPallasException extends PallasException {

	public NotPrintStackPallasException(String message, String errorCode,String className,String method) {
		super(message, errorCode);
		setStackTrace(new StackTraceElement[] { new StackTraceElement(className, method, null, -1)});
	}

	final public void setStackTrace(StackTraceElement[] stackTrace) {//NOSONAR
		super.setStackTrace(stackTrace);
	}

}
