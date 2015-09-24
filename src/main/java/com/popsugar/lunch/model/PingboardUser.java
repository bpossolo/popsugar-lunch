package com.popsugar.lunch.model;

import java.io.Serializable;

public class PingboardUser implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Long id;
	private String email;
	private String avatarUrlSmall;
	
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
