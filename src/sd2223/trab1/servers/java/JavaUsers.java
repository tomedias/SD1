package sd2223.trab1.servers.java;


import sd2223.trab1.api.Discovery;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.java.Users;
import sd2223.trab1.clients.FeedsClientFactory;
import sd2223.trab1.clients.rest.RestFeedsClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JavaUsers implements Users {

    private static final Map<String,User> users = new HashMap<>();

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

    private final String domain;

    Discovery discovery = Discovery.getInstance();




    public JavaUsers(String domain) {
        this.domain = domain;
    }

    @Override
    public Result<String> createUser(User user) {
        Log.info("createUser : " + user);

        // Check if user data is valid
        if(user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null) {
            Log.info("User object invalid.");
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }
        synchronized (users){
            if( users.putIfAbsent(user.getName(), user) != null ) {
                Log.info("User already exists.");
                return Result.error( Result.ErrorCode.CONFLICT);
            }
            Log.info("Success creating " +  users.get(user.getName()).getName() );

        }

        return Result.ok( String.format("%s@%s",user.getName(), domain));
    }

    @Override
    public Result<User> getUser(String name, String pwd) {
        Log.info("getUser : user = " + name + "; pwd = " + pwd);

        // Check if user is valid
        if(name == null || pwd == null) {
            Log.info("Name or Password null.");
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }
        synchronized (users){
            User user = users.get(name);


            if( user == null ) {
                Log.info("User does not exist.");
                return Result.error( Result.ErrorCode.NOT_FOUND);
            }

            if( !user.getPwd().equals( pwd)) {
                Log.info("Password is incorrect.");
                return Result.error( Result.ErrorCode.FORBIDDEN);
            }

            return Result.ok(user);
        }

    }

    @Override
    public Result<User> updateUser(String name, String pwd, User user) {
        if(name == null || pwd == null || user == null || !user.getName().equals(name)) {
            Log.info("Name or Password null.");

            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }
        synchronized (users){
            User Ouser = users.get(name);


            // Check if user exists
            if( Ouser == null ) {
                Log.info("User does not exist.");
                return Result.error( Result.ErrorCode.NOT_FOUND);
            }

            //Check if the password is correct
            if( !Ouser.getPwd().equals( pwd)) {
                Log.info("Password is incorrect.");
                return Result.error( Result.ErrorCode.FORBIDDEN);
            }
            if(user.getPwd() != null) {
                Ouser.setPwd(user.getPwd());
            }
            if(user.getDisplayName() != null) {
                Ouser.setDisplayName(user.getDisplayName());
            }
            if(user.getDomain() != null) {
                Ouser.setDomain(user.getDomain());
            }

            return Result.ok(Ouser);
        }
    }





    @Override
    public Result<User> deleteUser(String name, String pwd) {
        Log.info("Deleting user " + name);
        if(name == null || pwd == null) {
            Log.info("Name or Password null.");
            return Result.error( Result.ErrorCode.BAD_REQUEST);
        }

        synchronized (users){
            User user = users.get(name);

            if( user == null ) {
                Log.info("User does not exist.");
                return Result.error( Result.ErrorCode.NOT_FOUND);
            }
            if( !user.getPwd().equals( pwd)) {
                Log.info("Password is incorrect.");
                return Result.error( Result.ErrorCode.FORBIDDEN);
            }

            users.remove(name);



            URI uri = discovery.knownUrisOf(domain, "feeds");
            Feeds client = FeedsClientFactory.get(uri);
            client.deleteFeed(name + "@" + domain);


            return Result.ok(user);
        }
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        Log.info("searchUsers : pattern = " + pattern);
        List<User> result = new ArrayList<>();
        synchronized (users) {
            for (User u : users.values()) {
                if (u.getName().toLowerCase().contains(pattern)) {
                    result.add(u);
                }
            }
            return Result.ok(result);
        }
    }

    @Override
    public Result<Void> verifyPassword(String name, String pwd) {
        synchronized (users){
            var res = getUser(name, pwd);
            if( res.isOK() )
                return Result.ok();
            else
                return Result.error( res.error() );
        }
    }

}
