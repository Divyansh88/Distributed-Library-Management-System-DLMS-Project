package comp.dsd.dlms.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import comp.dsd.dlms.logger.DlmsLogger;

public class ExchangeList {
	//-- fields
		private ConcurrentHashMap<String,ArrayList<ExchangeItem>> data;
		//-------------------------------------------
		
		//-- constructors
		public ExchangeList(String libraryName, DlmsLogger libLogger) {
			data = new ConcurrentHashMap<String,ArrayList<ExchangeItem>>();
		}
		//-------------------------------------------

		
		//-- methods		
		/**
		 * this method is responsible for finding an exchange item with specified key
		 * @param key - exchange item list key userID,oldItemID,newItemID
		 * @return - return found wait list
		 */
		public ArrayList<ExchangeItem> get(String key) {
			try {
				return data.get(key);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return null;
			}
		}
		
		/**
		 * this method is responsible to add exchange item to list with specified key and value
		 * @param key - wait list key
		 */
		public void put(String key , ArrayList<ExchangeItem> item) {
			try {
				data.put(key,item);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
		public void putItem(String userID , ExchangeItem item) {
			try {
				ArrayList<ExchangeItem> exchItems = this.get(userID);
				if(exchItems == null) {
					exchItems = new ArrayList<ExchangeItem>();
				}
				exchItems.add(item);
				data.put(userID,exchItems);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
		/**
		 * this method is responsible for removing a exchange item from the list
		 * @param key - wait list key
		 */
		public void remove(String key, ExchangeItem item) {
			try {
				data.get(key).remove(item);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
	
		public ExchangeItem removeItem(String userID, String itemID) {
			try {
				ExchangeItem removeditem = null;
				ArrayList<ExchangeItem> exchItems = this.get(userID);
				if(exchItems != null) {
					// search for the specific item
					for(ExchangeItem item: exchItems) {
						if(item.getNewItemID().equalsIgnoreCase(itemID)) {
							removeditem = new ExchangeItem(userID, item.getNewItemID(), item.getOldItemID());
							while(exchItems != null && exchItems.remove(item));
                            return removeditem;
						}
					}
				}

				return removeditem;
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return null;
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
		
		
		//-------------------------------------------
		
		
		//-- fields accessor
		public ConcurrentHashMap<String,ArrayList<ExchangeItem>> getData() {
			return data;
		}


		public void setData(ConcurrentHashMap<String,ArrayList<ExchangeItem>> data) {
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
