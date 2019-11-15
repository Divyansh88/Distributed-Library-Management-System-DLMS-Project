import java.time.LocalDateTime;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import DLMSApp.ServerInterface;
import DLMSApp.ServerInterfaceHelper;

/**
 * @author Divyansh
 *
 */
public class Test {
	static ServerInterface siu = null;
	
	public static void main(String arg[]) {
		Runnable task1 = () -> {
			concordiaUser(arg);
		};
		Runnable task2 = () -> {
			mcgillUser(arg);
		};

				
		Thread thread1 = new Thread(task1);
		Thread thread2 = new Thread(task2);
		
		thread1.start();
		thread2.start();
		
		
	}

	//Case 1: User tries already borrowed a book from other server and tries to exchange the same book from the same server. ---> Success
	//        User again tries to exchange the book from other server which is not available so atomicity fails.  ---> Fail
	//        User again tries to exchange with the unborrowed book so atomicity fails.  ---> Fail
	public static void concordiaUser(String arg[]) {
		String user_id = "CONU1111";
		
		try {
			ORB orb = ORB.init(arg, null);
			// -ORBInitialPort 1050 -ORBInitialHost localhost
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			siu = (ServerInterface) ServerInterfaceHelper.narrow(ncRef.resolve_str("Concordia"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("Concordia's user");
		
		String item_id = "MCG2288";
		int number_of_days = 17;
		String response;
		response = siu.borrowItem(user_id, item_id, number_of_days);
		System.out.println(response);
		
		String new_item_id = "MCG2222", old_item_id = "MCG2288";
		response = siu.exchangeItem(user_id, new_item_id, old_item_id);  // Success
		System.out.println(response);
		
		new_item_id = "CON1144";
		old_item_id = "MCG2222";
		response = siu.exchangeItem(user_id, new_item_id, old_item_id);  // Fail
		System.out.println(response);
		
		new_item_id = "CON1188";
		old_item_id = "CON1144";
		response = siu.exchangeItem(user_id, new_item_id, old_item_id);  // Fail
		System.out.println(response);
		
		
		
	}
	
	
	//Case 2: User tries already borrowed a book from other server and tries to exchange a book from the same server. ---> Success
	public static void mcgillUser(String arg[]) {
		String user_id = "MCGU1111";
		
		try {
			ORB orb = ORB.init(arg, null);
			// -ORBInitialPort 1050 -ORBInitialHost localhost
			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			siu = (ServerInterface) ServerInterfaceHelper.narrow(ncRef.resolve_str("McGill"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		System.out.println("McGill's's user");
		
		String item_id = "CON1188";
		int number_of_days = 26;
		String response;
		response = siu.borrowItem(user_id, item_id, number_of_days);
		System.out.println(response);
		
		item_id = "MCG2288";
		number_of_days = 8;
		response = siu.borrowItem(user_id, item_id, number_of_days);
		System.out.println(response);
		
		String new_item_id = "CON1122", old_item_id = "MCG2288";
		response = siu.exchangeItem(user_id, new_item_id, old_item_id);  // Fail
		System.out.println(response);
		
	}

}