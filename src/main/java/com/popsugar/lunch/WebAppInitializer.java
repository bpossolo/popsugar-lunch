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
import com.popsugar.lunch.dao.UserDAO;
import com.popsugar.lunch.service.LunchManager;
import com.popsugar.lunch.service.PingboardService;

public class WebAppInitializer implements ServletContextListener {
	
	public static final String LunchManager = LunchManager.class.getSimpleName();
	public static final String Datastore = DatastoreService.class.getSimpleName();
	public static final String PingboardService = PingboardService.class.getSimpleName();
	public static final String UserDAO = UserDAO.class.getSimpleName();
	
	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
		MailService mailService = MailServiceFactory.getMailService();
		
		PingboardService pingboard = new PingboardService();
		pingboard.setUrlFetchService(urlFetchService);
		pingboard.setMemcache(memcache);
		
		LunchGroupDAO lunchGroupDao = new LunchGroupDAO();
		lunchGroupDao.setDatastore(datastore);
		
		UserDAO userDao = new UserDAO();
		userDao.setDatastore(datastore);
		
		LunchManager lunchManager = new LunchManager();
		lunchManager.setMemcache(memcache);
		lunchManager.setLunchGroupDao(lunchGroupDao);
		lunchManager.setUserDao(userDao);
		lunchManager.setPingboard(pingboard);
		lunchManager.setMailService(mailService);
		
		ServletContext servletContext = contextEvent.getServletContext();
		
		servletContext.setAttribute(LunchManager, lunchManager);
		servletContext.setAttribute(Datastore, datastore);
		servletContext.setAttribute(PingboardService, pingboard);
		servletContext.setAttribute(UserDAO, userDao);
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		//never called on app engine
	}

}
