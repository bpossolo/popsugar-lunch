package com.popsugar.lunch.ui.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * The entry point into the application.
 */
public class UiEntryPoint implements EntryPoint {

	@Override
	public void onModuleLoad() {
		RootPanel htmlBody = RootPanel.get();
		htmlBody.add(new HomeView());
	}
	
}
