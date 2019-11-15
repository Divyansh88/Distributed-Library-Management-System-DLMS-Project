package Users;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.time.LocalDateTime;
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
public class Manager {
	static ServerInterface siu = null;
	static String path = "src\\Log files";
	static String manager_id, message, check = "";
	static Scanner choice = new Scanner(System.in);
	
	public static void main(String args[]) throws NotBoundException, IOException {		
		int i; 
		boolean valid = true;
		Manager manager = new Manager();
		
		while(valid) {
			System.out.println("\nEnter Your Manager ID: ");
			manager_id = choice.next();
			Registry registry;
		
			check = manager_id.substring(0, 4);
			
			if(check.equalsIgnoreCase("CONM")) {
//				registry = LocateRegistry.getRegistry(1111);
//				siu = (ServerInterface) registry.lookup("Concordia");
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
				System.out.println("Concordia's manager");
				break;
			}
			else if(check.equalsIgnoreCase("MCGM")) {
//				registry = LocateRegistry.getRegistry(2222);
//				siu = (ServerInterface) registry.lookup("McGill");
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
				System.out.println("McGill's's manager");
				break;
			}
			else if(check.equalsIgnoreCase("MONM")) {
//				registry = LocateRegistry.getRegistry(3333);
//				siu = (ServerInterface) registry.lookup("Montreal");
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
				System.out.println("Montreal's manager");
				break;
			}
			else {
				System.out.println("Enter valid manager ID.");
			}
		}
		
		
		path = path + "\\" + manager_id;
		File file = new File(path);
		file.createNewFile();
		
		
		while(valid){
			System.out.println("\n Please select managers's operation. Enter your choice:  " );
			System.out.println("\n 1. Add Book " );
			System.out.println("\n 2. Remove Book " );
			System.out.println("\n 3. List Available Books " );
			System.out.println("\n 4. Exit" );
			System.out.println("\n Enter Your Choice: " );
			i = choice.nextInt();
			
			switch(i){
				case 1: manager.addBooks(choice); 
						valid = true;break;
				case 2: manager.removeBooks(choice);
						valid = true;break;
				case 3: manager.listAvailableBooks();
						valid = true;break;
				case 4: System.out.println("Exited"); 
						valid = false;System.exit(1); break;
				default:System.out.println("Choice should be in range of 1-3");
						valid =true;break;
			}			
		}
		
	}
	
	/**
	 * @throws RemoteException 
	 * 
	 */
	public void listAvailableBooks() throws RemoteException {
		message = siu.listItemAvailability(manager_id);
		System.out.println(message);
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"LIST"+"--"+manager_id +"--"+message+"\n";
		appendUsingFileWriter(path, print_response);
		
	}

	/**
	 * @param choice
	 * @throws RemoteException 
	 */
	public void removeBooks(Scanner choice) throws RemoteException {
		int quantity = 0, i; 
		String item_id;
		System.out.println("\nEnter Book ID: " );
		item_id = choice.next();
		System.out.println("\n 1. Completely Remove the Book " );
		System.out.println("\n 2. Remove Particular Amount of the Book  " );
		System.out.println("\n Enter Your Choice: " );
		i = choice.nextInt();
		
		switch(i){
			case 1: quantity = -1; 
					break;
			case 2: System.out.println("\nEnter Quantity you want to delete: " );
					quantity =choice.nextInt();
					break;
		}		
		
		message = (String) siu.removeItem(manager_id, item_id, quantity);
		System.out.println(message);
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"REMOVE"+"--"+manager_id +", "+item_id+", "+quantity+"--"+message+"\n";
		appendUsingFileWriter(path, print_response);
	}

	/**
	 * @param choice
	 * @throws RemoteException 
	 */
	public void addBooks(Scanner choice) throws RemoteException {
		int quantity; 
		String item_id, item_name;
		System.out.println("\nEnter Book ID: " );
		item_id = choice.next();
		System.out.println("\nEnter Book Name: " );
		item_name = choice.next();
		System.out.println("\nEnter Quantity: " );
		quantity = choice.nextInt();
		
		message = siu.addItem(manager_id, item_id, item_name, quantity);
		System.out.println(message);
		LocalDateTime current_time = LocalDateTime.now();
		String print_response = current_time+"--"+"ADD"+"--"+manager_id +", "+item_id+", "+item_name+", "+quantity+"--"+message+"\n";
		appendUsingFileWriter(path, print_response);		
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

