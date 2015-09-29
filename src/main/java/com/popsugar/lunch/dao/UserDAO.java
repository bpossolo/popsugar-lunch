package com.popsugar.lunch.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreFailureException;
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
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;
import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.model.Location;
import com.popsugar.lunch.model.User;
import com.popsugar.lunch.util.DatastoreUtil;

public class UserDAO {
	
	public static final String Kind = User.class.getSimpleName();
	public static final String EmailProp = "email";
	public static final String ActiveProp = "active";
	public static final String NameProp = "name";
	public static final String LocationProp = "location";
	public static final String GroupTypesProp = "groupTypes";
	public static final String PingboardIdProp = "pingboardId";
	public static final String PingboardAvatarUrlSmallProp = "pingboardAvatarUrlSmallProp";
	public static final String BuddyKeyProp = "buddyKey";
	
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
		// TODO reactive the user if one already exists and update name
	}
	
	public void linkBuddies(Long userAId, Long userBId) throws EntityNotFoundException {
		Key userAKey = KeyFactory.createKey(Kind, userAId);
		Key userBKey = KeyFactory.createKey(Kind, userBId);
		
		Entity userA = datastore.get(userAKey);
		Entity userB = datastore.get(userBKey);
		
		userA.setProperty(BuddyKeyProp, userBId);
		userB.setProperty(BuddyKeyProp, userAId);
		
		List<Entity> users = new ArrayList<>(2);
		users.add(userA);
		users.add(userB);
		
		TransactionOptions txOptions = TransactionOptions.Builder.withXG(true);
		Transaction tx = datastore.beginTransaction(txOptions);
		try {
			datastore.put(tx, users);
			tx.commit();
		}
		catch(DatastoreFailureException | ConcurrentModificationException e){
			tx.rollback();
			throw e;
		}
	}
	
	public User getUserById(Long userId) throws EntityNotFoundException {
		Key key = KeyFactory.createKey(Kind, userId);
		Entity e = datastore.get(key);
		User user = decodeEntity(e);
		return user;
	}
	
	public void updateUser(User user) {
		Entity e = encodeEntity(user);
		datastore.put(e);
	}
	
	public void updateUsers(List<User> users) {
		List<Entity> entities = encodeEntities(users);
		datastore.put(entities);
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
	
	public Map<Long,User> getAllUsersMappedByKey(){
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
		Entity e;
		if (user.getKey() == null) {
			e = new Entity(Kind);
		}
		else {
			e = new Entity(Kind, user.getKey());
		}
		e.setProperty(NameProp, user.getName());
		e.setProperty(ActiveProp, true);
		e.setProperty(EmailProp, user.getEmail());
		e.setProperty(PingboardIdProp, user.getPingboardId());
		e.setProperty(PingboardAvatarUrlSmallProp, user.getPingboardAvatarUrlSmall());
		DatastoreUtil.setEnum(e, LocationProp, user.getLocation());
		DatastoreUtil.setEnumList(e, GroupTypesProp, user.getGroupTypes());
		e.setProperty(BuddyKeyProp, user.getBuddyKey());
		return e;
	}
	
	private User decodeEntity(Entity e) {
		long key = e.getKey().getId();
		String name = (String)e.getProperty(NameProp);
		String email = (String)e.getProperty(EmailProp);
		Boolean active = (Boolean)e.getProperty(ActiveProp);
		Long pingboardId = (Long)e.getProperty(PingboardIdProp);
		String pingboardAvatarUrlSmall = (String)e.getProperty(PingboardAvatarUrlSmallProp);
		Location location = DatastoreUtil.getEnum(e, LocationProp, Location.class);
		List<GroupType> groupTypes = DatastoreUtil.getEnumList(e, GroupTypesProp, GroupType.class);
		Long buddyKey = (Long)e.getProperty(BuddyKeyProp);
		
		User user = new User();
		user.setKey(key);
		user.setName(name);
		user.setEmail(email);
		user.setActive(active);
		user.setPingboardId(pingboardId);
		user.setPingboardAvatarUrlSmall(pingboardAvatarUrlSmall);
		user.setLocation(location);
		user.setGroupTypes(groupTypes);
		user.setBuddyKey(buddyKey);
		
		return user;
	}
	
	private ArrayList<Entity> encodeEntities(Collection<User> users) {
		ArrayList<Entity> entities = new ArrayList<>(users.size());
		for (User user : users) {
			Entity e = encodeEntity(user);
			entities.add(e);
		}
		return entities;
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
