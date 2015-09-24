package com.popsugar.lunch.api;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class JerseyApp extends ResourceConfig {
	
	public JerseyApp() {
		super(
			JacksonFeature.class,
			LunchAPI.class
		);
	}
}
