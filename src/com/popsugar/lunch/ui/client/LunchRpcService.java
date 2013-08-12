package com.popsugar.lunch.ui.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("lunch-rpc-service")
public interface LunchRpcService extends RemoteService {
	
	void createUser(String name, String email);
	
	LunchGroupData getLunchGroups();

}
