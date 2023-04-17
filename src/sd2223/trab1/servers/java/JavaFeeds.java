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
import sd2223.trab1.clients.rest.RestFeedsClient;
import sd2223.trab1.clients.rest.RestUsersClient;
import sd2223.trab1.servers.rest.RestFeedsServer;
import sd2223.trab1.servers.rest.RestUsersResource;
import sd2223.trab1.servers.rest.RestUsersServer;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JavaFeeds implements Feeds {

    private final static Map<String, HashMap<Long, Message>> feeds = new HashMap<String, HashMap<Long, Message>>();

    private final static Map<String,List<String>> subs  = new HashMap<String, List<String>>();

    private final static Map<String,List<Long>> removed = new HashMap<>();


    private static Logger Log = Logger.getLogger(JavaFeeds.class.getName());

    Discovery discovery = Discovery.getInstance();

    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {

            Log.info("Posting Message to feed in domain: " + RestFeedsServer.getDomain());
            if(user==null || pwd==null || msg==null || !user.split("@")[1].equals(RestFeedsServer.getDomain())){
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

            msg.setId(RestFeedsServer.getSeqA() * 256 + RestFeedsServer.getBase());
            synchronized (feeds){
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



    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        Log.info("Removing message from feed" + user + " mid: " + mid);
        if(user==null || pwd==null || !user.split("@")[1].equals(RestFeedsServer.getDomain())){
            Log.info("UserId or password null.");
            return Result.error(ErrorCode.BAD_REQUEST );
        }
        Log.info("Checked null");
        if(!checkUserExist(user)){
            Log.info("User does not exist");
            return Result.error(ErrorCode.NOT_FOUND);
        }
        Log.info("Checked user exist");
        if(!checkUser(user,pwd)){
            Log.info("Password or domain is incorrect.");
            return Result.error(ErrorCode.FORBIDDEN);
        }
        Log.info("Checked passed");
        synchronized (feeds){
            synchronized (removed){
            List<Message> userFeed = getAllFeedMessaged(user);


            boolean message = userFeed.removeIf(m->m.getId()==mid);
            if(feeds.get(user)!=null){
                feeds.get(user).remove(mid);
            }
            if (!message) {
                Log.info("Message does not exist");
                return Result.error(ErrorCode.NOT_FOUND);
            }

                if(removed.get(user)==null){
                    List<Long> list = new ArrayList<Long>();
                    list.add(mid);
                    removed.put(user, list);
                }else{
                    removed.get(user).add(mid);
                }


                return Result.ok();
            }

        }

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
        if(messages.isEmpty()) {
            Log.info("Messag id: " + mid + " not found");
            return Result.error(ErrorCode.NOT_FOUND);
        }


        else
            return Result.ok(messages.get(0));
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        Log.info("Getting Messages from feed "+ user);
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
        String[] pathS = user.split("@");
        String domainS = pathS[1];
        String userIDS= pathS[0];



        if(domainS.equals(RestFeedsServer.getDomain())){
            synchronized (feeds) {
                synchronized (subs){
                    HashMap<Long, Message> userFeed = feeds.get(user);
                    List<Message> list = new ArrayList<>();
                    if (userFeed != null) {
                        list.addAll(userFeed.values().stream().toList());
                    }
                    if (subs.get(user) != null) {
                        for (String u : subs.get(user)) {
                            if (u.split("@")[1].equals(domainS)) {
                                if(feeds.get(u)!=null)
                                    list.addAll(feeds.get(u).values().stream().toList());
                            } else {
                                String[] path = u.split("@");
                                String domain = path[1];
                                String userID = path[0];

                                URI uri = discovery.knownUrisOf(domain, "feeds");
                                RestFeedsClient client = new RestFeedsClient(uri);
                                Result<List<Message>> r = client.getPersonalFeeds(u);

                                if (r.isOK()) {
                                    List<Message> listM = r.value();
                                    Log.info("Got feed");
                                    if(listM!=null){
                                        list.addAll(listM);
                                        Log.info("Added personal feed");
                                    }

                                }

                            }
                        }
                    }
                    if (removed.get(user) != null) list.removeIf(m -> removed.get(user).contains(m.getId()));
                    return list;
                }

            }
        }else{
            URI uri = discovery.knownUrisOf(domainS,"feeds");
            RestFeedsClient client = new RestFeedsClient(uri);
            Result<List<Message>> r = client.getMessages(user,0);

            if(r.isOK()){
                List<Message> listM = r.value();
                Log.info("Added personal feed");
                return listM!=null ? listM : new ArrayList<Message>();
            }
        }
        return new ArrayList<>();
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        Log.info(user +" subscribed to " + userSub);
        if(user== null || userSub==null || pwd==null || !user.split("@")[1].equals(RestFeedsServer.getDomain()) ){
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
        synchronized (subs){
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

    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        Log.info(user +" unsubscribed to " + userSub);
        if(user== null || userSub==null || pwd==null || !user.split("@")[1].equals(RestFeedsServer.getDomain())){
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
        synchronized (subs){
            List<String> list = subs.get(user);

            if(list!=null) {
                if (!list.remove(userSub)) {
                    return Result.error(ErrorCode.NOT_FOUND);
                }
            }
            return Result.ok();
        }

    }

    @Override
    public Result<List<String>> listSubs(String user) {
        Log.info("listing subs");
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        synchronized (subs) {
            if(!checkUserExist(user) ){
                Log.info("User does not exist");
                return Result.error(ErrorCode.NOT_FOUND);
            }
            subs.computeIfAbsent(user, k -> new ArrayList<>());
            List<String> list =subs.get(user);
            list.removeIf(u -> !checkUserExist(u));
            return Result.ok(list);
        }

    }



    private boolean checkUser(String user,String password) {
        //Log.info("Sending request to user server");
        String[] path = user.split("@");
        String domain = path[1];
        String userID= path[0];
        URI uri = discovery.knownUrisOf(domain,"users");
        RestUsersClient client = new RestUsersClient(uri);
        Result<Void> r = client.verifyPassword(userID,password);
        if(r!=null){
            return r.isOK();
        }
        return false;
    }

    private boolean checkUserExist(String user){
        Log.info("Sending request to user server");
        String[] path = user.split("@");
        String domain = path[1];
        String userID= path[0];
        URI uri = discovery.knownUrisOf(domain,"users");
        //Log.info("Found uri: " + uri.toString());
        RestUsersClient client = new RestUsersClient(uri);
        Result<List<User>> r = client.searchUsers(userID);
        List<User> users = r.value();
        Log.info("Got List");
            if(r.isOK() && users!=null){
                for (User userC : users) {
                    //Log.info("Found user: " + userC.getName());
                    if(userC!=null && userC.getName().equals(userID))
                        return true;
                }
            }


        return false;
    }

    public Result<Void> deleteFeed(String user){
        synchronized (feeds){
            synchronized (subs){
                Log.info("Removed feed " + user);
                feeds.remove(user);
                subs.remove(user);
                for(List<String> list : subs.values().stream().toList()){
                    list.remove(user);
                }
                return Result.ok();
            }
        }


    }

    public  Result<List<Message>> getPersonalFeeds(String user){
        synchronized (feeds){
            Log.info("Get personal feed " + user);
            if(user!=null){
                HashMap<Long, Message> feed = feeds.get(user);
                if(feed!=null){
                    return Result.ok(feed.values().stream().toList());
                }
            }
            return Result.ok(new ArrayList<>());
        }

    }




}
