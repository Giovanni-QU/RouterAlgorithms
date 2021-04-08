
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
	ArrayList<HashMap<Integer, Integer>> nMapList; //this is an arraylist of neighbor distances

	public DistanceVectorRouter(int nsap, NetworkInterface nic) {
		super(nsap, nic);
		debug = Debug.getInstance(); // For debugging!

		int numLinks = nic.getOutgoingLinks().size();
		distances = new int[numLinks];
		routingDistance = new HashMap<>();
		routingLink = new HashMap<>();
		nMapList = new ArrayList<>(numLinks);
		for(int i =0; i< numLinks; i++){
			distances[i]=10000;  // assume large delay initially, 10 seconds
			nMapList.add(new HashMap<>());  //allocated memory for hashmap in arraylist 
		}

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
					processDistanceVectorPacket(toRoute.originator, (DistanceVectorPacket)toRoute.data);
				}
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

	private void processDistanceVectorPacket(int o, DistanceVectorPacket d) {

		debug.println(4,
				"(DistanceVectorRouter.pDV): Distance Vector Packet Recieved! " + o + ": " + d.routingDistance);

		int link = nic.getOutgoingLinks().indexOf(o);
		nMapList.set(link, d.routingDistance);
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
		for(int i = 0; i<distances.length; i++){
			//iterate through each link
			HashMap<Integer,Integer> neighborMap= nMapList.get(i);
			for(Integer dest: neighborMap.keySet()){
				int dist = neighborMap.get(dest) + distances[i];
				Integer bestDistance = routingDistance.get(dest);
				if(bestDistance == null || dist < bestDistance){
					routingDistance.put(dest, dist);
					routingLink.put(dest, i);
				}
			}
		}
		//print out table
		
		//send our routing table to our neighbors
		for (int i = 0; i < nic.getOutgoingLinks().size(); i++) {
			// make packet
			DistanceVectorPacket p = new DistanceVectorPacket(routingDistance);
			nic.sendOnLink(i, p);
		}
	}
}
