
/***************
 * DistanceVectorRouter
 * Author: Christian Duncan
 * Modified by: 
 * Represents a router that uses a Distance Vector Routing algorithm.
 ***************/
import java.util.ArrayList;
import java.util.HashMap;
<<<<<<< HEAD

public class DistanceVectorRouter extends Router {
	// A generator for the given DistanceVectorRouter class
	public static class Generator extends Router.Generator {

		public Router createRouter(int id, NetworkInterface nic) {
			return new DistanceVectorRouter(id, nic);
			// create routerTable hashmap
		}
	}
	public static class DistanceVectorPacket {
		// This is how we will store our Packet Header information

		HashMap<Integer, Integer> routingDistance;

		public DistanceVectorPacket(HashMap<Integer, Integer> routingDistance) {
			this.routingDistance = routingDistance;

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

	Debug debug;
	int distances[];
	HashMap<Integer, Integer> routingDistance; // Distances to all nodes in the network
	HashMap<Integer, Integer> routingLink; // link to use to route to all those nodes

	public DistanceVectorRouter(int nsap, NetworkInterface nic) {
		super(nsap, nic);
		debug = Debug.getInstance(); // For debugging!

		int numLinks = nic.getOutgoingLinks().size();
		distances = new int[numLinks];
		routingDistance = new HashMap<>();
		routingLink = new HashMap<>();

		// new method to set array
		// getOutgoinglinks is neighbors
		// make ping packet, set timestamp and then calculate time diff when its
		// received to get distance

	}

	long delay = 1000;

	public void run() {
		long timeToReCalc = System.currentTimeMillis() + delay;

		while (true) {
			if (System.currentTimeMillis() > timeToReCalc) {
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
				debug.println(3, "(DistanceVectorRouter.run): I am being asked to transmit: " + toSend.data
						+ " to the destination: " + toSend.destination);
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
				if (toRoute.data instanceof DistanceVectorPacket) {
					//arraylist of hashmaps
			}

			if (!process) {
				// Didn't do anything, so sleep a bit
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public void processPingPacket(int originator, PingPacket p) {
		debug.println(3,
				"(DistanceVectorRouter.pPP): PingPacket Recieved! " + originator + " to the destination: " + p.ping);
		// if packet is recieved and ping is false, send ack, if false calc time stamp
		// diff/2
		int i = nic.getOutgoingLinks().indexOf(originator);
		if (p.ping) {
			// calculate time taken
			long dist = (System.currentTimeMillis() - p.timestamp) / 2;
			debug.println(3, "DistanceVectorRouter.pPP): nsap = " + nsap + " link = " + i + " distance = " + dist);
			distances[i] = (int) dist;
		} else {
			// send back
			p.ping = true;

			nic.sendOnLink(i, p);
		}
	}

	public void calcDistances() {

		for (int i = 0; i < nic.getOutgoingLinks().size(); i++) {
			// make packet
			PingPacket p = new PingPacket();
			nic.sendOnLink(i, p);
		}

	}

	public void buildRoutingTable() {
		//start a new table, insert our info
		routingDistance = new HashMap<>();
		routingLink = new HashMap<>();
		routingDistance.put(nsap, 0);
		routingLink.put(nsap, -1);
		//build the table from the other neighbor info
		
		//send our routing table to our neighbors
		for (int i = 0; i < nic.getOutgoingLinks().size(); i++) {
			// make packet
			DistanceVectorPacket p = new DistanceVectorPacket(routingDistance);
			nic.sendOnLink(i, p);
		}
	}
=======
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
>>>>>>> 700b7da8d378964718a816b7dcbdcb83b7353a82
}
