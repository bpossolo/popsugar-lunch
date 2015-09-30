package com.popsugar.lunch.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User implements Serializable {
	
	private static final long serialVersionUID = -4720363515695180669L;

	private Long key;
	private String name;
	private String email;
	private boolean active;
	private Location location;
	private List<GroupType> groupTypes;
	private Long pingboardId;
	private String pingboardAvatarUrlSmall;
	private Long buddyKey;
	private User buddy;
	
	public User(){}
	
	public User(String name, String email, Location location){
		this.name = name;
		this.email = email;
		this.location = location;
		this.active = true;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if( this == obj )
			return true;
		if( obj == null )
			return false;
		if( getClass() != obj.getClass() )
			return false;
		User other = (User)obj;
		if( email == null ){
			if( other.email != null )
				return false;
		}
		else if( ! email.equals(other.email) )
			return false;
		return true;
	}
	
	public static Map<Long,User> mapByKey(List<User> users) {
		Map<Long,User> map = new HashMap<>();
		for (User user : users) {
			map.put(user.getKey(), user);
		}
		return map;
	}

	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getEmail() {
		return email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}
	
	public List<GroupType> getGroupTypes() {
		return groupTypes;
	}
	
	public void setGroupTypes(List<GroupType> groupTypes) {
		this.groupTypes = groupTypes;
	}
	
	public void addGroupType(GroupType groupType) {
		if (groupTypes == null) {
			groupTypes = new ArrayList<>(2);
		}
		groupTypes.add(groupType);
	}
	
	public void setPingboardId(Long pingboardId) {
		this.pingboardId = pingboardId;
	}
	
	public Long getPingboardId() {
		return pingboardId;
	}
	
	public String getPingboardAvatarUrlSmall() {
		return pingboardAvatarUrlSmall;
	}
	
	public void setPingboardAvatarUrlSmall(String pingboardAvatarUrlSmall) {
		this.pingboardAvatarUrlSmall = pingboardAvatarUrlSmall;
	}
	
	public Long getBuddyKey() {
		return buddyKey;
	}
	
	public void setBuddyKey(Long buddyKey) {
		this.buddyKey = buddyKey;
	}
	
	public User getBuddy() {
		return buddy;
	}
	
	public void setBuddy(User buddy) {
		this.buddy = buddy;
	}

}
