package com.popsugar.lunch.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.appengine.api.mail.MailService;
import com.google.appengine.api.mail.MailService.Message;
import com.google.appengine.api.mail.MailServiceFactory;
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.popsugar.lunch.dao.LunchGroupDAO;
import com.popsugar.lunch.dao.UserDAO;
import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.model.Location;
import com.popsugar.lunch.model.LunchGroup;
import com.popsugar.lunch.model.User;
import com.popsugar.lunch.util.CollectionUtil;

public class LunchServiceTest {
	
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
		new LocalMemcacheServiceTestConfig(),
		new LocalMailServiceTestConfig().setLogMailBody(true));
	
	private LunchService lunchService;
	private UserDAO userDao;
	private LunchGroupDAO lunchGroupDao;
	private MailService mailService;
	private MailService mockMailService;
	private PingboardService pingboard;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		mailService = MailServiceFactory.getMailService();
		mockMailService = Mockito.mock(MailService.class);
		userDao = Mockito.mock(UserDAO.class);
		lunchGroupDao = Mockito.mock(LunchGroupDAO.class);
		pingboard = Mockito.mock(PingboardService.class);
		
		lunchService = new LunchService();
		lunchService.setMailService(mailService);
		lunchService.setUserDao(userDao);
		lunchService.setLunchGroupDao(lunchGroupDao);
		lunchService.setPingboard(pingboard);
	}

	@After
	public void tearDown() throws Exception {
		helper.tearDown();
	}
	
	@Test
	public void testCalendarWeekOfYear() {
		Calendar date = Calendar.getInstance();
		date.clear();
		
		date.set(2014, Calendar.DECEMBER, 28);
		Assert.assertEquals(1, date.get(Calendar.WEEK_OF_YEAR));
		
		date.set(2015, Calendar.JANUARY, 3);
		Assert.assertEquals(1, date.get(Calendar.WEEK_OF_YEAR));
		
		date.set(2015, Calendar.JANUARY, 4);
		Assert.assertEquals(2, date.get(Calendar.WEEK_OF_YEAR));
		
		date.set(2015, Calendar.FEBRUARY, 1);
		Assert.assertEquals(6, date.get(Calendar.WEEK_OF_YEAR));
	}
	
	@Test
	public void testGetLunchGroups(){
		
		User benji = new User("benji", "benji@home", Location.SanFrancisco);
		User tom = new User("tom", "tom@home", Location.SanFrancisco);
		
		LunchGroup group1 = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		group1.addUserAndKey(benji);
		
		LunchGroup group2 = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		group2.addUserAndKey(tom);
		
		List<LunchGroup> groups = Arrays.asList(group1, group2);
		Mockito.when(lunchGroupDao.getActiveLunchGroupsByType(GroupType.Regular)).thenReturn(groups);
		
		List<LunchGroup> result = lunchService.getLunchGroupsWithUsers(GroupType.Regular);
		
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(benji.getName(), groups.get(0).getUsers().get(0).getName());
		Assert.assertEquals(tom.getName(), groups.get(1).getUsers().get(0).getName());
	}

	@Test
	public void testDistributeUsersInUndersizedGroupToOtherGroupsFourPlusOne() {
		
		User benji = new User("benji", "benji@home", Location.SanFrancisco);
		User tiers = new User("tiers", "tiers@home", Location.SanFrancisco);
		User berta = new User("berta", "berta@fairfax", Location.SanFrancisco);
		User lee = new User("lee", "lee@fairfax", Location.SanFrancisco);
		User ash = new User("ash", "ash@ashburn", Location.SanFrancisco);
		
		LunchGroup group = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		group.addUserAndKey(benji);
		group.addUserAndKey(tiers);
		group.addUserAndKey(berta);
		group.addUserAndKey(lee);
		
		LunchGroup undersizedGroup = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		undersizedGroup.addUserAndKey(ash);
		
		List<LunchGroup> groups = CollectionUtil.singletonList(group);
		
		Date now = new Date();
		
		lunchService.distributeUsersInUndersizedGroupToOtherGroups(GroupType.Regular, groups, undersizedGroup, now);
		
		Assert.assertEquals(5, group.size());
		Assert.assertEquals("ash", group.getUsers().get(4).getName());
		Assert.assertEquals("ash@ashburn", group.getUsers().get(4).getEmail());
	}
	
	@Test
	public void testDistributeUsersInUndersizedGroupToOtherGroupsFourPlusTwo() {
		
		User benji = new User("benji", "benji@home", Location.SanFrancisco);
		User tiers = new User("tiers", "tiers@home", Location.SanFrancisco);
		User berta = new User("berta", "berta@fairfax", Location.SanFrancisco);
		User lee = new User("lee", "lee@fairfax", Location.SanFrancisco);
		User ash = new User("ash", "ash@ashburn", Location.SanFrancisco);
		User sam = new User("sam", "sam@donutshop", Location.SanFrancisco);
		
		LunchGroup group = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		group.addUserAndKey(benji);
		group.addUserAndKey(tiers);
		group.addUserAndKey(berta);
		group.addUserAndKey(lee);
		
		LunchGroup undersizedGroup = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		undersizedGroup.addUserAndKey(ash);
		undersizedGroup.addUserAndKey(sam);
		
		List<LunchGroup> groups = CollectionUtil.singletonList(group);
		Date now = new Date();
		
		lunchService.distributeUsersInUndersizedGroupToOtherGroups(GroupType.Regular, groups, undersizedGroup, now);
		
		LunchGroup group1 = groups.get(0);
		LunchGroup group2 = groups.get(1);
		
		Assert.assertEquals(3, group1.size());
		Assert.assertEquals(3, group2.size());
		
		Assert.assertEquals("benji", group1.getUsers().get(0).getName());
		Assert.assertEquals("tiers", group1.getUsers().get(1).getName());
		Assert.assertEquals("berta", group1.getUsers().get(2).getName());
		
		Assert.assertEquals("lee", group2.getUsers().get(0).getName());
		Assert.assertEquals("ash", group2.getUsers().get(1).getName());
		Assert.assertEquals("sam", group2.getUsers().get(2).getName());
	}
	
	@Test
	public void testDistributeUsersInUndersizedGroupToOtherGroupsTwoGroupsOfFourPlusTwo() {
		
		User benji = new User("benji", "benji@home", Location.SanFrancisco);
		User tiers = new User("tiers", "tiers@home", Location.SanFrancisco);
		User berta = new User("berta", "berta@fairfax", Location.SanFrancisco);
		User lee = new User("lee", "lee@fairfax", Location.SanFrancisco);
		User ash = new User("ash", "ash@ashburn", Location.SanFrancisco);
		User sam = new User("sam", "sam@donutshop", Location.SanFrancisco);
		User jeffrey = new User("jeffrey", "jeffrey@dc", Location.SanFrancisco);
		User tim = new User("tim", "tim@dc", Location.SanFrancisco);
		User jelena = new User("jelena", "jelena@dc", Location.SanFrancisco);
		User byrd = new User("byrd", "byrd@dc", Location.SanFrancisco);
		
		LunchGroup group1 = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		group1.addUserAndKey(benji);
		group1.addUserAndKey(tiers);
		group1.addUserAndKey(berta);
		group1.addUserAndKey(lee);
		
		LunchGroup group2 = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		group2.addUserAndKey(jeffrey);
		group2.addUserAndKey(tim);
		group2.addUserAndKey(jelena);
		group2.addUserAndKey(byrd);
		
		LunchGroup undersizedGroup = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		undersizedGroup.addUserAndKey(ash);
		undersizedGroup.addUserAndKey(sam);
		
		List<LunchGroup> groups = CollectionUtil.asList(group1, group2);
		Date now = new Date();
		
		lunchService.distributeUsersInUndersizedGroupToOtherGroups(GroupType.Regular, groups, undersizedGroup, now);
		
		Assert.assertEquals(5, group1.size());
		Assert.assertEquals(5, group2.size());
		
		Assert.assertEquals("benji", group1.getUsers().get(0).getName());
		Assert.assertEquals("tiers", group1.getUsers().get(1).getName());
		Assert.assertEquals("berta", group1.getUsers().get(2).getName());
		Assert.assertEquals("lee", group1.getUsers().get(3).getName());
		Assert.assertEquals("ash", group1.getUsers().get(4).getName());
		
		Assert.assertEquals("jeffrey", group2.getUsers().get(0).getName());
		Assert.assertEquals("tim", group2.getUsers().get(1).getName());
		Assert.assertEquals("jelena", group2.getUsers().get(2).getName());
		Assert.assertEquals("byrd", group2.getUsers().get(3).getName());
		Assert.assertEquals("sam", group2.getUsers().get(4).getName());
	}
	
	@Test
	public void testNotifyUsersAboutNewLunchGroups() throws IOException {
		
		lunchService.setMailService(mockMailService);
		
		User benji = new User(1L, "benji", "benji@home", Location.SanFrancisco);
		User tiers = new User(2L, "tiers", "tiers@home", Location.SanFrancisco);
		User berta = new User(3L, "berta", "berta@fairfax", Location.SanFrancisco);
		User lee = new User(4L, "lee", "lee@fairfax", Location.SanFrancisco);
		User jeffrey = new User(5L, "jeffrey", "jeffrey@dc", Location.SanFrancisco);
		User tim = new User(6L, "tim", "tim@dc", Location.SanFrancisco);
		User jelena = new User(7L, "jelena", "jelena@dc", Location.SanFrancisco);
		User byrd = new User(8L, "byrd", "byrd@dc", Location.SanFrancisco);
		
		LunchGroup group1 = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		group1.addUsersAndKeys(benji, tiers, berta, lee);
		
		LunchGroup group2 = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		group2.addUsersAndKeys(jeffrey, tim, jelena, byrd);
		
		List<LunchGroup> groups = CollectionUtil.asList(group1, group2);
		lunchService.notifyUsersAboutNewLunchGroups(groups);
		
		Mockito.verify(mockMailService, Mockito.times(8)).send(Mockito.any(Message.class));
	}
	
	@Test
	public void testNotifyUsersAboutNewLunchGroupsPrintToConsole() throws IOException {
		lunchService.setMailService(mailService);
		
		User benji = new User(1L, "benji", "benji@home", Location.SanFrancisco);
		User rick = new User(2L, "rick", "rick@home", Location.SanFrancisco);
		
		LunchGroup group = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		group.addUsersAndKeys(benji, rick);
		
		List<LunchGroup> groups = Collections.singletonList(group);
		lunchService.notifyUsersAboutNewLunchGroups(groups);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void testGenerateLunchGroups() throws IOException {
		
		lunchService.setMailService(mockMailService);
		
		User benji = new User(1L, "benji", "benji@home", Location.SanFrancisco, GroupType.Regular);
		User tiers = new User(2L, "tiers", "tiers@home", Location.LosAngeles, GroupType.Regular);
		User lee = new User(3L, "lee", "lee@fairfax", Location.SanFrancisco, GroupType.Regular);
		User jelena = new User(4L, "jelena", "jelena@dc", Location.SanFrancisco, GroupType.Regular);
		User byrd = new User(5L, "byrd", "byrd@dc", Location.SanFrancisco, GroupType.Regular);
		
		User berta = new User(6L, "berta", "berta@fairfax", Location.NewYork, GroupType.Regular);
		User ash = new User(7L, "ash", "ash@ashburn", Location.NewYork, GroupType.Regular);
		User sam = new User(8L, "sam", "sam@donutshop", Location.NewYork, GroupType.Regular);
		
		User jeffrey = new User(9L, "jeffrey", "jeffrey@dc", Location.LosAngeles, GroupType.Regular);
		User tim = new User(10L, "tim", "tim@dc", Location.LosAngeles, GroupType.Regular);
		
		List<User> sanfranciscans = CollectionUtil.asList(benji, tiers, lee, jelena, byrd);
		List<User> yankees = CollectionUtil.asList(berta, ash, sam);
		List<User> angels = CollectionUtil.asList(jeffrey, tim);
		
		Mockito.when(userDao.getActiveUsersByLocationAndGroupType(Location.SanFrancisco, GroupType.Regular)).thenReturn(sanfranciscans);
		Mockito.when(userDao.getActiveUsersByLocationAndGroupType(Location.NewYork, GroupType.Regular)).thenReturn(yankees);
		Mockito.when(userDao.getActiveUsersByLocationAndGroupType(Location.LosAngeles, GroupType.Regular)).thenReturn(angels);
		
		lunchService.generateLunchGroups(GroupType.Regular, true);
		
		Mockito.verify(lunchGroupDao, Mockito.times(3)).persistLunchGroups(Mockito.anyList());
		Mockito.verify(mockMailService, Mockito.times(10)).send(Mockito.any(Message.class));
	}

}
