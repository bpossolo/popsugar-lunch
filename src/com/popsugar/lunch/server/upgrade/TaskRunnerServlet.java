package com.popsugar.lunch.server.upgrade;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TaskRunnerServlet extends HttpServlet {
	
	private static final Logger log = Logger.getLogger(TaskRunnerServlet.class.getName());
	
	private static final long serialVersionUID = 4960874882726592370L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		String taskNum = req.getParameter("task");
		String className = "com.popsugar.lunch.server.upgrade.Task" + taskNum;
		log.log(Level.INFO, "Executing upgrade task {0}", className);
		try{
			UpgradeTask task = (UpgradeTask)Class.forName(className).newInstance();
			task.setServletContext(getServletContext());
			task.run();
			resp.getWriter().print("Task executed OK!");
		}
		catch(ClassNotFoundException | IllegalAccessException | InstantiationException e ){
			throw new ServletException("Unable to execute task [" + className + "]", e);
		}
	}

}
