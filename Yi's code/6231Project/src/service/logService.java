package service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.ZonedDateTime;

public class logService {

	private FileWriter writer;
	private File file;
	private String name;
	private String filename;
	
	public logService(String name){
		this.name = name;
		this.filename = name + ".txt";
		file = new File(filename);
		
		if(file.exists() && file.isFile()){
			try{
				writer = new FileWriter(file,true);
			}
			catch(IOException e){
				e.printStackTrace();
			}	
		}
		else{
			try{
				file.createNewFile();
				writer = new FileWriter(file,true);
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
			
	}
	
	public void writeLog(String input){
		try {
			writer.write("\n" + ZonedDateTime.now() + input);
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void writeErrorLog(String input){
		try {
			writer.write("\n" + ZonedDateTime.now() + "Error|" + this.name + ":" + input);
			writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
