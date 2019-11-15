import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import DLMSApp.ServerInterface;
import DLMSApp.ServerInterfaceHelper;

/**
 * @author Divyansh
 *
 */
public class TestConcurrent {
	static ServerInterface siu = null;
	
	public static void main(String arg[]) {
		Runnable task1 = () -> {
			firstUser(arg);
		};
		Runnable task2 = () -> {
			secondUser(arg);
		};
				
		Thread thread1 = new Thread(task1);
		Thread thread2 = new Thread(task2);	
		thread1.start();
		thread2.start();
		
	}
	
	public static void firstUser(String arg[]) {
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
		
		String item_id = "CON1122";
		int number_of_days = 26;
		String response;
		response = siu.borrowItem(user_id, item_id, number_of_days);
		System.out.println("User 1:"+response);
	}
	
	public static void secondUser(String arg[]) {
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
		
		String item_id = "CON1122";
		int number_of_days = 17;
		String response;
		response = siu.borrowItem(user_id, item_id, number_of_days);
		System.out.println("User 2:"+response);
	}
}
