/***************
 * LinkStateRouter
 * Author: Christian Duncan
 * Modified by: 
 * Represents a router that uses a Distance Vector Routing algorithm.
 ***************/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LinkStateRouter extends Router {
    // A generator for the given LinkStateRouter class
    public static class Generator extends Router.Generator {
        public Router createRouter(int id, NetworkInterface nic) {
            return new LinkStateRouter(id, nic);
        }
    }
    public static class LinkStatePacket {
		// This is how we will store our Packet Header information

		HashMap<Integer, Integer> neighborDistance; 
        int source; //source of this packet
        int sequenceNum;
        //not sure what exactly to send in the packet, I think it should send its neighbors to every other node,
        //and then each node independently calcs neighbor distances.
      //send to nieghors, create new sequence numer
        //send list of neighors and distances hashmap key is neighor id, value is distance (int, long)
		public LinkStatePacket(HashMap<Integer, Integer> neighborDistance, int nsap,int currSeqNum) {
			this.neighborDistance = neighborDistance;
            this.source = nsap;
            this.sequenceNum = currSeqNum;
		}
	}
public static class PingPacket {
		// This is how we will store our Packet Header information

		boolean ping; // The payload!
		long timestamp;

		public PingPacket() {
			ping = false;
			timestamp = System.currentTimeMillis();

		}
	}
    public static class Packet {
		// This is how we will store our Packet Header information

		Integer source; 
		Integer destination;
        Object payload;
        int hopCount;

		public Packet(Integer source, Integer destination, Object payload, int hopCount) {
			this.source = source;
            this.destination = destination;
            this.payload = payload;
            this.hopCount = hopCount;
		}
	}
    Debug debug;
    int distances[];//the distance to each neighbor of this node
    HashMap<Integer, HashMap<Integer, Integer>> networkGraph; //the graph of the network containing each node and then each node's neighbors and distance to that neighot
    HashMap<Integer, Integer> nodeSeqNums; //used for tracking seq num for new link state packets
    HashMap<Integer, Integer> routingTable; //maps destinations to links
    int currSeqNum;
    public LinkStateRouter(int nsap, NetworkInterface nic) {
        super(nsap, nic);
        debug = Debug.getInstance();  // For debugging!
        int numLinks = nic.getOutgoingLinks().size();
        distances = new int[numLinks];
        networkGraph = new HashMap<>();
        currSeqNum = 0;
        nodeSeqNums = new HashMap<>();
        routingTable = new HashMap<>();
    }
    long delay = 1000;
    public void run() {
        long timeToReCalc = System.currentTimeMillis() + delay;
        while (true) {
            if (System.currentTimeMillis() > timeToReCalc) {
				sendPing();
                sendLinkPacket();
                rebuildTable();
				timeToReCalc = System.currentTimeMillis() + delay;
			}
            // See if there is anything to process
            boolean process = false;
            NetworkInterface.TransmitPair toSend = nic.getTransmit();
            if (toSend != null) {
                // There is something to send out
                process = true;
                debug.println(3, "(LinkStateRouter.run): I am being asked to transmit: " + toSend.data + " to the destination: " + toSend.destination);
                Packet p = new Packet(nsap, toSend.destination, toSend.data, 20);
                routePacket(p);
            }

            NetworkInterface.ReceivePair toRoute = nic.getReceived();
            if (toRoute != null) {
                // There is something to route through - or it might have arrived at destination
                process = true;
                if (toRoute.data instanceof PingPacket) {
					processPingPacket(toRoute.originator, (PingPacket) toRoute.data);
					// packet recieved
					// PingPacket p = (PingPacket) toRoute.data;
					//
				}
                else if (toRoute.data instanceof LinkStatePacket) {
					
					processLinkStatePacket(toRoute.originator, (LinkStatePacket) toRoute.data); 
				}
                 else if (toRoute.data instanceof Packet) {
                      Packet p = (Packet) toRoute.data;
					if (p.destination == nsap) {
                        // It made it!  Inform the "network" for statistics tracking purposes
                        debug.println(4, "(FloodRouter.run): Packet has arrived!  Reporting to the NIC - for accounting purposes!");
                        debug.println(6, "(FloodRouter.run): Payload: " + p.payload);
                        nic.trackArrivals(p.payload);
                    } else if (p.hopCount > 0) {
                        // Still more routing to do
                        p.hopCount--;
                       routePacket(p); 
                    } else {
                        debug.println(5, "Packet has too many hops.  Dropping packet from " + p.source + " to " + p.destination + " by router " + nsap);
                    }
					
				}
                else{
                debug.println(3, "Unrecognized Packet Type");

                }
            }

            if (!process) {
                // Didn't do anything, so sleep a bit
                try { Thread.sleep(1); } catch (InterruptedException e) { }
            }
        }
    }
    public void printTable(HashMap<Integer,Integer> t){
         debug.println(3, "//////////////");
        for(Map.Entry<Integer,Integer> e: t.entrySet()){
           debug.println(3, e.getKey() + " " + e.getValue());
        }
        debug.println(3, "//////////////");
    }
    
  public void routePacket(Packet p){
      Integer link = routingTable.get(p.destination);
      if(link == null){
          debug.println(3, "Unrecognized IP address");
      }
      else{
          nic.sendOnLink(link, p);
      }
  }

  public void sendPing(){
      for (int i = 0; i < nic.getOutgoingLinks().size(); i++) {
			// make packet
			PingPacket p = new PingPacket();
			nic.sendOnLink(i, p);
		}
  }
  public void sendLinkPacket(){
      HashMap<Integer, Integer> neighborDistance = new HashMap<>();
        currSeqNum++;
      for(int i = 0;i<distances.length;i++){
         neighborDistance.put(nic.getOutgoingLinks().get(i), distances[i]);
         //populate nieghor distances
      }
    
    for(int i = 0;i<nic.getOutgoingLinks().size();i++){
  LinkStatePacket p = new LinkStatePacket(neighborDistance, nsap, currSeqNum);
     //send to all neighors
     nic.sendOnLink(i, p);

    }
    
  }
  public void processPingPacket(int originator, PingPacket p) {

		debug.println(3,
				"(LinkStateRouter.pPP): PingPacket Recieved! " + originator + " to the destination: " + p.ping);
		// if packet is recieved and ping is false, send ack, if false calc time stamp
		// diff/2
		int i = nic.getOutgoingLinks().indexOf(originator);
		if (p.ping) {
			// calculate time taken
			long dist = (System.currentTimeMillis() - p.timestamp) / 2;
			debug.println(3, "LinkStateRouter.pPP): nsap = " + nsap + " link = " + i + " distance = " + dist);
			distances[i] = (int) dist;
		} else {
			// send back
			p.ping = true;

			nic.sendOnLink(i, p);
		}
	}
    public void processLinkStatePacket(int originator, LinkStatePacket p){
        	debug.println(3,
				"(LinkStateRouter.pPP): LinkStatePacket Recieved! " + originator + " to the destination: " );
                Integer oldSeqNum = nodeSeqNums.get(p.source);
                 if(oldSeqNum == null || oldSeqNum < p.sequenceNum) {
                    nodeSeqNums.put(p.source, p.sequenceNum);
                    networkGraph.put(p.source, p.neighborDistance);
                    for (int i = 0; i < nic.getOutgoingLinks().size(); i++) {
                        nic.sendOnLink(i, p);
                }
            

    
    }
    }
    public void rebuildTable(){
        System.out.println("uild tale eing called");
        HashMap<Integer, Integer> finalLink = new HashMap<>(); //Key: Integer (NSAP Destination), Value: Integer (Link Index to use)
        HashMap<Integer, Integer> finalDistance = new HashMap<>(); //Key: Integer (NSAP Destination), Value: Integer (Shortest Distance)

        HashMap<Integer, Integer> workingLink = new HashMap<>(); //Key: Integer (NSAP Destination), Value: Integer (Link Index to use)
        HashMap<Integer, Integer> workingDistance = new HashMap<>();// Key: Integer (NSAP Destination), Value: Integer (Shortest (Current Known) Distance)

  //Initially, all four HashMaps are empty.
  workingLink.put(nsap, -1); //Add the router's own NSAP to workingLink with value -1.
  workingDistance.put(nsap, 0); //Add the router's own NSAP to workingDistance with value 0
   
while(!workingDistance.isEmpty()){
     Integer minKey = -1;
    Integer minDistance = -1;
    Integer minLink = -1;
    // search through the workingDistance to find the key with smallest distance
    for(Map.Entry<Integer,Integer> e: workingDistance.entrySet()){
        if(minKey == -1 || e.getValue() < minDistance) {
            minKey = e.getKey();
            minDistance = e.getValue();
            minLink = workingLink.get(minKey);
    }
    }
     workingDistance.remove(minKey);
      workingLink.remove(minKey);  
    finalLink.put(minKey, minLink);
    finalDistance.put(minKey, minDistance);
    HashMap<Integer, Integer> neighbors = networkGraph.get(minKey);
    if(neighbors != null){
    for(Map.Entry<Integer,Integer> u : neighbors.entrySet()){
        if(finalDistance.containsKey(u.getKey())){
            //skip node is finalized
        }
        else {
            Integer newDistance = minDistance + u.getValue();
            Integer oldDistance = workingDistance.get(u.getKey());
            if(oldDistance == null || newDistance < oldDistance){
                workingDistance.put(u.getKey(),newDistance);
                if(minLink != -1){
                    workingLink.put(u.getKey(), minLink);
                }
                else{
                    workingLink.put(u.getKey(), nic.getOutgoingLinks().indexOf(u.getKey()));
                }
            }
        }
      }
     }
    }
    routingTable = finalLink;
     System.out.println(routingTable);
    printTable(routingTable); 
    }
}
