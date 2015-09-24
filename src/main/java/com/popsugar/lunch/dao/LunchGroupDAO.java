package com.popsugar.lunch.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.model.Location;
import com.popsugar.lunch.model.LunchGroup;

public class LunchGroupDAO {
	
	public static final String Kind = LunchGroup.class.getSimpleName();
	public static final String CoordinatorKeyProp = "coordinatorKey";
	public static final String LocationProp = "location";
	public static final String UserKeysProp = "userKeys";
	public static final String TypeProp = "type";
	
	private DatastoreService datastore;
	
	public LunchGroupDAO(){}
	
	public LunchGroupDAO(DatastoreService datastore){
		this.datastore = datastore;
	}
	
	public List<LunchGroup> getLunchGroups(GroupType groupType){
		Query q = new Query(Kind).setFilter(new FilterPredicate(TypeProp, FilterOperator.EQUAL, groupType.name()));
		List<Entity> entities = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		List<LunchGroup> groups = decodeEntities(entities);
		return groups;
	}
	
	public void deleteLunchGroups(GroupType groupType){
		Query q = new Query(Kind)
			.setFilter(new FilterPredicate(TypeProp, FilterOperator.EQUAL, groupType.name()))
			.setKeysOnly();
		List<Entity> entities = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		ArrayList<Key> keys = new ArrayList<>(entities.size());
		for (Entity e : entities) {
			keys.add(e.getKey());
		}
		datastore.delete(keys);
	}
	
	public void persistLunchGroups(List<LunchGroup> groups){
		List<Entity> entities = encodeEntities(groups);
		datastore.put(entities);
	}
	
	public void setDatastore(DatastoreService datastore) {
		this.datastore = datastore;
	}
	
	private Entity encodeEntity(LunchGroup group) {
		Entity e = new Entity(Kind);
		e.setProperty(CoordinatorKeyProp, group.getCoordinatorKey());
		e.setProperty(UserKeysProp, group.getUserKeys());
		if (group.getType() != null) {
			e.setProperty(TypeProp, group.getType().name());
		}
		if (group.getLocation() != null) {
			e.setProperty(LocationProp, group.getLocation().name());
		}
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
	
	@SuppressWarnings("unchecked")
	private LunchGroup decodeEntity(Entity e){
		Long key = e.getKey().getId();
		Long coordinatorKey = (Long)e.getProperty(CoordinatorKeyProp);
		String location = (String)e.getProperty(LocationProp);
		String type = (String)e.getProperty(TypeProp);
		ArrayList<Long> userKeys = (ArrayList<Long>)e.getProperty(UserKeysProp);
		
		LunchGroup group = new LunchGroup();
		group.setKey(key);
		group.setCoordinatorKey(coordinatorKey);
		group.setUserKeys(userKeys);
		if (StringUtils.isNotBlank(location)) {
			group.setLocation(Location.valueOf(location));
		}
		if (StringUtils.isNotBlank(type)) {
			group.setType(GroupType.valueOf(type));
		}
		
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
