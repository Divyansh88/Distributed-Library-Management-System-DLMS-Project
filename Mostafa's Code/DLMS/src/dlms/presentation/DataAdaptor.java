package comp.dsd.dlms.presentation;

public class DataAdaptor {
	//-- fields
		private String sequenceID;
		private String request;
		private String fEUdpInetAddress;
		private String fEUdpPort;
		private String[] requestParam;
	    private String[] messageSegment;
		//------------------------------------------fields
		
		//constructor
		public DataAdaptor() {
			this.setSequenceID("");
			this.setRequest("");
			this.setRequestParam(null);
		}
		//------------------------------------------ constructor
		
		//--methods
		public void udpMessageUnPacking(String udpMessage) {
			try {			
				messageSegment = udpMessage.split("_");
				this.setSequenceID(messageSegment[0]);
				this.setFEUdpInetAddress(messageSegment[1].split(":")[0]);
				this.setFEUdpPort(messageSegment[1].split(":")[1]);
				this.setRequest(messageSegment[2]);
				this.setRequestParam(messageSegment[3].split("@"));
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
		public String udpMessagePacking() {
			try {
				String udpMessage = "";
				udpMessage = this.getSequenceID() + "_";
				udpMessage = udpMessage + this.getFEUdpInetAddress() + ":" + this.getFEUdpPort() + "_";
				udpMessage = udpMessage + this.getRequest() + "_"; 
				int i = 0;
				for(String s : this.getRequestParam()) {
					if(i > 0)
						udpMessage = udpMessage + "@";
					udpMessage = udpMessage + s ;
					i++;
				}
				udpMessage = udpMessage + "@_" ;
				return udpMessage;
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return null;
			}
		}
		
		public String frontEndUdpMessagePacking() {
			try {
				String udpMessage = "";
				udpMessage = this.getRequest() + "_"; 
				int i = 0;
				for(String s : this.getRequestParam()) {
					if(i > 0) {
						udpMessage = udpMessage + "@";
					}
					udpMessage = udpMessage + s ;
					i++;
				}
				udpMessage = udpMessage + "@_" ;
				return udpMessage;
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return null;
			}
		}
		
		public void frontEndudpMessageUnPacking(String udpMessage) {
			try {			
				messageSegment = udpMessage.split("_");
				this.setRequest(messageSegment[0]);
				this.setRequestParam(messageSegment[1].split("@"));
				
			} catch (Exception e) {
				System.out.println(e.getMessage());
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

		public String getSequenceID() {
			return sequenceID;
		}

		public void setSequenceID(String sequenceID) {
			this.sequenceID = sequenceID;
		}

		public String getFEUdpInetAddress() {
			return fEUdpInetAddress;
		}

		public void setFEUdpInetAddress(String fEUdpInetAddress) {
			this.fEUdpInetAddress = fEUdpInetAddress;
		}

		public String getFEUdpPort() {
			return fEUdpPort;
		}

		public void setFEUdpPort(String fEUdpPort) {
			this.fEUdpPort = fEUdpPort;
		}

	
		
		//-------------------------------------------fields accessor

		


		

}
