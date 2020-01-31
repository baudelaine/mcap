package com.dma.nicomains;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class Main_CA_Parser_Encodage {
	
	static Map<String, String> labelMap_FR;
	static Map<String, String> ScreenTipMap_FR;
	static Map<String, String> labelMap_EN;
	static Map<String, String> ScreenTipMap_EN;
	static Properties properties = new Properties();
	static Path logFile = null;
	static Boolean debug = false;
	
	public static void main(String[] args){
		// TODO Auto-generated method stub
		
		
		labelMap_FR = new HashMap<String, String>();
		ScreenTipMap_FR = new HashMap<String, String>();
		
		labelMap_EN = new HashMap<String, String>();
		ScreenTipMap_EN = new HashMap<String, String>();
		
		Boolean fr = false;
		Boolean en = false;

		String printLog = "";
		
		try {

			try {
				properties.load(Main_CA_Parser_Encodage.class.getClassLoader().getResourceAsStream("conf.properties"));
			}
			catch (NullPointerException npe) { 
				throw new Exception("Error when loading conf.properties.");
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
					throw new Exception("Error when creating log file.");
				}
	            logFile.toFile().setWritable(true);
	        }
	        
	        if (properties.getProperty("modeDebug").equals("true")) {
	        	debug = true;
	        }
	        printLog = new java.util.Date().toString();
	        printLog(logFile, printLog);
	        
			String sLocales = properties.getProperty("locales");
			String tabLocales[] = StringUtils.split(sLocales,";");

			for (int i = 0; i < tabLocales.length; i++) {
				if (tabLocales[i].equals("fr")) {
					fr = true;
					printLog = "Choosen language : fr";
					printLog(logFile, printLog);
				}
				if (tabLocales[i].equals("en")) {
					en = true;
					printLog = "Choosen language : en";
					printLog(logFile, printLog);
				}
			}
			
			Path path = Paths.get(properties.getProperty("PathToCSVDictionnary"));
			
			// Load the driver.
			Class.forName("org.relique.jdbc.csv.CsvDriver");

			// Create a connection using the directory containing the file(s)
			Properties props = new java.util.Properties();
			props.put("separator",";");
			props.put("charset", properties.getProperty("CSV_charset"));

//			props.put("suppressHeaders","true");
			Connection conn = DriverManager.getConnection("jdbc:relique:csv:" + path.getParent().toString(), props);

			// Create a Statement object to execute the query with.
			String sql = properties.getProperty("requestCSV_FR");

			PreparedStatement stm = conn.prepareStatement(sql);

			// Query the table. The name of the table is the name of the file without ".csv"
			ResultSet results = stm.executeQuery();
			while(results.next()) {
				String sNomVue = results.getString("NomVue_Physique");
				String sNomChamp = results.getString("NomChamp_Physique");
				if (fr) {
					String sLabelfr = results.getString("Label_FR");
					String sScreenTipfr = results.getString("Definition_FR");
					sLabelfr = replaceSpecialChar(sLabelfr);
					sScreenTipfr = replaceSpecialChar(sScreenTipfr);
					labelMap_FR.put(sNomVue + '.' + sNomChamp, sLabelfr);
					if (debug) {
						printLog(logFile, sNomVue + '.' + sNomChamp + ", " + sLabelfr);
					}
					ScreenTipMap_FR.put(sNomVue + '.' + sNomChamp, sScreenTipfr);
					if (debug) {
						printLog(logFile, sNomVue + '.' + sNomChamp + ", " + sScreenTipfr);
					}
				}
				if (en) {
					String sLabelen = results.getString("Label_EN");
					String sScreenTipen = results.getString("Definition_EN");
					sLabelen = replaceSpecialChar(sLabelen);
					sScreenTipen = replaceSpecialChar(sScreenTipen);
					labelMap_EN.put(sNomVue + '.' + sNomChamp, sLabelen);
					if (debug) {
						printLog(logFile, sNomVue + '.' + sNomChamp + ", " + sLabelen);
					}
					ScreenTipMap_EN.put(sNomVue + '.' + sNomChamp, sScreenTipen);
					if (debug) {
						printLog(logFile, sNomVue + '.' + sNomChamp + ", " + sScreenTipen);
					}
				}

			}
			
			if(results != null) {results.close();}
			if(stm != null) {stm.close();}
			if(conn != null) {conn.close();}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());
		}
		finally{
		}		

		String projectName = properties.getProperty("projectName");
		String javaModelsPath = properties.getProperty("modelsPathFromJava");
		Path projectPath = Paths.get(javaModelsPath + "/" + projectName);
		Charset charset = StandardCharsets.UTF_8;
		
		printLog(logFile, "START MODEL.XML MODIFICATIONS");
		try {
			
			String modelSharedPath = projectPath + "/model.xml";
			
			Path input = Paths.get(modelSharedPath);
			Path output = Paths.get(modelSharedPath);
			
			String datas = null;
						
			String inputSearch = "xmlns=\"http://www.developer.cognos.com/schemas/bmt/60/12\"";
			String outputSearch = "queryMode=\"dynamic\"";
			String outputReplace = outputSearch + " " + inputSearch;
			
			if(Files.exists(input)){
				datas = new String(Files.readAllBytes(input), charset);
			}

			datas = StringUtils.replace(datas, inputSearch, "");
			
			// modifs XML
			
			SAXReader reader = new SAXReader();
			Document document = reader.read(new ByteArrayInputStream(datas.getBytes(charset)));

			String namespaceName = properties.getProperty("searchNamespace");
			String spath = "/project/namespace/namespace";
			int k=1;
			
			Element namespace = (Element) document.selectSingleNode(spath + "[" + k + "]/name");
			while(!namespace.getStringValue().equals(namespaceName) && namespace != null)
			{
			k++;
			namespace = (Element) document.selectSingleNode(spath + "[" + k + "]/name");
			}
			
			spath = spath + "[" + k + "]";
			
			printLog(logFile, "Find namespace : " + namespace.getStringValue());

			if (fr) {
				printLog(logFile, "START MODEL.XML MODIFICATIONS FR");
				recursiveParserQS(document, spath, "fr", labelMap_FR, ScreenTipMap_FR);
			}
			if (en) {
				printLog(logFile, "START MODEL.XML MODIFICATIONS EN");
				recursiveParserQS(document, spath, "en", labelMap_EN, ScreenTipMap_EN);
			}
			
			try {

				datas = document.asXML();

				datas = StringUtils.replace(datas, outputSearch, outputReplace);
			
				printLog(logFile, "END MODEL.XML MODIFICATION : " + new java.util.Date().toString());
				Files.write(output, datas.getBytes());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				throw new Exception("Error when writing to output file.");
			}
			
			// fin test writer
			
		} catch (DocumentException | IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		
	}
	
	public static void recursiveParserQS(Document document, String spath, String locale, Map<String, String> labelMap, Map<String, String> screenTiplMap) {
		
		int i = 1;
		Element qsname = (Element) document.selectSingleNode(spath + "/querySubject[" + i + "]/name");
		
		int j = 1;
		Element qsfname = (Element) document.selectSingleNode(spath + "/folder[" + j + "]/name");
				
		while (qsfname != null)
		{
			if (debug) {
				printLog(logFile, "FIND QuerySubject Directory : " + qsfname.getStringValue());
			}
			
			String nextFPath = spath + "/folder[" + j + "]";
			recursiveParserQS(document, nextFPath, locale, labelMap, screenTiplMap);
			j++;
			qsfname = (Element) document.selectSingleNode(spath + "/folder[" + j + "]/name");
		}
		
		while (qsname != null)
		{
			
			if (debug) {
				printLog(logFile, "FIND QuerySubject : " + qsname.getStringValue());
			}
			
			String nextQIPath = spath + "/querySubject[" + i + "]";
			recursiveParserQI(document, nextQIPath, locale, labelMap, screenTiplMap, qsname.getStringValue(), "");
			i++;
			qsname = (Element) document.selectSingleNode(spath + "/querySubject[" + i + "]/name");
		}
	}
	
	public static void recursiveParserQI(Document document, String spath, String locale, Map <String, String> labelMap, Map <String, String> screenTipMap, String qsFinal, String prefix) {
		
		int i = 1;
		Element qiname = (Element) document.selectSingleNode(spath + "/queryItem[" + i + "]/name");
		Element qiNameLocale = (Element) document.selectSingleNode(spath + "/queryItem[" + i + "]/name[@locale=\"" + locale + "\"]");
		Element qiScreenTipLocale = (Element) document.selectSingleNode(spath + "/queryItem[" + i + "]/screenTip[@locale=\"" + locale + "\"]");
		
		int j = 1;
		Element qifname = (Element) document.selectSingleNode(spath + "/queryItemFolder[" + j + "]/name");
		Element qifNameLocale = (Element) document.selectSingleNode(spath + "/queryItemFolder[" + j + "]/name[@locale=\"" + locale + "\"]");
		
		while (qifname != null)
		{
			if (debug) {
				printLog(logFile, "FIND QueryItem Folder : " + qifname.getStringValue() + " GET locale value : " + qifNameLocale.getStringValue());
			}
			
			prefix = "(" + qifNameLocale.getStringValue() + ") ";
			String nextsPath = spath + "/queryItemFolder[" + j + "]";
			// cas de répertoires ou on a mis le TEC_PARTY
			if (qifname.getStringValue().startsWith(".")) {
				if (debug) {
					printLog(logFile, " startsWith(\".\") : IT'S TEC_PARTY REFERENCE");
				}
				recursiveParserQI(document, nextsPath, locale, labelMap, screenTipMap, "TEC_PARTY", prefix);
			}
			else {
				if (debug) {
					printLog(logFile, "startsWith(\".\") : NO, IT'S NOT TEC_PARTY REFERENCE");
				}
				recursiveParserQI(document, nextsPath, locale, labelMap, screenTipMap, qsFinal, "");
			}
			// end cas TEC_PARTY
			prefix = "";
			j++;
			qifname = (Element) document.selectSingleNode(spath + "/queryItemFolder[" + j + "]/name");
			qifNameLocale = (Element) document.selectSingleNode(spath + "/queryItemFolder[" + j + "]/name[@locale=\"" + locale + "\"]");
		}
		
		while (qiname != null)
		{
			String fieldNameTab[] = StringUtils.split(qiname.getStringValue(), ".");
			String fieldName = fieldNameTab[fieldNameTab.length - 1];
			//Label
			String label = prefix + labelMap.get(qsFinal + "." + fieldName);
			if (labelMap.get(qsFinal + "." + fieldName) != null) {
				if (debug) {
					printLog(logFile, "Traduction exists : " + label  + " (" + fieldName + ")");
				}
				qiNameLocale.setText(label  + " (" + fieldName + ")");      // valeur de map
				} else {
					if (debug) {
						printLog(logFile, "Traduction doesn't exist : " + prefix + " (" + fieldName + ")");
					}
					qiNameLocale.setText(prefix + " (" + fieldName + ")");
				}
			String desc = "";
			if (screenTipMap.get(qsFinal + "." + fieldName) != null) {
				desc = " " + screenTipMap.get(qsFinal + "." + fieldName);
			}
			String screenTip = "(" + qsFinal + "." + fieldName + ")" + desc;
			if (qiScreenTipLocale != null) {
				if (debug) {
					printLog(logFile, qsFinal + "." + fieldName + " ScreenTip : " + screenTip);
				}
				qiScreenTipLocale.setText(screenTip);
			}
			i++;
			qiname = (Element) document.selectSingleNode(spath + "/queryItem[" + i + "]/name");
			qiNameLocale = (Element) document.selectSingleNode(spath + "/queryItem[" + i + "]/name[@locale=\"" + locale + "\"]");
			qiScreenTipLocale = (Element) document.selectSingleNode(spath + "/queryItem[" + i + "]/screenTip[@locale=\"" + locale + "\"]");
			
		}
	}
	
	public static String replaceSpecialChar(String s) {
		s = s.replaceAll("[\\u00c2\\u0092]", "'");
		return s;
	}
	
	public static void printLog(Path logFile, String printLog) throws Exception{
		System.out.println(printLog);	
		printLog = printLog + "\n";
		try {
			Files.write(logFile, printLog.getBytes(), StandardOpenOption.APPEND);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new Exception("Error when writing to log file.");
		}
	}
}
