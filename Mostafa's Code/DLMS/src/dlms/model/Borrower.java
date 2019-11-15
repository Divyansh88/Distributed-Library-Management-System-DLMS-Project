package comp.dsd.dlms.model;

public class Borrower {
	//-- fields
		private String userId;
		private int numberOfDays;
		private int remainingDate;
		//-------------------------------------------
		
		//-- constructors
		public Borrower(String userId, int numberOfDays) {
			this.setUserId(userId);
			this.setNumberOfDays(numberOfDays);
			this.setRemainingDate(numberOfDays);
		}
		//-------------------------------------------

		
		//-- methods		
		/**
		 * this method is responsible to decrease from remaining date
		 * @param n - desire amount of reduction
		 */
		public void decreaseRemainingDate(int n) {
			try {
				this.setRemainingDate(this.getRemainingDate() - n);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		//-------------------------------------------
		
		
		//-- fields accessor
		public String getUserId() {
			return userId;
		}
		public void setUserId(String userId) {
			this.userId = userId;
		}
		
		public int getNumberOfDays() {
			return numberOfDays;
		}

		public void setNumberOfDays(int quantity) {
			this.numberOfDays = quantity;
		}
		
		public int getRemainingDate() {
			return remainingDate;
		}
		public void setRemainingDate(int remainingDate) {
			this.remainingDate = remainingDate;
		}
		//-------------------------------------------
		
		
}
