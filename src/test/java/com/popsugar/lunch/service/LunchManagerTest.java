package com.popsugar.lunch.service;

import java.util.Calendar;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.popsugar.lunch.dao.LunchGroupDAO;
import com.popsugar.lunch.dao.UserDAO;

public class LunchManagerTest {
	
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
		new LocalMemcacheServiceTestConfig(),
		new LocalMailServiceTestConfig().setLogMailBody(true));
	
	private LunchManager manager;
	private UserDAO userDao;
	private LunchGroupDAO lunchGroupDao;
	private MemcacheService memcache;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		memcache = MemcacheServiceFactory.getMemcacheService();
		
		userDao = Mockito.mock(UserDAO.class);
		lunchGroupDao = Mockito.mock(LunchGroupDAO.class);
		
		manager = new LunchManager();
		manager.setUserDao(userDao);
		manager.setLunchGroupDao(lunchGroupDao);
		manager.setMemcache(memcache);
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
	
	/*
	@Test
	public void testPersistGetAndDeleteLunchGroups(){
		
		User benji = new User("benji", "benji@home", Location.SanFrancisco);
		persistInTx(benji);
		
		User tiers = new User("tiers", "tiers@home", Location.SanFrancisco);
		persistInTx(tiers);
		
		LunchGroup group1 = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		group1.addUserAndKey(benji);
		
		LunchGroup group2 = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		group2.addUserAndKey(tiers);
		
		List<LunchGroup> groups = Arrays.asList(group1, group2);
		
		manager.persistLunchGroupsInTx(em, groups);
		
		groups = manager.getLunchGroupsWithUsers();
		
		assertEquals(2, groups.size());
		assertEquals(benji.getKey(), groups.get(0).getUsers().get(0).getKey());
		assertEquals(tiers.getKey(), groups.get(1).getUsers().get(0).getKey());
		
		manager.deleteLunchGroupsInTx(em);
		
		groups = manager.getLunchGroupsWithUsers(em);
		assertTrue(groups.isEmpty());
	}

	@Test
	public void testDistributeUsersInUndersizedGroupToOtherGroupsFourPlusOne() {
		
		User benji = new User("benji", "benji@home", Location.SanFrancisco);
		User tiers = new User("tiers", "tiers@home", Location.SanFrancisco);
		User berta = new User("berta", "berta@fairfax", Location.SanFrancisco);
		User lee = new User("lee", "lee@fairfax", Location.SanFrancisco);
		User ash = new User("ash", "ash@ashburn", Location.SanFrancisco);
		persistInTx(benji);
		persistInTx(tiers);
		persistInTx(berta);
		persistInTx(lee);
		persistInTx(ash);
		
		LunchGroup group = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		group.addUserAndKey(benji);
		group.addUserAndKey(tiers);
		group.addUserAndKey(berta);
		group.addUserAndKey(lee);
		
		LunchGroup undersizedGroup = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		undersizedGroup.addUserAndKey(ash);
		
		ArrayList<LunchGroup> groups = new ArrayList<>();
		groups.add(group);
		
		manager.distributeUsersInUndersizedGroupToOtherGroups(GroupType.Regular, groups, undersizedGroup);
		
		assertEquals(5, group.size());
		assertEquals("ash", group.getUsers().get(4).getName());
		assertEquals("ash@ashburn", group.getUsers().get(4).getEmail());
	}
	
	@Test
	public void testDistributeUsersInUndersizedGroupToOtherGroupsFourPlusTwo() {
		
		User benji = new User("benji", "benji@home", Location.SanFrancisco);
		User tiers = new User("tiers", "tiers@home", Location.SanFrancisco);
		User berta = new User("berta", "berta@fairfax", Location.SanFrancisco);
		User lee = new User("lee", "lee@fairfax", Location.SanFrancisco);
		User ash = new User("ash", "ash@ashburn", Location.SanFrancisco);
		User sam = new User("sam", "sam@donutshop", Location.SanFrancisco);
		persistInTx(benji);
		persistInTx(tiers);
		persistInTx(berta);
		persistInTx(lee);
		persistInTx(ash);
		persistInTx(sam);
		
		LunchGroup group = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		group.addUserAndKey(benji);
		group.addUserAndKey(tiers);
		group.addUserAndKey(berta);
		group.addUserAndKey(lee);
		
		LunchGroup undersizedGroup = new LunchGroup(Location.SanFrancisco, GroupType.Regular);
		undersizedGroup.addUserAndKey(ash);
		undersizedGroup.addUserAndKey(sam);
		
		ArrayList<LunchGroup> groups = new ArrayList<>();
		groups.add(group);
		
		manager.distributeUsersInUndersizedGroupToOtherGroups(GroupType.Regular, groups, undersizedGroup);
		
		LunchGroup group1 = groups.get(0);
		LunchGroup group2 = groups.get(1);
		
		assertEquals(3, group1.size());
		assertEquals(3, group2.size());
		
		assertEquals("benji", group1.getUsers().get(0).getName());
		assertEquals("tiers", group1.getUsers().get(1).getName());
		assertEquals("berta", group1.getUsers().get(2).getName());
		
		assertEquals("lee", group2.getUsers().get(0).getName());
		assertEquals("ash", group2.getUsers().get(1).getName());
		assertEquals("sam", group2.getUsers().get(2).getName());
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
		persistInTx(benji);
		persistInTx(tiers);
		persistInTx(berta);
		persistInTx(lee);
		persistInTx(ash);
		persistInTx(sam);
		persistInTx(jeffrey);
		persistInTx(tim);
		persistInTx(jelena);
		persistInTx(byrd);
		
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
		
		ArrayList<LunchGroup> groups = new ArrayList<>();
		groups.add(group1);
		groups.add(group2);
		
		manager.distributeUsersInUndersizedGroupToOtherGroups(GroupType.Regular, groups, undersizedGroup);
		
		assertEquals(5, group1.size());
		assertEquals(5, group2.size());
		
		assertEquals("benji", group1.getUsers().get(0).getName());
		assertEquals("tiers", group1.getUsers().get(1).getName());
		assertEquals("berta", group1.getUsers().get(2).getName());
		assertEquals("lee", group1.getUsers().get(3).getName());
		assertEquals("ash", group1.getUsers().get(4).getName());
		
		assertEquals("jeffrey", group2.getUsers().get(0).getName());
		assertEquals("tim", group2.getUsers().get(1).getName());
		assertEquals("jelena", group2.getUsers().get(2).getName());
		assertEquals("byrd", group2.getUsers().get(3).getName());
		assertEquals("sam", group2.getUsers().get(4).getName());
	}
	
	@Test
	public void testNotifyUsersAboutNewLunchGroups(){
		
		User benji = new User("benji", "benji@home", Location.SanFrancisco);
		User tiers = new User("tiers", "tiers@home", Location.SanFrancisco);
		User berta = new User("berta", "berta@fairfax", Location.SanFrancisco);
		User lee = new User("lee", "lee@fairfax", Location.SanFrancisco);
		User jeffrey = new User("jeffrey", "jeffrey@dc", Location.SanFrancisco);
		User tim = new User("tim", "tim@dc", Location.SanFrancisco);
		User jelena = new User("jelena", "jelena@dc", Location.SanFrancisco);
		User byrd = new User("byrd", "byrd@dc", Location.SanFrancisco);
		persistInTx(benji);
		persistInTx(tiers);
		persistInTx(berta);
		persistInTx(lee);
		persistInTx(jeffrey);
		persistInTx(tim);
		persistInTx(jelena);
		persistInTx(byrd);
		
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
		
		List<LunchGroup> groups = Arrays.asList(group1, group2);
		
		manager.notifyUsersAboutNewLunchGroups(groups);
	}
	
	@Test
	public void testRegenerateLunchGroups(){
		
		TestUtils.createUser(datastore, "benji", "benji@home", Location.SanFrancisco, true);
		TestUtils.createUser(datastore, "tiers", "tiers@home", Location.LosAngeles, true);
		TestUtils.createUser(datastore, "berta", "berta@fairfax", Location.NewYork, true);
		TestUtils.createUser(datastore, "lee", "lee@fairfax", Location.SanFrancisco, true);
		TestUtils.createUser(datastore, "ash", "ash@ashburn", Location.NewYork, true);
		TestUtils.createUser(datastore, "sam", "sam@donutshop", Location.NewYork, true);
		TestUtils.createUser(datastore, "jeffrey", "jeffrey@dc", Location.LosAngeles, true);
		TestUtils.createUser(datastore, "tim", "tim@dc", Location.LosAngeles, true);
		TestUtils.createUser(datastore, "jelena", "jelena@dc", Location.SanFrancisco, true);
		TestUtils.createUser(datastore, "byrd", "byrd@dc", Location.SanFrancisco, true);
		
		manager.regenerateLunchGroups();
		
		String week = null;
		
		List<LunchGroup> groups = manager.getLunchGroupsWithUsers(week);
		
		assertEquals(3, groups.size());
		for( LunchGroup group : groups ){
			switch( group.getLocation() ){
			
			case SanFrancisco :
				assertTrue(group.contains(benji));
				assertTrue(group.contains(lee));
				assertTrue(group.contains(jelena));
				assertTrue(group.contains(byrd));
				break;
				
			case NewYork :
				assertTrue(group.contains(berta));
				assertTrue(group.contains(ash));
				assertTrue(group.contains(sam));
				break;
				
			case LosAngeles :
				assertTrue(group.contains(tiers));
				assertTrue(group.contains(jeffrey));
				assertTrue(group.contains(tim));
				break;
			}
		}
	}
	*/

}
