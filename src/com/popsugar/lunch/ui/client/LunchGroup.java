package com.popsugar.lunch.ui.client;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.google.gwt.user.client.rpc.GwtTransient;
import com.google.gwt.user.client.rpc.IsSerializable;

@Entity
public class LunchGroup implements IsSerializable, Serializable {
	
	private static final long serialVersionUID = -750883076227779321L;

	public static final int MinGroupSize = 3;
	
	public static final int UsersPerGroup = 4;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long key;
	
	private Long coordinatorKey;
	
	@Basic
	@GwtTransient
	private List<Long> userKeys;
	
	@Transient
	private List<User> users;
	
	private Location location;
	
	LunchGroup(){}
	
	public LunchGroup(Location location){
		this.location = location;
	}

	public List<Long> getUserKeys() {
		return userKeys;
	}
	
	public List<User> getUsers() {
		return users;
	}
	
	public boolean isCoordinatedBy(User user){
		return user.getKey().equals(coordinatorKey);
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
}
