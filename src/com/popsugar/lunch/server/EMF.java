/**
 * 
 */
package com.popsugar.lunch.server;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * @author Benjamin Possolo
 * 
 */
public final class EMF {
	
	private static final EntityManagerFactory entityManagerFactory;
	
	static {
		Logger log = Logger.getLogger(EMF.class.getName());
		long start = new Date().getTime();
		log.fine("Creating EntityManagerFactory");
		entityManagerFactory = Persistence.createEntityManagerFactory("transactions-required");
		long duration = (new Date().getTime() - start) / 1000;
		log.log(Level.FINE, "EntityManagerFactory created in {0} seconds", duration);
	}
	
	private EMF(){}
	
	public static EntityManagerFactory get(){
		return entityManagerFactory;
	}

}
