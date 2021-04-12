/***************
 * LinkStateRouter
 * Author: Christian Duncan
 * Modified by: 
 * Represents a router that uses a Distance Vector Routing algorithm.
 ***************/
import java.util.ArrayList;

public class LinkStateRouter extends Router {
    // A generator for the given LinkStateRouter class
    public static class Generator extends Router.Generator {
        public Router createRouter(int id, NetworkInterface nic) {
            return new LinkStateRouter(id, nic);
        }
    }
    public static class LinkStatePacket {
		// This is how we will store our Packet Header information

		ArrayList<Integer> neighbors; 
        //not sure what exactly to send in the packet, I think it should send its neighbors to every other node,
        //and then each node independently calcs neighbor distances.
		public LinkStatePacket(ArrayList<Integer> neighbors) {
			this.neighbors = neighbors;

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
    int distances[];//the distance to each neighbor of this node
    HashMap<Integer, ArrayList<Integer, Integer>> networkGraph; //the graph of the network containing each node and then each node's neighbors
    HashMap<Integer, Integer> routingTable; //each node in the graph and distance to that node
    public LinkStateRouter(int nsap, NetworkInterface nic) {
        super(nsap, nic);
        debug = Debug.getInstance();  // For debugging!
        int numLinks = nic.getOutgoingLinks().size();
        distances = new int[numLinks];
        networkGraph = new HashMap<>();
        routingTable = new HashMap<>();
      
    }
long delay = 1000;
    public void run() {
        long timeToReCalc = System.currentTimeMillis() + delay;
        while (true) {
            if (System.currentTimeMillis() > timeToReCalc) {
				sendPing();
				timeToReCalc = System.currentTimeMillis() + delay;
			}
            // See if there is anything to process
            boolean process = false;
            NetworkInterface.TransmitPair toSend = nic.getTransmit();
            if (toSend != null) {
                // There is something to send out
                process = true;
                if (toRoute.data instanceof PingPacket) {
					processPingPacket(toRoute.originator, (PingPacket) toRoute.data);
					// packet recieved
					// PingPacket p = (PingPacket) toRoute.data;
					//
				}
                if (toRoute.data instanceof LinkStatePacket) {
					
					processLinkStatePacket(); //not implemented yet
				}
                debug.println(3, "(LinkStateRouter.run): I am being asked to transmit: " + toSend.data + " to the destination: " + toSend.destination);
            }

            NetworkInterface.ReceivePair toRoute = nic.getReceived();
            if (toRoute != null) {
                // There is something to route through - or it might have arrived at destination
                process = true;
                debug.println(3, "(LinkStateRouter.run): I am being asked to transmit: " + toSend.data + " to the destination: " + toSend.destination);
            }

            if (!process) {
                // Didn't do anything, so sleep a bit
                try { Thread.sleep(1); } catch (InterruptedException e) { }
            }
        }
    }
  public void sendPing(){
      for (int i = 0; i < numLinks; i++) {
			// make packet
			PingPacket p = new PingPacket();
			nic.sendOnLink(i, p);
		}
  }
  public void sendLinkPacket(){
      LinkStatePacket p = new LinkStatePacket(nic.getOutgoingLinks);
      //not sure how to send to all nodes in the network
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
}
