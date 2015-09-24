package com.popsugar.lunch.api.dto;

import com.popsugar.lunch.model.Location;

public class MyLocationDTO {
	
	public String city;
	public String state;
	public Location location;
	
	public MyLocationDTO(String city, String state, Location location) {
		this.city = city;
		this.state = state;
		this.location = location;
	}
}
