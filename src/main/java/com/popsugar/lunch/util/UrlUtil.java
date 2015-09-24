package com.popsugar.lunch.util;

public class UrlUtil {

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
}
