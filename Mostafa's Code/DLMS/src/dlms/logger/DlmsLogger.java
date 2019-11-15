package comp.dsd.dlms.logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;

public class DlmsLogger {
	
	//-- fields
	private String sourceLogId;
	private PrintStream sysOut = System.out;
    private PrintStream logOut;
    //------------------------------------------ fields
	
	
	//-- constructor 
	public DlmsLogger(String sourceLogId) {
		try {
			this.sourceLogId = sourceLogId;
			File logDir = new File("./logFile/");
			if(!logDir.exists())
				logDir.mkdir();
			File logFile = new File(logDir.getPath() + "/" + sourceLogId + "_LogFile.txt");
			if(!logFile.exists())
				logFile.createNewFile();
			logOut = new PrintStream(new FileOutputStream(logFile, true));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	/**
	 * this method is responsible to log the input message and print it in console 
	 * @param message - the message which should be print and logged
	 * @param doPrint - if true then also print the file else just log
	 */
	public void log(String message, boolean doPrint) {
		try {
			logOut.println("> " + LocalDateTime.now() + " : "+ message);
			logOut.println("");
			if(doPrint) {
				System.out.println(sourceLogId + " > " + message);
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} 
	}
}

