/***************
 * DistanceVectorRouter
 * Author: Christian Duncan
 * Modified by: 
 * Represents a router that uses a Distance Vector Routing algorithm.
 ***************/
import java.util.ArrayList;
import java.util.Hashmap;
public class DistanceVectorRouter extends Router {
    // A generator for the given DistanceVectorRouter class
    public static class Generator extends Router.Generator {
        public Router createRouter(int id, NetworkInterface nic) {
            return new DistanceVectorRouter(id, nic);
            //create routerTable hashmap
        }
    }

     public static class PingPacket {
        // This is how we will store our Packet Header information
        
        boolean ping;  // The payload!
        long timestamp;
        public PingPacket() {
                ping = false;
                timestamp = System.currentTimeMillis();
       
        }
    }

    Debug debug;
    
    public DistanceVectorRouter(int nsap, NetworkInterface nic) {
        super(nsap, nic);
        debug = Debug.getInstance();  // For debugging!
         HashMap<int, int> routingTable = new HashMap<int, int>();
    
        //new method to set array
       //getOutgoinglinks is neighbors
       //make ping packet, set timestamp and then calculate time diff when its received to get distance
    
    }
        long delay = 1000;
    public void run() {
        long timeToReCalc = System.currentTimeMillis() + delay;

        while (true) {
            if(System.currentTimeMillis() > timeToReCalc){
                 calcDistances();
                 timeToReCalc = System.currentTimeMillis() + delay;
                }  
            // See if there is anything to process
            boolean process = false;
            NetworkInterface.TransmitPair toSend = nic.getTransmit();
            if (toSend != null) {
                // There is something to send out
                process = true;
                debug.println(3, "(DistanceVectorRouter.run): I am being asked to transmit: " + toSend.data + " to the destination: " + toSend.destination);
            }

            NetworkInterface.ReceivePair toRoute = nic.getReceived();
            if (toRoute != null) {
                // There is something to route through - or it might have arrived at destination
                process = true;
                if(toRoute.data instanceof PingPacket){
                    //packet recieved
                    PingPacket p = (PingPacket) toRoute.data;
                debug.println(3, "(DistanceVectorRouter.run): PingPacket Recieved! " + toRoute.originator + " to the destination: " + p.ping);
                    //if packet is recieved and ping is false, send ack, if false calc time stamp diff/2
                }
            }

            if (!process) {
                // Didn't do anything, so sleep a bit
                try { Thread.sleep(1); } catch (InterruptedException e) { }
            }
        }
    }
    public void calcDistances(){
        for(int i=0;i<toSend.getOutgoinglinks.size();i++){
            //make packet
            PingPacket p = new PingPacket();
            nic.sendOnLink(i,p);
        }

        
    }
}
