package comp.dsd.dlms.presentation;

import java.util.Arrays;
import java.util.Iterator;

public class DataPresentation {
	
	//-- fields
	private String userId;
	private String request;
	private String[] requestParam;
	private String[] reply;
	private String[] messageSegment;
	//------------------------------------------fields
	
	//constructor
	public DataPresentation() {
		this.setRequest("");
		this.setRequestParam(null);
		this.setReply(null);
	}
	//------------------------------------------ constructor
	
	//--methods
	public void udpMessageUnPacking(String udpMessage) {
		try {			
			messageSegment = udpMessage.split("&");
			this.setRequest(messageSegment[0]);
			this.setRequestParam(messageSegment[1].split("@"));
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public String udpMessagePacking() {
		try {
			String udpMessage = "";
			udpMessage = this.getRequest() + "&"; 
			int i = 0;
			for(String s : this.getRequestParam()) {
				if(i > 0)
					udpMessage = udpMessage + "@";
				udpMessage = udpMessage + s ;
				i++;
			}
			udpMessage = udpMessage + "&" ;
      
			return udpMessage;
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}	
	//------------------------------------------methods
	
	
    //-- fields accessor

	public String getRequest() {
		return request;
	}

	public void setRequest(String command) {
		this.request = command;
	}


	public String[] getRequestParam() {
		return requestParam;
	}

	public void setRequestParam(String[] commandParam) {
		this.requestParam = commandParam;
	}

	public String[] getReply() {
		return reply;
	}

	public void setReply(String[] replyData) {
		this.reply = replyData;
	}
	
	public String[] getMessageSegment() {
		return messageSegment;
	}

	public void setMessageSegment(String[] messageSegment) {
		this.messageSegment = messageSegment;
	}
	//-------------------------------------------fields accessor

	
	

	
	
}
