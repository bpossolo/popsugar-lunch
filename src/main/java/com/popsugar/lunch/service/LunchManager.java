package com.popsugar.lunch.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailService.Message;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.popsugar.lunch.dao.LunchGroupDAO;
import com.popsugar.lunch.dao.LunchPairDAO;
import com.popsugar.lunch.dao.UserDAO;
import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.model.Location;
import com.popsugar.lunch.model.LunchGroup;
import com.popsugar.lunch.model.PingboardUser;
import com.popsugar.lunch.model.User;

public class LunchManager {

	//-------------------------------------------------------------------------------------------
	//Class variables
	//-------------------------------------------------------------------------------------------
	
	private static final Logger log = Logger.getLogger(LunchManager.class.getName());
	
	//-------------------------------------------------------------------------------------------
	//Member variables
	//-------------------------------------------------------------------------------------------
	
	private LunchGroupDAO lunchGroupDao;
	private UserDAO userDao;
	private LunchPairDAO lunchPairDao;
	private PingboardService pingboard;
	private MemcacheService memcache;
	
	//-------------------------------------------------------------------------------------------
	//Public methods
	//-------------------------------------------------------------------------------------------
	
	public List<User> getActiveUsers() {
		return userDao.getAllUsers();
	}
	
	public User createUser(String name, String email, Location location) {
		User user = new User(name, email, location);
		userDao.createUser(user);
		return user;
	}
	
	public void generateLunchGroups(GroupType groupType){
		lunchGroupDao.deleteLunchGroups(groupType);
		for( Location location : Location.values() ){
			List<User> users = userDao.getActiveUsersByLocation(location, groupType);
			List<LunchGroup> groups = buildLunchGroups(users, location, groupType);
			lunchGroupDao.persistLunchGroups(groups);
			notifyUsersAboutNewLunchGroups(groups);
		}
		String week = getCurrentWeek();
		memcache.delete(week);
	}
	
	@SuppressWarnings("unchecked")
	public List<LunchGroup> getLunchGroupsWithUsers(String week, GroupType groupType){
		List<LunchGroup> groups = (ArrayList<LunchGroup>)memcache.get(week);
		if( groups == null ){
			groups = lunchGroupDao.getLunchGroups(groupType);
			
			// populate the groups with the user objects
			Map<Long,User> userMap = userDao.getAllUsersMapped();
			for( LunchGroup group : groups ){
				for( Long userKey : group.getUserKeys() ){
					User user = userMap.get(userKey);
					group.addUser(user);
				}
			}
			
			// cache the results
			// Calendar oneWeekFromToday = Calendar.getInstance();
			// oneWeekFromToday.add(Calendar.WEEK_OF_MONTH, 1);
			// memcache.put(week, groups, Expiration.onDate(oneWeekFromToday.getTime()));
		}
		return groups;
	}
	
	public String getCurrentWeek(){
		Calendar startOfWeek = Calendar.getInstance();
		startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		SimpleDateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
		return df.format(startOfWeek.getTime());
	}
	
	public Location estimateUserLocation(String city, String state){
		
		log.log(Level.INFO, "State {0}, City {1}", new Object[]{state, city});
		
		if( "ca".equalsIgnoreCase(state) ){
			if( "san francisco".equalsIgnoreCase(city) )
				return Location.SanFrancisco;
			if( "los angeles".equalsIgnoreCase(city) )
				return Location.LosAngeles;
		}
		else if( "ny".equalsIgnoreCase(state) ){
			return Location.NewYork;
		}
		
		return null;
	}
	
	public void createPair(Long user1Key, Long user2Key) {
		lunchPairDao.createPair(user1Key, user2Key);
	}
	
	public void updateUsersWithPingboardData() {
		List<User> users = userDao.getAllUsers();
		List<PingboardUser> pingboardUsers = pingboard.getAllUsers();
		Map<String,PingboardUser> map = pingboard.buildEmailUserMap(pingboardUsers);
		
		List<User> usersToUpdate = new ArrayList<User>();
		
		for (User user : users) {
			String email = user.getEmail();
			PingboardUser pingboardUser = map.get(email);
			if (pingboardUser != null) {
				user.setPingboardId(pingboardUser.getId());
				user.setPingboardAvatarUrlSmall(pingboardUser.getAvatarUrlSmall());
				usersToUpdate.add(user);
			}
		}
		
		userDao.updateUsers(usersToUpdate);
	}
	
	//-------------------------------------------------------------------------------------------
	//Package protected methods
	//-------------------------------------------------------------------------------------------
	
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
			
			body.append("\nIf you know anyone that would like to join Lunch for Four, feel free to invite them via http://popsugar-lunch.appspot.com\n\n")
			.append("If you would like to be removed from Lunch for Four, please email Benjamin Possolo (bpossolo@popsugar.com) or Human Resources (hr@popsugar.com)");
			
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
	
	List<LunchGroup> buildLunchGroups(List<User> users, Location location, GroupType groupType){
		
		ArrayList<LunchGroup> groups = new ArrayList<>();
		
		if( users.isEmpty() )
			return groups;
		
		Collections.shuffle(users);
		
		// TODO set lunch group week
		
		LunchGroup currentGroup = new LunchGroup(location, groupType);
		Iterator<User> i = users.iterator();
		while( i.hasNext() ){
			User user = i.next();
			currentGroup.addUserAndKey(user);
			if( currentGroup.isFull() ){
				groups.add(currentGroup);
				if( i.hasNext() )
					currentGroup = new LunchGroup(location, groupType);
			}
		}
		
		if( ! currentGroup.isFull() ){
			if( currentGroup.size() >= LunchGroup.MinGroupSize )
				groups.add(currentGroup);
			else
				distributeUsersInUndersizedGroupToOtherGroups(groupType, groups, currentGroup);
		}
		
		return groups;
	}
	
	void distributeUsersInUndersizedGroupToOtherGroups(GroupType groupType, List<LunchGroup> groups, LunchGroup undersizedGroup){
		
		Location location = undersizedGroup.getLocation();
		
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
				LunchGroup groupOne = new LunchGroup(location, groupType);
				LunchGroup groupTwo = new LunchGroup(location, groupType);
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
	
	//-------------------------------------------------------------------------------------------
	//Getters/setters
	//-------------------------------------------------------------------------------------------
	
	public void setLunchGroupDao(LunchGroupDAO lunchGroupDao) {
		this.lunchGroupDao = lunchGroupDao;
	}
	
	public void setUserDao(UserDAO userDao) {
		this.userDao = userDao;
	}
	
	public void setLunchPairDao(LunchPairDAO lunchPairDao) {
		this.lunchPairDao = lunchPairDao;
	}
	
	public void setMemcache(MemcacheService memcache) {
		this.memcache = memcache;
	}
	
	public void setPingboard(PingboardService pingboard) {
		this.pingboard = pingboard;
	}

}
