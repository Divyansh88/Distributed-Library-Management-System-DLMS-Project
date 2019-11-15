package Others;


/**
 * @author Divyansh
 *
 */
public class Books {
	private String item_id, item_name;
	private int quantity;
	private boolean flag = false;
	public Books(String item_id, String item_name, int quantity) {
		this.item_id = item_id;
		this.item_name = item_name;
		this.quantity = quantity;
	}

	public String getItemName() {
		return item_name;
	}

	public String getItemId() {
		return item_id;
	}

	public int getQuantity() {
		return quantity;
	}
	
	public void setItemName(String item_name) {
		this.item_name = item_name;
	}
	
	public void setItemId(String item_id) {
		this.item_id = item_id;
	}
	
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	
	public void borrowBook(){
		this.quantity--;
	}
	
	public void returnBook(){
		this.quantity++;
	}
}

