package comp.dsd.dlms.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import comp.dsd.dlms.logger.DlmsLogger;

public class ItemList{
	//-- fields
	private ConcurrentHashMap<String,Item> data;

	//-------------------------------------------
	
	//-- constructors
	public ItemList(String libraryName, DlmsLogger libLogger) {
		data = new ConcurrentHashMap<String,Item>(); 
	}
	//-------------------------------------------

	
	//-- methods		
	/**
	 * this method is responsible for finding an item with specified key
	 * @param key
	 * @return - return found item
	 */
	public Item get(String key) {
		try {
			return data.get(key);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return null;
		}
	}
	
	/**
	 * this method is responsible to add an item to list with specified key and value
	 * @param key - item key
	 */
	public void put(String key , Item item) {
		try {
			data.put(key,item);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * this method is responsible for removing an item from the list
	 * @param key - item key
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
	 * this method is responsible for atomic increase of item quantity
	 */
	public void atomicAddQuantity(String itemID,String itemName,int q) {
		try {
			Action action = new Action();
			action.unCheck();
			data.computeIfAbsent(itemID, k -> {action.check();return new Item(itemName,q);});
			if(!action.isChecked()) {
				data.computeIfPresent(itemID, (k,v) -> {v.addQuantity(q);return v; });
			}
						
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * this method is responsible for atomic decrease of item quantity
	 */
	public boolean atomicDecreaseQuantity(String itemID, int q) {
		try {
			Action action = new Action();
			action.unCheck();
			data.computeIfPresent(itemID, (k,v) -> { if(v.getQuantity() >= q) {v.decreaseQuantity(q);action.check();}return v;});	
			return action.isChecked();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	//-------------------------------------------
	
	
	//-- fields accessor
	public ConcurrentHashMap<String,Item> getData() {
		return data;
	}


	public void setData(ConcurrentHashMap<String,Item> data) {
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
