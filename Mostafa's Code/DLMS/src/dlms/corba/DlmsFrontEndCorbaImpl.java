package comp.dsd.dlms.corba;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import org.omg.CORBA.ORB;

import comp.dsd.dlms.corba.DlmsServerCorbaImpl.FindItemThread;
import comp.dsd.dlms.corba.classes.DlmsServerCorbaInterfacePOA;
import comp.dsd.dlms.logger.DlmsLogger;
import comp.dsd.dlms.model.BorrowList;
import comp.dsd.dlms.model.Borrower;
import comp.dsd.dlms.model.ExchangeItem;
import comp.dsd.dlms.model.ExchangeList;
import comp.dsd.dlms.model.Item;
import comp.dsd.dlms.model.ItemList;
import comp.dsd.dlms.model.WaitList;
import comp.dsd.dlms.presentation.DataAdaptor;
import comp.dsd.dlms.presentation.DataPresentation;
import comp.dsd.dlms.server.DlmsServerImpl;

public class DlmsFrontEndCorbaImpl extends DlmsServerCorbaInterfacePOA{
	//-- fields
    private DlmsLogger frontEndLogger;
    private HashMap<String,String[]> udpInfo;
    DataAdaptor dataAdaptor;
    private ORB orb;
    private DatagramSocket udpSocket;
    private String faultMode;
    private long sentTime;
    private ArrayList<String> serversResponse;
    private int  udpTransmissionTime = 500;
    private int faultId;
    //------------------------------------------ fields
    
    
    //-- constructors    
    public DlmsFrontEndCorbaImpl(String fautMode, HashMap<String,String[]> udpInfo, DlmsLogger frontEndLogger, ORB orb) throws RemoteException{
    	this.setUdpInfo(udpInfo);
    	this.setFrontEndLogger(frontEndLogger); 
    	this.setOrb(orb);
    	dataAdaptor = new DataAdaptor(); 
    	this.faultMode = fautMode;
    	this.serversResponse = new ArrayList<String>();
    	faultId = 0;
    }
    //------------------------------------------ constructors
    
	//-- methods
	@Override
	public String addItem(String  managerID, String itemID, String itemName, int quantity){
		try {
			String request, result = "";
			
			//check for manager crash command
			if(quantity == -1000) {
				request = "1000_RM1_" + managerID.substring(0, 3) + "_[crash]";
				multicastRMs(request);
				result = "server " + managerID.substring(0, 3) + "is crashed";
				return result;
			}
			
			//-- send UDP add item request to the sequencer
			String[] param = {managerID,itemID,itemName,String.valueOf(quantity)};
			String[] sequencerUdpInfo = getUdpInfo().get("SEQUENCER");
			result = sendUdpRequest("addItem", param, sequencerUdpInfo);
			return result;
				
		} catch (Exception e) {

			return ("front end response = " + e.getMessage());
		}
		
	}
	
	@Override
	public String removeItem(String managerID, String itemID, int quantity){
		try {
			
			String request, result = "";
			//-- send UDP removeItem request to the sequencer
			String[] param = {managerID,itemID,String.valueOf(quantity)};
			String[] sequencerUdpInfo = getUdpInfo().get("SEQUENCER");
			result = sendUdpRequest("removeItem", param, sequencerUdpInfo);
			return result;
				
		} catch (Exception e) {
			return ("front end response = " + e.getMessage());
		}
		
	}

	@Override
	public String listItemAvailability(String managerID){
		try {
			
			String request, result = "";
			//-- send UDP listItemAvailability request to the sequencer
			String[] param = {managerID};
			String[] sequencerUdpInfo = getUdpInfo().get("SEQUENCER");
			result = sendUdpRequest("listItemAvailability", param, sequencerUdpInfo);
			return result;
				
		} catch (Exception e) {
			return ("front end response = " + e.getMessage());
		}
		
	}

	@Override
	public String borrowItem(String userID, String itemID, int numberOfDays){
		try {
			String request, result = "";
			//-- send UDP borrowItem request to the sequencer
			String[] param = {userID,itemID,String.valueOf(numberOfDays)};
			String[] sequencerUdpInfo = getUdpInfo().get("SEQUENCER");
			result = sendUdpRequest("borrowItem", param, sequencerUdpInfo);
			return result;
		} catch (Exception e) {
			return ("front end response = " + e.getMessage());
		}
	}
	
	
	@Override
	public String joinToItemWaitList(String userID, String itemID, int numberOfDays){
		try {
			String request, result = "";
			//-- send UDP joinToItemWaitList request to the sequencer
			String[] param = {userID,itemID,String.valueOf(numberOfDays)};
			String[] sequencerUdpInfo = getUdpInfo().get("SEQUENCER");
			result = sendUdpRequest("addWaitQueue", param, sequencerUdpInfo);
			return result;
		} catch (Exception e) {
			return ("front end response = " + e.getMessage());
		}
	}
	
	
	@Override
	public String leaveItemWaitList(String userID, String itemID){
		try {
			String request, result = "";
			//-- send UDP leaveItemWaitList request to the sequencer
			String[] param = {userID,itemID};
			String[] sequencerUdpInfo = getUdpInfo().get("SEQUENCER");
			result = sendUdpRequest("leaveItemWaitList", param, sequencerUdpInfo);
			return result;
		} catch (Exception e) {
			return ("front end response = " + e.getMessage());
		}
	}
	
	
	@Override
	public String findItem(String userID, String itemName){
		try {
			String request, result = "";
			//-- send UDP findItem request to the sequencer
			String[] param = {userID,itemName};
			String[] sequencerUdpInfo = getUdpInfo().get("SEQUENCER");
			result = sendUdpRequest("findItem", param, sequencerUdpInfo);
			return result;
		} catch (Exception e) {
			return ("front end response = " + e.getMessage());
		}
	}
	
	
	
	@Override
	public String returnItem(String userID, String itemID){
		try {
			String request, result = "";
			//-- send UDP returnItem request to the sequencer
			String[] param = {userID,itemID};
			String[] sequencerUdpInfo = getUdpInfo().get("SEQUENCER");
			result = sendUdpRequest("returnItem", param, sequencerUdpInfo);
			return result;
		} catch (Exception e) {
			return ("front end response = " + e.getMessage());
		}
	}
	
		
	@Override
	public String exchangeItem(String userID, String newItemID, String oldItemID) {
		try {
			String request, result = "";
			//-- send UDP exchangeItem request to the sequencer
			String[] param = {userID,newItemID,oldItemID};
			String[] sequencerUdpInfo = getUdpInfo().get("SEQUENCER");
			result = sendUdpRequest("exchangeItem", param, sequencerUdpInfo);
			return result;
		} catch (Exception e) {
			return ("front end response = " + e.getMessage());
		}
	}
	
	
	@Override
	public void shutDown() {
		orb.shutdown(false);		
	}
	
		

	/**
	 * this method is responsible for sending a UDP request
	 * @param req - request to be send
	 * @param reqParam - request parameters to be send
	 * @param receiverInfo - Internet address and port number of the receiver
	 */
	public String sendUdpRequest(String req, String[] reqParam, String[] receiverInfo) {
		this.udpSocket = null;
		try {	
		    this.udpSocket = new DatagramSocket();
		    
			String udpRequest = "send udp " + req + " request with these params " + Arrays.toString(reqParam)+ " to <" + receiverInfo[0] + ":" + receiverInfo[1] + ">";
			getFrontEndLogger().log((reqParam[0] + " sent udp request : " + udpRequest), true);
		    
			dataAdaptor.setRequest(req);
			dataAdaptor.setRequestParam(reqParam);
		    byte [] m = (dataAdaptor.frontEndUdpMessagePacking()).getBytes();
			DatagramPacket request = new DatagramPacket(m,  m.length, InetAddress.getByName(receiverInfo[0]), Integer.parseInt(receiverInfo[1]));
			
			// send multiple udp message to the sequencer
			this.serversResponse.clear();
			this.sentTime = System.currentTimeMillis();
			this.udpSocket.send(request);
			this.udpSocket.send(request);
			this.udpSocket.send(request);
			
			// listen for replicas response
			runUdpListener();
			runCheckUdpTimeOut();
			return getUdpResponse();
			
		}catch (SocketException e){
			System.out.println(e.getMessage());
			return null;
		}catch (IOException e){
			System.out.println(e.getMessage());
			return null;
		} 
	}
	
	
	/**
	 * this method is responsible to setup a UDP listener for this server 
	 */
	private void listenToUdpSocket() {
		try {

			String reqResponse = "";
			while (true) {
				byte[] requestBuffer = new byte[10000];
				DatagramPacket response = new DatagramPacket(requestBuffer, requestBuffer.length);
				this.udpSocket.receive(response);	
				reqResponse = new String(response.getData()).substring(0, response.getLength()+1);
				this.serversResponse.add(reqResponse+"#"+response.getAddress()+"#"+response.getPort());
				getFrontEndLogger().log((dataAdaptor.getRequestParam()[0] + " received udp response : " + reqResponse + " from <" + response.getAddress() + ":" + response.getPort() + ">"), true);
			}
			
		}catch (SocketException e){
			System.out.println("Socket: " + e.getMessage());
		}catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		}finally {
			if(this.udpSocket != null) this.udpSocket.close();}
	}
	
	/**
	 * this method is responsible for run UDP listener in concurrent thread
	 */
	private void runUdpListener() {
		try {
			Runnable udpListenerR = () -> {listenToUdpSocket();};
			Thread udpListenerT = new Thread(udpListenerR);
			udpListenerT.start();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	private void checkUDPTimeOut() {
		try {
			InterruptableUDPThread iUDPSocket = new InterruptableUDPThread(this.udpSocket);
			while (System.currentTimeMillis() - this.sentTime <= this.udpTransmissionTime) {}
			iUDPSocket.interrupt();
			// check available failure after timeout session
			detectFailure();
		}catch (Exception e){

		}
	}
	
	private void runCheckUdpTimeOut() {
		try {
			Runnable udpTimeoutR = () -> {checkUDPTimeOut();};
			Thread udpTimeoutT = new Thread(udpTimeoutR);
			udpTimeoutT.start();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	/**
	 * this method is responsible for catching proper response based on the failure type
	 * @return the final response
	 */
	private String getUdpResponse() {
		try {
            String response = "";
            
			if(this.faultMode.equalsIgnoreCase("CF")) {
				// wait for first server response and return it			
				while(String.valueOf(this.serversResponse.size()).equalsIgnoreCase("0")) {}
				return (this.serversResponse.get(0).split("#")[0]).split("_")[1];
			}
			else {

				int succeedCount = 0;
				int failedCount = 0;
				int waitCount = 0;
				while(succeedCount != 2 && failedCount != 2 && waitCount != 2 ) {
					succeedCount = 0;
					failedCount = 0;
					waitCount = 0;
					getFrontEndLogger().log(":::" + this.serversResponse.size(),false) ;
					for(String serverResp : this.serversResponse) {
						if(serverResp.contains("[succeed]")) {
							succeedCount ++;
							if(succeedCount == 2) {
								return (serverResp.split("#")[0]).split("_")[1];
							}
						}
						else if(serverResp.contains("[failed]")){
							failedCount ++;
							if(failedCount == 2) {
								return (serverResp.split("#")[0]).split("_")[1];
							}
						}
						else {
							waitCount ++;
							if(waitCount == 2) {
								return (serverResp.split("#")[0]).split("_")[1];
							}
						}
					}
					
				}
			    
			    return "???";		
			}

	
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return "";
		}
	}
	
	
	
	
	
	/**
	 * this method is responsible for informing RMs about possible failure based on the failure type
	 */
	private void detectFailure() {
		try {
			boolean failure = false;
			String failureMessage = "";
			if(this.faultMode.equalsIgnoreCase("CF")) {
				// if crash failure happen
				if(this.serversResponse.size() != 3) {					 
					String replicaName = "RM1RM2RM3";
					for(String resp : this.serversResponse) {
						for (String key: this.udpInfo.keySet()){							
		  					if((key.equalsIgnoreCase("RM1") || key.equalsIgnoreCase("RM2") || key.equalsIgnoreCase("RM3")) && resp.split("#")[1].contains(this.udpInfo.get(key)[0])) {
		  						replicaName = replicaName.replaceAll(key, "");
		  					}
		  				}
					}

					String serverName = "";
					for (String key: this.udpInfo.keySet()){
	  					if((key.equalsIgnoreCase("CON") || key.equalsIgnoreCase("MCG") || key.equalsIgnoreCase("MON")) && this.udpInfo.get(key)[1].equalsIgnoreCase(this.serversResponse.get(0).split("#")[2])) {
	  						serverName = key;
	  					}
	  				}					
					
					failure = true;
					failureMessage = replicaName + "_"+ serverName +"_[cf]";
					getFrontEndLogger().log(" crash failure happened for : " + replicaName + " " + serverName + " server", true);
				}
				
			}
			else {
				// if software failure happen make proper udp message to inform RMs
                ArrayList<String> succeedResponsesList = new ArrayList<String>();
                ArrayList<String> failedResponsesList = new ArrayList<String>();
                ArrayList<String> waitResponsesList = new ArrayList<String>();
                String softwareFailureResponse = "";
                
                for(String resp : this.serversResponse) {
                	if(resp.contains("[succeed]")) {
                		succeedResponsesList.add(resp);
                	}
                	else if(resp.contains("[failed]")) {
                		failedResponsesList.add(resp);
                	}
                	else {
                		waitResponsesList.add(resp);
                	}
                }
                
                if(succeedResponsesList.size() == 1) {
                	softwareFailureResponse = succeedResponsesList.get(0);
                }
                else if(failedResponsesList.size() == 1) {
                	softwareFailureResponse = failedResponsesList.get(0);
                }
                else if(waitResponsesList.size() == 1) {
                	softwareFailureResponse = waitResponsesList.get(0);
                }
                
                if(!softwareFailureResponse.equalsIgnoreCase("")) {             	
                	String replicaName = "";
                	String serverName = "";
					for (String key: this.udpInfo.keySet()){							
	  					if((key.equalsIgnoreCase("RM1") || key.equalsIgnoreCase("RM2") || key.equalsIgnoreCase("RM3")) && softwareFailureResponse.split("#")[1].contains(this.udpInfo.get(key)[0])) {
	  						replicaName = key;
	  					}
	  					if((key.equalsIgnoreCase("CON") || key.equalsIgnoreCase("MCG") || key.equalsIgnoreCase("MON")) && softwareFailureResponse.split("#")[2].equalsIgnoreCase(this.udpInfo.get(key)[1])) {
	  						serverName = key;
	  					}
	  				}	
                	
                	failure = true;
                	failureMessage = replicaName + "_"+ serverName +"_[sf]";
                	getFrontEndLogger().log(" software failure happened for : " + replicaName + " " + serverName + " server", true);
                }
			}
			
			if(failure) {
				// multicast RMs about failure message
				faultId ++ ;
				multicastRMs(String.valueOf(faultId)+"_"+failureMessage);
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	
	

	public void multicastRMs(String failureMessage) {
		try {	
			this.udpSocket = new DatagramSocket();
			String udpRequest = "send failure udp message to < Replica Managers>";
			getFrontEndLogger().log(udpRequest, true);
		    
		    byte [] m = (failureMessage).getBytes();
		    DatagramPacket request;
		    for(int i = 0 ; i < 3 ; i++) {
		    	//-- send to RM1
		    	if(this.faultMode.equalsIgnoreCase("CF") || (this.faultMode.equalsIgnoreCase("SF") && failureMessage.split("_")[1].equalsIgnoreCase("RM1"))  || (failureMessage.contains("[crash]"))) {
		    		request = new DatagramPacket(m,  m.length, InetAddress.getByName(this.udpInfo.get("RM1")[0]), Integer.parseInt(this.udpInfo.get("RM1")[1]));
					this.udpSocket.send(request);
		    	}
				
			    //-- send to RM2
		    	if(this.faultMode.equalsIgnoreCase("CF") || (this.faultMode.equalsIgnoreCase("SF") && failureMessage.split("_")[1].equalsIgnoreCase("RM2"))) {
		    		request = new DatagramPacket(m,  m.length, InetAddress.getByName(this.udpInfo.get("RM2")[0]), Integer.parseInt(this.udpInfo.get("RM2")[1]));
					this.udpSocket.send(request);
		    	}
				
				//-- send to RM3
		    	if(this.faultMode.equalsIgnoreCase("CF") || (this.faultMode.equalsIgnoreCase("SF") && failureMessage.split("_")[1].equalsIgnoreCase("RM3"))) {
		    		request = new DatagramPacket(m,  m.length, InetAddress.getByName(this.udpInfo.get("RM3")[0]), Integer.parseInt(this.udpInfo.get("RM3")[1]));
					this.udpSocket.send(request);
		    	}
		    }
			
			
		}catch (SocketException e){
			System.out.println(e.getMessage());
		}catch (IOException e){
			System.out.println(e.getMessage());
		} 
	}
	
	
	
	

	
					
    //------------------------------------------ methods
	
	
	//-- inner class	
	public class InterruptableUDPThread extends Thread{

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
  	//------------------------------------------ inner class

	
	//-- fields accessor
	
	public HashMap<String,String[]> getUdpInfo() {
		return udpInfo;
	}
	
	public void setUdpInfo(HashMap<String,String[]> libServersUdpInfo) {
		this.udpInfo = libServersUdpInfo;
	}
	
	public DlmsLogger getFrontEndLogger() {
		return frontEndLogger;
	}

	public void setFrontEndLogger(DlmsLogger frontEndLogger) {
		this.frontEndLogger = frontEndLogger;
	}
	
	public ORB getOrb() {
		return orb;
	}

	public void setOrb(ORB orb) {
		this.orb = orb;
	}

	
	//------------------------------------------ fields accessor


}
