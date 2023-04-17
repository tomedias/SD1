package sd2223.trab1.clients.rest;


import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.User;
import sd2223.trab1.api.java.Feeds;
import sd2223.trab1.api.java.Result;
import sd2223.trab1.api.rest.FeedsService;
import sd2223.trab1.api.rest.UsersService;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

public class RestFeedsClient extends RestClient implements Feeds {

    final WebTarget target;

    private static Logger Log = Logger.getLogger(RestFeedsClient.class.getName());

    public RestFeedsClient( URI serverURI ) {
        super( serverURI );
        target = client.target( serverURI ).path( FeedsService.PATH );
    }
    @Override
    public Result<Long> postMessage(String user, String pwd, Message msg) {
        return null;
    }

    @Override
    public Result<Void> removeFromPersonalFeed(String user, long mid, String pwd) {
        return null;
    }

    @Override
    public Result<Message> getMessage(String user, long mid) {
        return null;
    }

    @Override
    public Result<List<Message>> getMessages(String user, long time) {
        Response r = target.path(user).queryParam( UsersService.QUERY, time).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        return super.listToJavaResult(r, new GenericType<>(){});
    }

    @Override
    public Result<Void> subUser(String user, String userSub, String pwd) {
        return null;
    }

    @Override
    public Result<Void> unsubscribeUser(String user, String userSub, String pwd) {
        return null;
    }

    @Override
    public Result<List<String>> listSubs(String user) {
        return null;
    }

    @Override
    public Result<Void> deleteFeed(String user) {
        return null;
    }

    @Override
    public Result<List<Message>> getPersonalFeeds(String user) {
        Log.info("Chegou ao request");
        Response r = target.path("/").queryParam( FeedsService.USERSUB, user).request()
                .accept(MediaType.APPLICATION_JSON)
                .get();
        Log.info("Saiu do request");
        return super.listToJavaResult(r, new GenericType<>(){});

    }


}
