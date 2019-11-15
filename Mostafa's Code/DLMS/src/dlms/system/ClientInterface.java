package comp.dsd.dlms.system;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import comp.dsd.dlms.client.DlmsClient;
import comp.dsd.dlms.server.DlmsServer;

public class ClientInterface {
   
	static String[] orbInfo;

	public static void main(String[] args) {
		try {
			// get ORB address and port information
			orbInfo = args;
			
			Scanner kb = new Scanner(System.in);
			String clientID = "1";
			while(!clientID.equalsIgnoreCase("0")) {
				System.out.print("please enter your client ID: "); 
				clientID = kb.nextLine();
				if(!clientID.equalsIgnoreCase("0")) {
					System.out.println("");
					DlmsClient client = new DlmsClient(clientID, orbInfo);
					client.startUserInterface();
					
				}
				System.out.println("");
			}
		
			System.out.println("terminated...");
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		
		


	}

}

