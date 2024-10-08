package sd2223.trab1.servers.rest;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import sd2223.trab1.api.Discovery;


import java.net.InetAddress;
import java.net.URI;
import java.util.logging.Logger;



public class RestFeedsServer {
    private static Logger Log = Logger.getLogger(RestFeedsServer.class.getName());

    private static Discovery discovery = Discovery.getInstance();

    private static String domain;
    private static long base;
    private static long num_sql=0;

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
    }

    public static final int PORT = 8081;
    public static final String SERVICE = "feeds";
    private static final String SERVER_URI_FMT = "http://%s:%s/rest";

    public static void main(String[] args) {
        try {

            try{
                domain = args[0];
                base = Long.parseLong(args[1]);
            }catch(Exception e){
                Log.severe(e.getMessage());
            }
            ResourceConfig config = new ResourceConfig();
            config.register(new RestFeedsResource());

            String ip = InetAddress.getLocalHost().getHostAddress();
            String serverURI = String.format(SERVER_URI_FMT, ip, PORT);
            JdkHttpServerFactory.createHttpServer( URI.create(serverURI), config);

            discovery.announce(domain,SERVICE,serverURI);



            Log.info(String.format("%s Server ready @ %s\n",  SERVICE, serverURI));

        } catch( Exception e) {
            Log.severe(e.getMessage());
        }
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