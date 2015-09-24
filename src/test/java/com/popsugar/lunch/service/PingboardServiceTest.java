package com.popsugar.lunch.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.popsugar.lunch.model.PingboardUser;

public class PingboardServiceTest {
	
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
			new LocalMemcacheServiceTestConfig(),
			new LocalURLFetchServiceTestConfig());
	
	private PingboardService pingboardService;
	
	@Before
	public void setUp(){
		helper.setUp();
		MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();
		URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
		
		pingboardService = new PingboardService();
		pingboardService.setMemcache(memcache);
		pingboardService.setUrlFetchService(urlFetchService);
	}
	
	@After
	public void tearDown(){
		helper.tearDown();
	}
	
	@Test
	public void testInitAccessToken() {
		pingboardService.initAccessToken();
		String accessToken = pingboardService.getAccessToken();
		Assert.assertNotNull(accessToken);
	}
	
	@Test
	public void testGetAllUsers(){
		Set<String> emails = new HashSet<>();
		ArrayList<PingboardUser> users = pingboardService.getAllUsers();
		for (PingboardUser user : users){
			emails.add(user.getEmail());
		}
		Assert.assertTrue(emails.contains("bpossolo@popsugar.com"));
	}

}
