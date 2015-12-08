package com.popsugar.lunch.api.dto;

import java.util.List;

public class UpdateLunchGroupDTO {
	
	private Long key;
	private List<Long> userKeys;
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public List<Long> getUserKeys() {
		return userKeys;
	}
	
	public void setUserKeys(List<Long> userKeys) {
		this.userKeys = userKeys;
	}

}
