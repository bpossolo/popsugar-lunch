package com.popsugar.lunch.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections4.CollectionUtils;

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

public class LunchService {

	//-------------------------------------------------------------------------------------------
	//Class variables
	//-------------------------------------------------------------------------------------------
	
	private static final Logger log = Logger.getLogger(LunchService.class.getName());
	
	private static final String LunchGroupsRegularMemcacheKey = "lunch-groups-regular";
	private static final String LunchGroupsPalsMemcacheKey = "lunch-groups-pals";
	
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
	
	public User createOrUpdateUser(String name, String email, Location location) {
		PingboardUser pingboardUser = pingboard.getUserByEmail(email);
		User user = new User(name, email, location, GroupType.Regular);
		if (pingboardUser != null) {
			user.setPingboardId(pingboardUser.getId());
			user.setPingboardAvatarUrlSmall(pingboardUser.getAvatarUrlSmall());
		}
		userDao.createOrUpdateUser(user);
		return user;
	}
	
	public void generateLunchGroups(GroupType groupType, boolean email){
		lunchGroupDao.deactivateCurrentLunchGroups(groupType);
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
			//cacheLunchGroups(groups, groupType);
			if (email) {
				notifyUsersAboutNewLunchGroups(groups);
			}
		}
	}
	
	public LunchGroup getLunchGroupById(Long id) throws EntityNotFoundException {
		return lunchGroupDao.getById(id);
	}
	
	public void updateLunchGroup(LunchGroup group) {
		List<LunchGroup> list = Collections.singletonList(group);
		lunchGroupDao.persistLunchGroups(list);
	}
	
	public List<LunchGroup> getLunchGroupsWithUsers(GroupType groupType){
		List<LunchGroup> groups = getCachedLunchGroups(groupType);
		if (CollectionUtils.isEmpty(groups)) {
			groups = lunchGroupDao.getActiveLunchGroupsByType(groupType);
			
			// populate the groups with the users
			List<User> users = userDao.getActiveUsers();
			Map<Long,User> userMap = User.mapByKey(users);
			for( LunchGroup group : groups ){
				for( Long userKey : group.getUserKeys() ){
					User user = userMap.get(userKey);
					if (user == null) {
						// the user might have been marked inactive after the group
						// was created so try to lookup the user by key
						try {
							user = userDao.getUserByKey(userKey);
						}
						catch(EntityNotFoundException e){
							log.log(Level.WARNING, "Group {0} contains user key {1} which doesn't map to user", 
								new Object[] {group.getKey().toString(), userKey.toString()});
						}
					}
					if (user != null) {
						group.addUser(user);
					}
				}
			}
			//cacheLunchGroups(groups, groupType);
		}
		return groups;
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
	
	public void updateUsersWithPingboardData() {
		List<User> users = userDao.getActiveUsers();
		List<PingboardUser> pingboardUsers = pingboard.getAllUsers();
		Map<String,PingboardUser> map = PingboardUser.mapByEmail(pingboardUsers);
		
		List<User> usersToUpdate = new ArrayList<>();
		
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
		
		SimpleDateFormat df = new SimpleDateFormat("MMMM yyyy");
		String date = df.format(Calendar.getInstance().getTime());
		String siteUrl = UrlUtil.getBaseUrl();
		String sender = "PopSugar Lunch for Four <noreply@popsugar-lunch.appspotmail.com>";
		String subject = "Lunch for Four";
		
		for (LunchGroup group : lunchGroups) {
			
			List<User> users = group.getUsers();
			
			StringBuilder template = new StringBuilder()
				.append(date)
				.append("\n\n")
				.append("Your upcoming Lunch for Four consists of:")
				.append("\n\n");
			
			for (User user : users) {
				template.append(" - ").append(user.getName());
				if( group.isCoordinatedBy(user) ){
					template.append(" (please coordinate the exact location/date)");
				}
				template.append('\n');
			}
			
			template.append('\n')
				.append("If you know anyone that would like to join Lunch for Four, feel free to invite them via ")
				.append(siteUrl)
				.append("\n\n")
				.append("To unsubscribe, go to ");
			
			for (User user : users) {
				String to = user.getEmail();
				String unsubscribeUrl = UrlUtil.getUnsubscribeUrl(user);
				StringBuilder body = new StringBuilder(template).append(unsubscribeUrl);
				Header listUnsubscribe = new Header("List-Unsubscribe", "<" + unsubscribeUrl + ">");
				Message msg = new Message(sender, to, subject, body.toString());
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
		
		Date now = new Date();
		
		List<LunchGroup> groups = new ArrayList<>();
		if (users.isEmpty()) {
			return groups;
		}
		List<Pair> pairs = Pair.buildPairs(users);
		Collections.shuffle(pairs);
		
		Pair leftoverPair = null;
		if (pairs.size() % 2 == 1) {
			leftoverPair = pairs.remove(pairs.size() - 1);
		}
		
		LunchGroup currentGroup = new LunchGroup(location, GroupType.PopsugarPals, now);
		Iterator<Pair> i = pairs.iterator();
		while (i.hasNext()) {
			Pair pair = i.next();
			currentGroup.addUserAndKey(pair.getUserA());
			currentGroup.addUserAndKey(pair.getUserB());
			if (currentGroup.isFull()) {
				groups.add(currentGroup);
				if (i.hasNext()) {
					currentGroup = new LunchGroup(location, GroupType.PopsugarPals, now);
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
		
		Date now = new Date();
		
		List<LunchGroup> groups = new ArrayList<>();
		if( users.isEmpty() ){
			return groups;
		}
		Collections.shuffle(users);
		
		LunchGroup currentGroup = new LunchGroup(location, GroupType.Regular, now);
		Iterator<User> i = users.iterator();
		while( i.hasNext() ){
			User user = i.next();
			currentGroup.addUserAndKey(user);
			if( currentGroup.isFull() ){
				groups.add(currentGroup);
				if( i.hasNext() ){
					currentGroup = new LunchGroup(location, GroupType.Regular, now);
				}
			}
		}
		
		if( ! currentGroup.isFull() ){
			if( currentGroup.size() >= LunchGroup.MinGroupSize ){
				groups.add(currentGroup);
			}
			else {
				distributeUsersInUndersizedGroupToOtherGroups(GroupType.Regular, groups, currentGroup, now);
			}
		}
		
		return groups;
	}
	
	void distributeUsersInUndersizedGroupToOtherGroups(GroupType groupType, List<LunchGroup> groups, LunchGroup undersizedGroup, Date created){
		
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
				LunchGroup groupOne = new LunchGroup(location, groupType, created);
				LunchGroup groupTwo = new LunchGroup(location, groupType, created);
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
	//Private methods
	//-------------------------------------------------------------------------------------------
	
	private List<LunchGroup> getCachedLunchGroups(GroupType groupType) {
		String cacheKey = getLunchGroupCacheKey(groupType);
		@SuppressWarnings("unchecked")
		List<LunchGroup> groups = (ArrayList<LunchGroup>)memcache.get(cacheKey);
		return groups;
	}
	
	@SuppressWarnings("unused")
	private void cacheLunchGroups(List<LunchGroup> groups, GroupType groupType) {
		String cacheKey = getLunchGroupCacheKey(groupType);
		memcache.put(cacheKey, groups);
	}
	
	private String getLunchGroupCacheKey(GroupType groupType) {
		if (groupType == GroupType.Regular) {
			return LunchGroupsRegularMemcacheKey;
		}
		else {
			return LunchGroupsPalsMemcacheKey;
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
