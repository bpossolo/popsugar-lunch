package com.popsugar.lunch;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.popsugar.lunch.dao.LunchGroupDAO;
import com.popsugar.lunch.dao.RefreshTokenDAO;
import com.popsugar.lunch.dao.UserDAO;
import com.popsugar.lunch.service.LunchService;
import com.popsugar.lunch.service.PingboardService;

public class WebAppInitializer implements ServletContextListener {
	
	public static final String LunchService = LunchService.class.getSimpleName();
	public static final String Datastore = DatastoreService.class.getSimpleName();
	public static final String RefreshTokenDAO = RefreshTokenDAO.class.getSimpleName();
	
	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
		MailService mailService = MailServiceFactory.getMailService();
		
		LunchGroupDAO lunchGroupDao = new LunchGroupDAO(datastore);
		UserDAO userDao = new UserDAO(datastore);
		RefreshTokenDAO refreshTokenDao = new RefreshTokenDAO(datastore);
		
		PingboardService pingboard = new PingboardService(urlFetchService, refreshTokenDao);
		
		LunchService lunchManager = new LunchService();
		lunchManager.setMemcache(memcache);
		lunchManager.setMailService(mailService);
		lunchManager.setUserDao(userDao);
		lunchManager.setLunchGroupDao(lunchGroupDao);
		lunchManager.setPingboard(pingboard);
		
		ServletContext servletContext = contextEvent.getServletContext();
		servletContext.setAttribute(Datastore, datastore);
		servletContext.setAttribute(RefreshTokenDAO, refreshTokenDao);
		servletContext.setAttribute(LunchService, lunchManager);
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		//never called on app engine
	}

}
