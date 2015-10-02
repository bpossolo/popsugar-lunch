package com.popsugar.lunch.util;

import java.util.ArrayList;

public class CollectionUtil {

	public static <T> ArrayList<T> singletonList(T t) {
		ArrayList<T> list = new ArrayList<>(1);
		list.add(t);
		return list;
	}
	
	@SafeVarargs
	public static <T> ArrayList<T> asList(T...values) {
		ArrayList<T> list = new ArrayList<>(values.length);
		for (T value : values) {
			list.add(value);
		}
		return list;
	}
}
