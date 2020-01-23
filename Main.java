import com.cognos.developer.schemas.bibus._3.SearchPathSingleObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Main {
  static CRNConnect connection;
  
  static Deployment deployment;
  
  enum Action {
    ARCHIVES, FOLDERS, EXPORT, IMPORT;
  }
  
  static String[] getSelectedPubContent(Map<String, SearchPathSingleObject> paramMap) {
    ArrayList<String> arrayList = new ArrayList();
    for (Map.Entry<String, SearchPathSingleObject> entry : paramMap.entrySet())
      arrayList.add(((SearchPathSingleObject)entry.getValue()).get_value()); 
    return (String[])arrayList.stream().toArray(paramInt -> new String[paramInt]);
  }
  
  static String[] getSelectedPackageName(HashMap paramHashMap) {
    String[] arrayOfString = null;
    if (!paramHashMap.isEmpty()) {
      Object[] arrayOfObject = paramHashMap.keySet().toArray();
      arrayOfString = new String[arrayOfObject.length];
      for (byte b = 0; b < arrayOfObject.length; b = (byte)(b + 1))
        arrayOfString[b] = (String)arrayOfObject[b]; 
    } 
    return arrayOfString;
  }
  
  static void displayUsage() {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append("USAGE: ");
    stringBuffer.append("run [options...] parms...\n");
    stringBuffer.append("Options are: [ARCHIVES | FOLDERS | EXPORT | IMPORT]\nParms are: ARCHIVE_NAME and DEPLOY_ARCHIVE\n");
    stringBuffer.append("!!! WARNING : if archive name contains space then surround its name with double quotes e.g. \"my archives with spaces\" !!!\n");
    stringBuffer.append("Examples:\n");
    stringBuffer.append("run ARCHIVES\t\t\t\t\t to list existing archives\n");
    stringBuffer.append("run FOLDERS\t\t\t\t\t to list existing forlders\n");
    stringBuffer.append("run EXPORT ARCHIVE_NAME DEPLOY_ARCHIVE\t\t to export\n");
    stringBuffer.append("run IMPORT ARCHIVE_NAME DEPLOY_ARCHIVE\t\t to import\n");
    System.out.println(stringBuffer.toString());
  }
  
  public static void main(String[] paramArrayOfString) throws Exception {
    BasicConfigurator.configure();
    Logger.getRootLogger().setLevel(Level.INFO);
    try {
      Action action = Action.valueOf(paramArrayOfString[0].toUpperCase());
      String str1 = "";
      String str2 = "";
      Properties properties = new Properties();
      properties.load(Main.class.getClassLoader().getResourceAsStream("config.properties"));
      connection = new CRNConnect();
      connection.connectToCognosServer(properties.getProperty("CMURL"));
      Logon logon = new Logon();
      String str3 = logon.quickLogon(connection, properties.getProperty("namespace"), properties.getProperty("uid"), properties.getProperty("pwd"));
      System.out.println(str3);
      deployment = new Deployment();
      String[] arrayOfString1 = null;
      boolean bool = false;
      String[] arrayOfString2 = deployment.getListOfArchives(connection);
      String[] arrayOfString3 = deployment.getAllFolders(connection);
      switch (action) {
        case ARCHIVES:
          if (paramArrayOfString.length != 1)
            throw new IllegalArgumentException(); 
          if (arrayOfString2.length > 0) {
            System.out.println(Arrays.asList(arrayOfString2));
            break;
          } 
          System.out.println("No available archives.");
          break;
        case FOLDERS:
          if (paramArrayOfString.length != 1)
            throw new IllegalArgumentException(); 
          if (arrayOfString3.length > 0) {
            System.out.println(Arrays.asList(arrayOfString3));
            break;
          } 
          System.out.println("No available folders.");
          break;
        case EXPORT:
          if (paramArrayOfString.length != 3)
            throw new IllegalArgumentException(); 
          str1 = paramArrayOfString[1];
          str2 = paramArrayOfString[2];
          for (String str : Arrays.<String>asList(arrayOfString2)) {
            if (str.trim().contains(str2))
              bool = true; 
          } 
          if (bool) {
            arrayOfString1 = getSelectedPubContent(deployment.getPubFolderContent(str2, connection));
            if (arrayOfString1.length > 0) {
              System.out.println("Exporting " + str1 + "...");
              System.out.println(deployment.deployContent("export", str1, str2, arrayOfString1, connection));
              break;
            } 
            System.out.println(str2 + " is empty.");
            break;
          } 
          System.out.println(str2 + " is not valid.");
          System.out.println("Valid archive are :\n" + Arrays.<String>asList(arrayOfString2));
          break;
        case IMPORT:
          if (paramArrayOfString.length != 3)
            throw new IllegalArgumentException(); 
          str1 = paramArrayOfString[1];
          str2 = paramArrayOfString[2];
          for (String str : Arrays.<String>asList(arrayOfString2)) {
            if (str.trim().contains(str2))
              bool = true; 
          } 
          if (bool) {
            arrayOfString1 = getSelectedPubContent(deployment.getPubFolderContent(str2, connection));
            if (arrayOfString1.length > 0) {
              System.out.println("Importing " + str1 + "...");
              System.out.println(deployment.deployContent("import", str1, str2, arrayOfString1, connection));
              break;
            } 
            System.out.println(str2 + " is empty.");
            break;
          } 
          System.out.println(str2 + " is not valid.");
          System.out.println("Valid archives are :\n" + Arrays.<String>asList(arrayOfString2));
          break;
      } 
    } catch (Exception exception) {
      displayUsage();
    } 
  }
}
