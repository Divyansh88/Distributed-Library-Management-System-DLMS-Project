package comp.dsd.dlms.model;

public class ExchangeItem {
	//-- fields
		//private String id;
		private String userID;
		private String oldItemID;
		private String newItemID;
		//-------------------------------------------
		
		//-- constructors
		public ExchangeItem(String userID, String newItemID, String oldItemID) {
			this.setUserID(userID);
			this.setOldItemID(oldItemID);
			this.setNewItemID(newItemID);
		}
		//-------------------------------------------

		
		//-- methods
		
	
		//-------------------------------------------
		
		
		//-- fields accessor
		public String getUserID() {
			return userID;
		}

		public void setUserID(String name) {
			this.userID = name;
		}


		public String getOldItemID() {
			return oldItemID;
		}


		public void setOldItemID(String oldItemID) {
			this.oldItemID = oldItemID;
		}


		public String getNewItemID() {
			return newItemID;
		}


		public void setNewItemID(String newItemID) {
			this.newItemID = newItemID;
		}

		//-------------------------------------------
}
