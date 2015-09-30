package com.popsugar.lunch.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Pair {

	private User userA;
	private User userB;
	
	public Pair(User userA, User userB) {
		this.userA = userA;
		this.userB = userB;
	}

	public User getUserA() {
		return userA;
	}

	public void setUserA(User userA) {
		this.userA = userA;
	}

	public User getUserB() {
		return userB;
	}

	public void setUserB(User userB) {
		this.userB = userB;
	}
	
	public static List<Pair> buildPairs(List<User> users) {
		List<Pair> pairs = new ArrayList<>();
		Map<Long,User> map = User.mapByKey(users);
		Set<User> processed = new HashSet<>();
		for (User user : users) {
			if (!processed.contains(user)) {
				User buddy = map.get(user.getBuddyKey());
				Pair pair = new Pair(user, buddy);
				pairs.add(pair);
				processed.add(user);
				processed.add(buddy);
			}
		}
		return pairs;
	}
	
}
