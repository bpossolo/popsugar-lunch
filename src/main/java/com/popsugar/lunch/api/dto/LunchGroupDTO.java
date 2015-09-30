package com.popsugar.lunch.api.dto;

import java.util.ArrayList;
import java.util.List;

import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.model.Location;
import com.popsugar.lunch.model.LunchGroup;
import com.popsugar.lunch.model.User;

public class LunchGroupDTO {

	public Long key;
	public List<UserDTO> users;
	public GroupType type;
	public Location location;
	
	public LunchGroupDTO(LunchGroup group) {
		key = group.getKey();
		type = group.getType();
		location = group.getLocation();
		users = new ArrayList<>(group.getUsers().size());
		for (User user : group.getUsers()) {
			boolean isCoordinator = group.isCoordinatedBy(user);
			UserDTO userDto = new UserDTO(user, isCoordinator);
			users.add(userDto);
		}
	}
}
