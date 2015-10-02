package com.popsugar.lunch.model;

import java.util.ArrayList;

public enum GroupType {
	Regular,
	PopsugarPals;
	
	public ArrayList<GroupType> asList() {
		ArrayList<GroupType> list = new ArrayList<>(1);
		list.add(this);
		return list;
	}
}
