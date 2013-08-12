package com.popsugar.lunch.ui.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LunchGroupData implements IsSerializable {

	private ArrayList<LunchGroup> groups;
	
	private String week;
	
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
}
