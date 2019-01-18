package com.vip.pallas.search.exception;

public class PallasTimeoutException extends Exception {
	private String message;
	private static final long serialVersionUID = 1900926677490660714L;

	/**
	 * Constructs a <tt>TimeoutException</tt> with no specified detail message.
	 */
	public PallasTimeoutException() {
	}

	/**
	 * Constructs a <tt>TimeoutException</tt> with the specified detail message.
	 *
	 * @param message the detail message
	 */
	public PallasTimeoutException(String message) {
		this.message = message;
	}

	@Override
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}