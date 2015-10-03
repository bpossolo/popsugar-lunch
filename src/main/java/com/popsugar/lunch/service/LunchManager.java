package com.popsugar.lunch.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailService.Header;
import com.google.appengine.api.mail.MailService.Message;
import com.google.appengine.api.memcache.MemcacheService;
import com.popsugar.lunch.dao.LunchGroupDAO;
import com.popsugar.lunch.dao.UserDAO;
import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.model.Location;
import com.popsugar.lunch.model.LunchGroup;
import com.popsugar.lunch.model.Pair;
import com.popsugar.lunch.model.PingboardUser;
import com.popsugar.lunch.model.User;
import com.popsugar.lunch.util.UrlUtil;

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
	private PingboardService pingboard;
	private MemcacheService memcache;
	private MailService mailService;
	
	//-------------------------------------------------------------------------------------------
	//Public methods
	//-------------------------------------------------------------------------------------------
	
	public List<User> getActiveUsers() {
		List<User> users = userDao.getActiveUsers();
		return users;
	}
	
	public User createUser(String name, String email, Location location) {
		PingboardUser pingboardUser = pingboard.getUserByEmail(email);
		User user = new User(name, email, location, GroupType.Regular);
		if (pingboardUser != null) {
			user.setPingboardId(pingboardUser.getId());
			user.setPingboardAvatarUrlSmall(pingboardUser.getAvatarUrlSmall());
		}
		userDao.createUser(user);
		return user;
	}
	
	public void generateLunchGroups(GroupType groupType, boolean email){
		lunchGroupDao.deleteLunchGroups(groupType);
		for( Location location : Location.values() ){
			List<User> users = userDao.getActiveUsersByLocationAndGroupType(location, groupType);
			List<LunchGroup> groups;
			if (groupType == GroupType.PopsugarPals) {
				groups = buildPalsLunchGroups(users, location);
			}
			else {
				groups = buildRegularLunchGroups(users, location);
			}
			lunchGroupDao.persistLunchGroups(groups);
			if (email) {
				notifyUsersAboutNewLunchGroups(groups);
			}
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
			List<User> users = userDao.getActiveUsers();
			Map<Long,User> userMap = User.mapByKey(users);
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
	
	public void deactivateUser(Long userId) {
		userDao.deactivateUser(userId);
	}
	
	public void linkUsers(Long userAKey, Long userBKey) throws EntityNotFoundException {
		userDao.linkUsers(userAKey, userBKey);
	}
	
	public void unlinkUsers(Long userAKey, Long userBKey) throws EntityNotFoundException {
		userDao.unlinkUsers(userAKey, userBKey);
	}
	
	public void updateUsersWithPingboardData() {
		List<User> users = userDao.getActiveUsers();
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
	
	Set<String> getOtherEmails(LunchGroup group, User userToExclude) {
		Set<String> emails = group.getEmails();
		emails.remove(userToExclude.getEmail());
		return emails;
	}
	
	void notifyUsersAboutNewLunchGroups(List<LunchGroup> lunchGroups){
		
		String week = getCurrentWeek();
		String sender = "PopSugar Lunch for Four <noreply@popsugar-lunch.appspotmail.com>";
		String subject = "Lunch for Four";
		
		for (LunchGroup group : lunchGroups) {
			
			StringBuilder memberList = new StringBuilder();
			for (User user: group.getUsers()) {
				memberList.append(" - ").append(user.getName());
				if( group.isCoordinatedBy(user) )
					memberList.append(" (please coordinate the exact location/date)");
				memberList.append('\n');
			}
			
			for (User user: group.getUsers()) {
				String unsubscribeUrl = UrlUtil.getUnsubscribeUrl(user);
				
				StringBuilder body = new StringBuilder(week)
					.append("\n\n")
					.append("Your upcoming Lunch for Four consists of:")
					.append("\n\n")
					.append(memberList)
					.append('\n')
					.append("If you know anyone that would like to join Lunch for Four, feel free to invite them via http://lunch.popsugar.com")
					.append("\n\n")
					.append("If you would like to be removed from Lunch for Four, click here ")
					.append(unsubscribeUrl);
				
				String to = user.getEmail();
				Set<String> cc = getOtherEmails(group, user);
				Header listUnsubscribe = new Header("List-Unsubscribe", "<" + unsubscribeUrl + ">");
				Message msg = new Message(sender, to, subject, body.toString());
				msg.setCc(cc);
				msg.setHeaders(listUnsubscribe);
				try{
					mailService.send(msg);
				}
				catch(IOException e){
					log.log(Level.SEVERE, "Failed to send email to " + user.getEmail(), e);
				}
			}
		}
	}
	
	List<LunchGroup> buildPalsLunchGroups(List<User> users, Location location) {
		log.log(Level.INFO, "Building popsugar pals lunch groups for {0} people in {1}", 
				new Object[]{ users.size(), location });
		ArrayList<LunchGroup> groups = new ArrayList<>();
		if (users.isEmpty()) {
			return groups;
		}
		List<Pair> pairs = Pair.buildPairs(users);
		Collections.shuffle(pairs);
		
		Pair leftoverPair = null;
		if (pairs.size() % 2 == 1) {
			leftoverPair = pairs.remove(pairs.size() - 1);
		}
		
		LunchGroup currentGroup = new LunchGroup(location, GroupType.PopsugarPals);
		Iterator<Pair> i = pairs.iterator();
		while (i.hasNext()) {
			Pair pair = i.next();
			currentGroup.addUserAndKey(pair.getUserA());
			currentGroup.addUserAndKey(pair.getUserB());
			if (currentGroup.isFull()) {
				groups.add(currentGroup);
				if (i.hasNext()) {
					currentGroup = new LunchGroup(location, GroupType.PopsugarPals);
				}
			}
		}
		
		if (leftoverPair != null) {
			currentGroup.addUserAndKey(leftoverPair.getUserA());
			currentGroup.addUserAndKey(leftoverPair.getUserB());
			if (groups.isEmpty()) {
				// special case where there was only a single pair
				groups.add(currentGroup);
			}
		}
		
		return groups;
	}
	
	List<LunchGroup> buildRegularLunchGroups(List<User> users, Location location){
		log.log(Level.INFO, "Building regular lunch groups for {0} people in {1}", 
				new Object[]{ users.size(), location });
		ArrayList<LunchGroup> groups = new ArrayList<>();
		if( users.isEmpty() ){
			return groups;
		}
		Collections.shuffle(users);
		
		LunchGroup currentGroup = new LunchGroup(location, GroupType.Regular);
		Iterator<User> i = users.iterator();
		while( i.hasNext() ){
			User user = i.next();
			currentGroup.addUserAndKey(user);
			if( currentGroup.isFull() ){
				groups.add(currentGroup);
				if( i.hasNext() ){
					currentGroup = new LunchGroup(location, GroupType.Regular);
				}
			}
		}
		
		if( ! currentGroup.isFull() ){
			if( currentGroup.size() >= LunchGroup.MinGroupSize ){
				groups.add(currentGroup);
			}
			else {
				distributeUsersInUndersizedGroupToOtherGroups(GroupType.Regular, groups, currentGroup);
			}
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
	
	public void setMemcache(MemcacheService memcache) {
		this.memcache = memcache;
	}
	
	public void setPingboard(PingboardService pingboard) {
		this.pingboard = pingboard;
	}
	
	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

}
