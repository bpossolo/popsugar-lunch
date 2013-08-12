package com.popsugar.lunch.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class WebAppInitializer implements ServletContextListener {
	
	public static final String LunchManager = "lunchManager";
	
	@Override
	public void contextInitialized(ServletContextEvent contextEvent) {
		LunchManager lunchManager = new LunchManager();
		contextEvent.getServletContext().setAttribute(LunchManager, lunchManager);
	}

	@Override
	public void contextDestroyed(ServletContextEvent contextEvent) {
		//never called on GAE
	}

}
