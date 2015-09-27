package com.popsugar.lunch.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.appengine.api.datastore.Entity;

public class DatastoreUtil {
	
	public static <E extends Enum<E>> E getEnum(Entity e, String propertyName, Class<E> clazz){
		String value = (String)e.getProperty(propertyName);
		if (StringUtils.isBlank(value)) {
			return null;
		}
		return Enum.valueOf(clazz, value);
	}
	
	public static void setEnum(Entity e, String propertyName, Enum<?> val){
		if (val != null) {
			e.setProperty(propertyName, val.name());
		}
	}
	
	public static <E extends Enum<E>> List<E> getEnumList(Entity e, String propertyName, Class<E> clazz){
		@SuppressWarnings("unchecked")
		List<String> stringValues = (ArrayList<String>)e.getProperty(propertyName);
		if (stringValues == null){
			return null;
		}
		List<E> enumValues = new ArrayList<>(stringValues.size());
		for (String stringValue : stringValues){
			E enumValue = Enum.valueOf(clazz, stringValue);
			enumValues.add(enumValue);
		}
		return enumValues;
	}
	
	public static void setEnumList(Entity e, String propertyName, List<? extends Enum<?>> values) {
		if (values == null) {
			e.setProperty(propertyName, null);
		}
		else {
			ArrayList<String> names = new ArrayList<>(values.size());
			for (Enum<?> value : values) {
				names.add(value.name());
			}
			e.setProperty(propertyName, names);
		}
	}

}
