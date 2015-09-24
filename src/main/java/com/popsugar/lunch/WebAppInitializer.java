package com.popsugar.lunch;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.popsugar.lunch.dao.LunchGroupDAO;
import com.popsugar.lunch.dao.LunchPairDAO;
import com.popsugar.lunch.dao.UserDAO;
import com.popsugar.lunch.service.LunchManager;

public class WebAppInitializer implements ServletContextListener {
	
	public static final String LunchManager = LunchManager.class.getSimpleName();
	public static final String Datastore = DatastoreService.class.getSimpleName();
	
	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		LunchGroupDAO lunchGroupDao = new LunchGroupDAO();
		lunchGroupDao.setDatastore(datastore);
		
		UserDAO userDao = new UserDAO();
		userDao.setDatastore(datastore);
		
		LunchPairDAO lunchPairDao = new LunchPairDAO();
		lunchPairDao.setDatastore(datastore);
		
		LunchManager lunchManager = new LunchManager();
		lunchManager.setMemcache(memcache);
		lunchManager.setLunchGroupDao(lunchGroupDao);
		lunchManager.setUserDao(userDao);
		lunchManager.setLunchPairDao(lunchPairDao);
		
		ServletContext servletContext = contextEvent.getServletContext();
		
		servletContext.setAttribute(LunchManager, lunchManager);
		servletContext.setAttribute(Datastore, datastore);
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		//never called on app engine
	}

}
