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

package com.vip.pallas.exception;


public class PallasException extends Exception {

	private static final long serialVersionUID = 8797062860178016604L;
	
	protected int returnCode;
	protected String returnMessage;

	public PallasException(String message) {
		super(message);
	}
	
	public PallasException() {
	}
	
	public PallasException(String message, int retCode) {
		super(message);
		this.returnCode = retCode;
		this.returnMessage = message;
	}

	public PallasException(Throwable cause) {
		super(cause);
	}

	public PallasException(String message, Throwable cause) {
		super(message, cause);
		this.returnMessage = message;
	}

	public String getReturnMessage() {
		return returnMessage;
	}

	public void setReturnMessage(String retMessage) {
		this.returnMessage = retMessage;
	}


	public int getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(int retCode) {
		this.returnCode = retCode;
	}
}