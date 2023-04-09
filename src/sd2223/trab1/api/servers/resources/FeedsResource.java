package sd2223.trab1.api.servers.resources;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.rest.services.FeedsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedsResource implements FeedsService {

    Map<String, HashMap<Long, Message>> feeds = new HashMap<String, HashMap<Long, Message>>();

    @Override
    public long postMessage(String user, String pwd, Message msg) {
        HashMap<Long, Message> userFeed = feeds.get(user);
        userFeed.put(msg.getId(), msg);
        return msg.getId();

        // TODO Auto-generated method stub


    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) {
        // TODO Auto-generated method stub
        HashMap<Long, Message> userFeed = feeds.get(user);
        userFeed.remove(mid);
        throw new WebApplicationException(Response.Status.OK);

    }

    @Override
    public Message getMessage(String user, long mid) {
        // TODO Auto-generated method stub
        HashMap<Long, Message> userFeed = feeds.get(user);
        return userFeed.get(mid);
    }

    @Override
    public List<Message> getMessages(String user, long time) {
        // TODO Auto-generated method stub
        HashMap<Long, Message> userFeed = feeds.get(user);
        return userFeed.values().stream().filter(m -> m.getCreationTime() > time).toList();
    }

    @Override
    public void subUser(String user, String userSub, String pwd) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'subUser'");
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'unsubscribeUser'");
    }

    @Override
    public List<String> listSubs(String user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'listSubs'");
    }

}
