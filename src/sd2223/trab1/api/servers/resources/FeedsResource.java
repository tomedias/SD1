package sd2223.trab1.api.servers.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.rest.services.FeedsService;
import sd2223.trab1.api.rest.services.UsersService;
import sd2223.trab1.api.servers.Discovery;
import sd2223.trab1.api.servers.FeedsServer;
import sd2223.trab1.api.servers.UsersServer;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class FeedsResource implements FeedsService {

    Map<String, HashMap<Long, Message>> feeds = new HashMap<String, HashMap<Long, Message>>();

    Map<String,List<String>> subs  = new HashMap<String, List<String>>();
    private static final Logger Log = Logger.getLogger(FeedsResource.class.getName());

    Discovery discovery = Discovery.getInstance();
    @Override
    public long postMessage(String user, String pwd, Message msg) {

        if(user==null || pwd==null || msg==null){
            Log.info("UserId or password null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        if(!checkUser(userID, domain,pwd)){
            Log.info("Password or domain is incorrect.");
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        HashMap<Long, Message> userFeed = feeds.get(userID);
        userFeed.put(msg.getId(), msg);
        return msg.getId();


    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {
        if(user==null || pwd==null){
            Log.info("UserId or password null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        if(!checkUser(userID, domain,pwd)){
            Log.info("Password or domain is incorrect.");
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        HashMap<Long, Message> userFeed = feeds.get(user);
        Message message = userFeed.remove(mid);

        if(message ==null){
            Log.info("Message does not exist");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        throw new WebApplicationException(Response.Status.NO_CONTENT);

    }

    @Override
    public Message getMessage(String user, long mid) {
        // TODO Auto-generated method stub
        if(user==null){
            Log.info("UserId null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        if(!checkUserExist(domain,userID)){
            Log.info("User does not exist");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        HashMap<Long, Message> userFeed = feeds.get(user);
        Message message = userFeed.get(mid);
        if(message ==null){
            Log.info("Message does not exist");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return message;
    }

    @Override
    public List<Message> getMessages(String user, long time) {
        // TODO Auto-generated method stub
        if(user==null){
            Log.info("UserId null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        if(!checkUserExist(domain,userID)){
            Log.info("User does not exist");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        HashMap<Long, Message> userFeed = feeds.get(user);
        return userFeed.values().stream().filter(m -> m.getCreationTime() > time).toList();
    }

    @Override
    public void subUser(String user, String userSub, String pwd) {
        // TODO Auto-generated method stub
        if(user== null || userSub==null || pwd==null){
            Log.info("UserId or password are null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        String[] parts2 = user.split("@");
        String userIDSub = parts[0];
        String domainSub = parts[1];
        if(!checkUser(userIDSub, domain,pwd)){
            Log.info("User or password dont match or exist");
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        if(!checkUserExist(domainSub,userIDSub)){
            Log.info("UserSub does not exist");
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        List<String> list = subs.get(userID);
        if(list==null){
            list = new ArrayList<String>();
        }
        list.add(userIDSub);
        throw new WebApplicationException(Response.Status.NO_CONTENT);
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) {
        if(user== null || userSub==null || pwd==null){
            Log.info("UserId or password are null.");
            throw new WebApplicationException( Response.Status.BAD_REQUEST );
        }
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        String[] parts2 = user.split("@");
        String userIDSub = parts[0];
        String domainSub = parts[1];
        if(!checkUser(userIDSub, domain,pwd)){
            Log.info("User or password dont match or exist");
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }

        List<String> list = subs.get(userID);
        if(!list.remove(userIDSub)){
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        throw new WebApplicationException(Response.Status.NO_CONTENT);
    }

    @Override
    public List<String> listSubs(String user) {
        // TODO Auto-generated method stub
        String[] parts = user.split("@");
        String userID = parts[0];
        String domain = parts[1];
        if(!checkUserExist(domain, userID)){
            Log.info("User dont exist");
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        }
        return subs.get(userID);
    }

    private boolean checkUser(String user,String domain,String password) {
        System.out.println("Sending request to server.");

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        URI uri = discovery.knownUrisOf(domain,"users");

        WebTarget target = client.target( uri).path( UsersService.PATH );

        Response r = target.path( user )
                .queryParam(UsersService.PWD, password).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
            var u = r.readEntity(User.class);
            return true;
        } else
            System.out.println("Error, HTTP error status: " + r.getStatus() );
        return false;
    }

    private boolean checkUserExist(String domain,String user){
        System.out.println("Sending request to server.");

        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        URI uri = discovery.knownUrisOf(domain,"users");

        WebTarget target = client.target( uri ).path( UsersService.PATH );

        Response r = target.path("/").queryParam( UsersService.QUERY, user).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();

        if( r.getStatus() == Response.Status.OK.getStatusCode() && r.hasEntity() ) {
            var users = r.readEntity(new GenericType<List<User>>() {});

            return users.size() != 0;
        } else
            System.out.println("Error, HTTP error status: " + r.getStatus() );

        return false;
    }

}
