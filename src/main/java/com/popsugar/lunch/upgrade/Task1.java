package com.popsugar.lunch.upgrade;

import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.popsugar.lunch.dao.LunchGroupDAO;
import com.popsugar.lunch.dao.UserDAO;
import com.popsugar.lunch.model.Location;

/**
 * Marks all users as active and sets location to SanFrancisco.
 */
public class Task1 extends UpgradeTask {
	
	@Override
	public void run() {
		
		Query q = new Query(UserDAO.Kind);
		List<Entity> users = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		for (Entity user : users) {
			user.setProperty(UserDAO.ActiveProp, true);
			user.setProperty(UserDAO.LocationProp, Location.SanFrancisco);
		}
		datastore.put(users);
		
		q = new Query(LunchGroupDAO.Kind);
		List<Entity> lunchGroups = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		for (Entity lunchGroup : lunchGroups) {
			lunchGroup.setProperty(LunchGroupDAO.LocationProp, Location.SanFrancisco.name());
		}
		datastore.put(lunchGroups);
	}

}
