package comp.dsd.dlms.model;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import comp.dsd.dlms.logger.DlmsLogger;

public class BorrowList{
	//-- fields
	private ConcurrentHashMap<String,ArrayList<Borrower>> data;

	//-------------------------------------------
	
	//-- constructors
	public BorrowList(String libraryName, DlmsLogger libLogger) {
		data = new ConcurrentHashMap<String,ArrayList<Borrower>>();
	}
	//-------------------------------------------

	
	//-- methods		
	/**
	 * this method is responsible for finding borrower with specified key
	 * @param key - borrower key
	 * @return - return found item
	 */
	public ArrayList<Borrower> get(String key) {
		try {
			return data.get(key);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	/**
	 * this method is responsible to add borrower to list with specified key and value
	 * @param key - borrower key
	 */
	public void put(String key , ArrayList<Borrower> item) {
		try {
			data.put(key,item);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * this method is responsible to add borrower to list with specified key and value in atomic fashion
	 * @param key - borrower key
	 */
	public void atomicPut(String key , ArrayList<Borrower> item) {
		try {
			data.computeIfAbsent(key, k -> item);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * this method is responsible for removing borrower from the list
	 * @param key - borrower key
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
	 * this method is responsible for atomic adding of a borrower
	 * @return TODO
	 */
	public boolean atomicAddBorrower(String itemID, Borrower borrower) {
		try {
			Action action = new Action();
			action.unCheck();
			if(borrower != null) {
				data.computeIfAbsent(itemID, k -> {action.check(); ArrayList<Borrower> brList = new ArrayList<Borrower>(); brList.add(borrower); return brList;});
				if(!action.isChecked()) {
					data.computeIfPresent(itemID, (k,v) -> {v.add(borrower); return v;});
					action.check();
				}
			}
			return action.isChecked();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	//-------------------------------------------
	
	
	//-- fields accessor
	public ConcurrentHashMap<String,ArrayList<Borrower>> getData() {
		return data;
	}


	public void setData(ConcurrentHashMap<String,ArrayList<Borrower>> data) {
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
