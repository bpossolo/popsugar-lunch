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
import com.popsugar.lunch.ui.client.Location;
import com.popsugar.lunch.ui.client.LunchGroup;
import com.popsugar.lunch.ui.client.User;

public class LunchManagerTest {
	
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
		new LocalDatastoreServiceTestConfig(),
		new LocalMailServiceTestConfig().setLogMailBody(true));
	
	private final LunchManager manager = new LunchManager();
	
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
	public void testGetActiveUsersByLocation(){
		
		User benji = new User("benji", "benji@home", Location.SanFrancisco);
		persistInTx(benji);
		
		User tiers = new User("tiers", "tiers@home", Location.LosAngeles);
		persistInTx(tiers);
		
		User ashley = new User("ashley", "ashley@home", Location.LosAngeles);
		ashley.setActive(false);
		persistInTx(ashley);
		
		User brad = new User("brad", "brad@home", Location.NewYork);
		persistInTx(brad);
		
		em.getTransaction().begin();
		List<User> laPeeps = manager.getActiveUsersByLocation(em, Location.LosAngeles);
		em.getTransaction().commit();
		assertEquals(1, laPeeps.size());
		assertEquals(tiers, laPeeps.get(0));
		
		em.getTransaction().begin();
		List<User> nyPeeps = manager.getActiveUsersByLocation(em, Location.NewYork);
		em.getTransaction().commit();
		assertEquals(1, nyPeeps.size());
		assertEquals(brad, nyPeeps.get(0));
	}
	
	@Test
	public void testPersistGetAndDeleteLunchGroups(){
		
		User benji = new User("benji", "benji@home", Location.SanFrancisco);
		persistInTx(benji);
		
		User tiers = new User("tiers", "tiers@home", Location.SanFrancisco);
		persistInTx(tiers);
		
		LunchGroup group1 = new LunchGroup(Location.SanFrancisco);
		group1.addUserAndKey(benji);
		
		LunchGroup group2 = new LunchGroup(Location.SanFrancisco);
		group2.addUserAndKey(tiers);
		
		List<LunchGroup> groups = Arrays.asList(group1, group2);
		
		manager.persistLunchGroupsInTx(em, groups);
		
		em.getTransaction().begin();
		groups = manager.getLunchGroupsWithUsers(em);
		em.getTransaction().commit();
		
		assertEquals(2, groups.size());
		assertEquals(benji.getKey(), groups.get(0).getUsers().get(0).getKey());
		assertEquals(tiers.getKey(), groups.get(1).getUsers().get(0).getKey());
		
		manager.deleteLunchGroupsInTx(em);
		
		groups = manager.getLunchGroupsWithUsers(em);
		assertTrue(groups.isEmpty());
	}
	
	@Test
	public void testDeleteLunchGroupsInTx(){
		
		LunchGroup group1 = new LunchGroup(Location.SanFrancisco);
		LunchGroup group2 = new LunchGroup(Location.SanFrancisco);
		
		persistInTx(group1);
		persistInTx(group2);
		
		manager.deleteLunchGroupsInTx(em);
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
		
		LunchGroup group = new LunchGroup(Location.SanFrancisco);
		group.addUserAndKey(benji);
		group.addUserAndKey(tiers);
		group.addUserAndKey(berta);
		group.addUserAndKey(lee);
		
		LunchGroup undersizedGroup = new LunchGroup(Location.SanFrancisco);
		undersizedGroup.addUserAndKey(ash);
		
		ArrayList<LunchGroup> groups = new ArrayList<>();
		groups.add(group);
		
		manager.distributeUsersInUndersizedGroupToOtherGroups(groups, undersizedGroup);
		
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
		
		LunchGroup group = new LunchGroup(Location.SanFrancisco);
		group.addUserAndKey(benji);
		group.addUserAndKey(tiers);
		group.addUserAndKey(berta);
		group.addUserAndKey(lee);
		
		LunchGroup undersizedGroup = new LunchGroup(Location.SanFrancisco);
		undersizedGroup.addUserAndKey(ash);
		undersizedGroup.addUserAndKey(sam);
		
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
		
		LunchGroup group1 = new LunchGroup(Location.SanFrancisco);
		group1.addUserAndKey(benji);
		group1.addUserAndKey(tiers);
		group1.addUserAndKey(berta);
		group1.addUserAndKey(lee);
		
		LunchGroup group2 = new LunchGroup(Location.SanFrancisco);
		group2.addUserAndKey(jeffrey);
		group2.addUserAndKey(tim);
		group2.addUserAndKey(jelena);
		group2.addUserAndKey(byrd);
		
		LunchGroup undersizedGroup = new LunchGroup(Location.SanFrancisco);
		undersizedGroup.addUserAndKey(ash);
		undersizedGroup.addUserAndKey(sam);
		
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
		
		LunchGroup group1 = new LunchGroup(Location.SanFrancisco);
		group1.addUserAndKey(benji);
		group1.addUserAndKey(tiers);
		group1.addUserAndKey(berta);
		group1.addUserAndKey(lee);
		
		LunchGroup group2 = new LunchGroup(Location.SanFrancisco);
		group2.addUserAndKey(jeffrey);
		group2.addUserAndKey(tim);
		group2.addUserAndKey(jelena);
		group2.addUserAndKey(byrd);
		
		List<LunchGroup> groups = Arrays.asList(group1, group2);
		
		manager.notifyUsersAboutNewLunchGroups(groups);
	}
	
	@Test
	public void testRegenerateLunchGroups(){
		
		User benji = new User("benji", "benji@home", Location.SanFrancisco);
		User tiers = new User("tiers", "tiers@home", Location.LosAngeles);
		User berta = new User("berta", "berta@fairfax", Location.NewYork);
		User lee = new User("lee", "lee@fairfax", Location.SanFrancisco);
		User ash = new User("ash", "ash@ashburn", Location.NewYork);
		User sam = new User("sam", "sam@donutshop", Location.NewYork);
		User jeffrey = new User("jeffrey", "jeffrey@dc", Location.LosAngeles);
		User tim = new User("tim", "tim@dc", Location.LosAngeles);
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
		
		manager.regenerateLunchGroups(em);
		
		List<LunchGroup> groups = manager.getLunchGroups(em);
		
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
	
	private void persistInTx(Object obj){
		em.getTransaction().begin();
		em.persist(obj);
		em.getTransaction().commit();
	}

}
