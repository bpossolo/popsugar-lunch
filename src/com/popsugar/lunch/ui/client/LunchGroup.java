package com.popsugar.lunch.ui.client;

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
public class LunchGroup implements IsSerializable {
	
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

	public List<Long> getUserKeys() {
		return userKeys;
	}
	
	public List<User> getUsers() {
		return users;
	}
	
	public boolean isCoordinatedBy(User user){
		return user.getKey().equals(coordinatorKey);
	}
	
	public void addUserKey(Long userKey){
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
	
	public int size(){
		return (userKeys == null) ? 0 : userKeys.size();
	}
}
