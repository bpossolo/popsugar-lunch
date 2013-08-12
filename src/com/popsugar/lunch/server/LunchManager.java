package com.popsugar.lunch.server;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailService.Message;
import com.google.appengine.api.mail.MailServiceFactory;
import com.popsugar.lunch.ui.client.LunchGroup;
import com.popsugar.lunch.ui.client.LunchGroupData;
import com.popsugar.lunch.ui.client.User;

public class LunchManager {

	//-------------------------------------------------------------------------------------------
	//Class variables
	//-------------------------------------------------------------------------------------------
	
	private static final Logger log = Logger.getLogger(LunchManager.class.getName());
	
	//-------------------------------------------------------------------------------------------
	//Public methods
	//-------------------------------------------------------------------------------------------
	
	public void createUserInTx(EntityManager em, User user){
		try{
			String q = "select u from User u where email = :email";
			em.getTransaction().begin();
			em.createQuery(q, User.class).setParameter("email", user.getEmail()).getSingleResult();
		}
		catch(NoResultException e){
			em.persist(user);
		}
		finally{
			em.getTransaction().commit();
		}
	}
	
	public LunchGroupData getLunchGroupData(EntityManager em){
		ArrayList<LunchGroup> groups = getLunchGroupsWithUsers(em);
		String week = getCurrentWeek();
		return new LunchGroupData(groups, week);
	}
	
	public void regenerateLunchGroups(EntityManager em){
		deleteLunchGroupsInTx(em);
		List<User> users = getAllUsers(em);
		List<LunchGroup> groups = buildLunchGroups(users);
		persistLunchGroupsInTx(em, groups);
		notifyUsersAboutNewLunchGroups(groups);
	}
	
	//-------------------------------------------------------------------------------------------
	//Package protected methods
	//-------------------------------------------------------------------------------------------
	
	ArrayList<LunchGroup> getLunchGroupsWithUsers(EntityManager em){
		
		List<LunchGroup> groups = getLunchGroups(em);
		
		Map<Long,User> userMap = getAllUsersMapped(em);
		for( LunchGroup group : groups ){
			for( Long userKey : group.getUserKeys() ){
				User user = userMap.get(userKey);
				group.addUser(user);
			}
		}
		
		return new ArrayList<>(groups);
	}
	
	String getCurrentWeek(){
		Calendar startOfWeek = Calendar.getInstance();
		startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
		return df.format(startOfWeek.getTime());
	}
	
	List<LunchGroup> getLunchGroups(EntityManager em){
		String q = "select g from LunchGroup g";
		return em.createQuery(q, LunchGroup.class).getResultList();
	}
	
	void notifyUsersAboutNewLunchGroups(List<LunchGroup> lunchGroups){
		
		MailService mailService = MailServiceFactory.getMailService();
		String subject = "Lunch for Four";
		
		for( LunchGroup group : lunchGroups ){
			
			StringBuilder body = new StringBuilder()
				.append(getCurrentWeek())
				.append("\n\nYour upcoming Lunch for Four consists of:\n\n");
			
			ArrayList<String> recipients = new ArrayList<>();
			
			for( User user : group.getUsers() ){
				recipients.add(user.getEmail());
				body.append(" - ").append(user.getName());
				if( group.isCoordinatedBy(user) )
					body.append(" (please coordinate the exact location/date)");
				body.append('\n');
			}
			
			Message msg = new Message();
			msg.setSender("PopSugar Lunch for Four <noreply@popsugar-lunch.appspotmail.com>");
			msg.setBcc(recipients);
			msg.setSubject(subject);
			msg.setTextBody(body.toString());
			try{
				mailService.send(msg);
			}
			catch(IOException e){
				log.log(Level.SEVERE, "Failed to send email to {0}" + recipients, e);
			}
		}
	}
	
	List<User> getAllUsers(EntityManager em){
		String q = "select u from User u";
		return new ArrayList<>(em.createQuery(q, User.class).getResultList());
	}
	
	Map<Long,User> getAllUsersMapped(EntityManager em){
		HashMap<Long,User> map = new HashMap<>();
		for( User user : getAllUsers(em) )
			map.put(user.getKey(), user);
		return map;
	}
	
	List<LunchGroup> buildLunchGroups(List<User> users){
		
		ArrayList<LunchGroup> groups = new ArrayList<>();
		
		if( users.isEmpty() )
			return groups;
		
		Collections.shuffle(users);
		
		LunchGroup currentGroup = new LunchGroup();
		Iterator<User> i = users.iterator();
		while( i.hasNext() ){
			User user = i.next();
			currentGroup.addUserAndKey(user);
			if( currentGroup.isFull() ){
				groups.add(currentGroup);
				if( i.hasNext() )
					currentGroup = new LunchGroup();
			}
		}
		
		if( ! currentGroup.isFull() ){
			if( currentGroup.size() >= LunchGroup.MinGroupSize )
				groups.add(currentGroup);
			else
				distributeUsersInUndersizedGroupToOtherGroups(groups, currentGroup);
		}
		
		return groups;
	}
	
	void deleteLunchGroupsInTx(EntityManager em){
		em.getTransaction().begin();
		List<LunchGroup> groups = getLunchGroups(em);
		em.getTransaction().commit();
		for( LunchGroup group : groups ){
			em.getTransaction().begin();
			em.remove(group);
			em.getTransaction().commit();
		}
	}
	
	void persistLunchGroupsInTx(EntityManager em, List<LunchGroup> groups){
		for( LunchGroup group : groups ){
			em.getTransaction().begin();
			em.persist(group);
			em.getTransaction().commit();
		}
	}
	
	void distributeUsersInUndersizedGroupToOtherGroups(List<LunchGroup> groups, LunchGroup undersizedGroup){
		
		if( groups.isEmpty() ){
			groups.add(undersizedGroup);
		}
		else if( undersizedGroup.size() == 1 ){
			//distribute the one user in the undersized group to create a group with 5 people
			groups.get(0).addUserAndKey(undersizedGroup.getUsers().get(0));
		}
		else if( undersizedGroup.size() == 2 ){
			if( groups.size() == 1 ){
				//create two groups of three
				LunchGroup groupOne = new LunchGroup();
				LunchGroup groupTwo = new LunchGroup();
				groupOne.addUserAndKey(groups.get(0).getUsers().get(0));
				groupOne.addUserAndKey(groups.get(0).getUsers().get(1));
				groupOne.addUserAndKey(groups.get(0).getUsers().get(2));
				groupTwo.addUserAndKey(groups.get(0).getUsers().get(3));
				groupTwo.addUserAndKey(undersizedGroup.getUsers().get(0));
				groupTwo.addUserAndKey(undersizedGroup.getUsers().get(1));
				groups.clear();
				groups.add(groupOne);
				groups.add(groupTwo);
			}
			else{ //distribute the two users in order to create two groups of 5 people
				groups.get(0).addUserAndKey(undersizedGroup.getUsers().get(0));
				groups.get(1).addUserAndKey(undersizedGroup.getUsers().get(1));
			}
		}
	}

}
