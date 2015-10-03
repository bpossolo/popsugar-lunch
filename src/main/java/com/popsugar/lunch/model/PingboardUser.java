package com.popsugar.lunch.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PingboardUser implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String email;
	private String avatarUrlSmall;
	
	public static Map<String,PingboardUser> mapByEmail(List<PingboardUser> users){
		Map<String,PingboardUser> map = new HashMap<>();
		for (PingboardUser user : users) {
			map.put(user.getEmail(), user);
		}
		return map;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getAvatarUrlSmall() {
		return avatarUrlSmall;
	}
	
	public void setAvatarUrlSmall(String avatarUrlSmall) {
		this.avatarUrlSmall = avatarUrlSmall;
	}
}
