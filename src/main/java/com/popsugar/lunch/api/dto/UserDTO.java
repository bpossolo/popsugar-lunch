package com.popsugar.lunch.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.popsugar.lunch.model.Location;
import com.popsugar.lunch.model.User;

@JsonInclude(Include.NON_NULL)
public class UserDTO {
	
	public Long key;
	public String name;
	public Location location;
	public Boolean coordinator;
	
	public UserDTO(User user) {
		key = user.getKey();
		name = user.getName();
		location = user.getLocation();
	}
	
	public UserDTO(User user, Boolean coordinator){
		this(user);
		this.coordinator = coordinator;
	}
}
