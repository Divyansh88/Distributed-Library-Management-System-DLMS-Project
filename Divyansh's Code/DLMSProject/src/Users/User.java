package Users;


import java.awt.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import DLMSApp.ServerInterface;
import DLMSApp.ServerInterfaceHelper;
import Servers.*;

/**
 * @author Divyansh
 *
 */
public class User {
	static ServerInterface siu = null;
	static String path = "src\\Log files";
	static String user_id,message,check = "";
	static Scanner choice = new Scanner(System.in);
	
	public static void main(String args[]) throws NotBoundException, IOException {
		int i; 
		boolean valid = true;
		User user = new User();
		
		
		while(valid) {
			System.out.println("\nEnter Your Student ID: ");
			user_id = choice.next();
			Registry registry;
			
			check = user_id.substring(0, 4);
			
			if(check.equalsIgnoreCase("CONU")) {
				//registry = LocateRegistry.getRegistry(1111);
				//siu = (ServerInterface) registry.lookup("Concordia");
				try {
					ORB orb = ORB.init(args, null);
					// -ORBInitialPort 1050 -ORBInitialHost localhost
					org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
					NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
					siu = (ServerInterface) ServerInterfaceHelper.narrow(ncRef.resolve_str("Concordia"));
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				System.out.println("Concordia's user");
				break;
			}
			else if(check.equalsIgnoreCase("MCGU")) {
				//registry = LocateRegistry.getRegistry(2222);
				//siu = (ServerInterface) registry.lookup("McGill");
				try {
					ORB orb = ORB.init(args, null);
					// -ORBInitialPort 1050 -ORBInitialHost localhost
					org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
					NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
					siu = (ServerInterface) ServerInterfaceHelper.narrow(ncRef.resolve_str("McGill"));
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				System.out.println("McGill's's user");
				break;
			}
			else if(check.equalsIgnoreCase("MONU")) {
				//registry = LocateRegistry.getRegistry(3333);
				//siu = (ServerInterface) registry.lookup("Montreal");
				try {
					ORB orb = ORB.init(args, null);
					// -ORBInitialPort 1050 -ORBInitialHost localhost
					org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
					NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
					siu = (ServerInterface) ServerInterfaceHelper.narrow(ncRef.resolve_str("Montreal"));
				}
				catch(Exception e) {
					e.printStackTrace();
				}
				System.out.println("Montreal's user");
				break;
			}
			else {
				System.out.println("Enter valid user ID.");
			}
		}
		path = path + "\\" + user_id;
		File file = new File(path);
		file.createNewFile();
		
		while(valid){
			System.out.println("\n Please select user's operation. Enter your choice:  " );
			System.out.println("\n 1. Borrow Book " );
			System.out.println("\n 2. Find Book " );
			System.out.println("\n 3. Return Book " );
			System.out.println("\n 4. Exchange Book " );
			System.out.println("\n 5. Exit" );
			System.out.println("\n Enter Your Choice: " );
			i = choice.nextInt();
			
			switch(i){
				case 1: user.borrowBooks(choice); 
						valid = true;break;
				case 2: user.findBooks(choice);
						valid = true;break;
				case 3: user.returnBooks(choice);
						valid = true;break;
				case 4: user.exchangeBooks(choice);
						valid = true;break;	
				case 5: System.out.println("Exited"); 
						valid = false;System.exit(1); break;
				default:System.out.println("Choice should be in range of 1-3");
						valid =true;break;
			}			
		}
		
	}
	
	/**
	 * @param choice
	 * @throws RemoteException
	 */
	private void exchangeBooks(Scanner choice) throws RemoteException{
		String new_item_id, old_item_id, response;
		System.out.println("\nEnter New Book ID: " );
		new_item_id = choice.next();
		System.out.println("\nEnter Old Book ID: " );
		old_item_id = choice.next();
		
		
		response = siu.exchangeItem(user_id, new_item_id, old_item_id);
		System.out.println(response);
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"RETURN"+"--"+user_id +", "+new_item_id+", "+old_item_id+"--"+response+"\n";
		//appendUsingFileWriter(path, current_time+"--"+"RETURN"+"--"+para +response+"\n");
		appendUsingFileWriter(path, print_response);
	}

	/**
	 * @param choice
	 * @throws RemoteException 
	 */
	public void returnBooks(Scanner choice) throws RemoteException {
		String item_id, response;
		System.out.println("\nEnter Book ID You Want to Return: " );
		item_id = choice.next();
		
		
		response = siu.returnItem(user_id, item_id);
		System.out.println(response);
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"RETURN"+"--"+user_id +", "+item_id+"--"+response+"\n";
		//appendUsingFileWriter(path, current_time+"--"+"RETURN"+"--"+para +response+"\n");
		appendUsingFileWriter(path, print_response);
	}

	/**
	 * @param choice
	 * @throws RemoteException 
	 */
	public void findBooks(Scanner choice) throws RemoteException {
		String item_name, response;
		System.out.println("\nEnter Book Name You Want to Search: " );
		item_name = choice.next();
		response = siu.findItem(user_id, item_name);
		System.out.println(response);
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"FIND"+"--"+user_id +", "+item_name+"--"+response+"\n";
		//appendUsingFileWriter(path, current_time+"--"+"FIND"+"--"+para +response+"\n");
		appendUsingFileWriter(path, print_response);
	}

	/**
	 * @param choice
	 * @throws RemoteException 
	 */
	public void borrowBooks(Scanner choice) throws RemoteException {
		String item_id;
		int number_of_days;
		String response="";
		String message="";
		System.out.println("\nEnter Book ID: " );
		item_id = choice.next();
		System.out.println("\nEnter Number of Days: " );
		number_of_days = choice.nextInt();
		response = siu.borrowItem(user_id, item_id, number_of_days);
		
		System.out.println(response);
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"BORROW"+"--"+user_id +", "+item_id+", "+number_of_days+"--"+response+"\n";
		//appendUsingFileWriter(path, current_time+"--"+"BORROW"+"--"+para +response+"\n");
		appendUsingFileWriter(path, print_response);
		if(response.equalsIgnoreCase("[failed] Book is not available to for borrowing.")) {
			int waiting_queue;
			System.out.println("Item you requested is not available in any library. Want to be in waiting queue?");
			System.out.println("\nEnter 1 for yes and 2 for no: " );
			waiting_queue = choice.nextInt();
			
			if(waiting_queue == 1) {
				message = siu.addToQueue(user_id, item_id);				
			}
			else if(waiting_queue == 2) {
				message = "You don't want to be in waiting queue. Thank you.";
			}
			System.out.println(message);
			String print_message = current_time+"--"+"WAITING QUEUE"+"--"+user_id +", "+item_id+"--"+message+"\n";
			//appendUsingFileWriter(path, current_time+"--"+"WAITING QUEUE"+"--"+para2 +message+"\n");
			appendUsingFileWriter(path, print_message);
		}
		
		
	}

	
	public static void appendUsingFileWriter(String filePath, String text) {
		File file = new File(filePath);
		FileWriter fr = null;
		try {
			fr = new FileWriter(file, true);
			fr.write(text);
		} 
		catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
