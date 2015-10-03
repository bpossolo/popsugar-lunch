package com.popsugar.lunch.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	
	private static final Logger log = Logger.getLogger(UserDAO.class.getName());
	
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
	
	public UserDAO(DatastoreService datastore){
		this.datastore = datastore;
	}
	
	public void createUser(User user){
		String email = user.getEmail();
		Entity entity = getEntityByEmail(email);
		if (entity == null) {
			log.log(Level.INFO, "Creating user {0}", email);
			Entity userEntity = encodeEntity(user);
			Key key = datastore.put(userEntity);
			user.setKey(key.getId());
		}
		else {
			log.log(Level.INFO, "Updating user {0}", email);
			entity.setProperty(NameProp, user.getName());
			entity.setProperty(EmailProp, email);
			entity.setProperty(ActiveProp, true);
			DatastoreUtil.setEnum(entity, LocationProp, user.getLocation());
			datastore.put(entity);
		}
	}
	
	public void deactivateUser(Long userId) throws EntityNotFoundException {
		Key key = KeyFactory.createKey(Kind, userId);
		Entity e = datastore.get(key);
		e.setProperty(ActiveProp, false);
		datastore.put(e);
	}
	
	public void linkUsers(Long userAId, Long userBId) throws EntityNotFoundException {
		Key userAKey = KeyFactory.createKey(Kind, userAId);
		Key userBKey = KeyFactory.createKey(Kind, userBId);
		
		TransactionOptions txOptions = TransactionOptions.Builder.withXG(true);
		Transaction tx = datastore.beginTransaction(txOptions);
		try {
			Entity userA = datastore.get(userAKey);
			userA.setProperty(BuddyKeyProp, userBId);
			DatastoreUtil.setEnumList(userA, GroupTypesProp, GroupType.PopsugarPals.asList());
			
			Entity userB = datastore.get(userBKey);
			userB.setProperty(BuddyKeyProp, userAId);
			DatastoreUtil.setEnumList(userB, GroupTypesProp, GroupType.PopsugarPals.asList());
			
			List<Entity> users = Arrays.asList(userA, userB);
			datastore.put(tx, users);
			tx.commit();
		}
		catch(DatastoreFailureException | ConcurrentModificationException e){
			tx.rollback();
			throw e;
		}
	}
	
	public void unlinkUsers(Long userAId, Long userBId) throws EntityNotFoundException {
		Key userAKey = KeyFactory.createKey(Kind, userAId);
		Key userBKey = KeyFactory.createKey(Kind, userBId);
		
		TransactionOptions txOptions = TransactionOptions.Builder.withXG(true);
		Transaction tx = datastore.beginTransaction(txOptions);
		try {
			Entity userA = datastore.get(userAKey);
			Entity userB = datastore.get(userBKey);
			
			userA.setProperty(BuddyKeyProp, null);
			userB.setProperty(BuddyKeyProp, null);
			
			List<Entity> users = Arrays.asList(userA, userB);
			datastore.put(tx, users);
			tx.commit();
		}
		catch(DatastoreFailureException | ConcurrentModificationException e){
			tx.rollback();
			throw e;
		}
	}
	
	public User getUserByKey(Long userId) throws EntityNotFoundException {
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
	
	public List<User> getActiveUsers(){
		Query q = new Query(Kind).setFilter(new FilterPredicate(ActiveProp, FilterOperator.EQUAL, true));
		List<Entity> entities = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		List<User> users = decodeEntities(entities);
		return users;
	}
	
	public List<User> getActiveUsersByLocationAndGroupType(Location location, GroupType groupType){
		Query q = new Query(Kind).setFilter(
			CompositeFilterOperator.and(
				new FilterPredicate(ActiveProp, FilterOperator.EQUAL, true),
				new FilterPredicate(LocationProp, FilterOperator.EQUAL, location.name()),
				new FilterPredicate(GroupTypesProp, FilterOperator.EQUAL, groupType.name())
			)
		);
		List<Entity> entities = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
		List<User> users = decodeEntities(entities);
		return users;
	}
	
	public void setDatastore(DatastoreService datastore) {
		this.datastore = datastore;
	}
	
	private Entity getEntityByEmail(String email) {
		Query q = new Query(Kind).setFilter(new FilterPredicate(EmailProp, FilterOperator.EQUAL, email));
		Entity entity = datastore.prepare(q).asSingleEntity();
		return entity;
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
		e.setProperty(ActiveProp, user.isActive());
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
