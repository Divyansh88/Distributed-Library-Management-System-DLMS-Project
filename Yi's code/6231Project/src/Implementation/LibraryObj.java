package Implementation;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;
import service.logService;

public class LibraryObj {
	private HashMap <String, book> booklist = new HashMap <String, book>();
	logService log;
	Semaphore mutex = new Semaphore(1);
	
	public LibraryObj(){
	}
	
	public LibraryObj(boolean sf){
	}
	
	public LibraryObj(String logName){
		// super( );
		this.log = new logService(logName);
	}
	
	public void popMap(HashMap <String, book> newBookList) {
		
		this.booklist = (HashMap<String, book>) newBookList.clone();
	}

	public synchronized String adItem(String managerID, String itemID, String itemName, int quantity, boolean sf) {
		try {
			// Add to existing record
			if(booklist.containsKey(itemID.trim())){
				book currentbook = booklist.get(itemID);
				String firstWaiteList = "";
				boolean flag = currentbook.waitListEmpty();
				currentbook.setQuantity(quantity);
				
				int currentQ = currentbook.getQuantity();
				while(!flag && currentQ > 0){
					firstWaiteList = currentbook.waitListFirst();
					currentbook.setBorrow(firstWaiteList);
					log.writeLog("Book is borrowed to: " + firstWaiteList +" " + itemID);
					flag = currentbook.waitListEmpty();
					currentQ = currentbook.getQuantity();
				}		
				if(sf){
					log.writeLog("Add item succeed!"+ managerID +" "+ itemID +" "+ itemName +" "+ quantity);	
					return "[succeed] Add item succeed! "+ managerID +" "+ itemID +" "+ itemName +" "+ quantity;
				}
				else{
					log.writeLog("Add item failed!"+ managerID +" "+ itemID +" "+ itemName +" "+ quantity);	
					return "[failed]";
				}
			}	
			
			// Add new record
			else{
				booklist.put(itemID, new book(itemName, quantity));
				log.writeLog("Add new record succeed! "+ managerID +" "+ itemID +" "+ itemName +" "+ quantity);
				if(sf){
					log.writeLog("[succeed] Add new record succeed! "+ managerID +" "+ itemID +" "+ itemName +" "+ quantity);	
					return "[succeed] Add new record succeed! "+ managerID +" "+ itemID +" "+ itemName +" "+ quantity;
				}
				else{
					log.writeLog("[failed]");	
					return "[failed]";
				}
			}		
		} catch (Exception e) {
			e.printStackTrace();
			log.writeLog("Add Item Fail!"+ managerID +" "+ itemID +" "+ itemName +" "+ quantity);
		}
		if(sf){
			return "[failed] Add Item Fail!"+ managerID +" "+ itemID +" "+ itemName +" "+ quantity;
		}
		else{	
			return "[succeed]";
		}	
	}

	public String removeItem(String managerID, String itemID, int quantity) {
		try {
			mutex.acquire();
			
			if(booklist.containsKey(itemID.trim())){
				book currentbook = booklist.get(itemID);
				int currentQuantity = currentbook.getQuantity();
				if(quantity > 0){
					if(quantity > currentQuantity){
						log.writeLog("[failed] Not enough quantity of book to remove "+managerID+" "+itemID+" "+"current quantity "+currentbook.getQuantity());
						return "[failed] Not enough quantity of book to remove "+managerID+" "+itemID+" "+"current quantity "+currentbook.getQuantity();
					}
					else{
						currentbook.setQuantity(0-quantity);
						log.writeLog("[succeed] remove item success! "+managerID+" "+itemID+" "+"current quantity "+currentbook.getQuantity());
						return "[succeed] remove item success! "+managerID+" "+itemID+" "+"current quantity "+currentbook.getQuantity();
					}	
				}
				else if(quantity == 0){
					log.writeLog("[succeed] entered quantity is 0, no deletion performed "+managerID+" "+itemID);
					return "[succeed] entered quantity is 0, no deletion performed "+managerID+" "+itemID;
				}
				else{
					booklist.remove(itemID);
					log.writeLog("[succeed] remove item success, and record is deleted "+managerID+" "+itemID);
					return "[succeed] remove item success, and record is deleted "+managerID+" "+itemID;
				}
			}
			else{
				log.writeLog("[failed] record does not exist, no deletion performed "+managerID+" "+itemID);
				return "[failed] record does not exist, no deletion performed "+managerID+" "+itemID;
			}
		}
		catch (InterruptedException e1) {
			log.writeLog(e1.getMessage());
			log.writeLog("remove item failure"+managerID+" "+itemID);
		}finally{
			mutex.release();
		}
		return "[failed] remove item failure"+managerID+" "+itemID;	
	}

	public String listItemAvailability(String managerID) {
		String items = "";
		
		try{
			for (String name: booklist.keySet()){
				String key = name.toString();
				book value = booklist.get(name); 
				String temp = key +" " + value.getName() +" "+ value.getQuantity();
				items = items + "\n" +temp;
			}
			log.writeLog("[succeed] list Item Availability Succeed! managerID: " + managerID);
			items += "[succeed]";
			
			return items;
		}
		catch (Exception e) {
			System.out.println(e);
			log.writeLog(" [failed] list Item Availability Fail! managerID: " + managerID);
			items = "[failed]";	
			return items;
		}
	}

	public String borrowItem(String userID, String itemID, String days) {
		
		String library = userID.substring(0, 3);
		String bookLocation = itemID.substring(0, 3);	
		// Situation 1: if book is in the same library that user belongs to
		if(library.equals(itemID.substring(0, 3))){
			try{
				String result = this.borrow(userID, itemID, days);	
				return result;	
			}
			catch (Exception e) {
				System.out.println(e);
				log.writeLog("failure");
			} 
			
			return "[failed]";	
		} 		
		// Situation 2: if book is in the other libraries
		else{								
			try{
				int serverPort = 0;
				switch(bookLocation){
					case "CON": {
						serverPort = 1111;
						break;
					}
					case "MCG":{
						serverPort = 2222;
						break;
					}
					case "MON": {
						serverPort = 3333;
						break;
					}
				}
				String answer = sendMessage(serverPort, "borrowItem_" + userID +"_"+ itemID +"_"+ days);
				return answer;
			}
			catch (Exception e) {
				System.out.println(e);
			} 
			return "failure";
		}
	}
	
	public synchronized String borrow(String userID, String itemID, String days){
		try{
			String userLib = userID.substring(0, 3);
			String bookLib = itemID.substring(0, 3);
			if(!userLib.equals(bookLib)){
				for (String name: booklist.keySet()){
					String key = name.toString();
					book value = booklist.get(name); 
					if(value.checkBorrow(userID)){
						System.out.println("borrow list is:");
						value.printBorrowList();
						return "[failed] Not qualify to borrow";
					}
				}
			}
			// If record exists
			if(booklist.containsKey(itemID)){
				book currentbook = booklist.get(itemID);
				int quantity = booklist.get(itemID).getQuantity();
				if(currentbook.checkBorrow(userID.trim())) {
					log.writeLog("[failed] Item is already borrowed by the user."+ userID + itemID);
					return "[failed] Item is already borrowed by the user.";
				}
				else{
					if(quantity > 0){
						currentbook.setBorrow(userID);
						log.writeLog("[succeed] Borrow Item Succeed: " + userID +" " + itemID);
						return "[succeed] borrow succeed";
					}						
					else{
						log.writeLog("[wait] Borrow Item Not Available: " + userID +" " + itemID);
						return "[wait] NotAvailable";
					}	
				}
			}
			// If no record exists
			else{
				log.writeLog("[failed] No record exists."+ userID +" " + itemID);
				return "[failed] no record";
			}	
		}
		
		catch (Exception e) {
			System.out.println(e.getStackTrace());
			log.writeLog("[failed] Error "+ userID +" " + itemID + e);
		} 
		return "[failed] error";
		
	}

	public String addWaitQueue(String userID, String itemID) {
		String library = userID.substring(0, 3);
		String bookLocation = itemID.substring(0, 3);
		
		// Situation 1: if book is in the same library that user belongs to
		if(library.equals(itemID.substring(0, 3))){
			try{
				return this.reserve(userID, itemID);	
			}
			catch (Exception e) {
				System.out.println(e);
				log.writeLog("[failed]"+ userID + " " + itemID);
			} 
			return "[failed]";	
		} 		
		// Situation 2: if book is in the other libraries
		else{								
			try{
				int serverPort = 0;
				switch(bookLocation){
					case "CON": {
						serverPort = 1111;
						break;
					}
					case "MCG":{
						serverPort = 2222;
						break;
					}
					case "MON": {
						serverPort = 3333;
						break;
					}
				}
				String answer = sendMessage(serverPort, "reserveItem_" + userID +"_"+ itemID);	
				return answer;
			}
			catch (Exception e) {
				System.out.println(e);
			} 
			return "[failed]";
		}
	}
	
	public synchronized String reserve(String userID, String itemID){
		try{
			book currentbook = booklist.get(itemID);
			currentbook.addWaitlist(userID);
			// currentbook.printWaitlist();
			log.writeLog("[succeed] reserve succeed: "+ userID + " " + itemID);
			return "[succeed] reserve succeed";
		}
		catch (Exception e) {
			System.out.println(e);
			log.writeLog("[failed] "+ userID + " " + itemID);
		} 
		return "[failed]";
	}
	
	public String cancelReserve(String userID, String itemID) {
		String library = userID.substring(0, 3);
		String bookLocation = itemID.substring(0, 3);
		if(library.equals(itemID.substring(0, 3))){
			try{
				return this.cancelR(userID, itemID);	
			}
			catch (Exception e) {
				System.out.println(e);
				log.writeLog("[failed]"+ userID + " " + itemID);
			} 
			return "[failed]";	
		} 	
		else{								
			try{
				int serverPort = 0;
				switch(bookLocation){
					case "CON": {
						serverPort = 1111;
						break;
					}
					case "MCG":{
						serverPort = 2222;
						break;
					}
					case "MON": {
						serverPort = 3333;
						break;
					}
				}
				String answer = sendMessage(serverPort, "cancelReserve_" + userID +"_"+ itemID);	
				return answer;
			}
			catch (Exception e) {
				System.out.println(e);
			} 
			return "[failed]";
		}
	}
	
	public String cancelR(String userID, String itemID){
		try{
			book currentbook = booklist.get(itemID);
			currentbook.removeWaitlist(userID);

			log.writeLog("[succeed] Cancel reserve succeed: "+ userID + " " + itemID);
			return "[succeed] Cancel reserve succeed";
		}
		catch (Exception e) {
			System.out.println(e);
			log.writeLog("[failed]: "+ userID + " " + itemID);
		} 
		return "[failed] Cancel reserve fail";
	}

	public String findItem(String userID, String itemName) {
		
		String books = "";
		// get local books
		try{
			for (String s: booklist.keySet()){
				String key = s.toString();
				book value = booklist.get(s); 
				String bookName = value.getName();
				int quantity = value.getQuantity();
				if(bookName.equals(itemName))
					books = books + "_" + key + " " + quantity;		
			}
		}
		catch (Exception e) {
			e.getStackTrace();
			books = "[failed]";
		} 
		//get other libraries' books
		try{
			String answer1 = null;
			String answer2 = null;
			String currentLibrary = userID.substring(0, 3);
			switch(currentLibrary){
			case "CON": {
				answer1 = sendMessage(2222, "findItem_" + userID +"_"+ itemName);
				answer2 = sendMessage(3333, "findItem_" + userID +"_"+ itemName);
				break;
			}
			case "MCG": {
				answer1 = sendMessage(1111, "findItem_" + userID +"_"+ itemName);
				answer2 = sendMessage(3333, "findItem_" + userID +"_"+ itemName);
				break;
			}
			case "MON": {
				answer1 = sendMessage(1111, "findItem_" + userID +"_"+ itemName);
				answer2 = sendMessage(2222, "findItem_" + userID +"_"+ itemName);
				break;	
			}
			}	
			
			if(!answer1.trim().equals("[failed]"))
				books = books + answer1 + "\n";
			if(!answer2.trim().equals("[failed]"))
				books = books + answer2;
			
			if(!books.contains("[failed])")) {
				books += "[succeed]";
			}
			
		}
		catch (Exception e) {
			e.getStackTrace();
			books = "[failed]";
		} 
		return books;
	}
	
	public String find(String userID, String itemName){
		String books ="";
		try{
			for (String s: booklist.keySet()){
				String key = s.toString();
				book value = booklist.get(s); 
				String bookName = value.getName();
				int quantity = value.getQuantity();
				if(bookName.equals(itemName))
					books = books + "\n" + key+ " " + quantity;	
			}
			if(books.length() == 0){
				books = "[failed]";
			}
		}
		catch (Exception e) {
			e.getStackTrace();
			books = "[failed]";
		} 
		return books;
	}

	public String returnItem(String userID, String itemID) {
		String library = userID.substring(0, 3);
		String bookLocation = itemID.substring(0, 3);	
		// Situation 1: if book is in the same library that user belongs to
		if(library.equals(bookLocation)){
			try{
				return this.returnBook(userID, itemID);
			}
			catch (Exception e) {
				System.out.println(e);
				log.writeLog("[failed]" + userID + itemID);
			} 
			return "[failed]";
		} 		
		// Situation 2: if book is in the other libraries
		else{								
			try{
				int serverPort = 0;
				switch(bookLocation){
					case "CON": {
						serverPort = 1111;
						break;
					}
					case "MCG": {
						serverPort = 2222;
						break;
					}
					case "MON": {
						serverPort = 3333;
						break;
					}
				}
				String answer = sendMessage(serverPort, "returnItem_ " + userID +"_"+ itemID);	
				return answer;
			}
			catch (Exception e) {
				System.out.println(e);
			} 
			return "[failed]";
		}
	}
	
	public synchronized String returnBook(String userID, String itemID) {
		try{
			// If record exists
			if(booklist.containsKey(itemID.trim())){
				
				book currentbook = booklist.get(itemID);
				String firstWaiteList = "";
				boolean flag = currentbook.waitListEmpty();
				if(!flag){
					firstWaiteList = currentbook.waitListFirst();
				}
				if(currentbook.checkBorrow(userID.trim())) {
					currentbook.setReturn(userID.trim());
					log.writeLog("[succeed] Return Item Succeed: " + userID +" " + itemID);
					if(!firstWaiteList.equals("")){
						currentbook.setBorrow(firstWaiteList);
						log.writeLog("Book is borrowed to: " + firstWaiteList +" " + itemID);
					}
					return "[succeed] Return Item Succeed";
				}
				else {
					log.writeLog("[failed] Item not borrowed by the user:" + userID +" " + itemID);
					return "[failed] Item not borrowed by the user";
				}
			}
			// If no record exists
			else{
				log.writeLog("[failed] No record exists.");
				return "[failed] No record exists.";
			}	
		}
		catch (Exception e) {
			System.out.println(e);
			log.writeLog("[failed]");
		} 
		return "[failed]";
	}
	
	public String holdItem(String userID, String itemID){
		String library = userID.substring(0, 3);
		String bookLocation = itemID.substring(0, 3);	
		// Situation 1: if book is in the same library that user belongs to
		if(library.equals(bookLocation)){
			try{
				return this.holdBook(userID, itemID);
			}
			catch (Exception e) {
				System.out.println(e);
				log.writeLog("[failed]" + userID + itemID);
			} 
			return "[failed]";
		} 		
		// Situation 2: if book is in the other libraries
		else{								
			try{
				int serverPort = 0;
				switch(bookLocation){
					case "CON": {
						serverPort = 1111;
						break;
					}
					case "MCG": {
						serverPort = 2222;
						break;
					}
					case "MON": {
						serverPort = 3333;
						break;
					}
				}
				String answer = sendMessage(serverPort, "holdItem_ " + userID +"_"+ itemID);	
				return answer;
			}
			catch (Exception e) {
				System.out.println(e);
			} 
			return "[failed]";
		}
	}
	
	public String holdBook(String userID, String itemID){
		try{
			// If record exists
			
			if(booklist.containsKey(itemID.trim())){
				book currentbook = booklist.get(itemID.trim());
				if(currentbook.checkBorrow(userID.trim())) {
					currentbook.hold(userID.trim());
					log.writeLog("[succeed] Hold Item Succeed: " + userID +" " + itemID);
					return "[succeed]";
				}
				else {
					log.writeLog("[failed] Item not borrowed by the user:" + userID +" " + itemID);
					return "[failed] Item not borrowed by the user";
				}
			}
			// If no record exists
			else{
				log.writeLog("[failed] No record exists.");
				return "[failed] No record exists.";
			}	
		}
		catch (Exception e) {
			System.out.println(e);
			log.writeLog("[failed]");
		} 
		return "[failed]";
	}
	
	public String RholdItem(String userID, String itemID){
		String library = userID.substring(0, 3);
		String bookLocation = itemID.substring(0, 3);	
		// Situation 1: if book is in the same library that user belongs to
		if(library.equals(bookLocation)){
			try{
				return this.RholdBook(userID, itemID);
			}
			catch (Exception e) {
				System.out.println(e);
				log.writeLog("[failed]" + userID + itemID);
			} 
			return "[failed]";
		} 		
		// Situation 2: if book is in the other libraries
		else{								
			try{
				int serverPort = 0;
				switch(bookLocation){
					case "CON": {
						serverPort = 1111;
						break;
					}
					case "MCG": {
						serverPort = 2222;
						break;
					}
					case "MON": {
						serverPort = 3333;
						break;
					}
				}
				String answer = sendMessage(serverPort, "RholdItem_ " + userID +"_"+ itemID);	
				return answer;
			}
			catch (Exception e) {
				System.out.println(e);
			} 
			return "[failed]";
		}
	}
	
	public String RholdBook(String userID, String itemID){
		try{
			// If record exists
			if(booklist.containsKey(itemID.trim())){
				book currentbook = booklist.get(itemID.trim());
				currentbook.Rhold(userID.trim());
				log.writeLog("[succeed] RHold Item Succeed: " + userID +" " + itemID);
				return "[succeed]";
			}
			// If no record exists
			else{
				log.writeLog("[failed] No record exists.");
				return "[failed] No record exists.";
			}	
		}
		catch (Exception e) {
			System.out.println(e);
			log.writeLog("[failed]");
		} 
		return "[failed]";
	}
	
	public synchronized String exchangeItem(String studentID, String newItemID, String oldItemID) {

		String borrowAnswer="";
		String returnAnswer="";
		String holdAnswer="";
        boolean borrowAction = false;
        boolean returnAction = false;

    	try {
    		holdAnswer = this.holdItem(studentID, oldItemID);
    
    		if(holdAnswer.contains("[succeed]")){
    			borrowAnswer = this.borrowItem (studentID, newItemID, "10");
    		
    		}
    		if(borrowAnswer.contains("[succeed]")){
    			borrowAction = true;
    		}
    		this.RholdItem(studentID, oldItemID);
    		if(borrowAction){
    			returnAnswer = this.returnItem(studentID, oldItemID);
    			if(returnAnswer.contains("[succeed]")){
        			returnAction = true;
        		}
    		}
        } catch (Exception e) {
        	e.printStackTrace();
        } finally { 
            if (! (borrowAction && returnAction)) {
                if (borrowAction) {
                	this.returnItem(studentID, newItemID);  
                    this.log.writeLog("[failed] Exchange Item Failure " + studentID +" "+ newItemID+" "+oldItemID);
        			return "[failed] ExchangeItem Failure";
                }
                this.log.writeLog("[failed] Exchange Item Failure " + studentID +" "+ newItemID+" "+oldItemID);
                return "[failed] ExchangeItem Failure";
            }
        }
    	this.log.writeLog("[succeed] Exchange Item Succeed " + studentID +" "+ newItemID+" "+oldItemID);
		return "[succeed] ExchangeItem Succeed";
	}
	
	private String sendMessage(int serverPort, String input) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] message = input.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, input.length(), aHost, serverPort);
			aSocket.send(request);
			
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);			
			aSocket.receive(reply);
			return new String(reply.getData(),
					  request.getOffset(),
				      request.getLength(), "UTF-8");		
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
		return "[failed]";
	}


	
}

