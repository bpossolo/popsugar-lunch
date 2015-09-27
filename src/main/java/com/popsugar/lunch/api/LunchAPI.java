package com.popsugar.lunch.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.appengine.api.datastore.DatastoreService;
import com.popsugar.lunch.WebAppInitializer;
import com.popsugar.lunch.api.dto.CreatePairDTO;
import com.popsugar.lunch.api.dto.CreateUserDTO;
import com.popsugar.lunch.api.dto.LunchGroupDTO;
import com.popsugar.lunch.api.dto.LocationDTO;
import com.popsugar.lunch.api.dto.UserDTO;
import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.model.Location;
import com.popsugar.lunch.model.LunchGroup;
import com.popsugar.lunch.model.User;
import com.popsugar.lunch.service.LunchManager;
import com.popsugar.lunch.upgrade.UpgradeTask;

@Path("/lunch")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LunchAPI {
	
	private static final Logger log = Logger.getLogger(LunchAPI.class.getName());
	
	private LunchManager lunchManager;
	private DatastoreService datastore;
	
	public LunchAPI(@Context ServletContext servletContext){
		lunchManager = (LunchManager)servletContext.getAttribute(WebAppInitializer.LunchManager);
		datastore = (DatastoreService)servletContext.getAttribute(WebAppInitializer.Datastore);
	}
	
	@GET
	@Path("/my-location")
	public LocationDTO getMyLocation(@Context HttpServletRequest request){
		String city = request.getHeader("X-AppEngine-City");
		String state = request.getHeader("X-AppEngine-Region");
		Location location = lunchManager.estimateUserLocation(city, state);
		LocationDTO dto = new LocationDTO(city, state, location);
		return dto;
	}

	@GET
	@Path("/generate-groups")
	public Response generateLunchGroups(@QueryParam("type") GroupType type) {
		lunchManager.generateLunchGroups(type);
		return Response.status(200).entity("Lunch groups generated").build();
	}
	
	@GET
	@Path("/users")
	public List<UserDTO> getUsers() {
		List<User> users = lunchManager.getActiveUsers();
		List<UserDTO> result = new ArrayList<>(users.size());
		for (User user : users) {
			UserDTO dto = new UserDTO(user);
			result.add(dto);
		}
		return result;
	}
	
	@POST
	@Path("/create-user")
	public void createUser(CreateUserDTO dto) {
		lunchManager.createUser(dto.name, dto.email, dto.location);
	}
	
	@GET
	@Path("/groups")
	public List<LunchGroupDTO> getLunchGroups(@QueryParam("type") GroupType type) {
		
		String week = lunchManager.getCurrentWeek();
		List<LunchGroup> groups = lunchManager.getLunchGroupsWithUsers(week, type);
		
		ArrayList<LunchGroupDTO> result = new ArrayList<>(groups.size());
		for (LunchGroup group : groups){
			LunchGroupDTO dto = new LunchGroupDTO(group);
			result.add(dto);
		}
		return result;
	}
	
	@POST
	@Path("/create-pair")
	public void createPair(CreatePairDTO dto) {
		lunchManager.createPair(dto.user1Key, dto.user2Key);
	}
	
	@GET
	@Path("/upgrade")
	public Response upgrade(@Context ServletContext servletContext, @QueryParam("task") int taskNum) {
		String className = UpgradeTask.class.getPackage().getName() + ".Task" + taskNum;
		log.log(Level.INFO, "Executing upgrade task {0}", className);
		try{
			UpgradeTask task = (UpgradeTask)Class.forName(className).newInstance();
			task.setServletContext(servletContext);
			task.setDatastore(datastore);
			task.run();
			return Response.status(200).entity("Upgrade task " + taskNum + " complete").build();
		}
		catch(ClassNotFoundException | IllegalAccessException | InstantiationException e){
			throw new RuntimeException("Unable to execute task [" + className + "]", e);
		}
	}
	
	@GET
	@Path("/unsubscribe/{userId}")
	public Response unsubscribeFromLunchForFour(@PathParam("userId") Long userId) {
		lunchManager.deactivateUser(userId);
		return Response.status(200).entity("Unsubscribed").build();
	}
	
	@GET
	@Path("/update-users-with-pingboard-data")
	public Response updateUsersWithPingboardData() {
		lunchManager.updateUsersWithPingboardData();
		return Response.status(200).entity("Users updated with pingboard data").build();
	}
}
