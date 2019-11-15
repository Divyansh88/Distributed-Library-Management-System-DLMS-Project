package comp.dsd.dlms.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import javax.swing.text.StyledEditorKit.AlignmentAction;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import comp.dsd.dlms.corba.DlmsServerCorbaImpl;
import comp.dsd.dlms.corba.classes.DlmsServerCorbaInterface;
import comp.dsd.dlms.corba.classes.DlmsServerCorbaInterfaceHelper;
import comp.dsd.dlms.logger.DlmsLogger;
import comp.dsd.dlms.model.BorrowList;
import comp.dsd.dlms.model.Borrower;
import comp.dsd.dlms.model.Item;
import comp.dsd.dlms.model.ItemList;
import comp.dsd.dlms.model.WaitList;
import comp.dsd.dlms.presentation.DataPresentation;
import comp.dsd.dlms.rmi.DlmsServerRmiImpl;
import comp.dsd.dlms.rmi.DlmsServerRmiIntreface;


public class DlmsServer{
    
	//-- fields
    private String name;
    private String inetAddress;
    private int port;
    public static HashMap<String,String[]> libServersUdpInfo = new HashMap<String,String[]>();
    private DlmsLogger serverLogger;  
    private String ipcType = "";
    
    //-- corba specific
    private ORB orb;
    private String[] orbInfo;
    private DlmsServerCorbaImpl corbaObject;
    
    //-- rmi specific
    private int rmiRegistryPort;
    private DlmsServerRmiImpl rmiObject;
    
    //-- core implementation
    private DlmsServerImpl coreObject;
    //------------------------------------------ fields
    
    
    //-- constructors  
   
    // CORBA IPC specific constructor
    public DlmsServer(String name, String inetAddress, int port, String[] orbInfo) throws RemoteException{
    	this.setName(name);
    	this.setPort(port);   
    	this.setInetAddress(inetAddress);
    	libServersUdpInfo.put(name, new String[]{inetAddress,String.valueOf(port)});
    	serverLogger = new DlmsLogger(name);    
    	this.orbInfo = orbInfo;
    	ipcType = "CORBA";
    }
   
    // RMI IPC specific constructor
    public DlmsServer(String name, String inetAddress, int port, int rmiRegistryPort) throws RemoteException{
    	this.setName(name);
    	this.setPort(port);   
    	this.setInetAddress(inetAddress);
    	this.rmiRegistryPort = rmiRegistryPort;
    	libServersUdpInfo.put(name, new String[]{inetAddress,String.valueOf(port)});
    	serverLogger = new DlmsLogger(name);     	
    	ipcType = "RMI";

    }
    
    // Core implementation constructor
    public DlmsServer(String name, String inetAddress, int port) throws RemoteException{
    	this.setName(name);
    	this.setPort(port);   
    	this.setInetAddress(inetAddress);
    	libServersUdpInfo.put(name, new String[]{inetAddress,String.valueOf(port)});
    	serverLogger = new DlmsLogger(name);     	
    	ipcType = "CORE";

    }
    //------------------------------------------ constructors
    
    
	//-- methods	
	
	/**
	 * this method is responsible to create remote object of this server and register it with ORB naming service
	 */
	private void initializeCorbaServerObjectAndOrb() {
        try {
        	// create and initialize the ORB, get reference to rootpoa        	
        	this.orb = ORB.init(this.orbInfo, null);
        	
			// activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();	
			
			//get ORB Naming Context
			NamingContextExt ncRef = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
			
			// create servant and register it with the ORB Naming Service
			corbaObject = new DlmsServerCorbaImpl(this.getName(), this.getInetAddress(), this.getPort(), this.libServersUdpInfo, this.serverLogger, this.getOrb());
			corbaObject.setOrb(orb);			        	

			// get object reference from the servant
			DlmsServerCorbaInterface objRef = DlmsServerCorbaInterfaceHelper.narrow(rootpoa.servant_to_reference(corbaObject));
			

			// bind object reference in naming context
			NameComponent[] path = ncRef.to_name(this.getName());
			ncRef.rebind(path, objRef);
			
			serverLogger.log("Library Server -" + this.getName() + "- activate the orb", true);

			// wait for invocations from clients
			for (;;) {
				orb.run();
			}			
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}	
	}
	
	/**
	 * this method is responsible to create remote object of this server and register it with RMI registry
	 */
	private void initializeRmiServerObjectAndRmiReqistry() {
        try {        	
			// create RMI registry
        	Registry reg = LocateRegistry.getRegistry(this.rmiRegistryPort);	
			
        	// create server RMI object and register it with the RMI registry
			rmiObject = new DlmsServerRmiImpl(this.getName(), this.getInetAddress(), this.getPort(), this.libServersUdpInfo, this.serverLogger);
			reg.bind(this.getName(), rmiObject);			
			
			serverLogger.log("Library Server -" + this.getName() + "- bind its remote object to the RMI registry with port " + this.rmiRegistryPort, true);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}	
	}
	
	/**
	 * this method is responsible to create server object with core functionality
	 */
	private void initializeServerCoreObject() {
        try {        	
			
        	// create server core object
			coreObject = new DlmsServerImpl(this.getName(), this.getInetAddress(), this.getPort(), this.libServersUdpInfo, this.serverLogger);		
			
			serverLogger.log("Library Server -" + this.getName() + " instance is created and run its core functionality", true);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}	
	}
	
	
	/**
	 * this method is responsible to complete all pre-processing required for running the server 
	 */
	public void run() {
		try {
			
			if(ipcType.equalsIgnoreCase("CORBA")) {
				initializeCorbaServerObjectAndOrb();
			}
			else if(ipcType.equalsIgnoreCase("RMI")) {
				initializeRmiServerObjectAndRmiReqistry();	
			}
			else {
				initializeServerCoreObject();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
    //------------------------------------------ methods

	
	//-- fields accessor
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public String getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(String inetAddress) {
		this.inetAddress = inetAddress;
	}
	

	public ORB getOrb() {
		return orb;
	}


	public void setOrb(ORB orb) {
		this.orb = orb;
	}
	
	public DlmsServerCorbaImpl getCorbaObject() {
		return corbaObject;
	}
	
	public DlmsServerRmiImpl getRmiObject() {
		return rmiObject;
	}
	
	//------------------------------------------ fields accessor
	


	

}
