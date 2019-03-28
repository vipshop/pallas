package com.vip.pallas.search.filter.circuitbreaker;

public enum CircuitBreakStatus {
	OPEN("open"), HALF_OPEN("halfOpen"), CLOSED("close");

	private String title;

	CircuitBreakStatus(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
}
