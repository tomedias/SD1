package sd2223.trab1.servers.java;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import sd2223.trab1.api.Discovery;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;

import sd2223.trab1.api.java.Result.ErrorCode;
import sd2223.trab1.api.rest.UsersService;
import sd2223.trab1.clients.rest.RestClient;
import sd2223.trab1.clients.rest.RestUsersClient;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JavaFeeds implements Feeds {

    private final static Map<String, HashMap<Long, Message>> feeds = new HashMap<String, HashMap<Long, Message>>();

    private final static Map<String,List<String>> subs  = new HashMap<String, List<String>>();

    private static Logger Log = Logger.getLogger(JavaFeeds.class.getName());

    Discovery discovery = Discovery.getInstance();

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        Log.info("Posting Message");
        if(user==null || pwd==null || msg==null){
            Log.info("UserId or password null.");
            return Result.error(ErrorCode.BAD_REQUEST );
        }
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        if(!checkUser(userID, domain,pwd)){
            Log.info("Password or domain is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        HashMap<Long, Message> userFeed = feeds.get(userID);
        userFeed.put(msg.getId(), msg);
        return Result.ok(msg.getId());
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        if(user==null || pwd==null){
            Log.info("UserId or password null.");
            return Result.error(ErrorCode.BAD_REQUEST );
        }
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        if(!checkUser(userID, domain,pwd)){
            Log.info("Password or domain is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        HashMap<Long, Message> userFeed = feeds.get(user);
        Message message = userFeed.remove(mid);

        if(message ==null){
            Log.info("Message does not exist");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        return Result.ok();
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        if(user==null){
            Log.info("UserId null.");
            return Result.error(ErrorCode.BAD_REQUEST );
        }
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        if(!checkUserExist(domain,userID)){
            Log.info("User does not exist");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        HashMap<Long, Message> userFeed = feeds.get(user);
        Message message = userFeed.get(mid);
        if(message ==null){
            Log.info("Message does not exist");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        return Result.ok(message);
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        if(user==null){
            Log.info("UserId null.");
            return Result.error(ErrorCode.BAD_REQUEST );
        }
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        if(!checkUserExist(domain,userID)){
            Log.info("User does not exist");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        HashMap<Long, Message> userFeed = feeds.get(user);
        return Result.ok(userFeed.values().stream().filter(m -> System.currentTimeMillis()-m.getCreationTime() < time).toList());
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        if(user== null || userSub==null || pwd==null){
            Log.info("UserId or password are null.");
            return Result.error(ErrorCode.BAD_REQUEST );
        }
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        String[] parts2 = user.split("@");
        String userIDSub = parts2[0];
        String domainSub = parts2[1];
        if(!checkUser(userIDSub, domain,pwd)){
            Log.info("User or password dont match or exist");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        if(!checkUserExist(domainSub,userIDSub)){
            Log.info("UserSub does not exist");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        List<String> list = subs.get(userID);
        if(list==null){
            list = new ArrayList<String>();
        }
        list.add(userIDSub);
        return Result.ok();
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        if(user== null || userSub==null || pwd==null){
            Log.info("UserId or password are null.");
            return Result.error(ErrorCode.BAD_REQUEST );
        }
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        String[] parts2 = user.split("@");
        String userIDSub = parts2[0];
        String domainSub = parts2[1];
        if(!checkUser(userIDSub, domain,pwd)){
            Log.info("User or password dont match or exist");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        List<String> list = subs.get(userID);
        if(!list.remove(userIDSub)){
            return Result.error(ErrorCode.NOT_FOUND);
        }
        return Result.ok();
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        if(!checkUserExist(domain, userID)){
            Log.info("User dont exist");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        return Result.ok(subs.get(userID));
    }



    private boolean checkUser(String user,String domain,String password) {
        Log.info("Sending request to user server");
        URI uri = discovery.knownUrisOf(domain,"users");
        Log.info("Found uri: " + uri.toString());
        RestUsersClient client = new RestUsersClient(uri);
        Log.info("Successful connection: " + client);
        return client.getUser(user,password).isOK();
    }

    private boolean checkUserExist(String domain,String user){
        Log.info("Sending request to user server");
        URI uri = discovery.knownUrisOf(domain,"users");
        Log.info("Found uri: " + uri.toString());
        RestUsersClient client = new RestUsersClient(uri);
        return client.searchUsers(user).isOK();
    }
}
