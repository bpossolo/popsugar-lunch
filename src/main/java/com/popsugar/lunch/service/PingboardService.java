package com.popsugar.lunch.service;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.CharEncoding;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.popsugar.lunch.model.PingboardUser;
import com.popsugar.lunch.util.UrlUtil;

public class PingboardService {
	
	private static final String BaseUrl = "https://app.pingboard.com";
	private static final String BaseApiUrl = BaseUrl + "/api/v2";
	private static final String pingboardAuthUsername = "bpossolo@popsugar.com";
	private static final String pingboardAuthPassword = "lunchforfour";
	private static final String PingboardUsersListMemcacheKey = "pingboard-users-list";
	private static final String PingboardUsersIdMapMemcacheKey = "pingboard-users-map-id";
	private static final String PingboardUsersEmailMapMemcacheKey = "pingboard-users-map-email";
	private static final int TwoWeeksInSeconds = (int)TimeUnit.DAYS.toSeconds(14);
	private static final Expiration TwoWeekExpiration = Expiration.byDeltaSeconds(TwoWeeksInSeconds);
	
	private MemcacheService memcache;
	private URLFetchService urlFetchService;
	private long accessTokenExpirationInSeconds;
	private Date accessTokenTimestamp;
	private String accessToken;
	
	public void primeCache() {
		ArrayList<PingboardUser> users = getAllUsersCached();
		if (users == null) {
			users = getAllUsers();
			memcache.put(PingboardUsersListMemcacheKey, users, TwoWeekExpiration);
		}
		
		HashMap<Long,PingboardUser> idMap = getIdUserMapCached();
		if (idMap == null) {
			idMap = buildIdUserMap(users);
			memcache.put(PingboardUsersIdMapMemcacheKey, idMap, TwoWeekExpiration);
		}
		
		HashMap<String,PingboardUser> emailMap = buildEmailUserMap(users);
		if (emailMap == null){
			emailMap = buildEmailUserMap(users);
			memcache.put(PingboardUsersEmailMapMemcacheKey, emailMap, TwoWeekExpiration);
		}
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<PingboardUser> getAllUsersCached() {
		return (ArrayList<PingboardUser>)memcache.get(PingboardUsersListMemcacheKey);
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<Long,PingboardUser> getIdUserMapCached() {
		return (HashMap<Long,PingboardUser>)memcache.get(PingboardUsersIdMapMemcacheKey);
	}
	
	@SuppressWarnings("unchecked")
	public HashMap<Long,PingboardUser> getEmailUserMapCached() {
		return (HashMap<Long,PingboardUser>)memcache.get(PingboardUsersEmailMapMemcacheKey);
	}
	
	public PingboardUser getUserByEmail(String email) {
		try {
			initAccessToken();
			String encodedEmail = URLEncoder.encode(email, CharEncoding.UTF_8);
			
			String url = BaseApiUrl + "/users";
			url = addAccessToken(url);
			url = UrlUtil.addParam(url, "email", encodedEmail);
			
			HTTPResponse response = urlFetchService.fetch(new URL(url));
			
			byte[] content = response.getContent();
			String jsonStr = new String(content, CharEncoding.UTF_8);
			JSONObject json = new JSONObject(jsonStr);
			JSONArray users = json.getJSONArray("users");
			List<PingboardUser> pingboardUsers = decodeUsers(users);
			if (pingboardUsers.isEmpty()) {
				return null;
			}
			else {
				return pingboardUsers.get(0);
			}
		}
		catch(IOException | JSONException e) {
			throw new RuntimeException("Failed to get user by email", e);
		}
	}
	
	public ArrayList<PingboardUser> getAllUsers() {
		int pageNum = 1;
		ArrayList<PingboardUser> allUsers = new ArrayList<>();
		List<PingboardUser> pageOfUsers;
		do {
			pageOfUsers = getPageOfUsers(pageNum);
			allUsers.addAll(pageOfUsers);
			pageNum++;
		}
		while (!pageOfUsers.isEmpty());
		return allUsers;
	}
	
	public List<PingboardUser> getPageOfUsers(int pageNum) {
		try{
			initAccessToken();
			
			String url = BaseApiUrl + "/users";
			url = addAccessToken(url);
			url = UrlUtil.addParam(url, "page", Integer.toString(pageNum));
			
			HTTPResponse response = urlFetchService.fetch(new URL(url));
			
			byte[] content = response.getContent();
			String jsonStr = new String(content, CharEncoding.UTF_8);
			JSONObject json = new JSONObject(jsonStr);
			JSONArray jsonUsers = json.getJSONArray("users");
			List<PingboardUser> users = decodeUsers(jsonUsers);
			return users;
		}
		catch(IOException | JSONException e){
			throw new RuntimeException("Failed to get pingboard users", e);
		}
	}
	
	private List<PingboardUser> decodeUsers(JSONArray jsonUsers) {
		List<PingboardUser> users = new ArrayList<>(jsonUsers.length());
		for (int i = 0; i < jsonUsers.length(); i++ ){
			JSONObject jsonUser = jsonUsers.getJSONObject(i);
			PingboardUser user = decodeUser(jsonUser);
			users.add(user);
		}
		return users;
	}
	
	private PingboardUser decodeUser(JSONObject jsonUser) {
		PingboardUser user = new PingboardUser();
		user.setId(Long.valueOf(jsonUser.getString("id")));
		user.setEmail(jsonUser.getString("email"));
		JSONObject avatarUrls = jsonUser.optJSONObject("avatar_urls");
		if (avatarUrls != null) {
			user.setAvatarUrlSmall(avatarUrls.optString("small"));
		}
		return user;
	}
	
	public void setUrlFetchService(URLFetchService urlFetchService) {
		this.urlFetchService = urlFetchService;
	}
	
	public void setMemcache(MemcacheService memcache) {
		this.memcache = memcache;
	}
	
	public HashMap<String,PingboardUser> buildEmailUserMap(List<PingboardUser> users){
		HashMap<String,PingboardUser> map = new HashMap<>();
		for (PingboardUser user : users) {
			map.put(user.getEmail(), user);
		}
		return map;
	}
	
	public HashMap<Long,PingboardUser> buildIdUserMap(List<PingboardUser> users) {
		HashMap<Long,PingboardUser> map = new HashMap<>();
		for (PingboardUser user : users) {
			map.put(user.getId(), user);
		}
		return map;
	}
	
	private String addAccessToken(String url) {
		url = UrlUtil.addParam(url, "access_token", accessToken);
		return url;
	}
	
	private boolean isAccessTokenExpired() {
		if (accessToken == null){
			return true;
		}
		long start = accessTokenTimestamp.getTime();
		long end = new Date().getTime();
		long timeElapsedInSeconds = (end - start) / 1000;
		if ( timeElapsedInSeconds < accessTokenExpirationInSeconds) {
			return false;
		}
		return true;
	}
	
	String getAccessToken() {
		return accessToken;
	}
	
	void initAccessToken() {
		try {
			if (isAccessTokenExpired()) {
				String encodedEmail = URLEncoder.encode(pingboardAuthUsername, CharEncoding.UTF_8);
				String encodedPassword = URLEncoder.encode(pingboardAuthPassword, CharEncoding.UTF_8);
				
				String url = BaseUrl + "/oauth/token";
				url = UrlUtil.addParam(url, "grant_type", "password");
				url = UrlUtil.addParam(url, "username", encodedEmail);
				url = UrlUtil.addParam(url, "password", encodedPassword);
				
				HTTPRequest request = new HTTPRequest(new URL(url), HTTPMethod.POST);
				request.getFetchOptions().setDeadline(10.0);
				HTTPResponse response = urlFetchService.fetch(request);
				
				byte[] content = response.getContent();
				String jsonStr = new String(content, CharEncoding.UTF_8);
				JSONObject json = new JSONObject(jsonStr);
				
				accessToken = json.getString("access_token");
				accessTokenExpirationInSeconds = json.getLong("expires_in");
				accessTokenTimestamp = new Date();
			}
		}
		catch(IOException e){
			throw new RuntimeException("Failed to get access token", e);
		}
	}

}
