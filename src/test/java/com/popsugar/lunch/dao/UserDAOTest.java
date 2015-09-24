package com.popsugar.lunch.dao;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.model.Location;
import com.popsugar.lunch.model.User;

public class UserDAOTest {
	
	private final LocalServiceTestHelper helper = 
		new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	
	private UserDAO dao;
	private DatastoreService datastore;
	
	@Before
	public void setUp() {
		helper.setUp();
		datastore = DatastoreServiceFactory.getDatastoreService();
		dao = new UserDAO(datastore);
	}
	
	@After
	public void tearDown() {
		helper.tearDown();
	}

	@Test
	public void testGetActiveUsersByLocation(){
		TestUtils.createUser(datastore, "benji", "benji@home", Location.SanFrancisco, true, GroupType.Regular);
		TestUtils.createUser(datastore, "tiers", "tiers@home", Location.LosAngeles, true, GroupType.Regular);
		TestUtils.createUser(datastore, "ashley", "ashley@home", Location.LosAngeles, false, GroupType.Regular);
		TestUtils.createUser(datastore, "brad", "brad@home", Location.NewYork, true, GroupType.Regular);
		
		List<User> laPeeps = dao.getActiveUsersByLocation(Location.LosAngeles, GroupType.Regular);
		assertEquals(1, laPeeps.size());
		assertEquals("tiers", laPeeps.get(0).getName());
		
		List<User> nyPeeps = dao.getActiveUsersByLocation(Location.NewYork, GroupType.Regular);
		assertEquals(1, nyPeeps.size());
		assertEquals("brad", nyPeeps.get(0).getName());
	}
}
