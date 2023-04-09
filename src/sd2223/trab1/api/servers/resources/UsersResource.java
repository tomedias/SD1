package sd2223.trab1.api.servers.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.User;
import sd2223.trab1.api.rest.services.UsersService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class UsersResource implements UsersService {


    private final Map<String, User> users = new HashMap<String, User>();

    /**
     *
     */
    private static final Logger Log = Logger.getLogger(UsersResource.class.getName());


    @Override
    public String createUser(User user) {

        Log.info("createUser : " + user);

        // Check if user data is valid
        if(user.getName() == null || user.getPwd() == null || user.getDisplayName() == null ||
                user.getDomain() == null) {
            Log.info("User object invalid.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }

        // Insert new user, checking if userId already exists
        if( users.putIfAbsent( user.getName(), user) != null ) {
            Log.info("User already exists.");
            throw new WebApplicationException( Response.Status.CONFLICT );
        }
        return user.getName();
    }

    @Override
    public User getUser(String name, String pwd) {
        Log.info("getUser : user = " + name + "; pwd = " + pwd);

        if(name == null || pwd == null) {
            Log.info("UserId or password null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }
        User user = users.get(name);
        if (user == null) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (!user.getPwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return user;
    }

    @Override
    public User updateUser(String name, String pwd, User user) {
        Log.info("updateUser : user = " + name + "; pwd = " + pwd + " ; user = " + user);
        if(name == null || pwd == null) {
            Log.info("UserId or password null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }

        User u = users.get(name);
        if (u == null) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (!u.getPwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        users.replace(u.getName(),user);
        return u;
        // TODO Auto-generated method stub
    }

    @Override
    public User deleteUser(String name, String pwd) {
        Log.info("deleteUser : user = " + name + "; pwd = " + pwd);
        if(name == null || pwd == null) {
            Log.info("UserId or password null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }
        User u = users.get(name);
        if (u == null) {
            Log.info("User does not exist.");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        if (!u.getPwd().equals(pwd)) {
            Log.info("Password is incorrect.");
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        users.remove(name);
        return u;
    }

    @Override
    public List<User> searchUsers(String pattern) {
        Log.info("searchUsers : pattern = " + pattern);
        List<User> result = new java.util.ArrayList<User>();
        for (User u : users.values()) {
            if (u.getName().toLowerCase().contains(pattern)) {
                result.add(u);
            }
        }
        return result;
    }

}
