package com.popsugar.lunch.server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalMailServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.popsugar.lunch.ui.client.LunchGroup;
import com.popsugar.lunch.ui.client.User;

public class LunchManagerTest {
	
	private LocalServiceTestHelper helper = new LocalServiceTestHelper(
		new LocalDatastoreServiceTestConfig(),
		new LocalMailServiceTestConfig().setLogMailBody(true));
	
	private LunchManager manager = new LunchManager();
	
	private EntityManager em;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		em = EMF.get().createEntityManager();
	}

	@After
	public void tearDown() throws Exception {
		if( em.getTransaction().isActive() )
			em.getTransaction().rollback();
		em.close();
		helper.tearDown();
	}
	
	@Test
	public void testPersistGetAndDeleteLunchGroups(){
		
		User benji = new User("benji", "benji@home");
		persistInTx(benji);
		
		User tiers = new User("tiers", "tiers@home");
		persistInTx(tiers);
		
		LunchGroup group1 = new LunchGroup();
		group1.addUser(benji);
		
		LunchGroup group2 = new LunchGroup();
		group2.addUser(tiers);
		
		List<LunchGroup> groups = Arrays.asList(group1, group2);
		
		manager.persistLunchGroupsInTx(em, groups);
		
		groups = manager.getLunchGroups(em);
		
		assertEquals(2, groups.size());
		assertEquals(benji.getKey(), groups.get(0).getUsers().get(0).getKey());
		assertEquals(tiers.getKey(), groups.get(1).getUsers().get(0).getKey());
		
		manager.deleteLunchGroupsInTx(em);
		
		groups = manager.getLunchGroups(em);
		assertTrue(groups.isEmpty());
	}

	@Test
	public void testDistributeUsersInUndersizedGroupToOtherGroupsFourPlusOne() {
		
		LunchGroup group = new LunchGroup();
		group.addUser(new User("benji", "benji@home"));
		group.addUser(new User("tiers", "tiers@home"));
		group.addUser(new User("berta", "berta@fairfax"));
		group.addUser(new User("lee", "lee@fairfax"));
		
		LunchGroup undersizedGroup = new LunchGroup();
		undersizedGroup.addUser(new User("ash", "ash@ashburn"));
		
		ArrayList<LunchGroup> groups = new ArrayList<>();
		groups.add(group);
		
		manager.distributeUsersInUndersizedGroupToOtherGroups(groups, undersizedGroup);
		
		assertEquals(5, group.size());
		assertEquals("ash", group.getUsers().get(4).getName());
		assertEquals("ash@ashburn", group.getUsers().get(4).getEmail());
	}
	
	@Test
	public void testDistributeUsersInUndersizedGroupToOtherGroupsFourPlusTwo() {
		
		LunchGroup group = new LunchGroup();
		group.addUser(new User("benji", "benji@home"));
		group.addUser(new User("tiers", "tiers@home"));
		group.addUser(new User("berta", "berta@fairfax"));
		group.addUser(new User("lee", "lee@fairfax"));
		
		LunchGroup undersizedGroup = new LunchGroup();
		undersizedGroup.addUser(new User("ash", "ash@ashburn"));
		undersizedGroup.addUser(new User("sam", "sam@donutshop"));
		
		ArrayList<LunchGroup> groups = new ArrayList<>();
		groups.add(group);
		
		manager.distributeUsersInUndersizedGroupToOtherGroups(groups, undersizedGroup);
		
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
		
		LunchGroup group1 = new LunchGroup();
		group1.addUser(new User("benji", "benji@home"));
		group1.addUser(new User("tiers", "tiers@home"));
		group1.addUser(new User("berta", "berta@fairfax"));
		group1.addUser(new User("lee", "lee@fairfax"));
		
		LunchGroup group2 = new LunchGroup();
		group2.addUser(new User("jeffrey", "jeffrey@dc"));
		group2.addUser(new User("tim", "tim@dc"));
		group2.addUser(new User("jelena", "jelena@dc"));
		group2.addUser(new User("byrd", "byrd@dc"));
		
		LunchGroup undersizedGroup = new LunchGroup();
		undersizedGroup.addUser(new User("ash", "ash@ashburn"));
		undersizedGroup.addUser(new User("sam", "sam@donutshop"));
		
		ArrayList<LunchGroup> groups = new ArrayList<>();
		groups.add(group1);
		groups.add(group2);
		
		manager.distributeUsersInUndersizedGroupToOtherGroups(groups, undersizedGroup);
		
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
		
		LunchGroup group1 = new LunchGroup();
		group1.addUser(new User("benji", "benji@home"));
		group1.addUser(new User("tiers", "tiers@home"));
		group1.addUser(new User("berta", "berta@fairfax"));
		group1.addUser(new User("lee", "lee@fairfax"));
		
		LunchGroup group2 = new LunchGroup();
		group2.addUser(new User("jeffrey", "jeffrey@dc"));
		group2.addUser(new User("tim", "tim@dc"));
		group2.addUser(new User("jelena", "jelena@dc"));
		group2.addUser(new User("byrd", "byrd@dc"));
		
		List<LunchGroup> groups = Arrays.asList(group1, group2);
		
		manager.notifyUsersAboutNewLunchGroups(groups);
	}
	
	private void persistInTx(Object obj){
		em.getTransaction().begin();
		em.persist(obj);
		em.getTransaction().commit();
	}

}
