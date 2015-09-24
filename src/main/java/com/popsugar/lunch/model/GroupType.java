package com.popsugar.lunch.model;

import java.util.ArrayList;
import java.util.List;

public enum GroupType {
	Regular,
	PopsugarPals;
	
	public static ArrayList<String> asNameList(List<GroupType> types){
		ArrayList<String> names = new ArrayList<>(types.size());
		for (GroupType type : types) {
			names.add(type.name());
		}
		return names;
	}
	
	public static ArrayList<String> asNameList(GroupType...types) {
		if (types == null) {
			types = new GroupType[0];
		}
		ArrayList<String> names = new ArrayList<>(types.length);
		for (GroupType type : types) {
			names.add(type.name());
		}
		return names;
	}
	
	public static List<GroupType> asEnumList(List<String> names) {
		ArrayList<GroupType> types = new ArrayList<>(names.size());
		for (String name : names) {
			types.add(GroupType.valueOf(name));
		}
		return types;
	}
}
