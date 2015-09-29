package com.popsugar.lunch.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.popsugar.lunch.model.Location;
import com.popsugar.lunch.model.User;

@JsonInclude(Include.NON_NULL)
public class UserDTO {
	
	public Long key;
	public String name;
	public Location location;
	public Boolean coordinator;
	public Long pingboardId;
	public String pingboardAvatarUrlSmall;
	public UserDTO buddy;

	private UserDTO(User user){
		key = user.getKey();
		name = user.getName();
		location = user.getLocation();
		pingboardId = user.getPingboardId();
		pingboardAvatarUrlSmall = user.getPingboardAvatarUrlSmall();
	}
	
	public static class Builder {
		
		private UserDTO dto;
		
		public static Builder user(User user) {
			Builder builder = new Builder();
			builder.dto = new UserDTO(user);
			if (user.getBuddy() != null) {
				builder.dto.buddy = new UserDTO(user.getBuddy());
			}
			return builder;
		}
		
		public static Builder user(User user, Boolean coordinator) {
			return user(user).coordinator(coordinator);
		}
		
		public Builder coordinator(Boolean coordinator) {
			dto.coordinator = coordinator;
			return this;
		}
		
		public UserDTO build() {
			return dto;
		}
	}
}
