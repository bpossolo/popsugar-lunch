package com.popsugar.lunch.upgrade;

import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.popsugar.lunch.dao.LunchGroupDAO;

public class Task3 extends UpgradeTask {

	@Override
	public void run() {
		Query q = new Query(LunchGroupDAO.Kind);
		List<Entity> entities = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		for (Entity e : entities) {
			e.setProperty(LunchGroupDAO.ActiveProp, false);
		}
		datastore.put(entities);
	}

}
