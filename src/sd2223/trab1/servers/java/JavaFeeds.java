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
import sd2223.trab1.servers.rest.RestUsersResource;

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

    private static int id =0;
    Discovery discovery = Discovery.getInstance();

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        Log.info("Posting Message to feed");
        if(user==null || pwd==null || msg==null){
            Log.info("UserId or password null.");
            return Result.error(ErrorCode.BAD_REQUEST );
        }
        if(!checkUserExist(user)){
            Log.info("User does not exist");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        if(!checkUser(user,pwd)){
            Log.info("Password or domain is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        msg.setId(id++);
        HashMap<Long, Message> userFeed = feeds.get(user);
        if(userFeed ==null){
            userFeed= new HashMap<Long, Message>();
            userFeed.put(msg.getId(), msg);
            feeds.put(user, userFeed);
        }else{
            userFeed.put(msg.getId(), msg);
        }

        return Result.ok(msg.getId());
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        Log.info("Removing message from feed");
        if(user==null || pwd==null){
            Log.info("UserId or password null.");
            return Result.error(ErrorCode.BAD_REQUEST );
        }
        if(!checkUserExist(user)){
            Log.info("User does not exist");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        if(!checkUser(user,pwd)){
            Log.info("Password or domain is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        HashMap<Long, Message> userFeed = feeds.get(user);
        Message message;
        if(userFeed!=null) {
            message = userFeed.remove(mid);
            if (message == null) {
                Log.info("Message does not exist");
                return Result.error(ErrorCode.NOT_FOUND);
            }
        }


        return Result.ok();
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        Log.info("Getting Message from feed");
        if(user==null){
            Log.info("UserId null.");
            return Result.error(ErrorCode.BAD_REQUEST );
        }
        if(!checkUserExist(user)){
            Log.info("User does not exist");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        List<Message> messages = getAllFeedMessaged(user).stream().filter(m->m.getId()==mid).toList();
        if(messages.isEmpty()) {return Result.error(ErrorCode.NOT_FOUND);}


        else
            return Result.ok(messages.get(0));
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        Log.info("Getting Messages from feed than are HOUR: " + time);
        if(user==null){
            Log.info("UserId null.");
            return Result.error(ErrorCode.BAD_REQUEST );
        }
        if(!checkUserExist(user)){
            Log.info("User does not exist");
            return Result.error(ErrorCode.NOT_FOUND);
        }

        List<Message> list = getAllFeedMessaged(user).stream().filter(m->m.getCreationTime()>time).toList();
        return Result.ok(list);
    }

    private List<Message> getAllFeedMessaged(String user){
        Log.info("Getting All Messages from all feeds subscribed");
        HashMap<Long, Message> userFeed = feeds.get(user);
        List<Message> list = new ArrayList<>();
        if(userFeed!=null){
            list.addAll(userFeed.values().stream().toList());
        }
        if(subs.get(user)!=null){
            for(String u : subs.get(user)){
                if(feeds.get(u)!=null && checkUserExist(u)){
                    list.addAll(feeds.get(u).values().stream().toList());
                }
            }
        }

        return list;
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        Log.info(user +" subscribed to " + userSub);
        if(user== null || userSub==null || pwd==null){
            Log.info("UserId or password are null.");
            return Result.error(ErrorCode.BAD_REQUEST );
        }
        if(!checkUserExist(userSub) || !checkUserExist(user)){
            Log.info("UserSub does not exist");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        if(!checkUser(user,pwd)){
            Log.info("User or password dont match or exist");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        List<String> list = subs.get(user);
        if(list==null){
            list = new ArrayList<String>();
            list.add(userSub);
            subs.put(user,list);
        }else{
            if(!list.contains(userSub))
                list.add(userSub);
        }

        return Result.ok();
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        Log.info(user +" unsubscribed to " + userSub);
        if(user== null || userSub==null || pwd==null){
            Log.info("UserId or password are null.");
            return Result.error(ErrorCode.BAD_REQUEST );
        }
        if(!checkUserExist(userSub) || !checkUserExist(user)){
            Log.info("UserSub does not exist");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        if(!checkUser(user,pwd)){
            Log.info("User or password dont match or exist");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        List<String> list = subs.get(user);

        if(list!=null) {
            if (!list.remove(userSub)) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
        }
        return Result.ok();
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        Log.info("listing subs");
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        if(!checkUserExist(user) ){
            Log.info("User does not exist");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        subs.computeIfAbsent(user, k -> new ArrayList<>());
        List<String> list =subs.get(user);
        list.removeIf(u -> !checkUserExist(u));
        return Result.ok(list);
    }



    private boolean checkUser(String user,String password) {
        //Log.info("Sending request to user server");
        String[] path = user.split("@");
        String domain = path[1];
        String userID= path[0];
        URI uri = discovery.knownUrisOf(domain,"users");
        RestUsersClient client = new RestUsersClient(uri);
        Result<User> r = client.getUser(userID,password);
        if(r!=null){
            return r.isOK();
        }
        return false;
    }

    private boolean checkUserExist(String user){
        //Log.info("Sending request to user server");
        String[] path = user.split("@");
        String domain = path[1];
        String userID= path[0];
        URI uri = discovery.knownUrisOf(domain,"users");
        //Log.info("Found uri: " + uri.toString());
        RestUsersClient client = new RestUsersClient(uri);
        Result<List<User>> r = client.searchUsers(userID);
        List<User> users = r.value();
        if(r.isOK()){
            for (User userC : users) {
                //Log.info("Found user: " + userC.getName());
                if(userC.getName().equals(userID))
                    return true;
            }
        }
        return false;
    }


}
