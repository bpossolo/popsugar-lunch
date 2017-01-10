package com.popsugar.lunch.service;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.popsugar.lunch.dao.RefreshTokenDAO;
import com.popsugar.lunch.model.PingboardUser;
import com.popsugar.lunch.oauth.AccessToken;
import com.popsugar.lunch.oauth.OAuthApp;
import com.popsugar.lunch.util.UrlUtil;

public class PingboardService {
	
	private static final String BaseUrl = "https://app.pingboard.com";
	private static final String BaseApiUrl = BaseUrl + "/api/v2";
	
	private URLFetchService urlFetchService;
	private RefreshTokenDAO refreshTokenDao;
	private AccessToken accessToken;
	
	public PingboardService(URLFetchService urlFetchService, RefreshTokenDAO refreshTokenDao) {
		this.urlFetchService = urlFetchService;
		this.refreshTokenDao = refreshTokenDao;
	}

	public PingboardUser getUserByEmail(String email) {
		try {
			initAccessTokenFromCredentials();
			String encodedEmail = URLEncoder.encode(email, CharEncoding.UTF_8);
			
			String url = BaseApiUrl + "/users";
			url = UrlUtil.addAccessToken(url, accessToken);
			url = UrlUtil.addParam(url, "email", encodedEmail);
			
			HTTPResponse response = urlFetchService.fetch(new URL(url));
			
			byte[] content = response.getContent();
			String jsonStr = new String(content, CharEncoding.UTF_8);
			JSONObject json = new JSONObject(jsonStr);
			JSONArray jsonUsers = json.getJSONArray("users");
			List<PingboardUser> users = decodeUsers(jsonUsers);
			if (users.isEmpty()) {
				return null;
			}
			else {
				return users.get(0);
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
			initAccessTokenFromCredentials();
			
			String url = BaseApiUrl + "/users";
			url = UrlUtil.addAccessToken(url, accessToken);
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
	
	void initAccessTokenFromRefreshToken() {
		try {
			if (accessToken == null || accessToken.isExpired()) {
				
				String refreshToken = refreshTokenDao.getRefreshToken(OAuthApp.Pingboard);
				
				String url = BaseUrl + "/oauth/token";
				url = UrlUtil.addParam(url, "grant_type", "refresh_token");
				url = UrlUtil.addParam(url, "refresh_token", refreshToken);
				
				HTTPRequest request = new HTTPRequest(new URL(url), HTTPMethod.POST);
				request.getFetchOptions().setDeadline(10.0);
				HTTPResponse response = urlFetchService.fetch(request);
				
				byte[] content = response.getContent();
				String jsonStr = new String(content, CharEncoding.UTF_8);
				JSONObject json = new JSONObject(jsonStr);
				
				String accessTokenValue = json.getString("access_token");
				int expiration = json.getInt("expires_in");
				accessToken = new AccessToken(accessTokenValue, expiration);
				
				// oauth spec states that the old refresh token must be discarded if a
				// new one is provided
				String newRefreshToken = json.optString("refresh_token");
				if (StringUtils.isNotBlank(newRefreshToken)) {
					refreshTokenDao.saveRefreshToken(OAuthApp.Pingboard, refreshToken);
				}
			}
		}
		catch(EntityNotFoundException e) {
			throw new RuntimeException("Refresh token for pingboard app does not exist", e);
		}
		catch(IOException e){
			throw new RuntimeException("Failed to get access token", e);
		}
	}
	
	void initAccessTokenFromCredentials() {
		try {
			if (accessToken == null || accessToken.isExpired()) {
				
				String url = BaseUrl + "/oauth/token";
				url = UrlUtil.addParam(url, "grant_type", "password");
				url = UrlUtil.addParam(url, "username", "bpossolo@popsugar.com");
				url = UrlUtil.addParam(url, "password", "TODO");
				
				HTTPRequest request = new HTTPRequest(new URL(url), HTTPMethod.POST);
				request.getFetchOptions().setDeadline(10.0);
				HTTPResponse response = urlFetchService.fetch(request);
				
				byte[] content = response.getContent();
				String jsonStr = new String(content, CharEncoding.UTF_8);
				JSONObject json = new JSONObject(jsonStr);
				
				String accessTokenValue = json.getString("access_token");
				int expiration = json.getInt("expires_in");
				accessToken = new AccessToken(accessTokenValue, expiration);
			}
		}
		catch(IOException e){
			throw new RuntimeException("Failed to get access token", e);
		}
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
	
	private List<PingboardUser> decodeUsers(JSONArray jsonUsers) {
		List<PingboardUser> users = new ArrayList<>(jsonUsers.length());
		for (int i = 0; i < jsonUsers.length(); i++ ){
			JSONObject jsonUser = jsonUsers.getJSONObject(i);
			PingboardUser user = decodeUser(jsonUser);
			users.add(user);
		}
		return users;
	}
	
	AccessToken getAccessToken() {
		return accessToken;
	}

}
