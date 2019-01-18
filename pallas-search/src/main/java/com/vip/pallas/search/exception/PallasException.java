package com.vip.pallas.search.exception;

@SuppressWarnings("serial")
public class PallasException extends RuntimeException {
	public String errorCode;
	public String errorCause;
	private String message;

	public PallasException(String errorCode) {
		this.errorCode = errorCode;
	}

	public PallasException(String errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
	}

	public PallasException(String message, String errorCode) {
		this.message = message;
		this.errorCode = errorCode;
	}

	public PallasException(String message, String errorCode, Throwable cause) {
		super(message, cause);
		this.message = message;
		this.errorCode = errorCode;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

}
