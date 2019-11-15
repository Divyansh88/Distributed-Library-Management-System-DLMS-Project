package comp.dsd.dlms.system;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Scanner;

import comp.dsd.dlms.corba.DlmsFrontEndCorbaImpl;
import comp.dsd.dlms.server.DlmsServer;

public class FrontEndDriver {

	public static void main(String[] args) {
		try {
			
			String[] orbInfo = args;
			HashMap<String,String[]> udpInfo = new HashMap<String,String[]>();
			udpInfo.put("SEQUENCER", new String[]{"132.205.95.108","1234"});
			
			udpInfo.put("RM1", new String[]{"132.205.95.110","4444"});
			udpInfo.put("RM2", new String[]{"132.205.95.109","4444"});
			udpInfo.put("RM3", new String[]{"132.205.95.108","4444"});
			
			udpInfo.put("CON", new String[]{"132.205.95.109","1111"});
			udpInfo.put("MCG", new String[]{"132.205.95.109","2222"});
			udpInfo.put("MON", new String[]{"132.205.95.109","3333"});
			

			DlmsFrontEnd frontEnd = new DlmsFrontEnd("CF", udpInfo, orbInfo);						
			Runnable frontEndR = () -> {runFrontEnd(frontEnd);};				
			Thread frontEndT = new Thread(frontEndR);
						
			//-- start the FE 
			frontEndT.start();
			
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}		
		
	}
	
	/**
	 * this method is responsible for creating a new front end and run them
	 */
	private	 static void runFrontEnd(DlmsFrontEnd frontEnd) {
		try {			
			frontEnd.run();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	
	

}
