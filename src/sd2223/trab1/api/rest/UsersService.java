package sd2223.trab1.api.rest;

import java.util.logging.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import sd2223.trab1.api.User;

@Path(UsersService.PATH)
public interface UsersService {

	String PWD = "pwd";
	String NAME = "name";
	String QUERY = "query";
	String PATH = "/users";
	
	/**
	 * Creates a new user in the local domain.
	 * @param user User to be created
	 * @return 200 the address of the user (name@domain). 
	 * 		409 if the userId already exists. 
	 * 		400 otherwise.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	String createUser(User user);
	
	/**
	 * Obtains the information on the user identified by name
	 * @param name the name of the user
	 * @param pwd password of the user
	 * @return 200 and the user object, if the userId exists and password matches the
	 *         existing password; 
	 *         403 if the password is incorrect; 
	 *         404 if no user exists with the provided userId
	 * 		400 otherwise.
	 */
	@GET
	@Path("/{" + NAME+ "}")
	@Produces(MediaType.APPLICATION_JSON)
	User getUser(@PathParam(NAME) String name, @QueryParam( PWD ) String pwd);
	
	/**
	 * Modifies the information of a user. Values of null in any field of the user will be 
	 * considered as if the the fields is not to be modified (the name cannot be modified).
	 * @param name the name of the user
	 * @param pwd password of the user
	 * @param user Updated information
	 * @return 200 the updated user object, if the name exists and password matches
	 *         the existing password 
	 *         403 if the password is incorrect 
	 *         404 if no user exists with the provided userId 
	 *         400 otherwise.
	 */
	@PUT
	@Path("/{" + NAME+ "}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	User updateUser(@PathParam( NAME ) String name, @QueryParam( PWD ) String pwd, User user);
	
	/**
	 * Deletes the user identified by name
	 * @param name the name of the user
	 * @param pwd password of the user
	 * @return 200 the deleted user object, if the name exists and pwd matches the
	 *         existing password 
	 *         403 if the password is incorrect 
	 *         404 if no user exists with the provided userId
	 *         400 otherwise
	 */
	@DELETE
	@Path("/{" + NAME+ "}")
	@Produces(MediaType.APPLICATION_JSON)
	User deleteUser(@PathParam(NAME) String name, @QueryParam(PWD) String pwd);
	
	/**
	 * Returns the list of users for which the pattern is a substring of the name
	 * (of the user), case-insensitive. The password of the users returned by the
	 * query must be set to the empty string "".
	 * 
	 * @param pattern substring to search
	 * @return 200 when the search was successful, regardless of the number of hits
	 *         (including 0 hits). 400 otherwise.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	List<User> searchUsers(@QueryParam(QUERY) String pattern);
}




class UsersSystem implements UsersService{


	private final Map<String,User> users = new HashMap<String,User>();

	/**
	 *
	 */
	private static final Logger log = Logger.getLogger(UsersSystem.class.getName());


	

	@Override
	public String createUser(User user) {
		
		if(users.containsKey(user.getName())){
			throw new WebApplicationException(Status.CONFLICT);
		}
		if(user.getPwd() ==null || user.getDisplayName()==null || user.getDomain()==null || user.getName()==null){
			throw new WebApplicationException(Status.BAD_REQUEST);
        }
		users.put(user.getName(),user);
		throw new WebApplicationException(Status.OK);
	}

	@Override
	public User getUser(String name, String pwd) {
		User user = users.get(name);
		if(user==null){
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		if(!user.getPwd().equals(pwd)){
			throw new WebApplicationException(Status.FORBIDDEN);
        }
		return user;
	}

	@Override
	public User updateUser(String name, String pwd, User user) {
		User u = users.get(name);
        if(u==null){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        if(!u.getPwd().equals(pwd)){
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        u.setName(user.getName());
        u.setPwd(user.getPwd());
        u.setDisplayName(user.getDisplayName());
        u.setDomain(user.getDomain());
        return u;
		// TODO Auto-generated method stub
	}

	@Override
	public User deleteUser(String name, String pwd) {
		User u = users.get(name);
        if(u==null){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        if(!u.getPwd().equals(pwd)){
            throw new WebApplicationException(Status.FORBIDDEN);
        }
        users.remove(name);
        return u;
	}

	@Override
	public List<User> searchUsers(String pattern) {

		List<User> result = new java.util.ArrayList<User>();
		for(User u : users.values()){
            if(u.getName().toLowerCase().contains(pattern.toLowerCase())){
                result.add(u);
            }
        }
		return result;
	}

}
