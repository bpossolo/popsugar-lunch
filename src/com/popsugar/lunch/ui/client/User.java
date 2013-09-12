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
	
	private boolean active;
	
	private Location location;
	
	User(){}
	
	public User(String name, String email, Location location){
		this.name = name;
		this.email = email;
		active = true;
		this.location = location;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		User other = (User)obj;
		if( email == null ){
			if( other.email != null )
				return false;
		}
		else if( ! email.equals(other.email) )
			return false;
		return true;
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
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public Location getLocation() {
		return location;
	}

}
