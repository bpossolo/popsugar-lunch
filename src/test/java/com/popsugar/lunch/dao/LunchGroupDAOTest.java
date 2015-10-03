package com.popsugar.lunch.dao;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.model.Location;

public class LunchGroupDAOTest {
	
	private final LocalServiceTestHelper helper = 
		new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
		
	private LunchGroupDAO dao;
	private DatastoreService datastore;
	
	@Before
	public void setUp() {
		helper.setUp();
		datastore = DatastoreServiceFactory.getDatastoreService();
		dao = new LunchGroupDAO(datastore);
	}
	
	@After
	public void tearDown() {
		helper.tearDown();
	}
	
	@Test
	public void testDeactivateCurrentLunchGroups(){
		
		TestUtils.createLunchGroup(datastore, Location.SanFrancisco, GroupType.Regular);
		TestUtils.createLunchGroup(datastore, Location.SanFrancisco, GroupType.Regular);
		
		dao.deactivateCurrentLunchGroups(GroupType.Regular);
		
		Filter filter = new FilterPredicate(LunchGroupDAO.ActiveProp, FilterOperator.EQUAL, true);
		Query q = new Query(LunchGroupDAO.Kind).setFilter(filter);
		int num = datastore.prepare(q).countEntities(FetchOptions.Builder.withDefaults());
		Assert.assertEquals(0, num);
	}

}
