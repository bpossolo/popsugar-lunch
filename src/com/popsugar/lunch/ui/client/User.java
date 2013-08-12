package com.popsugar.lunch.ui.client;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.gwt.user.client.rpc.GwtTransient;
import com.google.gwt.user.client.rpc.IsSerializable;

@Entity
public class User implements IsSerializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long key;
	
	private String name;
	
	@GwtTransient
	private String email;
	
	User(){}
	
	public User(String name, String email){
		this.name = name;
		this.email = email;
	}
	
	public Long getKey() {
		return key;
	}
	
	public String getName() {
		return name;
	}
	
	public String getEmail() {
		return email;
	}

}
