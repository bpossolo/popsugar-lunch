package com.popsugar.lunch.api;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.popsugar.lunch.WebAppInitializer;
import com.popsugar.lunch.api.dto.BuddiesDTO;
import com.popsugar.lunch.api.dto.CreateUserDTO;
import com.popsugar.lunch.api.dto.LocationDTO;
import com.popsugar.lunch.api.dto.LunchGroupDTO;
import com.popsugar.lunch.api.dto.UserDTO;
import com.popsugar.lunch.dao.RefreshTokenDAO;
import com.popsugar.lunch.model.GroupType;
import com.popsugar.lunch.model.Location;
import com.popsugar.lunch.model.LunchGroup;
import com.popsugar.lunch.model.User;
import com.popsugar.lunch.oauth.OAuthApp;
import com.popsugar.lunch.service.LunchService;
import com.popsugar.lunch.upgrade.UpgradeTask;

@Path("/lunch")
public class LunchAPI {
	
	private static final Logger log = Logger.getLogger(LunchAPI.class.getName());
	
	private LunchService lunchService;
	private DatastoreService datastore;
	private RefreshTokenDAO refreshTokenDao;
	
	public LunchAPI(@Context ServletContext context){
		lunchService = (LunchService)context.getAttribute(WebAppInitializer.LunchService);
		refreshTokenDao = (RefreshTokenDAO)context.getAttribute(WebAppInitializer.RefreshTokenDAO);
		datastore = (DatastoreService)context.getAttribute(WebAppInitializer.Datastore);
	}
	
	@GET
	@Path("/my-location")
	@Produces(MediaType.APPLICATION_JSON)
	public LocationDTO getMyLocation(@Context HttpServletRequest request){
		String city = request.getHeader("X-AppEngine-City");
		String state = request.getHeader("X-AppEngine-Region");
		Location location = lunchService.estimateUserLocation(city, state);
		LocationDTO dto = new LocationDTO(city, state, location);
		return dto;
	}

	@GET
	@Path("/generate-groups")
	@Produces(MediaType.TEXT_PLAIN)
	public Response generateLunchGroups(
			@QueryParam("type") GroupType type,
			@QueryParam("email") @DefaultValue("false") boolean email) {
		if (type == null || type == GroupType.Regular) {
			lunchService.generateLunchGroups(GroupType.Regular, email);
		}
		if (type == null || type == GroupType.PopsugarPals) {
			lunchService.generateLunchGroups(GroupType.PopsugarPals, email);
		}
		return Response.ok("Lunch groups generated").build();
	}
	
	@GET
	@Path("/users")
	@Produces(MediaType.APPLICATION_JSON)
	public List<UserDTO> getUsers() {
		List<User> users = lunchService.getActiveUsers();
		List<UserDTO> result = new ArrayList<>(users.size());
		for (User user : users) {
			UserDTO dto = new UserDTO(user);
			result.add(dto);
		}
		return result;
	}
	
	@POST
	@Path("/create-user")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createUser(CreateUserDTO dto) {
		lunchService.createUser(dto.name, dto.email, dto.location);
		return Response.ok("User created").build();
	}
	
	@GET
	@Path("/groups")
	@Produces(MediaType.APPLICATION_JSON)
	public List<LunchGroupDTO> getLunchGroups(@QueryParam("type") GroupType type) {
		List<LunchGroup> groups = lunchService.getLunchGroupsWithUsers(type);
		ArrayList<LunchGroupDTO> result = new ArrayList<>(groups.size());
		for (LunchGroup group : groups){
			LunchGroupDTO dto = new LunchGroupDTO(group);
			result.add(dto);
		}
		return result;
	}
	
	@POST
	@Path("/buddies")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response linkUsers(BuddiesDTO dto) throws EntityNotFoundException {
		lunchService.linkUsers(dto.userAKey, dto.userBKey);
		return Response.ok("Users linked").build();
	}
	
	@DELETE
	@Path("/buddies")
	@Produces(MediaType.TEXT_PLAIN)
	public Response unlinkUsers(
		@QueryParam("userAKey") Long userAKey,
		@QueryParam("userBKey") Long userBKey)
				throws EntityNotFoundException {
		lunchService.unlinkUsers(userAKey, userBKey);
		return Response.ok("Users unlinked").build();
	}
	
	@GET
	@Path("/upgrade")
	@Produces(MediaType.TEXT_PLAIN)
	public Response upgrade(@Context ServletContext servletContext, @QueryParam("task") int taskNum) {
		String className = UpgradeTask.class.getPackage().getName() + ".Task" + taskNum;
		log.log(Level.INFO, "Executing upgrade task {0}", className);
		try{
			UpgradeTask task = (UpgradeTask)Class.forName(className).newInstance();
			task.setServletContext(servletContext);
			task.setDatastore(datastore);
			task.run();
			return Response.ok("Upgrade task " + taskNum + " complete").build();
		}
		catch(ClassNotFoundException | IllegalAccessException | InstantiationException e){
			throw new RuntimeException("Unable to execute task [" + className + "]", e);
		}
	}
	
	@GET
	@Path("/unsubscribe/{userId}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response unsubscribeFromLunchForFour(@PathParam("userId") Long userId) throws EntityNotFoundException {
		lunchService.deactivateUser(userId);
		return Response.ok("Unsubscribed").build();
	}
	
	@GET
	@Path("/update-users-with-pingboard-data")
	@Produces(MediaType.TEXT_PLAIN)
	public Response updateUsersWithPingboardData() {
		lunchService.updateUsersWithPingboardData();
		return Response.ok("Users updated with pingboard data").build();
	}
	
	@POST
	@Path("/oauth-refresh-token")
	@Produces(MediaType.TEXT_PLAIN)
	public Response setOAuthRefreshToken(
			@QueryParam("app") OAuthApp app,
			@QueryParam("refresh_token") String refreshToken) {
		refreshTokenDao.saveRefreshToken(app, refreshToken);
		return Response.ok("Refresh token saved").build();
	}
}
