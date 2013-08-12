package com.popsugar.lunch.server;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CronHandler extends HttpServlet {

	private static final long serialVersionUID = -6217679030930360743L;
	
	private LunchManager manager;
	
	@Override
	public void init() throws ServletException {
		manager = (LunchManager)getServletContext().getAttribute(WebAppInitializer.LunchManager);
	}
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		EntityManager em = EMF.get().createEntityManager();
		try{
			manager.regenerateLunchGroups(em);
			resp.getWriter().print("Lunch groups generated OK!");
		}
		finally{
			if( em.getTransaction().isActive() )
				em.getTransaction().rollback();
			em.close();
		}
	}

}
