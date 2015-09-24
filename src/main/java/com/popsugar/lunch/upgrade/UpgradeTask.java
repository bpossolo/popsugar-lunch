package com.popsugar.lunch.upgrade;

import javax.servlet.ServletContext;

import com.google.appengine.api.datastore.DatastoreService;

public abstract class UpgradeTask implements Runnable {
	
	protected ServletContext servletContext;
	
	protected DatastoreService datastore;
	
	public void setServletContext(ServletContext servletContext){
		this.servletContext = servletContext;
	}
	
	public void setDatastore(DatastoreService datastore) {
		this.datastore = datastore;
	}

}
