package com.popsugar.lunch.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Pair {
	
	private static final Logger log = Logger.getLogger(Pair.class.getName());

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
				if (buddy == null) {
					log.log(Level.WARNING, "{0} [{1}] does not have a buddy because {2} is inactive", 
						new Object[]{user.getName(), user.getKey().toString(), user.getBuddyKey().toString()});
				}
				else {
					Pair pair = new Pair(user, buddy);
					pairs.add(pair);
					processed.add(buddy);
				}
				processed.add(user);
			}
		}
		return pairs;
	}
	
}
