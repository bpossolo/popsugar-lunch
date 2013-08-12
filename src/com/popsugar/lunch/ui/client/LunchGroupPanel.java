package com.popsugar.lunch.ui.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class LunchGroupPanel extends Composite {
	
	public LunchGroupPanel(LunchGroup group){
		FlowPanel panel = new FlowPanel();
		for( User user : group.getUsers() ){
			String name = user.getName();
			if( group.isCoordinatedBy(user) )
				name += "*";
			panel.add(new Label(name));
		}
		initWidget(panel);
		setStyleName("lunchGroup");
	}

}
