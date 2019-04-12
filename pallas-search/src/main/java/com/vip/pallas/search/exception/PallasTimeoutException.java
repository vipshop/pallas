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
