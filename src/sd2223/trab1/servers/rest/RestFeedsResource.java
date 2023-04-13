package sd2223.trab1.servers.rest;

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
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.api.rest.UsersService;
import sd2223.trab1.api.Discovery;
import sd2223.trab1.servers.java.JavaFeeds;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RestFeedsResource extends RestResource implements FeedsService {

    final Feeds impl;
    public RestFeedsResource() {
        this.impl = new JavaFeeds();
    }
    @Override
    public long postMessage(String user, String pwd, Message msg) {

        return super.fromJavaResult( impl.postMessage(user, pwd, msg) );

    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {
        super.fromJavaResult( impl.removeFromPersonalFeed(user, mid, pwd) );

    }

    @Override
    public Message getMessage(String user, long mid) {
        return super.fromJavaResult( impl.getMessage(user, mid) );
    }

    @Override
    public List<Message> getMessages(String user, long time) {
        return super.fromJavaResult( impl.getMessages(user, time));
    }

    @Override
    public void subUser(String user, String userSub, String pwd) {
        super.fromJavaResult( impl.subUser(user, userSub, pwd) );
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) {

        super.fromJavaResult( impl.unsubscribeUser(user,userSub,pwd));
    }

    @Override
    public List<String> listSubs(String user) {
        return super.fromJavaResult( impl.listSubs(user));
    }

}
