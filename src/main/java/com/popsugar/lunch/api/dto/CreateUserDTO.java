package com.popsugar.lunch.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.popsugar.lunch.model.Location;

public class CreateUserDTO {
	
	@JsonProperty
	public String name;
	
	@JsonProperty
	public String email;
	
	@JsonProperty
	public Location location;

}
