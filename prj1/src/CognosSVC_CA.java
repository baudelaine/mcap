package com.dma.svc;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import com.cognos.developer.schemas.bibus._3.BiBusHeader;
import com.cognos.developer.schemas.bibus._3.XmlEncodedXML;
//import org.apache.axis.client.Stub;
import com.cognos.org.apache.axis.client.Stub;
//import org.apache.axis.message.SOAPHeaderElement;
import com.cognos.org.apache.axis.message.SOAPHeaderElement;
import com.dma.cognos.CRNConnect;


public class CognosSVC_CA {

	private CRNConnect crnConnect;
	private String modelPath;
	private Map<String, Element> actionsMap;
	private int i;
	private String pathToXML = "res";


	public CognosSVC_CA (String cognosDispatcher) {
		crnConnect = new CRNConnect();
		crnConnect.setDispatcher(cognosDispatcher);
		crnConnect.connectToCognosServer();
		actionsMap = new HashMap<String, Element>();
		i=1;
	}
	
	public void setPathToXML(String pathToXML){
		this.pathToXML = pathToXML;
	}
	
	public String getPathToXML(){
		return this.pathToXML;
	}
	
	public Document doc = null;

	public boolean logon(String cognosLogin, String cognosPassword, String cognosNamespace) {
		try {
			StringBuilder credentialXML = new StringBuilder();

			credentialXML.append("<credential>");
			credentialXML.append("<namespace>").append(cognosNamespace).append("</namespace>");
			credentialXML.append("<username>").append(cognosLogin).append("</username>");
			credentialXML.append("<password>").append(cognosPassword).append("</password>");
			credentialXML.append("</credential>");

			String encodedCredentials = credentialXML.toString();

			crnConnect.getCMService().logon(new XmlEncodedXML(encodedCredentials), null);

			// TODO Set the BiBusHeader
			SOAPHeaderElement temp = ((Stub) crnConnect.getCMService())
					.getResponseHeader("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader");
			BiBusHeader cmBiBusHeader = (BiBusHeader) temp
					.getValueAsType(new QName("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader"));
			((Stub) crnConnect.getCMService()).setHeader("http://developer.cognos.com/schemas/bibus/3/", "biBusHeader",
					cmBiBusHeader);
			System.out.println("Logon successful as " + cognosLogin);

		} catch (RemoteException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		} catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
		return true;

	}

	public void logoff() {

		try {
			crnConnect.getCMService().logoff();
			System.out.println("logoff");
		} catch (RemoteException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
		}

	}
	
	public void openModel(String modelName, String cognosFolder) {
		
		modelPath = cognosFolder + "/" + modelName + "/" + modelName + ".cpf";
		try {
			File xmlFile = new File(pathToXML + "/openModel.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);

			Node node = document.selectSingleNode("//@model");
			node.setText(modelPath);
			XmlEncodedXML xex = new XmlEncodedXML(node.getParent().asXML());
			@SuppressWarnings("unused")
			String res = crnConnect.getMetadataService().updateMetadata(xex).toString();
			System.out.println("openModel" + modelPath + " successful");
		} catch (DocumentException ex) {
			System.out.println(ex.getMessage());
		} catch (RemoteException ex) {
			System.out.println(ex.getMessage());
		}

	}

	public void saveModel() {

		try {
			File xmlFile = new File(pathToXML + "/saveModel.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node node = document.selectSingleNode("//@model");
			node.setText(modelPath);
			XmlEncodedXML xex = new XmlEncodedXML(node.getParent().asXML());
			@SuppressWarnings("unused")
			String res = crnConnect.getMetadataService().updateMetadata(xex).toString();
			System.out.println("saveModel " + modelPath);
		} catch (DocumentException ex) {
			System.out.println(ex.getMessage());
		} catch (RemoteException ex) {
			System.out.println(ex.getMessage());
		}

	}

	
	public void closeModel() {

		try {
			File xmlFile = new File(pathToXML + "/closeModel.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			Node node = document.selectSingleNode("//@model");
			node.setText(modelPath);
			XmlEncodedXML xex = new XmlEncodedXML(node.getParent().asXML());
			@SuppressWarnings("unused")
			String res = crnConnect.getMetadataService().updateMetadata(xex).toString();
			System.out.println("closeModel " + modelPath);
		} catch (DocumentException ex) {
			System.out.println(ex.getMessage());
		} catch (RemoteException ex) {
			System.out.println(ex.getMessage());
		}

	}

	public void executeModel(Document D) {
		
		Document scriptDocument = D;

		// 1/ remove root
		@SuppressWarnings("unchecked")
		List<Node> nodes = scriptDocument.selectNodes("//transaction/action");
		
		for (Node n : nodes) {

			Element e = (Element) n.detach();
			
			actionsMap.put(String.valueOf(i), e);
			i++;			
		} 
	}

	public void executeAllActions() {
		try {
			File rootFile = new File(pathToXML + "/executeModel.xml");

			SAXReader reader = new SAXReader();

			Document rootDocument = reader.read(rootFile);
			Element root = rootDocument.getRootElement();

			Node node = rootDocument.selectSingleNode("//@model");
			node.setText(modelPath);
			
			int j=1;
			List<Element> lst = new ArrayList<Element>();
			root.addElement("transaction");
			Element t = (Element) root.selectSingleNode("//transaction");
			
			while (actionsMap.get(String.valueOf(j)) != null) {

				Element e = actionsMap.get(String.valueOf(j));
				e.addAttribute("seq", String.valueOf(j));
				lst.add(e);
				j++;
				
			}
			
			t.setContent(lst);
			// t.addAttribute("commit", "y");   //pour balise transaction
			XmlEncodedXML xex = new XmlEncodedXML(root.asXML());
			//System.out.println(root.asXML());
			@SuppressWarnings("unused")
			String res = crnConnect.getMetadataService().updateMetadata(xex).toString();
			
		} catch (DocumentException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			saveModel();
			closeModel();
			logoff();   

		} catch (RemoteException ex) {
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			saveModel();
			closeModel();
			logoff();  

		}
	}
	

}
