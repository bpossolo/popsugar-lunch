package com.popsugar.lunch.ui.client;

import java.io.Serializable;
import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LunchGroupData implements IsSerializable, Serializable {

	private static final long serialVersionUID = 9003011986715231292L;

	private ArrayList<LunchGroup> groups;
	
	private String week;
	
	private Location userLocation;
	
	LunchGroupData(){}
	
	public LunchGroupData(ArrayList<LunchGroup> groups, String week){
		this.groups = groups;
		this.week = week;
	}
	
	public ArrayList<LunchGroup> getGroups() {
		return groups;
	}
	
	public String getWeek() {
		return week;
	}
	
	public Location getUserLocation() {
		return userLocation;
	}
	
	public void setUserLocation(Location userLocation) {
		this.userLocation = userLocation;
	}
}
