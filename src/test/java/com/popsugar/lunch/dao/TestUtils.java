package com.popsugar.lunch.dao;

import java.util.ArrayList;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.model.Location;

public class TestUtils {
	
	public static void createLunchGroup(
		DatastoreService datastore,
		Location location,
		GroupType type){
		
		Entity e = new Entity(LunchGroupDAO.Kind);
		e.setProperty(LunchGroupDAO.LocationProp, location.name());
		e.setProperty(LunchGroupDAO.TypeProp, type.name());
		e.setProperty(LunchGroupDAO.ActiveProp, true);
		datastore.put(e);
	}
	
	
	public static void createUser(
		DatastoreService datastore,
		String name, 
		String email, 
		Location location, 
		boolean active,
		GroupType groupType) {
		
		ArrayList<String> groupTypes = new ArrayList<>(1);
		groupTypes.add(groupType.name());
		
		Entity e = new Entity(UserDAO.Kind);
		e.setProperty(UserDAO.NameProp, name);
		e.setProperty(UserDAO.EmailProp, email);
		e.setProperty(UserDAO.LocationProp, location.name());
		e.setProperty(UserDAO.ActiveProp, active);
		e.setProperty(UserDAO.GroupTypesProp, groupTypes);
		datastore.put(e);
	}

}
