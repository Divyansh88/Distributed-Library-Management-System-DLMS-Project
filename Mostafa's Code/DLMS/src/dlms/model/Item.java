package comp.dsd.dlms.model;

public class Item {
	
	//-- fields
	//private String id;
	private String name;
	private int quantity;
	//-------------------------------------------
	
	//-- constructors
	public Item(String name, int quantity) {
		this.setName(name);
		this.setQuantity(quantity);
	}
	//-------------------------------------------

	
	//-- methods
	
	/**
	 * this method is responsible to add to item quantity
	 * @param n - desire amount of increase
	 */
	public void addQuantity(int n) {
		try {
			this.setQuantity(this.getQuantity() + n);
		}catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * this method is responsible to decrease from item quantity
	 * @param n - desire amount of reduction
	 */
	public void decreaseQuantity(int n) {
		try {
			this.setQuantity(this.getQuantity() - n);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	//-------------------------------------------
	
	
	//-- fields accessor
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	//-------------------------------------------


	
}
