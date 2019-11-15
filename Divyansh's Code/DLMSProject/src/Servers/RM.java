package Servers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


public class RM {
	static String RName = "RM3"; 
	static int CONport = 1111;
	static int MCGport = 2222;
	static int MONport = 3333;
	static List<String> failMessage = new ArrayList<String>();
	static List<String> requestMessage = new ArrayList<String>();
	static int count = 0;
	static int totalOrder = 1;
	static Queue<String> FIFO = new LinkedList<String>() ;
	static ServerDriver driver = new ServerDriver();
	
	static String RM1 = "132.205.95.110", RM2 = "132.205.95.109", RM3 = "132.205.95.108";
	static List<String> cfAck = new ArrayList<String>();
	static String path = "src\\Log files";
	
	//static logService log = new logService("RM1"); 

	public static void main(String[] args) throws InterruptedException, IOException {
		path = path + "\\" + "RM3";
		File file = new File(path);
		file.createNewFile();
		
		Runnable task = () -> {
			try {
				receive();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
		Thread thread = new Thread(task);
		thread.start();	
		driver.start();
	}
	
	private static void receive() throws InterruptedException {
		DatagramSocket aSocket = null;
		
		try {
			aSocket = new DatagramSocket(4444);
			
			System.out.println("RM Started............");
			while (true) {
				byte[] buffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String s = new String(request.getData(),
				      request.getOffset(),
				      request.getLength(), "UTF-8");
				String[] parts = s.split("_");
				System.out.println("received message is: " + s);
				
				if(!checkDuplicate(s)){
					//log.writeLog("#"+s);
					if(s.contains("[cf]") || s.contains("[sf]")){
						System.out.println("Receive fail message: " + s);
						processFailMessage(s);
					}
					else if(s.contains("[cfack]")){
						System.out.println("Receive [cfack]: " + s);
						cfAck.add(s);
						if(cfAck.size() == 3){
							recover();
							System.out.println("RM3 ROCOVER");
						}
					}
					else{
						int seqId = Integer.parseInt(parts[0]);
						if(totalOrder == seqId){
							processRequestMessage(s);
							totalOrder++;
							if(FIFO.size() > 0) {
								checkFIFO();
							}					
						}
						else{
							FIFO.add(s);
						}
					}	
				}
			}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}
	
	private static void checkFIFO(){
		int size = FIFO.size();
		for(int i=0; i<size; i++){
			String temp = FIFO.poll();
			int tempSeqId = Integer.parseInt(temp.split("_")[0]);
			if(totalOrder == tempSeqId){
				processRequestMessage(temp);
				totalOrder++;
				checkFIFO();
				break;
			}
			else{
				FIFO.add(temp);
			}
		}
	}
	
	private static void processFailMessage(String message) throws InterruptedException{
		String[] parts = message.split("_");
		String failMessageID = parts[0];
		String ReplicaName = parts[1];
		String serverName = parts[2];
		InetAddress aHost;
		String recoverMessage = "";
		
		System.out.println("failMessageID is: " + failMessageID);

		try{
			if(message.contains("[cf]")){
				System.out.println("Receive [cf]: " + message);
				if(ReplicaName.equals("RM1")){
					aHost = InetAddress.getByName(RM1);
				}
				else if(ReplicaName.equals("RM2")){
					aHost = InetAddress.getByName(RM2);
				}
				else{
					aHost = InetAddress.getByName(RM3);
				}
				
				if(serverName.equals("CON")){
					recoverMessage = "0"+"_"+ "localhost:4444" + "_" + "listItemAvailability" + "_" + "CONM1111" +"_";
					if(!sendPingMessage(aHost,CONport,recoverMessage)){
						if(ReplicaName.equals(RName)){
							cfAck.add("crash");
							if(cfAck.size() == 3){
								//log.writeLog("111111 cfAck: "+ cfAck.size() );
								System.out.println("111111 cfAck: "+ cfAck.size() );
								recover();
							}
						}
						else{
							sendMessage(aHost, 4444, "3_[cfack]");
						}
					}
				}
				if(serverName.equals("MON")){
					recoverMessage = "0"+"_"+ "localhost:4444" + "_" + "listItemAvailability" + "_" + "MONM1111" +"_";
					if(!sendPingMessage(aHost,MONport,recoverMessage)){
						if(ReplicaName.equals(RName)){
							cfAck.add("crash");
							if(cfAck.size() == 3){
								recover();
							}
						}
						else{
							sendMessage(aHost, 4444, "3_[cfack]");
						}
					}
				}
				if(serverName.equals("MCG")){
					recoverMessage = "0"+"_"+ "localhost:4444" + "_" + "listItemAvailability" + "_" + "MCGM1111" +"_";
					if(!sendPingMessage(aHost,MCGport,recoverMessage)){
						if(ReplicaName.equals(RName)){
							cfAck.add("crash");
							if(cfAck.size() == 3){
								//log.writeLog("111111 cfAck: "+ cfAck.size() );
								System.out.println("111111 cfAck: "+ cfAck.size() );
								recover();
							}
						}
						else{
							sendMessage(aHost, 4444, "3_[cfack]");
						}
					}
				}
			}
			else{	
				System.out.println("Receive [sf]: " + message);
				count++;
				System.out.println("count is: "+ count);
				if(count == 3){
					recover();
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		}
	}
	
	private static void processRequestMessage(String message){
		String[] parts = message.split("_");
		
		try{
			String server = parts[3].substring(0, 3);
			int serverPort = 0;
			InetAddress aHost = InetAddress.getByName("localhost");
			switch(server){
				case "CON": {
					serverPort = CONport;
					break;
				}
				case "MCG": {
					serverPort = MCGport;
					break;
				}
				case "MON": {
					serverPort = MONport;
					break;
				}
			}
			sendMessage(aHost, serverPort, message);	
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		}
	}
	
	private static boolean checkDuplicate(String message){
		if(message.contains("[cf]") || message.contains("[sf]")){
			if(failMessage.contains(message))
				return true;
			else{
				failMessage.add(message);
				return false;
			}
		}
		else{
			if(requestMessage.contains(message))
				return true;
			else{
				requestMessage.add(message);
				return false;
			}
		}
	}
	
	private static void sendMessage(InetAddress host, int serverPort, String input) {
		DatagramSocket aSocket = null;
		try {
			
			aSocket = new DatagramSocket();
			byte[] message = input.getBytes();
			DatagramPacket request = new DatagramPacket(message, input.length(), host, serverPort);
			aSocket.send(request);	
			
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
	}
	
	private static boolean sendPingMessage(InetAddress host, int serverPort, String input){
		final DatagramSocket aSocket ;
		try {
			aSocket = new DatagramSocket();
			byte[] message = input.getBytes();
			DatagramPacket request = new DatagramPacket(message, input.length(), host, serverPort);
			long currentTime = System.currentTimeMillis();
			aSocket.send(request);	
			Runnable task = () -> {
				checkUDPTimeOut(aSocket, currentTime);
			};
			Thread thread = new Thread(task);
			thread.start();
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			aSocket.receive(reply);
			return true;
			
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
			return false;
		} 
	}
	
	private static void recover() throws InterruptedException, RemoteException{
		System.out.println("Start recover ");
		
		driver.start();
			
		try{
			System.out.println("Start update data ... ");
			FileInputStream fstream = new FileInputStream("RM3.txt");
			System.out.println("Start update RM3 ... ");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				if(!strLine.equals("")) {
					if(!(strLine.contains("[cf]") || strLine.contains("[sf]") || strLine.contains("[cfack]"))){
				    	 System.out.println("strLine is : " + strLine);
				    	 String[] parts = strLine.split("#"); // STRUCTURE OF LOG file of RM: Time#receivedMessage
				    	 String request = parts[1];
				    	 System.out.println("parts[1] is : " + parts[1]);
				    	 String[] requestParts = request.split("_");
				    	 System.out.println("request is: " + request);
				    	 String seqID = requestParts[0];
				    	 String requestMessage = requestParts[2]+"_"+requestParts[3];
				    	 String newRequest = seqID + "_" + "localhost:9999" + "_" + requestMessage;
				    	 System.out.println("seqID: "+seqID+" requestMessage: "+ requestMessage +" newRequest: " +newRequest);
				    	 processRequestMessage(newRequest);
				     } 
				}     
			}
			System.out.println("Finish update data ...");
			fstream.close();
			} catch (Exception e) {
			     System.err.println("Error: " + e.getMessage());
			}
	}
	
	private static void checkUDPTimeOut(DatagramSocket udpSocket, long sentTime) {
		try {
			InterruptableUDPThread iUDPSocket = new InterruptableUDPThread(udpSocket);
			while (System.currentTimeMillis() - sentTime <= 500) {}
			iUDPSocket.interrupt();
		}catch (Exception e){
		}
	}

	public static class InterruptableUDPThread extends Thread{

		   private final DatagramSocket socket;

		   public InterruptableUDPThread(DatagramSocket socket){
		      this.socket = socket;
		   }
		   
		   @Override
		   public void interrupt(){
		     super.interrupt();
		     this.socket.close();
		   }
	}
	
	public static void appendUsingFileWriter(String filePath, String text) {
		File file = new File(filePath);
		FileWriter fr = null;
		try {
			fr = new FileWriter(file, true);
			fr.write(text);
		} 
		catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
