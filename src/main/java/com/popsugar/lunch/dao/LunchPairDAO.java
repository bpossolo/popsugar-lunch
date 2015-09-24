package com.popsugar.lunch.dao;

import java.util.Date;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.popsugar.lunch.model.LunchPair;

public class LunchPairDAO {
	
	private static final String Kind = LunchPair.class.getSimpleName();
	private static final String User1KeyProp = "user1Key";
	private static final String User2KeyProp = "user2Key";
	
	private DatastoreService datastore;
	
	public LunchPair createPair(Long user1Key, Long user2Key){
		Entity e = new Entity(Kind);
		e.setProperty(User1KeyProp, user1Key);
		e.setProperty(User2KeyProp, user2Key);
		Key key = datastore.put(e);
		
		LunchPair pair = new LunchPair(key.getId(), user1Key, user2Key);
		return pair;
	}
	
	public List<LunchPair> getLunchPairs(Date date) {
		return null;
	}
	
	public void setDatastore(DatastoreService datastore) {
		this.datastore = datastore;
	}

}
