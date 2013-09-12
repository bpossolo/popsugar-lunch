package com.popsugar.lunch.server;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.popsugar.lunch.ui.client.Location;
import com.popsugar.lunch.ui.client.LunchGroupData;
import com.popsugar.lunch.ui.client.LunchRpcService;
import com.popsugar.lunch.ui.client.User;

public class LunchRpcServiceImpl extends RemoteServiceServlet implements LunchRpcService {

	private static final long serialVersionUID = -7493925030237514544L;
	
	private LunchManager lunchManager;
	
	@Override
	public void init() throws ServletException {
		lunchManager = (LunchManager)getServletContext().getAttribute(WebAppInitializer.LunchManager);
	}
	
	@Override
	public void createUser(String name, String email, Location location) {
		EntityManager em = EMF.get().createEntityManager();
		try{
			lunchManager.createUserInTx(em, new User(name, email, location));
		}
		finally{
			if( em.getTransaction().isActive() )
				em.getTransaction().rollback();
			em.close();
		}
	}
	
	@Override
	public LunchGroupData getLunchGroups() {
		EntityManager em = EMF.get().createEntityManager();
		try{
			return lunchManager.getLunchGroupData(em);
		}
		finally{
			em.close();
		}
	}

}
