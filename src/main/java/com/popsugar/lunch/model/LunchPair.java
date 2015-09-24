package com.popsugar.lunch.model;

public class LunchPair {

	private Long key;
	private Long masterUserKey;
	private Long apprenticeUserKey;
	
	public LunchPair(Long key, Long masterUserKey, Long apprenticeUserKey) {
		this.key = key;
		this.masterUserKey = masterUserKey;
		this.apprenticeUserKey = apprenticeUserKey;
	}
	
	public Long getKey() {
		return key;
	}
	public void setKey(Long key) {
		this.key = key;
	}
	public Long getMasterUserKey() {
		return masterUserKey;
	}
	public void setMasterUserKey(Long masterUserKey) {
		this.masterUserKey = masterUserKey;
	}
	public Long getApprenticeUserKey() {
		return apprenticeUserKey;
	}
	public void setApprenticeUserKey(Long apprenticeUserKey) {
		this.apprenticeUserKey = apprenticeUserKey;
	}	
}
