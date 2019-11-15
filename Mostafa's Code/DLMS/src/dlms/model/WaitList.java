package comp.dsd.dlms.model;

import java.awt.List;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import comp.dsd.dlms.logger.DlmsLogger;


public class WaitList{
	//-- fields
	private ConcurrentHashMap<String,Queue<Borrower>> data;
	Borrower polledBorrower;

	//-------------------------------------------
	
	//-- constructors
	public WaitList(String libraryName, DlmsLogger libLogger) {
		data = new ConcurrentHashMap<String,Queue<Borrower>>();
	}
	//-------------------------------------------

	
	//-- methods		
	/**
	 * this method is responsible for finding a wait list with specified key
	 * @param key - wait list key
	 * @return - return found wait list
	 */
	public Queue<Borrower> get(String key) {
		try {
			return data.get(key);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	/**
	 * this method is responsible to add wait list to list with specified key and value
	 * @param key - wait list key
	 */
	public void put(String key , Queue<Borrower> item) {
		try {
			data.put(key,item);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * this method is responsible for removing a wait list from the list
	 * @param key - wait list key
	 */
	public void remove(String key) {
		try {
			data.remove(key);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * this method is responsible for returning the key set of the HasMap list
	 * @return
	 */
	public Set<String> keySet() {
		try {
			return data.keySet();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	/**
	 * this method is responsible to poll borrower from a waiting list in atomic fashion
	 * @param key - borrower key
	 */
	public Borrower atomicPollBorrower(String key) {
		try {
			//Borrower polledBorrower;
			data.computeIfPresent(key, (k,v) -> {polledBorrower = v.poll(); return v;});
			return polledBorrower;
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	/**
	 * this method is responsible for atomic adding of a borrower to waiting list
	 */
	public void atomicPutBorrower(String itemID, Borrower borrower) {
		try {
			Action action = new Action();
			action.unCheck();
			data.computeIfAbsent(itemID, k -> {action.check(); Queue<Borrower> brList = new LinkedList<Borrower>(); brList.add(borrower); return brList;});
			if(!action.isChecked()) {
				data.computeIfPresent(itemID, (k,v) -> {v.add(borrower); return v;});
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	//-------------------------------------------
	
	
	//-- fields accessor
	public ConcurrentHashMap<String,Queue<Borrower>> getData() {
		return data;
	}


	public void setData(ConcurrentHashMap<String,Queue<Borrower>> data) {
		this.data = data;
	}
	
	//-------------------------------------------
	
	//-- internal class
	private class Action{
		boolean done;
		public void check() {
			done = true;
		}
		public void unCheck() {
			done = false;
		}
		public boolean isChecked() {
			return done;
		}
	}
}
