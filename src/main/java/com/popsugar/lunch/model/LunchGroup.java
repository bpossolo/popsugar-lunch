package com.popsugar.lunch.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LunchGroup implements Serializable {
	
	private static final long serialVersionUID = -750883076227779321L;

	public static final int MinGroupSize = 3;
	public static final int UsersPerGroup = 4;
	
	private Long key;
	private Long coordinatorKey;
	private Location location;
	private Date week;
	private List<Long> userKeys;
	private List<User> users;
	private GroupType type;
	
	public LunchGroup(){}
	
	public LunchGroup(Location location, GroupType type){
		this.location = location;
		this.type = type;
	}
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	public List<Long> getUserKeys() {
		return userKeys;
	}
	
	public void setUserKeys(List<Long> userKeys) {
		this.userKeys = userKeys;
	}
	
	public List<User> getUsers() {
		return users;
	}
	
	public boolean isCoordinatedBy(User user){
		return user.getKey().equals(coordinatorKey);
	}
	
	public Long getCoordinatorKey() {
		return coordinatorKey;
	}
	
	public void setCoordinatorKey(Long coordinatorKey) {
		this.coordinatorKey = coordinatorKey;
	}
	
	public void addUserAndKey(User user){
		addUser(user);
		addUserKey(user.getKey());
	}
	
	private void addUserKey(Long userKey){
		if( userKeys == null )
			userKeys = new ArrayList<Long>();
		userKeys.add(userKey);
		if( coordinatorKey == null )
			coordinatorKey = userKey;
	}
	
	public void addUser(User user){
		if( users == null )
			users = new ArrayList<User>();
		users.add(user);
	}
	
	public boolean isFull(){
		if( userKeys == null )
			return false;
		return userKeys.size() == UsersPerGroup;
	}
	
	public boolean contains(User user){
		boolean keyExists = userKeys != null && userKeys.contains(user.getKey());
		if( keyExists )
			return true;
		boolean emailExists = users != null && users.contains(user);
		return emailExists;
	}
	
	public int size(){
		return (userKeys == null) ? 0 : userKeys.size();
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public GroupType getType() {
		return type;
	}
	
	public void setType(GroupType type) {
		this.type = type;
	}
	
	public Date getWeek() {
		return week;
	}
	
	public void setWeek(Date week) {
		this.week = week;
	}
}
