package comp.dsd.dlms.system;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;


/**
 * @author Divyansh
 *
 */
public class Sequencer {
	
	static String request_message;
	static ArrayList<String> queue = new ArrayList<String>();
	static int seq = 1;
	static String rp1_ip = "132.205.46.226", rp2_ip = "132.205.46.234", rp3_ip = "132.205.46.238";
	
	public static void main(String[] args) {		
		startListiner();			
	}
	
	public static void receive() {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket(1234);											
			System.out.println("Sequencer 1234 Started............");
			while (true) {
				byte[] buffer = new byte[100000];
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);				
				aSocket.receive(request);// request received			
				request_message = new String(request.getData()).trim();
								
				if(request_message!=null) {		
					request_message = request.getAddress().toString().substring(1, request.getAddress().toString().length())+":"+Integer.toString(request.getPort())+"_"+request_message;
					if(!queue.contains(request_message)) {
						queue.add(request_message);
						send(request_message);
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
	
	public static void send(String request) {
		
			String data = Integer.toString(seq) +"_" +request;
			
			for(int i=0; i<3; i++) {
				Runnable task1 = () -> {
					sendMessage(rp1_ip, 4444, data);
				};
				Runnable task2 = () -> {
					sendMessage(rp2_ip, 4444, data);
				};
				Runnable task3 = () -> {
					sendMessage(rp3_ip, 4444, data);
				};
				
				Thread thread1 = new Thread(task1);
				Thread thread2 = new Thread(task2);
				Thread thread3 = new Thread(task3);
				
				thread1.start();
				thread2.start();
				thread3.start();
			}
			seq++;
			//queue.remove(0);
	}
	
	public static String sendMessage(String IP, int serverPort, String req_message) {
		DatagramSocket aSocket = null;
		String reply_message = "";
		try {
			aSocket = new DatagramSocket();
			InetAddress aHost = InetAddress.getByName(IP);
			DatagramPacket request = new DatagramPacket(req_message.getBytes(), req_message.length(), aHost, serverPort);
			aSocket.send(request);
			System.out.println("Request message sent from the client to server with port number " + IP + ":" + serverPort + " is: "
					+ new String(request.getData()));
			
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
		return reply_message;
	}
	
	
	public static void startListiner() {
		
		Runnable listinerR = () -> {
			receive();
		};
		
		Thread listinerT = new Thread(listinerR);
		listinerT.start();
	}
		
	
	
}
