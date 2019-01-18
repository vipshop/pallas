package com.vip.pallas.search.monitor;

import java.util.concurrent.atomic.AtomicBoolean;

public class ServerStatus {
	private ServerStatus(){
		//Nothing to do
	}
	
	public static final AtomicBoolean offline = new AtomicBoolean(false);
}
