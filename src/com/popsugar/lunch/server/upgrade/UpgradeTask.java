package com.popsugar.lunch.server.upgrade;

import javax.servlet.ServletContext;

public abstract class UpgradeTask implements Runnable {
	
	protected ServletContext servletContext;
	
	protected void setServletContext(ServletContext servletContext){
		this.servletContext = servletContext;
	}

}
