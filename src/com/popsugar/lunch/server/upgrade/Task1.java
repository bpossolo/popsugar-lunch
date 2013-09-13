package com.popsugar.lunch.server.upgrade;

import java.util.List;

import javax.persistence.EntityManager;

import com.popsugar.lunch.server.EMF;
import com.popsugar.lunch.ui.client.Location;
import com.popsugar.lunch.ui.client.LunchGroup;
import com.popsugar.lunch.ui.client.User;

/**
 * Marks all users as active and sets location to SanFrancisco.
 */
public class Task1 extends UpgradeTask {
	
	@Override
	public void run() {
		EntityManager em = EMF.get().createEntityManager();
		
		String q = "select u from User u";
		em.getTransaction().begin();
		List<User> users = em.createQuery(q, User.class).getResultList();
		em.getTransaction().commit();
		
		//mark all users as active and location as SF
		for( User user : users ){
			em.getTransaction().begin();
			user.setActive(true);
			user.setLocation(Location.SanFrancisco);
			em.merge(user);
			em.getTransaction().commit();
		}
		
		q = "select g from LunchGroup g";
		em.getTransaction().begin();
		List<LunchGroup> groups = em.createQuery(q, LunchGroup.class).getResultList();
		em.getTransaction().commit();
		
		//set group locations to SF
		for( LunchGroup group : groups ){
			em.getTransaction().begin();
			group.setLocation(Location.SanFrancisco);
			em.merge(group);
			em.getTransaction().commit();
		}
		
		em.close();
	}

}
