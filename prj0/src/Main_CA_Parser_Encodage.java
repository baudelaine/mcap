package com.dma.nicomains;


import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
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
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class Main_CA_Parser_Encodage {
	
	static Map<String, String> labelMap_FR;
	static Map<String, String> ScreenTipMap_FR;
	static Map<String, String> labelMap_EN;
	static Properties properties = new Properties();
	
	static final Pattern pattern = Pattern.compile("[^\\w\\s\"«»\\(\\)\\[\\]\\.,!;\\{\\}%\\*/&~]\'", Pattern.UNICODE_CHARACTER_CLASS);

	public static void main(String[] args){
		// TODO Auto-generated method stub
		
		
//		BasicConfigurator.configure();
//	    Logger.getRootLogger().setLevel(Level.DEBUG);
		
		Path logFile = Paths.get("log");
	
		labelMap_FR = new HashMap<String, String>();
		ScreenTipMap_FR = new HashMap<String, String>();
		
		String s = properties.getProperty("ActiveLocale");
		System.out.println("Properties : " + s);


		try {

			try {
				properties.load(Main_CA_Parser_Encodage.class.getClassLoader().getResourceAsStream("config.properties"));
			}
			catch (NullPointerException npe) { 
				Path propsFile = Paths.get("/opt/wks/v1/dmaNC/WebContent/res/conf.properties");
				properties.load(new FileInputStream(propsFile.toFile()));
			}

			/*
			 * 
			 *Ecrire dans le fichier de log en mode ajout 
			 * 
			 */

		        if(!Files.exists(logFile)){
                		Files.createFile(logFile);
	                	logFile.toFile().setWritable(true);
        		}

			String blablabla = "blablabla";
			Files.write(logFile, blablabla.getBytes(), StandardOpenOption.APPEND);
			
			
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
				String sLabelfr = results.getString("Label_FR");
				String sScreenTipfr = results.getString("Definition_FR");

				sLabelfr = replaceSpecialChar(sLabelfr);

				labelMap_FR.put(sNomVue + '.' + sNomChamp, sLabelfr);
				ScreenTipMap_FR.put(sNomVue + '.' + sNomChamp, sScreenTipfr);
				System.out.println(sNomVue + '.' + sNomChamp + "*****" + sLabelfr);
			}
			
			if(results != null) {results.close();}
			if(stm != null) {stm.close();}
			if(conn != null) {conn.close();}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
		}		
		

		String projectName = properties.getProperty("projectName");
		String javaModelsPath = properties.getProperty("modelsPathFromJava");
		Path projectPath = Paths.get(javaModelsPath + "/" + projectName);
		Charset charset = StandardCharsets.UTF_8;
		
		System.out.println("START XML MODIFICATION");
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
			System.out.println("Find namespace : " + namespace.getStringValue());

			if (properties.getProperty("locales").contains("fr")) {
				recursiveParserQS(document, spath, "fr", labelMap_FR, ScreenTipMap_FR);
			}
			
			try {

				datas = document.asXML();

				datas = StringUtils.replace(datas, outputSearch, outputReplace);
			
				Files.write(output, datas.getBytes());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// fin test writer
			
		} catch (DocumentException | IOException e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	System.out.println("END XML MODIFICATION");		
		//publication
		
	}
	
	public static void recursiveParserQS(Document document, String spath, String locale, Map<String, String> labelMap, Map<String, String> screenTiplMap) {
		
		int i = 1;
		Element qsname = (Element) document.selectSingleNode(spath + "/querySubject[" + i + "]/name");
		
		int j = 1;
		Element qsfname = (Element) document.selectSingleNode(spath + "/folder[" + j + "]/name");
				
		while (qsfname != null)
		{
			String nextFPath = spath + "/folder[" + j + "]";
			recursiveParserQS(document, nextFPath, locale, labelMap, screenTiplMap);
			j++;
			qsfname = (Element) document.selectSingleNode(spath + "/folder[" + j + "]/name");
		}
		
		while (qsname != null)
		{
			System.out.println(qsname.getStringValue() + " *    *     *     *     *    * " + i /* + map.get(qsname.getStringValue())*/);  // clef de map
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
			System.out.println(" *   " + qifname.getStringValue() + " * * * * * * * " + qifNameLocale.getStringValue());  // clef de map
			
			prefix = "(" + qifNameLocale.getStringValue() + ") ";
			String nextsPath = spath + "/queryItemFolder[" + j + "]";
			// cas de répertoires ou on a mis le TEC_PARTY
			if (qifname.getStringValue().startsWith(".")) {
				recursiveParserQI(document, nextsPath, locale, labelMap, screenTipMap, "TEC_PARTY", prefix);
			}
			else {
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
				qiNameLocale.setText(label  + " (" + fieldName + ")");      // valeur de map
				System.out.println(qiname.getStringValue() + " **** " + label + " (" + fieldName + ")");
				} else {
					qiNameLocale.setText(prefix + fieldName);
					System.out.println(qiname.getStringValue() + " **** " + prefix + fieldName);
				}
			String desc = "";
			if (screenTipMap.get(qsFinal + "." + fieldName) != null) {
				desc = " " + screenTipMap.get(qsFinal + "." + fieldName);
			}
			String screenTip = "(" + qsFinal + "." + fieldName + ")" + desc;
			if (qiScreenTipLocale != null) {
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
	
	public static void printBytes(byte[] array, String name) {
		   for (int k = 0; k < array.length; k++) {
		       System.out.println(name + "[" + k + "] = " + "0x" +
		           UnicodeFormatter.byteToHex(array[k]));
		   }
		}

		    public static class UnicodeFormatter  {
		   	 
		 	  static public String byteToHex(byte b) {
		 	     // Returns hex String representation of byte b
		 	     char hexDigit[] = {
		 	        '0', '1', '2', '3', '4', '5', '6', '7',
		 	        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
		 	     };
		 	     char[] array = { hexDigit[(b >> 4) & 0x0f], hexDigit[b & 0x0f] };
		 	     return new String(array);
		 	  }
		 	 
		 	  static public String charToHex(char c) {
		 	     // Returns hex String representation of char c
		 	     byte hi = (byte) (c >>> 8);
		 	     byte lo = (byte) (c & 0xff);
		 	     return byteToHex(hi) + byteToHex(lo);
		 	  }
		 	 
		 	} // class	   
}
