package com.dma.svc;

import java.io.File;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

public class FactorySVC_CA {

	CognosSVC_CA csvc;
	// Path contextRoot
	public FactorySVC_CA (CognosSVC_CA csvc)
	{
		this.csvc = csvc;
	}
	
	public void setLocale(String cognosDefaultLocale) {
		try {
			File xmlFile = new File(csvc.getPathToXML() + "/setLocale.xml");

			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			@SuppressWarnings("unused")
			Element root = document.getRootElement();

			Node n1 = document.selectSingleNode("//action[@type='SetActiveLocale']/inputparams/param/value");
			Node n2 = document.selectSingleNode("//action[@type='SetDefaultLocale']/inputparams/param/value");

			n1.setText(cognosDefaultLocale);
			n2.setText(cognosDefaultLocale);
			
			System.out.println("setLocale " + "ActiveLocale=" + cognosDefaultLocale + " DefaultLocale=" + cognosDefaultLocale);
			csvc.executeModel(document);
		} catch (DocumentException ex) {
			System.out.println((ex.getMessage()));
		}

	}

	
	public void publishPackage(String packageName, String folder) {

		try {

			String securityViews = "[].[securityViews]";
			
			String securityViewsPath = securityViews + ".[" + packageName + "]";
			String packagesPath = "[].[packages]";
			String packagePath = packagesPath + ".[" + packageName + "]";

			File xmlFile = new File(csvc.getPathToXML() + "/publishPackage_CA.xml");
			SAXReader reader = new SAXReader();
			Document document = reader.read(xmlFile);
			// transaction 1
			Element ElemSecurityViewsPath = (Element) document.selectSingleNode("/bmtactionlog/transaction[1]/action[1]/inputparams/param[1]/value");
			ElemSecurityViewsPath.setText(securityViewsPath);
			// 2
			Element ElemPackagePath = (Element) document.selectSingleNode("/bmtactionlog/transaction[2]/action[1]/inputparams/param[1]/value");
			ElemPackagePath.setText(packagePath);
			// 3
			ElemPackagePath = (Element) document.selectSingleNode("/bmtactionlog/transaction[3]/action[1]/inputparams/param[1]/value");
			ElemPackagePath.setText(packagePath);
			Element ElemPackageFolder = (Element) document.selectSingleNode("/bmtactionlog/transaction[3]/action[1]/inputparams/param[3]/value");
			ElemPackageFolder.setText(folder);
			Element ElemPackageName = (Element) document.selectSingleNode("/bmtactionlog/transaction[3]/action[1]/inputparams/param[4]/value");
			ElemPackageName.setText(packageName);
			
			csvc.executeModel(document);
			} catch (DocumentException ex) {
				System.out.println(ex.getMessage());
			}
		}
}
