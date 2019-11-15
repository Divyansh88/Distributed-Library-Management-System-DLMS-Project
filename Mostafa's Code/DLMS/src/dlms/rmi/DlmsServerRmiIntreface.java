package comp.dsd.dlms.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * This interface is defined in order to indicate the remote methods signature
 * @author Mostafa Marandi : 40085773
 *
 */
public interface DlmsServerRmiIntreface extends Remote{
	// manager related methods
	/**
	 * this method is responsible for increase an item quantity
	 * @param managerID - id of the manager who invoke this method
	 * @param itemID - id of the library item to add to its quantity
	 * @param itemName - name of the library item name
	 * @param quantity - the amount of increase
	 * @throws RemoteException
	 */
	public String addItem (String managerID, String itemID, String itemName, int quantity) throws RemoteException;
	
	/**
	 * this method is responsible for decreasing or deleting an item
	 * @param managerID - id of the manager who invoke this method
	 * @param itemID - id of the library item to decrease its quantity
	 * @param quantity - the amount of decrease
	 * @throws RemoteException
	 */
	public String removeItem (String managerID, String itemID, int quantity) throws RemoteException;
	
	/**
	 * this method is responsible for providing a complete list of the library items along with their available quantity
	 * @param managerID - id of the manager who invoke this method
	 * @throws RemoteException
	 */
	public String listItemAvailability (String managerID) throws RemoteException;
	
	//user related methods
	/**
	 * this method is responsible for handle the process of item borrowing
	 * @param userID - id of the user who invoke this method
	 * @param itemID - id of the library item to be borrowed
	 * @param numberOfDays - number of days that the user wants to borrow the item
	 * @throws RemoteException
	 */
	public String borrowItem (String userID, String itemID, int numberOfDays) throws RemoteException;
	
	/**
	 * this method is responsible for handle the process of putting the user in waiting list for the desired item 
	 * @param userID - id of the user who invoke this method
	 * @param itemID - id of the library item to be borrowed
	 * @param numberOfDays - number of days that the user wants to borrow the item
	 * @throws RemoteException
	 */
	public String joinToItemWaitList (String userID, String itemID, int numberOfDays) throws RemoteException;
	
	/**
	 * this method is responsible for handle the process of removing the user from waiting list for the desired item 
	 * @param userID - id of the user who invoke this method
	 * @param itemID - id of the library item to be borrowed
	 * @throws RemoteException
	 */
	public String leaveItemWaitList (String userID, String itemID) throws RemoteException;
	
	/**
	 * this method is responsible to provide a complete list of the same name item along with its quantity in each server
	 * @param userID - id of the user who invoke this method
	 * @param itemName - name of the library item name
	 * @throws RemoteException
	 */
	public String findItem (String userID,String itemName) throws RemoteException;
	
	/**
	 * this method is responsible for handle the process of item returning
	 * @param userID - id of the user who invoke this method
	 * @param itemID - id of the library item to be returned
	 * @throws RemoteException
	 */
	public String returnItem (String userID, String itemID) throws RemoteException;
	
	public String exchangeItem(String userID, String newItemID, String oldItemID) throws RemoteException;
}

