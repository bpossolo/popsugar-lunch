package com.popsugar.lunch.oauth;

public class RefreshToken {
	
	private Long key;
	private OAuthApp app;
	private String value;
	
	public RefreshToken(){}
	
	public RefreshToken(Long key, OAuthApp app, String value) {
		this.key = key;
		this.app = app;
		this.value = value;
	}

	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public OAuthApp getApp() {
		return app;
	}
	
	public void setApp(OAuthApp app) {
		this.app = app;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

}
