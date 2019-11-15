package Implementation;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class book {
	private String name;
	private int quantity;
	private Queue<String> waitlist = new LinkedList<String>() ;
	private List<String> borrowList = new ArrayList<String>();
	
	public book(String n, int q){
		name = n;
		quantity = q;
	}
	
	public boolean waitListEmpty(){
		return waitlist.isEmpty();
	}
	
	public String waitListFirst(){
		return waitlist.poll();
	}
	
	public int getQuantity(){
		return this.quantity;
	}
	
	public String getName(){
		return this.name;
	}
	
	public boolean checkBorrow(String userID){
		for(String str: borrowList) {
		    if(str.trim().contains(userID))
		       return true;
		}
		return false;
	}
	
	public void setBorrow(String userID) {
		borrowList.add(userID);
		this.quantity -= 1;
	}
	
	public void setReturn(String userID) {
		borrowList.remove(userID);
		this.quantity += 1;
	}
	
	public void setQuantity(int q){
		this.quantity += q;
	}
	
	public void addWaitlist(String userID){
		this.waitlist.add(userID);
	}
	
	public void removeWaitlist(String userID){
		this.waitlist.remove(userID);
	}
	
	public void print(){
		System.out.println(name+" "+quantity);
	}
	
	public void printWaitlist(){
		for(String s : waitlist) { 
			  System.out.println(s.toString()); 
			}
	}
	
	public void printBorrowList(){
		for(String s : borrowList) { 
			  System.out.println(s.toString()); 
			}
	}
	
	public void hold(String userID){
		borrowList.remove(userID);
		System.out.println("111111");
	}

	public void Rhold(String userID){
		borrowList.add(userID);
		System.out.println("333");
	}
}
