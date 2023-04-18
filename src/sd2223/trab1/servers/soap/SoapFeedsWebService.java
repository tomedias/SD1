package sd2223.trab1.servers.soap;

import jakarta.jws.WebService;
import sd2223.trab1.api.Message;
import sd2223.trab1.api.java.Feeds;

import sd2223.trab1.api.soap.FeedsException;
import sd2223.trab1.api.soap.FeedsService;
import sd2223.trab1.servers.java.JavaFeeds;
import sd2223.trab1.servers.rest.RestFeedsServer;


import java.util.List;
import java.util.logging.Logger;


@WebService(serviceName= FeedsService.NAME, targetNamespace=FeedsService.NAMESPACE, endpointInterface=FeedsService.INTERFACE)
public class SoapFeedsWebService extends SoapWebService<FeedsException> implements FeedsService {

    static Logger Log = Logger.getLogger(SoapFeedsWebService.class.getName());

    final Feeds impl;
    SoapFeedsWebService() {
        super( (result)-> new FeedsException( result.error().toString()));
        this.impl = new JavaFeeds(SoapFeedsServer.getDomain());
    }

    public long postMessage(String user, String pwd, Message msg) throws FeedsException{
        msg.setId(SoapFeedsServer.getSeqA() * 256 + SoapFeedsServer.getBase());
        return super.fromJavaResult( impl.postMessage(user, pwd, msg) );

    }

    @Override
    public void removeFromPersonalFeed(String user, long mid, String pwd) throws FeedsException{
        super.fromJavaResult( impl.removeFromPersonalFeed(user, mid, pwd) );

    }

    @Override
    public Message getMessage(String user, long mid) throws FeedsException{
        return super.fromJavaResult( impl.getMessage(user, mid) );
    }

    @Override
    public List<Message> getMessages(String user, long time) throws FeedsException{
        return super.fromJavaResult( impl.getMessages(user, time));
    }

    @Override
    public void subUser(String user, String userSub, String pwd) throws FeedsException{
        super.fromJavaResult( impl.subUser(user, userSub, pwd) );
    }

    @Override
    public void unsubscribeUser(String user, String userSub, String pwd) throws FeedsException{

        super.fromJavaResult( impl.unsubscribeUser(user,userSub,pwd));
    }

    @Override
    public List<String> listSubs(String user) throws FeedsException{
        return super.fromJavaResult( impl.listSubs(user));
    }

    @Override
    public void deleteFeed(String user) throws FeedsException {
        super.fromJavaResult( impl.deleteFeed(user));
    }

    @Override
    public List<Message> getPersonalFeeds(String user) throws FeedsException {
        return super.fromJavaResult(impl.getPersonalFeeds(user));
    }

    @Override
    public void postOutsideMessage(String user, Message msg) throws FeedsException {
        super.fromJavaResult(impl.postOutsideMessage(user, msg));
    }

    @Override
    public void removeUserMessage(String user, long mid) throws FeedsException {
        super.fromJavaResult(impl.removeUserMessage(user, mid));
    }

    @Override
    public void addSub(String user, String userSub) throws FeedsException {
        super.fromJavaResult(impl.addSub(user, userSub));
    }

    @Override
    public void removeSub(String user, String userSub) throws FeedsException {
        super.fromJavaResult(impl.removeSub(user, userSub));
    }
}
