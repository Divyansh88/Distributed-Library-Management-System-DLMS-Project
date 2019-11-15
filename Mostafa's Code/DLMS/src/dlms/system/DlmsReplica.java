package comp.dsd.dlms.system;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.HashMap;

import comp.dsd.dlms.logger.DlmsLogger;
import comp.dsd.dlms.presentation.DataAdaptor;
import comp.dsd.dlms.presentation.DataPresentation;
import comp.dsd.dlms.server.DlmsServer;
import comp.dsd.dlms.server.DlmsServerImpl;

public class DlmsReplica {
	    //-- fields
		public static DlmsServer conLibServer, mcgLibServer, monLibServer;
		private DlmsLogger replicaLogger;

		//------------------------------------------fields
		
		public DlmsReplica() {
			try {	
				
				//-- start Distributed Library Management System Replica
				startDlms();
				
				//-- initialize data members
				replicaLogger = new DlmsLogger("Replica");
				
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}

		}
		
		/**
		 * this method is responsible for creating three library servers and 
		 * start the servers in concurrent 
		 */
		public static void startDlms(){
			try {			
								
				//-- initialize servers
				conLibServer = new DlmsServer("CON","localhost",1111);
				mcgLibServer = new DlmsServer("MCG","localhost",2222);
				monLibServer = new DlmsServer("MON","localhost",3333);
				
				
				//-- run library servers concurrently   			
				Runnable conLibServerR = () -> {runServer(conLibServer);};
				Runnable mcgLibServerR = () -> {runServer(mcgLibServer);};
				Runnable monLibServerR = () -> {runServer(monLibServer);};
				
				Thread conLibServerT = new Thread(conLibServerR);
				Thread mcgLibServerT = new Thread(mcgLibServerR);
				Thread monLibServerT = new Thread(monLibServerR);
				
				//-- start the servers in concurrent
				conLibServerT.start();
				mcgLibServerT.start();
				monLibServerT.start();
				
				System.out.println("Distributed Library Management System Replica starts ...");
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
		/**
		 * this method is responsible for creating a new server and run them
		 * @param libServer library server instance
		 */
		private	 static void runServer(DlmsServer libServer) {
			try {			
				libServer.run();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

	  	
	  	
}
