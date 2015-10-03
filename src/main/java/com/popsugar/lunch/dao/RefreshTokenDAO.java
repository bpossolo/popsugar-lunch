package com.popsugar.lunch.dao;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.popsugar.lunch.oauth.OAuthApp;

public class RefreshTokenDAO {
	
	public static final String Kind = "OAuthRefreshToken";
	public static final String ValueProp = "value";

	private DatastoreService datastore;
	
	public RefreshTokenDAO(DatastoreService datastore) {
		this.datastore = datastore;
	}
	
	public String getRefreshToken(OAuthApp app) throws EntityNotFoundException {
		Key key = KeyFactory.createKey(Kind, app.name());
		Entity entity = datastore.get(key);
		String value = (String)entity.getProperty(ValueProp);
		return value;
	}
	
	public void saveRefreshToken(OAuthApp app, String value) {
		Entity entity = new Entity(Kind, app.name());
		entity.setUnindexedProperty(ValueProp, value);
		datastore.put(entity);
	}
}
