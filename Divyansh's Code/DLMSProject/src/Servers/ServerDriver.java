package Servers;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * @author Divyansh
 *
 */
public class ServerDriver {
	public void start() throws RemoteException {
		
		ConcordiaServer concordiaServer = new ConcordiaServer(); 
		McGillServer mcgillServer = new McGillServer();
		MontrealServer montrealServer = new MontrealServer();
		 
		 Runnable task = () -> {
			 	try {
					concordiaServer.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
			Runnable task2 = () -> {
				try {
					mcgillServer.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
			Runnable task3 = () -> {
				try {
					montrealServer.start();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
			
			Thread thread = new Thread(task);
			Thread thread2 = new Thread(task2);
			Thread thread3 = new Thread(task3);	
			thread.start();
			thread2.start();
			thread3.start();
		 
		 
		
	}
}
