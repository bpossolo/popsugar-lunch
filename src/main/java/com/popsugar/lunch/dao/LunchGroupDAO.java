package com.popsugar.lunch.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.model.Location;
import com.popsugar.lunch.model.LunchGroup;
import com.popsugar.lunch.util.DatastoreUtil;

public class LunchGroupDAO {
	
	private static final Logger log = Logger.getLogger(LunchGroupDAO.class.getName());
	
	public static final String Kind = LunchGroup.class.getSimpleName();
	public static final String CoordinatorKeyProp = "coordinatorKey";
	public static final String LocationProp = "location";
	public static final String UserKeysProp = "userKeys";
	public static final String TypeProp = "type";
	public static final String ActiveProp = "active";
	public static final String CreatedProp = "created";
	
	private DatastoreService datastore;
	
	public LunchGroupDAO(DatastoreService datastore){
		this.datastore = datastore;
	}
	
	public List<LunchGroup> getActiveLunchGroupsByType(GroupType groupType){
		List<Entity> entities = getActiveLunchGroupEntitiesByType(groupType);
		List<LunchGroup> groups = decodeEntities(entities);
		return groups;
	}
	
	public void deactivateCurrentLunchGroups(GroupType groupType){
		log.log(Level.INFO, "Deactivating {0} lunch groups", groupType);
		List<Entity> entities = getActiveLunchGroupEntitiesByType(groupType);
		for (Entity entity : entities) {
			entity.setProperty(ActiveProp, false);
		}
		datastore.put(entities);
	}
	
	public void persistLunchGroups(List<LunchGroup> groups){
		List<Entity> entities = encodeEntities(groups);
		datastore.put(entities);
	}
	
	public LunchGroup getById(Long id) throws EntityNotFoundException {
		Key key = KeyFactory.createKey(Kind, id);
		Entity e = datastore.get(key);
		LunchGroup group = decodeEntity(e);
		return group;
	}
	
	private List<Entity> getActiveLunchGroupEntitiesByType(GroupType type) {
		Query q = new Query(Kind).setFilter(
			CompositeFilterOperator.and(
				new FilterPredicate(ActiveProp, FilterOperator.EQUAL, true),
				new FilterPredicate(TypeProp, FilterOperator.EQUAL, type.name())
			)
		);
		List<Entity> entities = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		return entities;
	}
	
	private Entity encodeEntity(LunchGroup group) {
		Entity e;
		if (group.getKey() == null) {
			e = new Entity(Kind);
		}
		else {
			e = new Entity(Kind, group.getKey());
		}
		e.setProperty(CoordinatorKeyProp, group.getCoordinatorKey());
		e.setProperty(UserKeysProp, group.getUserKeys());
		e.setProperty(ActiveProp, group.isActive());
		e.setProperty(CreatedProp, group.getCreated());
		DatastoreUtil.setEnum(e, TypeProp, group.getType());
		DatastoreUtil.setEnum(e, LocationProp, group.getLocation());
		return e;
	}
	
	private ArrayList<Entity> encodeEntities(List<LunchGroup> groups) {
		ArrayList<Entity> entities = new ArrayList<>(groups.size());
		for (LunchGroup group : groups){
			Entity e = encodeEntity(group);
			entities.add(e);
		}
		return entities;
	}
	
	private LunchGroup decodeEntity(Entity e){
		Long key = e.getKey().getId();
		Long coordinatorKey = (Long)e.getProperty(CoordinatorKeyProp);
		Location location = DatastoreUtil.getEnum(e, LocationProp, Location.class);
		GroupType type = DatastoreUtil.getEnum(e, TypeProp, GroupType.class);
		Boolean active = (Boolean)e.getProperty(ActiveProp);
		Date created = (Date)e.getProperty(CreatedProp);
		@SuppressWarnings("unchecked")
		ArrayList<Long> userKeys = (ArrayList<Long>)e.getProperty(UserKeysProp);
		
		LunchGroup group = new LunchGroup();
		group.setKey(key);
		group.setCoordinatorKey(coordinatorKey);
		group.setUserKeys(userKeys);
		group.setLocation(location);
		group.setType(type);
		group.setActive(active);
		group.setCreated(created);
		return group;
	}
	
	private ArrayList<LunchGroup> decodeEntities(Collection<Entity> entities){
		ArrayList<LunchGroup> groups = new ArrayList<>(entities.size());
		for (Entity e : entities) {
			LunchGroup group = decodeEntity(e);
			groups.add(group);
		}
		return groups;
	}

}
