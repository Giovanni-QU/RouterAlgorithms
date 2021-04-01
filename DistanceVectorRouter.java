/***************
 * DistanceVectorRouter
 * Author: Christian Duncan
 * Modified by: 
 * Represents a router that uses a Distance Vector Routing algorithm.
 ***************/
import java.util.ArrayList;
import java.util.HashMap;
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

    private static class DistanceLink {
        int distance;
        int link;
        public DistanceLink(int d, int l){
            distance = d;
            link = l;
        }
    }

    Debug debug;
    HashMap<Integer, DistanceLink> routingTable;
    int []distances;
    ArrayList<HashMap<Integer, DistanceLink>> neighborTables;

    public DistanceVectorRouter(int nsap, NetworkInterface nic) {
        super(nsap, nic);
        debug = Debug.getInstance();  // For debugging!
         routingTable = new HashMap<>();
        int size = nic.getOutgoingLinks().size();
        distances = new int [size];
        neighborTables = new ArrayList<>();

        for(int i =0; i < size; i++){
            distances[i]= Integer.MAX_VALUE;
            neighborTables.add(new HashMap<>());
        }

        
    
        //new method to set array
       
    
    }
        long delay = 1000;
    public void run() {
        long timeToReCalc = System.currentTimeMillis() + delay;

        while (true) {

        //getOutgoinglinks is neighbors
        //make ping packet, set timestamp and then calculate time diff when its received to get distance

            if(System.currentTimeMillis() > timeToReCalc){
                 calcDistances();
                 buildRoutingTable();
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
                    processPingPacket(p, toRoute.originator);
                
                    //if packet is recieved and ping is false, send ack, if false calc time stamp diff/2
                }
            }

            if (!process) {
                // Didn't do anything, so sleep a bit
                try { Thread.sleep(1); } catch (InterruptedException e) { }
            }
        }
    }
    private void processPingPacket(DistanceVectorRouter.PingPacket p, int originator) {
        int linkIndex = nic.getOutgoingLinks().indexOf(originator);
        debug.println(3, "(DistanceVectorRouter.run): PingPacket Recieved! " + originator + " to the destination: " + p.ping);
        if (p.ping){
            int distance = (int)(System.currentTimeMillis() - p.timestamp)/2;
            distances[linkIndex]=distance;
        }
        else{
            // mark it and send packet back
            p.ping = true;
            
            nic.sendOnLink(linkIndex, p);
        }
    }
    private void buildRoutingTable() {
        //display array for debugging purposes
        String msg = "Router " + nsap + ":";
        for(int i=0; i< distances.length; i++){
            msg = msg + " " + distances[i];
        }
        debug.println(5, msg);
        //create a new tmp routing table
        //add entry that is know, ergo distance to self = 0 and link index = -1
        //use neighbor tables to build my table  
        //send my table on link to neighbors (its a loop)
        //make tmp table the new routing table
        //for debugging, print out tmp table
    }
    public void calcDistances(){
        for(int i=0;i<nic.getOutgoingLinks().size();i++){
            //make packet
            PingPacket p = new PingPacket();
            nic.sendOnLink(i,p);
        }

        
    }
}
