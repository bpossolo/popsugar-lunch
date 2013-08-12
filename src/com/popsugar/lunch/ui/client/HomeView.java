package com.popsugar.lunch.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class HomeView extends Composite {
	
	//-------------------------------------------------------------------------------------------
	//UiBinder
	//-------------------------------------------------------------------------------------------

	interface Binder extends UiBinder<HTMLPanel, HomeView>{}
	
	private static final Binder binder = GWT.create(Binder.class);
	
	//-------------------------------------------------------------------------------------------
	//Class variables
	//-------------------------------------------------------------------------------------------
	
	private static final LunchRpcServiceAsync rpcService = GWT.create(LunchRpcService.class);
	
	//-------------------------------------------------------------------------------------------
	//Ui fields
	//-------------------------------------------------------------------------------------------
	
	@UiField
	TextBox name;
	
	@UiField
	TextBox email;
	
	@UiField
	Button signup;
	
	@UiField
	Label validationError;
	
	@UiField
	Label successMsg;
	
	@UiField
	Label weekLabel;
	
	@UiField
	FlowPanel lunchGroupsContainer;
	
	//-------------------------------------------------------------------------------------------
	//Constructors
	//-------------------------------------------------------------------------------------------

	public HomeView() {
		initWidget(binder.createAndBindUi(this));
		name.getElement().setAttribute("placeholder", "Your first and last name...");
		email.getElement().setAttribute("placeholder", "Your email...");
	}
	
	//-------------------------------------------------------------------------------------------
	//Overriding of super methods
	//-------------------------------------------------------------------------------------------
	
	@Override
	protected void onLoad() {
		super.onLoad();
		name.setFocus(true);
		fetchCurrentLunchGroupsAndRender();
	}
	
	//-------------------------------------------------------------------------------------------
	//Ui handler methods
	//-------------------------------------------------------------------------------------------
	
	@UiHandler("signup")
	void onClickSignup(ClickEvent e){
		signup();
	}
	
	@UiHandler("name")
	void onNameEnterKey(KeyDownEvent e){
		if( e.getNativeKeyCode() == KeyCodes.KEY_ENTER )
			signup();
	}
	
	@UiHandler("email")
    void onEmailEnterKey(KeyDownEvent e){
		if( e.getNativeKeyCode() == KeyCodes.KEY_ENTER )
			signup();
    }
	
	//-------------------------------------------------------------------------------------------
	//Private methods
	//-------------------------------------------------------------------------------------------
	
	private void signup(){
		signup.setEnabled(false);
		successMsg.setVisible(false);
		String userName = name.getValue();
		String userEmail = email.getValue();
		if( userName.trim().isEmpty() || userEmail.trim().isEmpty() ){
			validationError.setVisible(true);
			signup.setEnabled(true);
		}
		else{
			validationError.setVisible(false);
			createUser(userName, userEmail);
		}
	}
	
	private void fetchCurrentLunchGroupsAndRender(){
		rpcService.getLunchGroups(new AsyncCallback<LunchGroupData>() {
			@Override
			public void onSuccess(LunchGroupData data) {
				weekLabel.setText("Lunch groups for week of " + data.getWeek());
				for( LunchGroup group : data.getGroups() )
					lunchGroupsContainer.add(new LunchGroupPanel(group));
			};
			
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Oops! Looks like there is something wrong with our server! We couldn't fetch the current lunch groups.");
			}
		});
	}
	
	private void createUser(String userName, String userEmail){
		rpcService.createUser(userName, userEmail, new AsyncCallback<Void>() {
			@Override
			public void onSuccess(Void result) {
				successMsg.setVisible(true);
				name.setValue(null);
				email.setValue(null);
				signup.setEnabled(true);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Oops! Looks like there is something wrong with our server! Try again in a few moments.");
				signup.setEnabled(true);
			}
		});
	}

}
