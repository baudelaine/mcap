package com.dma.nicomains;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import com.dma.svc.CognosSVC;
import com.dma.svc.FactorySVC;
import com.dma.web.QuerySubject;
import com.dma.web.RelationShip;

public class Main_CA_PublishPackage_DataViz {

	static Map<String, Integer> gRefMap;
	static List<RelationShip> rsList;
	static Map<String, QuerySubject> query_subjects;
	static Map<String, Map<String, String>> labelMap;
	static Map<String, Map<String, String>> qsScreenTipMap;
	static Map<String, Map<String, String>> qiScreenTipMap;
	static Map<String, Map<String, String>> qifScreenTipMap;
	static Map<String, String> filterMap;
	static Map<String, String> filterMapApply;
	static Map<String, Boolean> folerMap;
	static List<QuerySubject> qsList = null;
	static CognosSVC csvc;
	static FactorySVC fsvc;
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
        /*
         * 
         *Ecrire dans le fichier de log en mode ajout 
         * 
         */

		
		
		Path logFile = Paths.get("log");

        if(!Files.exists(logFile)){
            try {
				Files.createFile(logFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
              logFile.toFile().setWritable(true);
        }

        String blablabla = "blablabla";
        try {
			Files.write(logFile, blablabla.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		String cognosDefaultLocale = "en-gb";
		String cognosLocales = "";
		
		String cognosDispatcher = "http://172.16.186.246:9300/p2pd/servlet/dispatch";
		String pathToXML = "/opt/wks/v1/dmaNC/WebContent/res/templates";
		String cognosLogin = "admin";
		String cognosPassword = "Freestyle05$";
		String cognosNamespace = "CognosEx";
		String projectName = "DataViz";
		String cognosFolder = "C:/models";
		String modelName = projectName;
							
		
			//start
			csvc = new CognosSVC(cognosDispatcher);
			csvc.setPathToXML(pathToXML);
			fsvc = new FactorySVC(csvc);
			csvc.logon(cognosLogin, cognosPassword, cognosNamespace);
			csvc.openModel(modelName, cognosFolder);
			fsvc.setLocale(cognosDefaultLocale);
			
			@SuppressWarnings("unused")
			String[] locales = {cognosLocales};
			fsvc.changePropertyFixIDDefaultLocale();
			System.out.println("Publish Package : " + modelName);
			fsvc.publishPackage(modelName,"/content/folder[@name='TestPublishFolder']");
			
			csvc.executeAllActions();
			
			csvc.saveModel();
			csvc.closeModel();
			csvc.logoff();
			
			System.out.println("End Publish Package : " + modelName);
			System.out.println("Model Generation Finished");
	}
	
}
