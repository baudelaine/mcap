package com.dma.nicomains;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import com.dma.svc.CognosSVC_CA;
import com.dma.svc.FactorySVC_CA;

public class Main_CA_PublishPackage2 {


	static CognosSVC_CA csvc;
	static FactorySVC_CA fsvc;
	
	static Properties properties = new Properties();
	static Path logFile = null;
	static Boolean debug = false;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String printLog = "";

		try {
	        
	        try {
				properties.load(Main_CA_PublishPackage2.class.getClassLoader().getResourceAsStream("conf.properties"));
			}
			catch (NullPointerException npe) {
				npe.printStackTrace();
			}
	        
			 /*
		     * 
		     *Ecrire dans le fichier de log en mode ajout 
		     * 
		     */

			logFile = Paths.get(properties.getProperty("logFile"));
			
			Files.deleteIfExists(logFile);
			
	        if(!Files.exists(logFile)){
	            try {
					Files.createFile(logFile);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            logFile.toFile().setWritable(true);
	        }
	        
	        if (properties.getProperty("modeDebug").equals("true")) {
	        	debug = true;
	        }
	        printLog = new java.util.Date().toString();
	        printLog(printLog);
			


		String cognosDefaultLocale = properties.getProperty("DefaultLocale");
		//Les locales utilisées sont déjà définies dans la création du Pack  création!=publication
		
		String cognosDispatcher = properties.getProperty("dispatcher");
		String pathToXML = "/opt/wks/v1/dmaNC/WebContent/res/templates";
		String cognosLogin = properties.getProperty("login");
		String cognosPassword = properties.getProperty("pwd");
		String cognosNamespace = properties.getProperty("namespace");
		String projectName = properties.getProperty("projectName");
		String cognosFolder = properties.getProperty("modelsPathFromCognos");
		String modelName = properties.getProperty("packageName");
		String publishFolder = properties.getProperty("publishFolder");
		

        
        if (properties.getProperty("modeDebug").equals("true")) {
        	debug = true;
        }
        
			//start
			csvc = new CognosSVC_CA(cognosDispatcher);
			csvc.setPathToXML(pathToXML);
			fsvc = new FactorySVC_CA(csvc);
			csvc.logon(cognosLogin, cognosPassword, cognosNamespace);
			csvc.openModel(projectName, cognosFolder);
			fsvc.setLocale(cognosDefaultLocale);
			
			printLog("Publish Package : " + modelName);
			fsvc.publishPackage(modelName, publishFolder);
			
			csvc.executeAllActions();
			
			csvc.saveModel();
			csvc.closeModel();
			csvc.logoff();
			
			printLog("End Publish Package : " + modelName);
	
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
		}
			
	}
	
	public static void printLog(String printLog) {
		System.out.println(printLog);	
		printLog = printLog + "\n";
		try {
			Files.write(logFile, printLog.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
