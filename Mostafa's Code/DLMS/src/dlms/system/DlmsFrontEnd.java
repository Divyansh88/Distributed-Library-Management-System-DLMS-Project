package comp.dsd.dlms.system;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import comp.dsd.dlms.corba.DlmsFrontEndCorbaImpl;
import comp.dsd.dlms.corba.classes.DlmsServerCorbaInterface;
import comp.dsd.dlms.corba.classes.DlmsServerCorbaInterfaceHelper;
import comp.dsd.dlms.logger.DlmsLogger;


public class DlmsFrontEnd {
	//-- fields
    public static HashMap<String,String[]> udpInfo = new HashMap<String,String[]>();
    private DlmsLogger frontEndLogger;  
    private String faultMode;
    
    //-- corba specific
    private ORB orb;
    private String[] orbInfo;
    private DlmsFrontEndCorbaImpl frontEndCorbaObject;
    
    //------------------------------------------ fields
    
    
    //-- constructors  
    public DlmsFrontEnd(String faultMode, HashMap<String,String[]> udpInfo, String[] orbInfo) throws RemoteException{   
    	this.udpInfo = udpInfo;
    	frontEndLogger = new DlmsLogger("FrontEnd");    
    	this.orbInfo = orbInfo;
    	this.faultMode = faultMode;
    }
    //------------------------------------------ constructors
    
    
	//-- methods	
	
	/**
	 * this method is responsible to create remote object of the front end and register it with ORB naming service
	 */
	private void initializeCorbaFrontEndObjectAndOrb() {
        try {
        	// create and initialize the ORB, get reference to rootpoa        	
        	this.orb = ORB.init(this.orbInfo, null);
        	
			// activate the POAManager
			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootpoa.the_POAManager().activate();	
			
			//get ORB Naming Context
			NamingContextExt ncRef = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
			
			// create servant and register it with the ORB Naming Service
			frontEndCorbaObject = new DlmsFrontEndCorbaImpl(this.faultMode, this.udpInfo, this.frontEndLogger, this.getOrb());
			frontEndCorbaObject.setOrb(orb);			        	

			// get object reference from the servant
			DlmsServerCorbaInterface objRef = DlmsServerCorbaInterfaceHelper.narrow(rootpoa.servant_to_reference(frontEndCorbaObject));
			

			// bind object reference in naming context
			NameComponent[] path = ncRef.to_name("FRONTEND");
			ncRef.rebind(path, objRef);
			
			frontEndLogger.log("Front End activate the orb", true);

			// wait for invocations from clients
			for (;;) {
				orb.run();
			}			
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}	
	}
	
	
	/**
	 * this method is responsible to complete all pre-processing required for running the front end 
	 */
	public void run() {
		try {
			
			initializeCorbaFrontEndObjectAndOrb();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
    //------------------------------------------ methods

	
	//-- fields accessor
	
	public ORB getOrb() {
		return orb;
	}

	public void setOrb(ORB orb) {
		this.orb = orb;
	}
	
	public DlmsFrontEndCorbaImpl getFrontEndCorbaObject() {
		return frontEndCorbaObject;
	}
	
	//------------------------------------------ fields accessor
	


	
}
