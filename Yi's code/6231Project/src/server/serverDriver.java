package server;

import java.util.concurrent.TimeUnit;

public class serverDriver {
	private CONServer concordiaServer = new CONServer(); 
	private MCGServer mcgillServer = new MCGServer();
	private MONServer montrealServer = new MONServer();

	public serverDriver() {
		
	}
	
	public void crash(String lib) {
		if(lib.contains("CON")) {
			concordiaServer.closeSocket();
			concordiaServer.stop();
			System.out.println("concordiaServer STOP ");
		}	
		else if(lib.contains("MCG")) {
			mcgillServer.closeSocket();
			mcgillServer.stop();
			System.out.println("mcgillServer STOP ");
		}
		else {
			montrealServer.closeSocket();
			montrealServer.stop();
			System.out.println("montrealServer STOP ");
		}
			
	}
	
	public void start(boolean recover) throws InterruptedException {
		
		Runnable task;
	
		if(recover == true) {
			System.out.println("concordia server para: true");
			
			concordiaServer.closeSocket();
			concordiaServer.stop();
			mcgillServer.closeSocket();
			mcgillServer.stop();
			montrealServer.closeSocket();
			montrealServer.stop();
			TimeUnit.MILLISECONDS.sleep(1000);
			 
			System.out.println("SOCKET ARE CLOSED");
			
			concordiaServer = new CONServer(); 
			mcgillServer = new MCGServer();
			montrealServer = new MONServer();
			System.out.println("SERVER ARE INITIALIZED");
			TimeUnit.MILLISECONDS.sleep(1000);
			
			task = () -> {
			 	concordiaServer.start(true);
			};
		}
		else {
			System.out.println("concordia server para: false");
			task = () -> {
				 	concordiaServer.start(false);
				};
		}
			Runnable task2 = () -> {
				mcgillServer.start();
			};
			Runnable task3 = () -> {
				montrealServer.start();
			};
//			
			Thread thread = new Thread(task);
			Thread thread2 = new Thread(task2);
			Thread thread3 = new Thread(task3);	
			thread.start();
			thread2.start();
			thread3.start();
	}

}
