package com.popsugar.lunch.util;

import com.google.appengine.api.utils.SystemProperty;
import com.popsugar.lunch.model.User;
import com.popsugar.lunch.oauth.AccessToken;

public class UrlUtil {
	
	public static String addAccessToken(String url, AccessToken accessToken) {
		return addParam(url, "access_token", accessToken.getValue());
	}

	public static String addParam(String url, String name, String value) {
		if (url.contains("?")) {
			url += "&";
		}
		else {
			url += "?";
		}
		url += name + "=" + value;
		return url;
	}
	
	public static String getBaseApiUrl() {
		if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) {
			return "http://lunch.popsugar.com/api/lunch";
		}
		else {
			return "http://localhost:8888/api/lunch";
		}
	}
	
	public static String getUnsubscribeUrl(User user) {
		return getBaseApiUrl() + "/unsubscribe/" + user.getKey();
	}
}
