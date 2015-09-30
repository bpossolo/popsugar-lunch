package com.popsugar.lunch.upgrade;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.popsugar.lunch.dao.UserDAO;
import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.util.DatastoreUtil;

public class Task2 extends UpgradeTask {

	@Override
	public void run() {
		
		List<GroupType> groups = new ArrayList<>(1);
		groups.add(GroupType.Regular);
		
		Query q = new Query(UserDAO.Kind);
		Iterable<Entity> results = datastore.prepare(q).asIterable();
		List<Entity> entities = new ArrayList<>();
		for (Entity entity : results) {
			DatastoreUtil.setEnumList(entity, UserDAO.GroupTypesProp, groups);
			entities.add(entity);
		}
		
		datastore.put(entities);
	}

}
