package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import Implementation.LibraryObj;
import service.logService;

public class CONServer implements Runnable {

	public DatagramSocket aSocket = null;
	private volatile boolean exit = false;
	
	public CONServer(){	
		try {
			aSocket = new DatagramSocket();
			System.out.println("initialize con server");
		}
		catch(SocketException e) {
			System.out.println("initialize con server error ..........");
			}
	}
	
	public void closeSocket(){
		aSocket.close();
	}

	public void start(boolean sf) {
		
		try {
			logService log = new logService("CONserver");
			LibraryObj obj = new LibraryObj(sf);
			obj = new LibraryObj("CONserver");	

			System.out.println("CON Server ready and waiting ...");
			log.writeLog("Server is Started");

			// Receive
			try {
			
				aSocket = new DatagramSocket(1111);
				byte[] buffer = new byte[1000];
				//System.out.println("Port 1111 ready............");
				

				
				while (true) {
					DatagramPacket request = new DatagramPacket(buffer, buffer.length);
					
					aSocket.receive(request);
					String s = new String(request.getData(),
						      request.getOffset(),
						      request.getLength(), "UTF-8");

					if(s.contains("@")){
						String[] messageSegment = s.split("_");
						String SequenceID = messageSegment[0];
						InetAddress FEUdpInetAddress = InetAddress.getByName(messageSegment[1].split(":")[0]);
						int FEUdpPort = Integer.parseInt(messageSegment[1].split(":")[1]);
						String requestName = messageSegment[2];
						String requestParam[] = messageSegment[3].split("@");
						
					//	System.out.println("SequenceID is: " + SequenceID);
								
						String answer = "";
						switch(requestName){
						case "addItem": {
							String managerID = requestParam[0];
							String itemID = requestParam[1];
							String itemName = requestParam[2];
							int quantity = Integer.parseInt(requestParam[3]);
							answer = obj.adItem(managerID, itemID, itemName, quantity, sf);
							break;
						}
						case "removeItem": {
							String managerID = requestParam[0];
							String itemID = requestParam[1];
							int quantity = Integer.parseInt(requestParam[2]);
							answer = obj.removeItem(managerID, itemID, quantity);
							break;
						}
						case "listItemAvailability": {
							String managerID = requestParam[0];
							answer = obj.listItemAvailability(managerID);
							break;
						}
						case "borrowItem": {
							String userID = requestParam[0];
							String itemID = requestParam[1];
							String days = requestParam[2];
							answer = obj.borrowItem(userID, itemID, days);
							break;
						}
						case "findItem": {
							String userID = requestParam[0];
							String itemName = requestParam[1];
							answer = obj.findItem(userID, itemName);
							break;
						}
						case "returnItem": {
							String userID = requestParam[0];
							String itemID = requestParam[1];
							answer = obj.returnItem(userID, itemID);
							break;
						}
						case "addWaitQueue": {
							String userID = requestParam[0];
							String itemID = requestParam[1];
							answer = obj.addWaitQueue(userID, itemID);
							break;
						}
						case "exchangeItem": {
							String userID = requestParam[0];
							String newItemID = requestParam[1];
							String oldItemID = requestParam[2];
							answer = obj.exchangeItem(userID, newItemID, oldItemID);
							break;
						}
						}
						answer = SequenceID +"_" + answer;
						System.out.println("Answer is: " + answer);
						byte[] message = answer.getBytes();
					//	System.out.println("ip is: "+FEUdpInetAddress + "port is: " + FEUdpPort);
						DatagramPacket reply = new DatagramPacket(message, answer.length(), FEUdpInetAddress,
								FEUdpPort);
						aSocket.send(reply);
					}
					else{
						String[] parts = s.split("_");
						String action = parts[0];
						String answer = "";
						switch(action){
						case "borrowItem": {
							String userID = parts[1];
							String itemID = parts[2];
							String days = parts[3];
							answer = obj.borrow(userID, itemID, days);
							break;
						}
						case "findItem": {
							String userID = parts[1];
							String itemName = parts[2];
							answer = obj.find(userID, itemName);
							break;
						}
						case "returnItem": {
							String userID = parts[1];
							String itemID = parts[2];
							answer = obj.returnBook(userID, itemID);
							break;
						}
						case "reserveItem": {
							String userID = parts[1];
							String itemID = parts[2];
							answer = obj.reserve(userID, itemID);
							break;
						}
						case "cancelReserve": {
							String userID = parts[1];
							String itemID = parts[2];
							answer = obj.cancelR(userID, itemID);
							break;
						}
						case "holdItem": {
							String userID = parts[1];
							String itemID = parts[2];
							answer = obj.holdBook(userID, itemID);
							break;
						}
						case "RholdItem": {
							String userID = parts[1];
							String itemID = parts[2];
							answer = obj.RholdBook(userID, itemID);
							break;
						}
						}
						byte[] message = answer.getBytes();
						DatagramPacket reply = new DatagramPacket(message, answer.length(), request.getAddress(),
								request.getPort());
						aSocket.send(reply);
					}	
				}
			} catch (SocketException e) {
				System.out.println("Socket CON: " + e.getMessage());

			} catch (IOException e) {
				System.out.println("IO: " + e.getMessage());
			} finally {
				if (aSocket != null)
					aSocket.close();
			}
		}
		catch (Exception e) {
			System.err.println("ERROR: " + e);
			e.printStackTrace(System.out);
		}
	}

	@Override
	public void run() {
		while(!exit){ System.out.println("Server is running....."); } 
		System.out.println("Server is stopped....");	
	}
	
	public void stop(){
        exit = true;
    }
}
