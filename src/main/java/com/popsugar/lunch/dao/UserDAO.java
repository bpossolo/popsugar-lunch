package com.popsugar.lunch.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.model.Location;
import com.popsugar.lunch.model.User;

public class UserDAO {
	
	public static final String Kind = User.class.getSimpleName();
	public static final String EmailProp = "email";
	public static final String ActiveProp = "active";
	public static final String NameProp = "name";
	public static final String LocationProp = "location";
	public static final String GroupTypesProp = "groupTypes";
	public static final String PingboardIdProp = "pingboardId";
	
	private DatastoreService datastore;
	
	public UserDAO(){}
	
	public UserDAO(DatastoreService datastore){
		this.datastore = datastore;
	}
	
	public void createUser(User user){
		Query q = new Query(Kind).setFilter(new FilterPredicate(EmailProp, FilterOperator.EQUAL, user.getEmail()));
		int count = datastore.prepare(q).countEntities(FetchOptions.Builder.withDefaults());
		if (count == 0) {
			Entity userEntity = encodeEntity(user);
			Key key = datastore.put(userEntity);
			user.setKey(key.getId());
		}
	}
	
	public List<User> getAllUsers(){
		Query q = new Query(Kind).setFilter(new FilterPredicate(ActiveProp, FilterOperator.EQUAL, true));
		List<Entity> entities = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		List<User> users = decodeEntities(entities);
		return users;
	}
	
	public List<User> getActiveUsersByLocation(Location location, GroupType groupType){
		Query q = new Query(Kind).setFilter(
			CompositeFilterOperator.and(
				new FilterPredicate(LocationProp, FilterOperator.EQUAL, location.name()),
				new FilterPredicate(ActiveProp, FilterOperator.EQUAL, true)
//				new FilterPredicate(GroupTypesProp, FilterOperator.EQUAL, groupType.name())
			)
		);
		List<Entity> entities = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		List<User> users = decodeEntities(entities);
		return users;
	}
	
	public Map<Long,User> getAllUsersMapped(){
		HashMap<Long,User> map = new HashMap<>();
		for (User user : getAllUsers()) {
			map.put(user.getKey(), user);
		}
		return map;
	}
	
	public void setDatastore(DatastoreService datastore) {
		this.datastore = datastore;
	}
	
	private Entity encodeEntity(User user) {
		Entity entity = new Entity(Kind);
		entity.setProperty(NameProp, user.getName());
		entity.setProperty(ActiveProp, true);
		entity.setProperty(EmailProp, user.getEmail());
		entity.setProperty(PingboardIdProp, user.getPingboardId());
		
		if (user.getLocation() != null) {
			entity.setProperty(LocationProp, user.getLocation().name());
		}
		if (user.getGroupTypes() != null) {
			ArrayList<String> groupTypes = GroupType.asNameList(user.getGroupTypes());
			entity.setProperty(GroupTypesProp, groupTypes);
		}
		return entity;
	}
	
	@SuppressWarnings("unchecked")
	private User decodeEntity(Entity e) {
		long key = e.getKey().getId();
		String name = (String)e.getProperty(NameProp);
		String email = (String)e.getProperty(EmailProp);
		Boolean active = (Boolean)e.getProperty(ActiveProp);
		String location = (String)e.getProperty(LocationProp);
		ArrayList<String> groupTypes = (ArrayList<String>)e.getProperty(GroupTypesProp);
		Long pingboardId = (Long)e.getProperty(PingboardIdProp);
		
		User user = new User();
		user.setKey(key);
		user.setName(name);
		user.setEmail(email);
		user.setActive(active);
		user.setPingboardId(pingboardId);
		if (StringUtils.isNotBlank(location)) {
			user.setLocation(Location.valueOf(location));
		}
		if (groupTypes != null) {
			user.setGroupTypes(GroupType.asEnumList(groupTypes));
		}
		return user;
	}
	
	private ArrayList<User> decodeEntities(Collection<Entity> entities) {
		ArrayList<User> users = new ArrayList<>(entities.size());
		for (Entity e : entities) {
			User user = decodeEntity(e);
			users.add(user);
		}
		return users;
	}

}
