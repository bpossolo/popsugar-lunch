package com.popsugar.lunch.ui.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LunchRpcServiceAsync {

	void createUser(String name, String email, Location location, AsyncCallback<Void> callback);

	void getLunchGroups(AsyncCallback<LunchGroupData> callback);

}
