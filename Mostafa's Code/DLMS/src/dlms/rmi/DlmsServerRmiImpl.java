package comp.dsd.dlms.rmi;

import java.beans.FeatureDescriptor;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

import comp.dsd.dlms.logger.DlmsLogger;
import comp.dsd.dlms.model.BorrowList;
import comp.dsd.dlms.model.Borrower;
import comp.dsd.dlms.model.Item;
import comp.dsd.dlms.model.ItemList;
import comp.dsd.dlms.model.WaitList;
import comp.dsd.dlms.presentation.DataPresentation;
import comp.dsd.dlms.server.DlmsServer;


public class DlmsServerRmiImpl extends UnicastRemoteObject implements DlmsServerRmiIntreface{
	//-- fields
    private String name;
    private DlmsLogger serverLogger;
    private String inetAddress;
    private int port;
    private HashMap<String,String[]> libServersUdpInfo;
    ItemList itemList;
    WaitList waitList;
    BorrowList borrowList;  
    DataPresentation dataPresentation;
    //------------------------------------------ fields
    
    
    //-- constructors    
    public DlmsServerRmiImpl(String name, String inetAddress, int port, HashMap<String,String[]> libServersUdpInfo, DlmsLogger serverLogger) throws RemoteException{
    	this.setName(name);
    	this.setPort(port);   
    	this.setInetAddress(inetAddress);
    	this.setLibServersUdpInfo(libServersUdpInfo);
    	this.setServerLogger(serverLogger); 
    	itemList = new ItemList(this.getName(),serverLogger);
    	waitList = new WaitList(this.getName(),serverLogger);
    	borrowList = new BorrowList(this.getName(),serverLogger);
    	dataPresentation = new DataPresentation(); 
    	
    	runUdpListener();
    }
    //------------------------------------------ constructors
    
	//-- methods
	public String addItem(String managerID, String itemID, String itemName, int quantity) throws RemoteException {
		try {
			String request, result;
			result = clientValidation(managerID, "addItem");
			if(result.equalsIgnoreCase("")) {
				request = "increase the quantity of item with id (" + itemID + ") and name (" + itemName + ") by " + quantity;
				getServerLogger().log((managerID + " request = " + request), true);
				
				Item item = itemList.get(itemID);
				if(item != null) {
					item.addQuantity(quantity);
					
					Queue<Borrower> waitUsers = waitList.get(itemID); // check waiting list
					if(waitUsers != null) {
						ArrayList<Borrower> borrowers = borrowList.get(itemID);
						if(borrowers == null ) {
							borrowers = new ArrayList<Borrower>();
							borrowList.put(itemID, borrowers); 
						}							
						while(waitUsers.size() > 0 && item.getQuantity() > 0) {
							Borrower newBorrower = waitUsers.poll();							
							borrowers.add(newBorrower);
							item.setQuantity(item.getQuantity()-1);
							if(waitUsers.size() == 0) { 
								waitList.remove(itemID);
							}
						}
						
					}
					
					result = "[succeed] the quantity of item with id (" + itemID + ") and name (" + itemName + ") increased by " + quantity + ". Total quantity is " + itemList.get(itemID).getQuantity();
					getServerLogger().log((managerID + " request result = " + result), true);
					return (this.getName() + " server response = " + result);
				}
				else {
					item = new Item(itemName, quantity);
					itemList.put(itemID, item);
					result = "[succeed] the new item with id (" + itemID + ") and name (" + itemName + ") is created with total quantity of " + quantity;
					getServerLogger().log((managerID + " request result = " + result), true);
					return (this.getName() + " server response = " + result);
				}
				
			}
			else				
				return result;
				
		} catch (Exception e) {
			System.out.println(e.getMessage());
			String result = "[failed!] the quantity of item with id (" + itemID + ") and name (" + itemName + ") could not increased by " + quantity + ". Total quantity is " + itemList.get(itemID).getQuantity();
			getServerLogger().log((managerID + " request result = " + result), true);
			return (this.getName() + " server response = " + result);
		}
		
	}

	public String removeItem(String managerID, String itemID, int quantity) throws RemoteException {
		try {
			String request, result;
			result = clientValidation(managerID, "removeItem");
			if(result.equalsIgnoreCase("")) {
				request = "remove or decrease the quantity of item with id (" + itemID + ") by " + quantity;
				getServerLogger().log((managerID + " request = " + request), true);
				
				Item item = itemList.get(itemID);
				if(item != null) {
					if(quantity != -1) { //-- reduction
						if(item.getQuantity() >= quantity) {
							item.decreaseQuantity(quantity);
							result = "[succeed] the quantity of item with id (" + itemID + ") successfully decreased by " + quantity + ". Total quantity is " + itemList.get(itemID).getQuantity();
							getServerLogger().log((managerID + " request result = " + result), true);
							return (this.getName() + " server response = " + result);
						}
						else {
							result = "[failed!] the quantity of item with id (" + itemID + ") is " + item.getQuantity() + " which is less than desired decreased amount " + quantity ;
							getServerLogger().log((managerID + " request result = " + result), true);
							return (this.getName() + " server response = " + result);
						}
					}
					else {//-- deletion
						itemList.remove(itemID);
						borrowList.remove(itemID);
						waitList.remove(itemID);
						result = "[succeed] the item with id (" + itemID + ") deleted";
						getServerLogger().log((managerID + " request result = " + result), true);
						return (this.getName() + " server response = " + result);
					}
				}
				else {
					result = "[failed!] the item with id (" + itemID + ") is not found";
					getServerLogger().log((managerID + " request result = " + result), true);
					return (this.getName() + " server response = " + result);
				}
			}
			else				
				return result;
				
		} catch (Exception e) {
			System.out.println(e.getMessage());
			String result = "[failed!] the quantity of item with id ("+ itemID +") can not decreased by ("+ quantity +") or deleted";
			getServerLogger().log((managerID + " request result = " + result), true);
			return (this.getName() + " server response = " + result);
		}
		
	}

	public String listItemAvailability(String managerID) throws RemoteException {
		try {
			String request, result;
			result = clientValidation(managerID, "listItemAvailability");
			if(result.equalsIgnoreCase("")) {
				request = "provide liste of all items availability" ;
				getServerLogger().log((managerID + " request = " + request), true);
				
				String line = "";
				for (String key: itemList.keySet()){
					Item item = itemList.get(key);
					line = line + key + "," + item.getName() + "," + item.getQuantity() + "\n";
				}
					
				result = "[succeed] list of all items availability is provided";
				getServerLogger().log((managerID + " request result = " + result), true);
				return (this.getName() + " server response = " + result + ": \n" + line);					
			}
			else				
				return result;
				
		
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
		
	}

	public String borrowItem(String userID, String itemID, int numberOfDays) throws RemoteException {
		try {
			String result;
			result = clientValidation(userID, "borrowItem");
			if(result.equalsIgnoreCase("")) {
				return borrowItemCore(userID, itemID, numberOfDays);
			}
			else
				return result;
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	/**
	 * this is the core method for borrowing an item 
	 * @param userID - Id of the user that wants to borrow the book
	 * @param itemID - Id of the item to be borrowed
	 * @param numberOfDays - number of days that item will be borrowed
	 * @return - result of borrowing an item
	 */
	private String borrowItemCore(String userID, String itemID, int numberOfDays){
		try {
			String request, result;
			request = "borrow item with id (" + itemID + ") for the period of " + numberOfDays + " days";
			getServerLogger().log((userID + " request = " + request), true);
			
			//-- check if desired item belong to other library send request to that library and return the result
			String itemLibName = itemID.substring(0, 3);
			if(itemLibName.equalsIgnoreCase(this.getName())) {
				
				//-- check borrow limitation if the user is not belong to this library
				String userLibName = userID.substring(0, 3);
				if(!userLibName.equalsIgnoreCase(this.getName()) && userHasRecord(userID)) {
					result = "[failed!] the users from other libraries could borrow our waiting of maxixmum one item";
					getServerLogger().log((userID + " request result = " + result), true);
					return (this.getName() + " server response = " + result); 
				}
				
				ArrayList<Borrower> borrowers = borrowList.get(itemID);
				// check if the user borrowed the book before
				if(borrowers != null) {
			    	for(Borrower b : borrowers) {
			    		if(b.getUserId().equalsIgnoreCase(userID)) {
			    			result = "[failed!] the users has borrowed the book before";
							getServerLogger().log((userID + " request result = " + result), true);
							return (this.getName() + " server response = " + result);
			    		}
			    	}
				}
				
				//-- check if desired item is available 
				Item item = itemList.get(itemID);
				if(item != null) {
					if(item.getQuantity() > 0) {			
					    if(borrowers == null) {
					    	borrowers = new ArrayList<Borrower>();
					    	borrowers.add(new Borrower(userID,numberOfDays));
					    	borrowList.put(itemID, borrowers);
					    }
					    else {					    	
					    	borrowers.add(new Borrower(userID,numberOfDays));
					    }
					    
					    item.setQuantity(item.getQuantity()-1);
						result = "[succeed] the item with id (" + itemID + ") successfully borrowed";
						getServerLogger().log((userID + " request result = " + result), true);
						return (this.getName() + " server response = " + result);
					    	
					}
					else { // ask wants to be added to wait list
						result = "[failed!] the item with id (" + itemID + ") is not available now to be borrowed";
						getServerLogger().log((userID + " request result = " + result), true);
					    return (this.getName() + " server response = " + result + ". However you can send waiting request to be placed in waiting list.");						
					}
				}
				else {// item not found in the library
					result = "[failed!] the item with id (" + itemID + ") is not found";
					getServerLogger().log((userID + " request result = " + result), true);
					return (this.getName() + " server response = " + result);
				}
			}
			else {// the item is belong to other library
				String[] param = {userID,itemID, String.valueOf(numberOfDays)};
				String[] otherLibUdpInfo =getLibServersUdpInfo().get(itemLibName);
				result = sendUdpRequest("borrowItem", param, otherLibUdpInfo);					
				getServerLogger().log((userID + " request result = " + result), true);
				return (this.getName() + " server response = " + result);			
		   }
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public String joinToItemWaitList(String userID, String itemID, int numberOfDays) throws RemoteException {
		try {
			String result;
			result = clientValidation(userID, "joinToItemWaitList");
			if(result.equalsIgnoreCase("")) {
				return joinToItemWaitListCore(userID, itemID, numberOfDays);
			}
			else
				return result;
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	/**
	 * this is the core method for putting a user in item waiting list
	 * @param userID - Id of the user that wants to borrow the book
	 * @param itemID - Id of the item to be borrowed
	 * @param numberOfDays - number of days that item will be borrowed
	 * @return - result of waiting for an item
	 */
	private String joinToItemWaitListCore(String userID, String itemID, int numberOfDays){
		try {
			String request, result;
			request = "wait for item with id (" + itemID + ") for the period of " + numberOfDays + " days";
			getServerLogger().log((userID + " request = " + request), true);
			
			//-- check if desired item belong to other library send request to that library and return the result
			String itemLibName = itemID.substring(0, 3);
			if(itemLibName.equalsIgnoreCase(this.getName())) {
				
				//-- check borrow limitation if the user is not belong to this library
				String userLibName = userID.substring(0, 3);
				if(!userLibName.equalsIgnoreCase(this.getName()) && userHasRecord(userID)) {
					result = "[failed!] the users from other libraries could borrow our waiting of maxixmum one item";
					getServerLogger().log((userID + " request result = " + result), true);
					return (this.getName() + " server response = " + result); 
				}
				
				//-- check if desired item is available 
				Item item = itemList.get(itemID);
				if(item != null) {
					
					// check if the user borrowed the book before
					ArrayList<Borrower> borrowersList = borrowList.get(itemID);
			    	for(Borrower b : borrowersList) {
			    		if(b.getUserId().equalsIgnoreCase(userID)) {
			    			result = "[failed!] the users has borrowed the book before";
							getServerLogger().log((userID + " request result = " + result), true);
							return (this.getName() + " server response = " + result);
			    		}
			    	}
					
					if(item.getQuantity() == 0) {			
						Queue<Borrower> borrowers = waitList.get(itemID);
					    if(borrowers == null) {
					    	borrowers = new LinkedList<Borrower>();
					    	borrowers.add(new Borrower(userID,numberOfDays));
					    	waitList.put(itemID, borrowers);
					    }
					    else {
					    	borrowers.add(new Borrower(userID,numberOfDays));
					    }
					    
						result = "[succeed] user add to the waiting list of item with id (" + itemID + ").";
						getServerLogger().log((userID + " request result = " + result), true);
						return (this.getName() + " server response = " + result);
					    	
					}
					else { // inform the user of item availabilty
						result = "[failed!] the item with id (" + itemID + ") is available now and it is not possible to be placed in waiting list";
						getServerLogger().log((userID + " request result = " + result), true);
					    return (this.getName() + " server response = " + result + ". So you can sent borrowing request for  that item.");						
					}
				}
				else {// item not found in the library
					result = "[failed!] the item with id (" + itemID + ") is not found";
					getServerLogger().log((userID + " request result = " + result), true);
					return (this.getName() + " server response = " + result);
				}
			}
			else {// the item is belong to other library
				String[] param = {userID,itemID, String.valueOf(numberOfDays)};
				String[] otherLibUdpInfo =getLibServersUdpInfo().get(itemLibName);
				result = sendUdpRequest("joinToItemWaitList", param, otherLibUdpInfo);									
				getServerLogger().log((userID + " request result = " + result), true);
				return (this.getName() + " server response = " + result);			
		   }
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public String leaveItemWaitList(String userID, String itemID) throws RemoteException {
		try {
			String result;
			result = clientValidation(userID, "leaveItemWaitList");
			if(result.equalsIgnoreCase("")) {
				return leaveItemWaitListCore(userID, itemID);
			}
			else
				return result;
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	/**
	 * this is the core method for removing a user from item waiting list
	 * @param userID - Id of the user that wants to borrow the book
	 * @param itemID - Id of the item to be borrowed
	 * @return - result of waiting for an item
	 */
	private String leaveItemWaitListCore(String userID, String itemID){
		try {
			String request, result;
			request = "leave waiting list of item with id (" + itemID + ")";
			getServerLogger().log((userID + " request = " + request), true);
			
			//-- check if desired item belong to other library send request to that library and return the result
			String itemLibName = itemID.substring(0, 3);
			if(itemLibName.equalsIgnoreCase(this.getName())) {
				//-- check if user is in waiting list
				Queue<Borrower> waitingUser = waitList.get(itemID);
				boolean isWaiting = false;
				if(waitingUser != null) {
					for(Borrower b : waitingUser) {
						if(b.getUserId().equalsIgnoreCase(userID)) {
							isWaiting = true;
							waitingUser.remove(b);
							if(waitingUser.size() == 0) {
								waitList.remove(itemID);
							}

							break;
						}
					}
				}
				
				if(!isWaiting) {
					result = "[failed!] the user is not in waiting list for item with id (" + itemID + ")";
				}
				else {
					result = "[succeed] user removed from waiting list of item with id (" + itemID + ").";
				}					
				
				getServerLogger().log((userID + " request result = " + result), true);
				return (this.getName() + " server response = " + result);
			}
			else {// the item is belong to other library
				String[] param = {userID,itemID};
				String[] otherLibUdpInfo =getLibServersUdpInfo().get(itemLibName);
				result = sendUdpRequest("leaveItemWaitList", param, otherLibUdpInfo);					
				getServerLogger().log((userID + " request result = " + result), true);
				return (this.getName() + " server response = " + "result = " + result);				
		   }
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	public String findItem(String userID, String itemName) throws RemoteException {
		try {
			String request, result;
			result = clientValidation(userID, "findItem");
			if(result.equalsIgnoreCase("")) {
				request = "find items with the same name as (" + itemName + ") along with their quantity within all libraries";
				getServerLogger().log((userID + " request = " + request), true);
				String list =  findItemCore(userID, itemName);
				if(!list.equalsIgnoreCase("null"))
					result = "[succeed] the items with same name (" + itemName + ") in this library are " + list;
				else {
					result = "[succeed] there is no items with same name (" + itemName + ") in this library";
					list = "";
				}
				getServerLogger().log((userID + " request result = " + result), true);	
				
				// send concurrent udp request to other libraries to provide item list
				ArrayList<String[]> serversUdpInfo = new ArrayList<String[]>();
				int serverNo = 0;
				for(String key : getLibServersUdpInfo().keySet()) {
					if(!key.equalsIgnoreCase(this.getName())) {
						String[] updInfo = getLibServersUdpInfo().get(key);
						serversUdpInfo.add(new String[] {key,updInfo[0],updInfo[1],String.valueOf(serverNo)});
						serverNo++;
					}
				}
				
				// create thread and run them concurrently
				String[] param = {userID,itemName};
				ArrayList<FindItemThread> concurrentFindThreadList = new ArrayList<FindItemThread>();
				final CountDownLatch latch = new CountDownLatch(serverNo);
				final String[] findResult = new String[serverNo];
				for(String[] serverUdpInfo : serversUdpInfo) {					
					concurrentFindThreadList.add(new FindItemThread(param, serverUdpInfo, findResult, this, latch));
				}
				
			    // wait for threads to finish their task and join the main thread
				latch.await();
				
				// merge all servers finding result
				for(String r : findResult) {
					if(!r.contains("null")) {
						if(!list.equalsIgnoreCase(""))
							list = list + "\n";
						list = list + r;
					}
					
				}

				// format final result
				list = list.replaceAll("&", "\n");
				
				if(list.equalsIgnoreCase("")) {
					result = "[succeed] there is no items with same name (" + itemName + ")";
					getServerLogger().log((userID + " request result = " + result), true);					
					return (this.getName() + " server response = " + result);		
				}
				else {
					result = "[succeed] the list of items with the same name (" + itemName + ") is provided";
					getServerLogger().log((userID + " request result = " + result), true);
					return (this.getName() + " server response = " + result + "\n" + list);
				}

			}
			else
				return result;
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	/**
	 * this is the core method for returning all items in all libraries with the same name 
	 * @param userID - Id of the user that wants to borrow the book
	 * @param itemName - name of the item to be borrowed
	 * @return - result of founded item list
	 */
	private String findItemCore(String userID, String itemName){
		try {		    
			String foundedItemList = "";
			int i = 0;
			for (String key: itemList.keySet()){
				Item item = itemList.get(key);
				if(item.getName().equalsIgnoreCase(itemName)) {
					if(i>0)
						foundedItemList = foundedItemList + "&";
					foundedItemList = foundedItemList + key + "," + item.getQuantity();
					i++;
				}
				
			}
			if(i != 0)
				return foundedItemList;
			else
				return "null";
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return "";
		}
	}

	public String returnItem(String userID, String itemID) throws RemoteException {
		try {
			String result;
			result = clientValidation(userID, "returnItem");
			if(result.equalsIgnoreCase("")) {
				return returnItemCore(userID, itemID);
			}
			else
				return result;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	/**
	 * this is the core method for returning an item 
	 * @param userID - Id of the user that wants to borrow the book
	 * @param itemID - Id of the item to be borrowed
	 * @return - result of returning the item
	 */
	private String returnItemCore(String userID, String itemID){
		try {
			String request, result;
			request = "return the item with id (" + itemID + ")";
			getServerLogger().log((userID + " request = " + request), true);
		
			//-- check if desired item belong to other library then send request to that library and return the result
			String itemLibName = itemID.substring(0, 3);
			if(itemLibName.equalsIgnoreCase(this.getName())) {
				//-- check if desired item is borrowed
				ArrayList<Borrower> borrowers = borrowList.get(itemID);
				if(borrowers != null) {
					
					for(Borrower brw : borrowers) {
						if(brw.getUserId().equalsIgnoreCase(userID)) {
							borrowers.remove(brw); // remove from borrower list							
							Item item = itemList.get(itemID);
							item.setQuantity(item.getQuantity() + 1); // add item quantity
							Queue<Borrower> waitUsers = waitList.get(itemID); // check waiting list
							if(waitUsers != null) {
								Borrower newBorrower = waitUsers.poll();
								if(waitUsers.size() == 0) 
									waitList.remove(itemID);
							    borrowers.add(newBorrower);
							    item.setQuantity(item.getQuantity()-1);
							}
							if(borrowers.size() == 0) 
								borrowList.remove(itemID);
							
							result = "[succeed] user return the item with id (" + itemID + ")";
							getServerLogger().log((userID + " request result = " + result), true);
							return (this.getName() + " server response = " + result);
						}
					}
					
					// the userId is not borrow the item
					result = "[failed!] user does not borrow the item with id (" + itemID + ")";
					getServerLogger().log((userID + " request result = " + result), true);
					return (this.getName() + " server response = " + result);
					
				}
				else {// item is not borrowed
					result = "[failed!] the item with id (" + itemID + ") is not borrowed yet";
					getServerLogger().log((userID + " request result = " + result), true);
					return (this.getName() + " server response = " + result);
				}
			}
			else {// the item is belong to other library				
				String[] param = {userID,itemID};
				String[] otherLibUdpInfo =getLibServersUdpInfo().get(itemLibName);	
				result = sendUdpRequest("returnItem", param, otherLibUdpInfo);					
				getServerLogger().log((userID + " request result = " + result), true);
				return (this.getName() + " server response = " + result);
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	

	public String exchangeItem(String userID, String newItemID, String oldItemID) throws RemoteException {
		try {
			String result;
			result = clientValidation(userID, "exchangeItem");
			if(result.equalsIgnoreCase("")) {
				return exchangeItemCore(userID, newItemID, oldItemID);
			}
			else
				return result;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}		
	}
	
	
	/**
	 * this is the core method for exchanging an item 
	 * @param userID - Id of the user that wants to exchange the items
	 * @param newItemID - Id of the new item to be borrowed
	 * @param oldItemID - Id of the old item to be return
	 * @return - result of exchanging the item
	 */
	private String exchangeItemCore(String userID, String newItemID, String oldItemID){
		try {
			String request, result;
			request = "exchange the old item with id (" + oldItemID + ") by new item with id ( " + newItemID + " )" ;
			getServerLogger().log((userID + " request = " + request), true);
		
			//-- check if desired item belong to other library then send request to that library and return the result
//			String itemLibName = itemID.substring(0, 3);
//			if(itemLibName.equalsIgnoreCase(this.getName())) {
//				//-- check if desired item is borrowed
//				ArrayList<Borrower> borrowers = borrowList.get(itemID);
//				if(borrowers != null) {
//					
//					for(Borrower brw : borrowers) {
//						if(brw.getUserId().equalsIgnoreCase(userID)) {
//							borrowers.remove(brw); // remove from borrower list							
//							Item item = itemList.get(itemID);
//							item.setQuantity(item.getQuantity() + 1); // add item quantity
//							Queue<Borrower> waitUsers = waitList.get(itemID); // check waiting list
//							if(waitUsers != null) {
//								Borrower newBorrower = waitUsers.poll();
//								if(waitUsers.size() == 0) 
//									waitList.remove(itemID);
//							    borrowers.add(newBorrower);
//							    item.setQuantity(item.getQuantity()-1);
//							}
//							if(borrowers.size() == 0) 
//								borrowList.remove(itemID);
//							
//							result = "[succeed] user return the item with id (" + itemID + ")";
//							getServerLogger().log((userID + " request result = " + result), true);
//							return (this.getName() + " server response = " + result);
//						}
//					}
//					
//					// the userId is not borrow the item
//					result = "[failed!] user does not borrow the item with id (" + itemID + ")";
//					getServerLogger().log((userID + " request result = " + result), true);
//					return (this.getName() + " server response = " + result);
//					
//				}
//				else {// item is not borrowed
//					result = "[failed!] the item with id (" + itemID + ") is not borrowed yet";
//					getServerLogger().log((userID + " request result = " + result), true);
//					return (this.getName() + " server response = " + result);
//				}
//			}
//			else {// the item is belong to other library				
//				String[] param = {userID,itemID};
//				String[] otherLibUdpInfo =getLibServersUdpInfo().get(itemLibName);	
//				result = sendUdpRequest("returnItem", param, otherLibUdpInfo);					
//				getServerLogger().log((userID + " request result = " + result), true);
//				return (this.getName() + " server response = " + result);
//			}
			return null;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
		
	/**
	 * this method check that if the client is an authenticated client for this server or not
	 * @param clientId: indicate user or manager id
	 * @return: [true] if the client is authenticated otherwise [false]
	 */
	private boolean clientAuthenticate(String clientId) {
		try {
			String clientServerName = clientId.substring(0, 3);
			String clientType = clientId.substring(3, 4);
			if(clientType.equalsIgnoreCase("m"))
				clientType = "manager";
			else
				clientType = "user";
			
			if(clientServerName.equalsIgnoreCase(this.getName())) {
				
				getServerLogger().log(clientId + " login result = [succeed] server authenticate the "+ clientType, true);
				return true;
			}
			else {
				getServerLogger().log(clientId + " login result = [failed!] server could not authenticate the "+ clientType, true);
				return false;
			}
						
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * this method check that if the client is an authorized to invoke the method
	 * @param clientId: indicate user or manager id
	 * @return: [true] if the client is authenticated otherwise [false]
	 */
	private boolean clientMethodAccessCheck(String clientId, String invokedMethod) {
		try {
			String clientType = clientId.substring(3, 4);
			if(invokedMethod.equalsIgnoreCase("addItem") || invokedMethod.equalsIgnoreCase("removeItem") || invokedMethod.equalsIgnoreCase("listItemAvailability")) {
				if(!clientType.equalsIgnoreCase("m")) {					
					getServerLogger().log(clientId + " access checking  = [failed!] server does not allow the user to invoke " + invokedMethod + " method", true);
					return false;
				}
				else {
					return true;
				}
			}
			if(invokedMethod.equalsIgnoreCase("borrowItem") || invokedMethod.equalsIgnoreCase("findItem") || invokedMethod.equalsIgnoreCase("returnItem") || invokedMethod.equalsIgnoreCase("joinToItemWaitList") || invokedMethod.equalsIgnoreCase("leaveItemWaitList")) {
				if(!clientType.equalsIgnoreCase("u")) {					
					getServerLogger().log(clientId + " access checking  = [failed!] server does not allow the manager to invoke " + invokedMethod + " method", true);
					return false;
				}
				else {
					return true;
				}
			}
			return false;
						
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * this method is responsible for validate all pre-requirements for client connection request
	 * @param clientId - ID of client
	 * @param invokedMethod - the name of method that client invoke
	 * @return - null if validation succeed else the failed message
	 */
	private String clientValidation(String clientId, String invokedMethod) {
		try {
			
			String clientType = "";
			if(clientId.substring(3, 4).equalsIgnoreCase("m"))
				clientType = "manager";
			else
				clientType = "user";
			
			//-- check login authentication
			if(!clientAuthenticate(clientId)) {
				return (this.getName() + " server response = [failed!]" + " can not authenticate the " + clientType +" ID (" + clientId + ")");  
			}
		    
			//-- check method access 
			if(!clientMethodAccessCheck(clientId,invokedMethod)) {				
				return (this.getName() + " server response = [failed!]" + " server does not allow the " + clientType +" with ID (" + clientId + ")" + " to invoke " + invokedMethod + " method");
			}
				
			return "";
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return "";
		}
	}
	
	/**
	 * this method is responsible for check that the user has any previous request record in this library
	 * @param userID user ID
	 * @return true if the user has active request at this library else false
	 */
	private boolean userHasRecord(String userID) {
		try {
			boolean isOB = false;
			for(String key : this.borrowList.keySet()) {
				ArrayList<Borrower> borrowers = this.borrowList.get(key);
				for(Borrower b : borrowers) {
					if(b.getUserId().equalsIgnoreCase(userID))
						return true;
				}
			}
			
			for(String key : this.waitList.keySet()) {
				Queue<Borrower> waitUsers = this.waitList.get(key);
				for(Borrower b : waitUsers) {
					if(b.getUserId().equalsIgnoreCase(userID))
						return true;
				}
			}
			
			return false;
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
		
	/**
	 * this method is responsible for sending a UDP request
	 * @param req - request to be send
	 * @param reqParam - request parameters to be send
	 * @param receiverInetAddress - Internet address of the receiver
	 * @param receiverUdpPort - UDP port of the receiver
	 */
	public String sendUdpRequest(String req, String[] reqParam, String[] receiverInfo) {
		DatagramSocket udpSocket = null;
		try {		
			String udpRequest = "send udp " + req + " request with these params " + Arrays.toString(reqParam)+ " to <" + receiverInfo[0] + ":" + receiverInfo[1] + ">";
			getServerLogger().log((reqParam[0] + " sent udp request : " + udpRequest), true);
			
		    udpSocket = new DatagramSocket();	
			dataPresentation.setRequest(req);
			dataPresentation.setRequestParam(reqParam);
		    byte [] m = (dataPresentation.udpMessagePacking()).getBytes();
			DatagramPacket request = new DatagramPacket(m,  m.length, InetAddress.getByName(receiverInfo[0]), Integer.parseInt(receiverInfo[1]));
			udpSocket.send(request);
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);	
			udpSocket.receive(reply);	
			String udpResponse = new String(reply.getData()).substring(0, reply.getLength());
			getServerLogger().log((reqParam[0] + " received udp response : " + udpResponse + " from <" + receiverInfo[0] + ":" + receiverInfo[1] + ">"), true);			
			return udpResponse;
			
		}catch (SocketException e){
			System.out.println(e.getMessage());
			return null;
		}catch (IOException e){
			System.out.println(e.getMessage());
			return null;
		}finally {if(udpSocket != null) udpSocket.close();}
	}
	
	/**
	 * this method is responsible to setup a UDP listener for this server 
	 */
	private void listenToUdpSocket() {
		DatagramSocket udpSocket = null;
		try {
			udpSocket = new DatagramSocket(this.getPort());	
			while (true) {
				byte[] requestBuffer = new byte[1000];
				DatagramPacket request = new DatagramPacket(requestBuffer, requestBuffer.length);
				udpSocket.receive(request);				
				dataPresentation.udpMessageUnPacking(new String(request.getData()));
				
				String udpRequest = dataPresentation.getRequest() + " request with these params " + Arrays.toString(dataPresentation.getRequestParam())+ " from <" + request.getAddress() + ":" + request.getPort() + ">";
				getServerLogger().log((dataPresentation.getRequestParam()[0] + " received udp request : " + udpRequest), true);
				
				String reqResult;
				reqResult = processUdpRequest(dataPresentation.getRequest(), dataPresentation.getRequestParam());
				getServerLogger().log((dataPresentation.getRequestParam()[0] + " sent udp response : " + reqResult + " to <" + request.getAddress() + ":" + request.getPort() + ">"), true);
				byte[] replyBuffer = new byte[reqResult.length()];
				replyBuffer = reqResult.getBytes();
				DatagramPacket reply = new DatagramPacket(replyBuffer, reqResult.length(), request.getAddress(), request.getPort());
				udpSocket.send(reply);
			}
		}catch (SocketException e){System.out.println("Socket: " + e.getMessage());
		}catch (IOException e) {System.out.println("IO: " + e.getMessage());
		}finally {if(udpSocket != null) udpSocket.close();}
	}
	
	/**
	 * this method is responsible for run UDP listener in concurrent thread
	 */
	private void runUdpListener() {
		try {
			Runnable udpListenerR = () -> {listenToUdpSocket();};
			Thread udpListenerT = new Thread(udpListenerR);
			udpListenerT.start();
			getServerLogger().log("Library Server: -" + this.getName() + "- run its UDP listener at port " + this.getPort(), true);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
				
	/**
	 * this methos is responsible for processing user UDP request and map it to the proper library server method 
	 * @param request user UDP request
	 * @param requestParam list of UDP request parameters
	 * @return result of the UDP request produced by the library server
	 */
	private String processUdpRequest(String request, String[] requestParam) {
		try {
			String result = "";
			if(request.equalsIgnoreCase("borrowItem")) {				
				result =  borrowItemCore(requestParam[0], requestParam[1], Integer.parseInt(requestParam[2]));
			}
			else if(request.equalsIgnoreCase("findItem")) {
				result = findItemCore(requestParam[0], requestParam[1]);
			}
			else if(request.equalsIgnoreCase("returnItem")) {
				result = returnItemCore(requestParam[0], requestParam[1]);
			}
			
			else if(request.equalsIgnoreCase("joinToItemWaitList")) {
				result = joinToItemWaitListCore(requestParam[0], requestParam[1], Integer.parseInt(requestParam[2]));
			}
			
			else if(request.equalsIgnoreCase("leaveItemWaitList")) {
				result = leaveItemWaitListCore(requestParam[0], requestParam[1]);
			}
			
			return result;
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
    //------------------------------------------ methods

	
	//-- fields accessor
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public String getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(String inetAddress) {
		this.inetAddress = inetAddress;
	}
	
	public HashMap<String,String[]> getLibServersUdpInfo() {
		return libServersUdpInfo;
	}
	
	public void setLibServersUdpInfo(HashMap<String,String[]> libServersUdpInfo) {
		this.libServersUdpInfo = libServersUdpInfo;
	}
	
	public DlmsLogger getServerLogger() {
		return serverLogger;
	}

	public void setServerLogger(DlmsLogger serverLogger) {
		this.serverLogger = serverLogger;
	}
	
	//------------------------------------------ fields accessor
		

	//-- inner class	
	class FindItemThread implements Runnable{
		
		Thread trd;
		String[] param;
		String inetAddress;
		int port;
		int resultNo;
		String[] result;
		DlmsServerRmiImpl server;
		CountDownLatch latch;
		
		public FindItemThread(String[] param, String[] toServerUdpInfo, String[] findResult, DlmsServerRmiImpl server , CountDownLatch latch) {
			
			trd = new Thread(this);
			this.param = param;
			this.inetAddress = toServerUdpInfo[1];
			this.port = Integer.parseInt(toServerUdpInfo[2]);
			this.resultNo = Integer.parseInt(toServerUdpInfo[3]);
			//this.tResult = result;
			this.result = findResult;
			this.server = server;
			this.latch = latch;
			trd.start();
		}
		
		public void run() {			
//			this.result[this.resultNo] = this.server.sendUdpRequest("findItem", this.param, this.inetAddress, this.port);
			this.result[this.resultNo] = this.server.sendUdpRequest("findItem", this.param, new String[] {this.inetAddress,String.valueOf(this.port)});
			this.latch.countDown();
		}
		
	}
	//------------------------------------------ fields accessor
}
