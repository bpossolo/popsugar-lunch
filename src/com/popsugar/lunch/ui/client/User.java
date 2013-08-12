package com.popsugar.lunch.ui.client;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.gwt.user.client.rpc.GwtTransient;
import com.google.gwt.user.client.rpc.IsSerializable;

@Entity
public class User implements IsSerializable, Serializable {
	
	private static final long serialVersionUID = -4720363515695180669L;

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
