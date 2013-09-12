package com.popsugar.lunch.server.upgrade;

import java.util.List;

import javax.persistence.EntityManager;

import com.popsugar.lunch.server.EMF;
import com.popsugar.lunch.server.LunchManager;
import com.popsugar.lunch.server.WebAppInitializer;
import com.popsugar.lunch.ui.client.User;

/**
 * Marks all users as active.
 */
public class Task1 extends UpgradeTask {
	
	@Override
	public void run() {
		LunchManager lunchManager = (LunchManager)servletContext.getAttribute(WebAppInitializer.LunchManager);
		EntityManager em = EMF.get().createEntityManager();
		
		em.getTransaction().begin();
		List<User> users = lunchManager.getAllUsers(em);
		em.getTransaction().commit();
		
		//mark all users as active
		for( User user : users ){
			em.getTransaction().begin();
			user.setActive(true);
			em.merge(user);
			em.getTransaction().commit();
		}
		em.close();
	}

}
