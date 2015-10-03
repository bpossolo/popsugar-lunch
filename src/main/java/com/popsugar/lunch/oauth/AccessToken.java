package com.popsugar.lunch.oauth;

import java.util.Date;

public class AccessToken {

	private String value;
	
	/**
	 * In seconds.
	 */
	private int expiration;
	
	private Date timestamp;
	
	public AccessToken(String value, int expiration) {
		this.value = value;
		this.expiration = expiration;
		this.timestamp = new Date();
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isExpired() {
		long start = timestamp.getTime();
		long now = new Date().getTime();
		long timeElapsedInSeconds = (now - start) / 1000;
		if ( timeElapsedInSeconds < expiration) {
			return false;
		}
		return true;
	}
}
