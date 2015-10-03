package com.popsugar.lunch.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.popsugar.lunch.dao.RefreshTokenDAO;
import com.popsugar.lunch.model.PingboardUser;
import com.popsugar.lunch.oauth.AccessToken;
import com.popsugar.lunch.oauth.OAuthApp;

public class PingboardServiceTest {
	
	private final LocalServiceTestHelper helper = new LocalServiceTestHelper(
			new LocalURLFetchServiceTestConfig());
	
	private PingboardService pingboardService;
	private RefreshTokenDAO refreshTokenDao;
	
	@Before
	public void setUp() throws Exception {
		helper.setUp();
		URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
		
		String refreshToken = "4f0411394fa9d2a6af0c90903c390eecbb43d1ca983f17caee6a415cb8ff0e90";
		refreshTokenDao = Mockito.mock(RefreshTokenDAO.class);
		Mockito.when(refreshTokenDao.getRefreshToken(OAuthApp.Pingboard)).thenReturn(refreshToken);
		
		pingboardService = new PingboardService(urlFetchService, refreshTokenDao);
	}
	
	@After
	public void tearDown(){
		helper.tearDown();
	}
	
	@Test
	public void testInitAccessToken() {
		pingboardService.initAccessToken();
		AccessToken accessToken = pingboardService.getAccessToken();
		Assert.assertNotNull(accessToken);
		Assert.assertNotNull(accessToken.getValue());
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
