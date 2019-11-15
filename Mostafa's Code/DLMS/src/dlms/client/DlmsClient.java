package comp.dsd.dlms.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import comp.dsd.dlms.corba.classes.DlmsServerCorbaInterface;
import comp.dsd.dlms.corba.classes.DlmsServerCorbaInterfaceHelper;
import comp.dsd.dlms.logger.DlmsLogger;
import comp.dsd.dlms.rmi.DlmsServerRmiIntreface;


public class DlmsClient {
	
	//-- fields
    private String id;
    public DlmsLogger clientLogger;    
    //-- corba specific
    private String[] orbInfo;
    public String result;
    //------------------------------------------ fields 
    
    
    //-- constructors
    // CORBA specific constructor
    public DlmsClient(String clientId, String[] orbInfo) {
    	try {
    		this.setId(clientId); 
    		this.orbInfo = orbInfo;
    		clientLogger = new DlmsLogger(clientId);
    		clientLogger.log("client with id: " + this.getId() + " is created", false);
    		
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
    	
    }
    //------------------------------------------ constructors
    
    
	
	//-- methods	
	/** this method is responsible for getting server remote object reference from ORB Naming Service which is used in client side
	 * @param bindName - indicate the name of binded remote object
	 * @return an stub of type DlmsServerIntreface
	 */
	private DlmsServerCorbaInterface getCorbaStub(String bindName) {
		try {	
			// initialize ORB
    		ORB orb = ORB.init(this.orbInfo, null);
    		// get ORB Naming Context
			NamingContextExt ncRef = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
			//return servant object reference
			return DlmsServerCorbaInterfaceHelper.narrow(ncRef.resolve_str(bindName));
			
		} catch (Exception e) {
			System.out.println(e.getMessage());			
			return null;
		}
	}
	
		
	
	/**
	 * this method is responsible to create new item or add to to its quantity
	 * @param serverBindName - library server bind name in rmi registry 
	 * @param itemID - ID of the library item
	 * @param itemName - name of the library item
	 * @param quantity - the desire amount to be add to item quantity
	 */
	public boolean addItem(String serverBindName, String itemID, String itemName, int quantity) {
		try {
			String request = "increase the quantity of item with id (" + itemID + ") and name (" + itemName + ") by " + quantity;
			clientLogger.log(("request to server "+ serverBindName + " = " + request), false);
			//String result;
			result = getCorbaStub(serverBindName).addItem(this.getId(), itemID, itemName, quantity);
			clientLogger.log((result), true);
			return evaluteResult(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * this method is responsible to delete an item or reduce its quantity
	 * @param serverBindName - library server bind name in rmi registry
	 * @param itemID - ID of the library item
	 * @param quantity - the desire item quantity to be removed
	 */
	public boolean removeItem(String serverBindName, String itemID, int quantity) {
		try {
			String request = "remove or decrease the quantity of item with id (" + itemID + ") by " + quantity;
			clientLogger.log(("request to server "+ serverBindName + " = " + request), false);
			String result;
			result = getCorbaStub(serverBindName).removeItem(this.getId(), itemID, quantity);
			clientLogger.log((result), true);
			return evaluteResult(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * this method is responsible to provide list of all item availability
	 * @param serverBindName - library server bind name in rmi registry
	 */
	public boolean listItemAvailability(String serverBindName) {
		try {
			String request = "provide liste of all items availability";
			clientLogger.log(("request to server "+ serverBindName + " = " + request), false);
			String result;
			result = getCorbaStub(serverBindName).listItemAvailability(this.getId());
			clientLogger.log((result), true);	
			return evaluteResult(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * this method is responsible to borrow an item from library
	 * @param serverBindName - library server bind name in rmi registry
	 */
	public boolean borrowItem(String serverBindName, String itemID, int numberOfDays) {
		try {
			String request = "borrow the item with id (" + itemID + ") for the period of " + numberOfDays + " days";
			clientLogger.log(("request to server "+ serverBindName + " = " + request), false);
			String result;
			result = getCorbaStub(serverBindName).borrowItem(this.getId(),itemID,numberOfDays);
			clientLogger.log((result), true);	
			//return evaluteResult(result);
			return evaluteResultForWaiting(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * this method is responsible to placed user in an item waiting list
	 * @param serverBindName - library server bind name in rmi registry
	 */
	public boolean joinToItemWaitList(String serverBindName, String itemID, int numberOfDays) {
		try {
			String request = "wait for the item with id (" + itemID + ") to borrow for the period of " + numberOfDays + " days";
			clientLogger.log(("request to server "+ serverBindName + " = " + request), false);
			String result;
			result = getCorbaStub(serverBindName).joinToItemWaitList(this.getId(),itemID,numberOfDays);
			clientLogger.log((result), true);	
			return evaluteResult(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * this method is responsible to remove user from item waiting list
	 * @param serverBindName - library server bind name in rmi registry
	 */
	public boolean leaveItemWaitList(String serverBindName, String itemID) {
		try {
			String request = "leave waiting list of the item with id (" + itemID + ")";
			clientLogger.log(("request to server "+ serverBindName + " = " + request), false);
			String result;
			result = getCorbaStub(serverBindName).leaveItemWaitList(this.getId(),itemID);
			clientLogger.log((result), true);	
			return evaluteResult(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * this method is responsible to return an item to library
	 * @param serverBindName - library server bind name in rmi registry
	 */
	public boolean returnItem(String serverBindName, String itemID) {
		try {
			String request = "return the item with id (" + itemID + ")";
			clientLogger.log(("request to server "+ serverBindName + " = " + request), false);
			String result;
			result = getCorbaStub(serverBindName).returnItem(this.getId(),itemID);
			clientLogger.log((result), true);	
			return evaluteResult(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * this method is responsible to return list of all item with the same name and their quantity in each three library
	 * @param serverBindName - library server bind name in rmi registry
	 */
	public boolean findItem(String serverBindName, String itemName) {
		try {
			String request = "find items with the same name as (" + itemName + ") along with their quantity within all libraries";
			clientLogger.log(("request to server "+ serverBindName + " = " + request), false);
			String result;
			result = getCorbaStub(serverBindName).findItem(this.getId(),itemName);
			clientLogger.log((result), true);	
			return evaluteResult(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	/**
	 * this method is responsible for exchanging an old item with new item
	 * @param serverBindName - library server bind name in rmi registry
	 * @param newItemID - new library item ID
	 * @param serverBindName - old library item ID to be changed
	 */
	public boolean exchangeItem(String serverBindName, String newItemID, String oldItemID) {
		try {
			String request = "exchange old item (" + oldItemID + ") with new item " + newItemID ;
			clientLogger.log(("request to server "+ serverBindName + " = " + request), false);
			String result;
			result = getCorbaStub(serverBindName).exchangeItem(this.getId(),newItemID, oldItemID);
			
			if(result.contains("waiting request")) {
				clientLogger.log((result), true);
				System.out.println("Do you want to continue exchange item process and be place in waiting list? (Y/N)");
				Scanner k = new Scanner(System.in);
				
				if(k.nextLine().equalsIgnoreCase("y")) {
					result = getCorbaStub(serverBindName).joinToItemWaitList(this.getId(),newItemID, 1);
				}
				else {
					result = getCorbaStub(serverBindName).leaveItemWaitList(this.getId(),newItemID);
				}
			}
			clientLogger.log((result), true);	
			return evaluteResult(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	private boolean evaluteResult(String result) {
		if(result.contains("[succeed]")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private boolean evaluteResultForWaiting(String result) {
		if(result.contains("[wait]")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	
	/**
	 * this method is responsible for interact with user and map his request to proper server function
	 */
	public void startUserInterface() {
		
		String itemID ="";
		String newItemID = "";
		String itemName = "";
		String serverBindName = "FRONTEND";
		int quantity = 0;
		int numberOfDays = 0;
		int userRequest = 10;
		Scanner kb = new Scanner(System.in);
		
		
		if(this.getId().substring(3, 4).equalsIgnoreCase("U")) {
			// welcome message
			System.out.println(this.getId() + " welcome user to the DLMS system ");
			System.out.println("---------------------------");
			System.out.println("");
			
			// while loop for getting user request
			while(!serverBindName.equalsIgnoreCase("") && userRequest != 0) {
				
				if(!serverBindName.equalsIgnoreCase("")) {
					
					// show guide message
					System.out.println("----------------------------------------------------------------------------------");
					System.out.println("choose desire request (1-6) to send to " + serverBindName + ". [enter 0 to exit the program]: ");
					System.out.println("----------------------------------------------------------------------------------");
					System.out.println("");
					System.out.println("1. borrow item");
					System.out.println("2. join item wait list");
					System.out.println("3. leave item wait list");
					System.out.println("4. return item");
					System.out.println("5. find item");
					System.out.println("6. exchange item");
					System.out.println("");
					System.out.print("enter a number: ");
					userRequest = kb.nextInt();
					System.out.println("");
				
					if(userRequest != 0) {				
						// map user request to the server methods
						switch(userRequest) {
							case 1:
								System.out.println("enter required parameters for borrowItem: ");
								System.out.print("enter itemID: "); itemID = kb.next();
								System.out.print("enter borrowing days: "); numberOfDays = kb.nextInt();
								if(borrowItem(serverBindName, itemID, numberOfDays)) {
									// ask for waiting 
									System.out.println("Do you want to be place in waiting list? (Y/N)");
									Scanner k = new Scanner(System.in);								
									if(k.nextLine().equalsIgnoreCase("y")) {
										joinToItemWaitList(serverBindName, itemID, numberOfDays);
									}								
								}
								break;
							case 2:
								System.out.println("enter required parameters for joinToItemWaitList: ");
								System.out.print("enter itemID: "); itemID = kb.next();
								System.out.print("enter borrowing days: "); numberOfDays = kb.nextInt();
								joinToItemWaitList(serverBindName, itemID, numberOfDays);
								break;
							case 3:
								System.out.println("enter required parameters for leaveItemWaitList: ");
								System.out.print("enter itemID: "); itemID = kb.next();
								leaveItemWaitList(serverBindName, itemID);
								break;
							case 4:
								System.out.println("enter required parameters for returnItem: ");
								System.out.print("enter itemID: "); itemID = kb.next();
								returnItem(serverBindName, itemID);
								break;
							case 5:
								System.out.println("enter required parameters for findItem: ");
								System.out.print("enter item Name: "); itemName = kb.next();
								findItem(serverBindName, itemName);
								break;
							case 6:
								System.out.println("enter required parameters for exchangeItem: ");
								System.out.print("enter new item ID: "); newItemID = kb.next();
								System.out.print("enter old item ID: "); itemID = kb.next();
								exchangeItem(serverBindName, newItemID, itemID);
								break;	
							
							default :
								break;
									
						}
					}
					
				}			
				
			}
	    }
		
		else if(this.getId().substring(3, 4).equalsIgnoreCase("M")) {
			// welcome message
			System.out.println(this.getId() + " welcome manager to the DLMS system ");
			System.out.println("---------------------------");
			System.out.println("");
			
			// while loop for getting user request
			while(!serverBindName.equalsIgnoreCase("") && userRequest != 0) {
				
				if(!serverBindName.equalsIgnoreCase("")) {
					
					// show guide message
					System.out.println("----------------------------------------------------------------------------------");
					System.out.println("choose desire request (1-3) to send to " + serverBindName + ". [enter 0 to exit the program]: ");
					System.out.println("----------------------------------------------------------------------------------");
					System.out.println("");
					System.out.println("1. add item");
					System.out.println("2. remove item");
					System.out.println("3. item list");
					System.out.println("");
					System.out.print("enter a number: ");
					userRequest = kb.nextInt();
					System.out.println("");
				
					if(userRequest != 0) {				
						// map user request to the server methods
						switch(userRequest) {
							case 1:
								System.out.println("enter required parameters for addItem: ");
								System.out.print("enter itemID: "); itemID = kb.next();
								System.out.print("enter item Name: "); itemName = kb.next();
								System.out.print("enter quantity: "); quantity = kb.nextInt();
								addItem(serverBindName,  itemID, itemName, quantity);
								break;
							case 2:
								System.out.println("enter required parameters for removeItem: ");
								System.out.print("enter itemID: "); itemID = kb.next();
								System.out.print("enter quantity: "); quantity = kb.nextInt();
								removeItem(serverBindName, itemID, quantity);
								break;
							case 3:
								listItemAvailability(serverBindName);
								break;
							default :
								break;
									
						}
					}
					
				}			
				
			}
	    }
		
		else {
			System.out.println("enter correct client id !!");
		}
		
		System.out.println("exit ...");
		
	}
	//------------------------------------------ methods
	
	
	//-- fields accessor
	public String getId() {
		return id;
	}
	
	public void setId(String name) {
		this.id = name;
	}
	
	
	//------------------------------------------ fields accessor


	
}

