package sd2223.trab1.servers.soap;

import jakarta.xml.ws.Endpoint;
import sd2223.trab1.api.Discovery;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SoapFeedsServer {
    public static final int PORT = 8083;
    public static final String SERVICE_NAME = "feeds";
    public static String SERVER_BASE_URI = "http://%s:%s/soap";

    private static Discovery discovery = Discovery.getInstance();
    private static String domain;
    private static long base;
    private static long num_sql=0;

    private static Logger Log = Logger.getLogger(SoapFeedsServer.class.getName());

    public static void main(String[] args) throws Exception {

//		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
//		System.setProperty("com.sun.xml.internal.ws.transport.http.client.HttpTransportPipe.dump", "true");
//		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
//		System.setProperty("com.sun.xml.internal.ws.transport.http.HttpAdapter.dump", "true");
        try{
            domain = args[0];
            base = Long.parseLong(args[1]);
        }catch(Exception e){
            Log.severe(e.getMessage());
        }
        Log.setLevel(Level.INFO);

        String ip = InetAddress.getLocalHost().getHostAddress();
        String serverURI = String.format(SERVER_BASE_URI, ip, PORT);

        Endpoint.publish(serverURI.replace(ip, "0.0.0.0"), new SoapFeedsWebService());

        discovery.announce(domain,SERVICE_NAME,serverURI);
        Log.info(String.format("%s Soap Server ready @ %s\n", SERVICE_NAME, serverURI));
    }
    public static String getDomain() {
        return domain;
    }
    public static long getBase(){
        return base;
    }
    public static long getSeqA(){
        return num_sql++;
    }

}
