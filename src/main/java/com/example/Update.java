package com.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.w3c.dom.Attr;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import com.sforce.soap.enterprise.Connector;
import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.ws.ConnectorConfig;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


@Path("/setMercuryNetworkStatus")
public class Update extends HttpServlet{
	
	static final String USERNAME = "vipapi@vipmtginc.com";
	static final String PASSWORD = "Spear@vip20153ZA7AuisoxbbVhx8q3E926Vi";
	static final String AUTHENDPOINT = "https://login.salesforce.com/services/Soap/c/40.0";

	static EnterpriseConnection connection;

	@POST
	@Produces(MediaType.TEXT_XML)
	public String parseXML(String xmlRecords , @Context ServletContext context) throws Exception {

		com.sforce.soap.SpearAppraisalAPI.OrderResponse response = null;
		String pathPdf = context.getRealPath("/WEB-INF/Appraisal/");
		System.out.println("pathpdf is :::::" + pathPdf);
		String decodedValue = "";
		
		xmlRecords = URLDecoder.decode(xmlRecords);
		
		 Map<String, String> xmlDataMap = new HashMap<String, String>();
		 ConnectorConfig config = new ConnectorConfig();
		 config.setUsername(USERNAME);
		 config.setPassword(PASSWORD);
		 config.setAuthEndpoint(AUTHENDPOINT);
		 String trackingId = "";

		try {

			connection = Connector.newConnection(config);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlRecords));
			Document doc = db.parse(is);

			NodeList loanNodes = doc.getElementsByTagName("LOANNUMBER");
			if(loanNodes.getLength() > 0){
				Element loanElm = (Element) loanNodes.item(0);
				xmlDataMap.put("LOANNUMBER", getCharacterDataFromElement(loanElm));
			}
			
			NodeList stNodes = doc.getElementsByTagName("STATUSNAME");
			if(stNodes.getLength() > 0){
				Element stElm = (Element) stNodes.item(0);
				xmlDataMap.put("STATUSNAME", getCharacterDataFromElement(stElm));
			}

			NodeList nodes1 = doc.getElementsByTagName("TRACKINGID");
			if(nodes1.getLength() > 0){
				Element line2 = (Element) nodes1.item(0);
				xmlDataMap.put("TRACKINGID", getCharacterDataFromElement(line2));
				trackingId = getCharacterDataFromElement(line2);
			}

			NodeList statusNode = doc.getElementsByTagName("STATUSID");
			if(statusNode.getLength() > 0){
				Element statusElement = (Element) statusNode.item(0);
				xmlDataMap.put("STATUSID", getCharacterDataFromElement(statusElement));
			}

			NodeList statusTSNode = doc.getElementsByTagName("STATUSTIMESTAMP");
			if(statusTSNode.getLength() > 0){
				Element statusTSElement = (Element) statusTSNode.item(0);
				xmlDataMap.put("STATUSTIMESTAMP", getCharacterDataFromElement(statusTSElement));
			}
			
			NodeList dueDateNode = doc.getElementsByTagName("DUEDATE");
			if(dueDateNode.getLength() > 0){
				Element dueDateElement = (Element) dueDateNode.item(0);
				xmlDataMap.put("DUEDATE", getCharacterDataFromElement(dueDateElement));
			}
			
			
			
			NodeList nodes = doc.getElementsByTagName("STATUSCOMMENT");
			Element line1 = (Element) nodes.item(0);
			xmlDataMap.put("STATUSCOMMENTTEXT", getCharacterDataFromElement(line1));
			//System.out.println("Statuscomment body is : " + getCharacterDataFromElement(line1));
			
			for (int i = 0; i < nodes.getLength(); i++) {
			Element element = (Element) nodes.item(i);
			NodeList documentNameNodes = element.getElementsByTagName("DOCUMENT");
			for(int j = 0 ; j < documentNameNodes.getLength() ; j++){

				Element docElement = (Element) documentNameNodes.item(j);
				Node currentItem = documentNameNodes.item(j);
				//String attributeKey = currentItem.getAttributes().getNamedItem("Name").getNodeValue();
				NodeList contentNodelist = docElement.getElementsByTagName("CONTENT");
				
				//System.out.println("checking type is :::" + docElement.getAttribute("TYPE"));;
				
				for(int k = 0;k < contentNodelist.getLength();k++){

					Element contentElement = (Element) contentNodelist.item(k);
					if(docElement.getAttribute("TYPE") != null && docElement.getAttribute("TYPE").contains("MISMO")){
						decodedValue = decodeEncodedValue(getCharacterDataFromElement(contentElement).replace(" ", "+"));
						//createXML(getCharacterDataFromElement(contentElement).replace(" ", "+"),pathPdf);
					}
					
					if(docElement.getAttribute("TYPE") != null && docElement.getAttribute("TYPE").contains("Appraisal")){
						//mismoPDF = decodeEncodedValue(getCharacterDataFromElement(contentElement).replace(" ", "+"));
						createPDF(getCharacterDataFromElement(contentElement).replace(" ", "+"),pathPdf);
					}
				}
			}
			
			}//end of if

			com.sforce.soap.SpearAppraisalAPI.SoapConnection soap = com.sforce.soap.SpearAppraisalAPI.Connector.newConnection("","");
			System.out.println("session is" + config.getSessionId());
			soap.setSessionHeader(config.getSessionId());
			System.out.println("**********************************"+config.getServiceEndpoint());
			
		String endpointurl=	config.getServiceEndpoint().split("/services")[0] ; 
			
			
			XStream xStream = new XStream(new DomDriver());
			xStream.alias("map", java.util.Map.class);
			String xmlDataString = xStream.toXML(xmlDataMap);
			
			//System.out.println("xmlDataString" + xmlDataString);
			
			//System.out.println("decodedValue is ::::" + decodedValue);
			
			String xmlString = createXML(xmlDataMap,decodedValue);
			
			System.out.println("xmlString :::::::::" + xmlString);
			
			//com.sforce.soap.SpearAppraisalAPI.OrderResponse response = soap.setMercuryNetworkStatus(xmlDataString);///caspers's code
			response = soap.setMercuryNetworkStatus(xmlString);
			//System.out.println("endpointurl :::::::::" + endpointurl); 
			//System.out.println("response.getErrorDescription() is" + response.getErrorDescription());
			
			String mnNetworkId = response.getErrorDescription().split(" ")[6];
			String mnResponse = createXMLForRsponse(mnNetworkId);
			String caseid=response.getErrorDescription().split("&&")[1].split("&&")[0];
			postChatter(endpointurl, caseid, config.getSessionId(),pathPdf );
			//return response.getErrorDescription();
			return mnResponse;
			
		} // end of try
		catch (Exception e) {
		e.printStackTrace();
		//return e.printStackTrace();
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		
		sendEmailToAdminWithTracking(response.getErrorDescription(),sw.toString(),trackingId);
		
		return response.getErrorDescription() + sw.toString();
		}
		
	}
	
	public static void sendEmailToAdminWithTracking(String errorDesc , String writer,String trackingId){
		try{
			
			final String form1 ="sumit.km@teclever.com"; 
			final String pwd = "sumit906088";
			
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
    		props.put("mail.smtp.starttls.enable", "true");
    		props.put("mail.smtp.host", "smtp.gmail.com");
    		props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
    		props.put("mail.debug.auth", "true");
			props.put("mail.smtp.port", "587");
    		props.setProperty("mail.transport.protocol", "smtp");
    		
    		
    		try { 
    			Authenticator auth = new Authenticator() {
    				protected PasswordAuthentication getPasswordAuthentication() {
    					return new PasswordAuthentication(form1, pwd);
    				}
    			};
    			javax.mail.Session session = javax.mail.Session.getInstance(props,auth); 
    			javax.mail.Transport transport = session.getTransport(); 
    			transport.connect(); 
    					Message message = new MimeMessage(session); 
    					message.setFrom(new InternetAddress("sumit.km@teclever.com"));
    					message.setRecipients(Message.RecipientType.TO,InternetAddress.parse("pavlel@vipmtginc.com"));
    					message.setSubject("Heroku/Salesforce Error Log "); 
    					message.setText(errorDesc+" Tracking Id Being Sent Is :   "+trackingId + writer); 
    					Transport.send(message);
    		}catch(Exception ex){
    			ex.printStackTrace();
    		}
			
			
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

	public static String getCharacterDataFromElement(Element e) {

		try{
			Node child = e.getFirstChild();
			if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
			}	
		}catch(Exception ex){
			ex.printStackTrace();
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			
			sendEmailToAdmin("Error While Parsing Request XML Sent By MN",sw.toString());
		}
		
		return "";
		
	}
	
	public static String decodeEncodedValue(String encodedValue){

		String decodesString = "";
		try{
		byte[] decodedValue = Base64.getDecoder().decode(encodedValue); 
		decodesString = new String(decodedValue);
		//System.out.println(decodesString);
		}catch(Exception ex){
		ex.printStackTrace();
		
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		
		sendEmailToAdmin("Error While Docoding Encoding Values",sw.toString());
		
		}
		return decodesString;
		
		
	}
	
	public String createXML(Map<String, String> xmlDataMap,String decodedString) throws Exception{
	String retString = "";
	try{

		String parsedXML = parsinAndCreatinXML(decodedString);
		
		System.out.println("decodedString is " + parsedXML);
		
		//String hardcoded = " <VALUATION_RESPONSE><VALUATION PropertyAppraisedValueAmount=\"170000\" AppraisalEffectiveDate=\"2005-07-22\"></VALUATION><PROPERTY><STRUCTURE PropertyStructureBuiltYear=\"2\" LivingUnitCount=\"1\" _AccessoryUnitExistsIndicator=\"N\"></STRUCTURE></PROPERTY><PARTIES><APPRAISER Name=\"Test Appraiser\" CompanyName=\"Test Company\"><APPRAISER_LICENSE Type=\"License\" Identifier=\"OK-201\"></APPRAISER_LICENSE></APPRAISER><SUPERVISOR><APPRAISER_LICENSE Type=\"License\" Identifier=\"OK-301\"></APPRAISER_LICENSE></SUPERVISOR></PARTIES></VALUATION_RESPONSE>";
		String encoded = Base64.getEncoder().encodeToString(parsedXML.getBytes(StandardCharsets.UTF_8));
		
		// System.out.println("Base64 Encoded String : " + new String(encoded));
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();
		
		Element rootElement = doc.createElement("STATUS");
		doc.appendChild(rootElement);
		
		if(xmlDataMap.containsKey("LOANNUMBER")){
			 
			 Element loanNumber = doc.createElement("LOANNUMBER");
			 loanNumber.appendChild(doc.createTextNode(xmlDataMap.get("LOANNUMBER")));
			 rootElement.appendChild(loanNumber);
		 }
	    
	    if(xmlDataMap.containsKey("STATUSNAME")){
	   	 
	   	 Element statusName = doc.createElement("STATUSNAME");
	   	 statusName.appendChild(doc.createTextNode(xmlDataMap.get("STATUSNAME")));
	   	 rootElement.appendChild(statusName);
	    }
	    
	    if(xmlDataMap.containsKey("TRACKINGID")){
	   	 
	   	 Element trackingId = doc.createElement("TRACKINGID");
	   	 trackingId.appendChild(doc.createTextNode(xmlDataMap.get("TRACKINGID")));
	   	 rootElement.appendChild(trackingId);
	    }
	    
	    if(xmlDataMap.containsKey("STATUSID")){
	   	 
	   	 Element statusId = doc.createElement("STATUSID");
	   	 statusId.appendChild(doc.createTextNode(xmlDataMap.get("STATUSID")));
	   	 rootElement.appendChild(statusId);
	    }
	    
	    if(xmlDataMap.containsKey("STATUSTIMESTAMP")){
	   	 
	   	 Element statusTime = doc.createElement("STATUSTIMESTAMP");
	   	 statusTime.appendChild(doc.createTextNode(xmlDataMap.get("STATUSTIMESTAMP")));
	   	 rootElement.appendChild(statusTime);
	    }
	    
	    if(xmlDataMap.containsKey("DUEDATE")){
		   	 Element dueDate = doc.createElement("DUEDATE");
		   	 dueDate.appendChild(doc.createTextNode(xmlDataMap.get("DUEDATE")));
		   	 rootElement.appendChild(dueDate);
		    
	    }
	    
	    
		
		Element statusCmnt = doc.createElement("STATUSCOMMENT");
		if(xmlDataMap.containsKey("STATUSCOMMENTTEXT")){
			System.out.println("oooooooooooooooooo");
			statusCmnt.appendChild(doc.createTextNode(xmlDataMap.get("STATUSCOMMENTTEXT")));
		}
		
		Element documentXml = doc.createElement("DOCUMENT");
		Attr attr = doc.createAttribute("Name");
		attr.setValue("MISMORpt.XML");
		documentXml.setAttributeNode(attr);
		
		Attr attr2 = doc.createAttribute("Type");
		attr2.setValue("MISMO 2.6 GSE");
		documentXml.setAttributeNode(attr2);
		
		Element contentElement = doc.createElement("CONTENT");
		contentElement.appendChild(doc.createTextNode(new String(encoded)));
		documentXml.appendChild(contentElement);
		
		statusCmnt.appendChild(documentXml);
		rootElement.appendChild(statusCmnt);
		retString = printXmlDocument(doc);
		
	}catch(Exception ex){
	   ex.printStackTrace();
	   
	   StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		
		sendEmailToAdmin("Error While Creating XML To Send To Salesforce",sw.toString());
	}
	retString = retString.replace("&gt;", ">");
	retString = retString.replace("&lt;", "<");
	return retString;
	
	} 
	
	public static String printXmlDocument(Document document) {
		String xml = "";
		try{
		DOMImplementationLS domImplementationLS = (DOMImplementationLS) document.getImplementation();
		LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
		xml = lsSerializer.writeToString(document);
		// System.out.println("1111111111111111111111111111" + xml);
		}catch(Exception ex){
		ex.printStackTrace();
		
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		
		sendEmailToAdmin("Error While Creating XML For Response",sw.toString());
		}
		
		return xml;
		
	}
	
	public static String parsinAndCreatinXML(String xmlValue){
	String parsedStrin = "";
	try{
		NamedNodeMap namedNodeattr = null;
		Element usableElement = null;
		
		//String xmlValue = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><VALUATION_RESPONSE MISMOVersionID=\"2.6GSE\"><VALUATION_DUMMY PropertyAppraisedValueAmount=\"101\"></VALUATION_DUMMY><VALUATION PropertyAppraisedValueAmount=\"170000\" AppraisalEffectiveDate=\"2005-07-22\"></VALUATION><PROPERTY><STRUCTURE PropertyStructureBuiltYear=\"2\" LivingUnitCount=\"1\" _AccessoryUnitExistsIndicator=\"N\"></STRUCTURE></PROPERTY><PARTIES ><APPRAISER _Name=\"Test Appraiser\" _CompanyName=\"Test Company\"><APPRAISER_LICENSE _Type=\"License\" _Identifier=\"OK-201\"></APPRAISER_LICENSE></APPRAISER><SUPERVISOR><APPRAISER_LICENSE _Type=\"License\" _Identifier=\"OK-301\"></APPRAISER_LICENSE></SUPERVISOR></PARTIES></VALUATION_RESPONSE>";
		 
		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db = dbf.newDocumentBuilder();
	     
	     Element rootElement = null;
	     Attr createAttribute = null;
	     
	     InputSource is = new InputSource();
	     is.setCharacterStream(new StringReader(xmlValue));
	     Document doc = db.parse(is);
	     
	     DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		 DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	     Document createdoc = docBuilder.newDocument();
	     
	     NodeList mainNodeList = doc.getElementsByTagName("VALUATION_RESPONSE");
	     //System.out.println("mainNodeList.getLength()" + mainNodeList.getLength());
	     for(int i= 0 ; i < mainNodeList.getLength();i++){//first for
	    	  Element rootValuationElement = (org.w3c.dom.Element) mainNodeList.item(i);
	    	  //usableElement.
	    	  namedNodeattr = rootValuationElement.getAttributes();
	    	 //el.getAttribute("type_id").equals("4218")
	    	 if (rootValuationElement.hasAttribute("MISMOVersionID")) {//mismo if check  && namedNodeattr.getLength() == 1
	    		 rootElement = createdoc.createElement("VALUATION_RESPONSE");
	    		 createAttribute = createdoc.createAttribute("MISMOVersionID");
	    		 createAttribute.setValue(rootValuationElement.getAttribute("MISMOVersionID"));
				 rootElement.setAttributeNode(createAttribute);
	    		 
	    		 NodeList valuationNodeList = rootValuationElement.getElementsByTagName("VALUATION");
	    		 for(int j= 0 ; j < valuationNodeList.getLength();j++){//valuation for
	    			 Element valElement = (org.w3c.dom.Element) valuationNodeList.item(j);
	    			 namedNodeattr = valElement.getAttributes();
	    			 //if (valElement.hasAttribute("PropertyAppraisedValueAmount") && valElement.hasAttribute("AppraisalEffectiveDate") && namedNodeattr.getLength() == 2) {
	    				 Element valuationElement = createdoc.createElement("VALUATION");

	    				 createAttribute = createdoc.createAttribute("PropertyAppraisedValueAmount");
	    				 createAttribute.setValue(valElement.getAttribute("PropertyAppraisedValueAmount"));
						 valuationElement.setAttributeNode(createAttribute);
							
						 createAttribute = createdoc.createAttribute("AppraisalEffectiveDate");
						 createAttribute.setValue(valElement.getAttribute("AppraisalEffectiveDate"));
						 valuationElement.setAttributeNode(createAttribute);
							
						 rootElement.appendChild(valuationElement);
	    				 
	    			 //}
	    		 }//end of valuation for
	    		 
	    		 NodeList propertyNodeList = rootValuationElement.getElementsByTagName("PROPERTY");
	    		 System.out.println("lklk" + propertyNodeList.getLength());
	    		 for(int j= 0 ; j < propertyNodeList.getLength();j++){//property for
	    			 Element propertyElement = createdoc.createElement("PROPERTY");
	    			 usableElement = (org.w3c.dom.Element) propertyNodeList.item(j);
	    			 NodeList structureNodeList = usableElement.getElementsByTagName("STRUCTURE");
	    			 for(int k = 0 ; k < structureNodeList.getLength();k++){
	    				//valuation for
		    			 usableElement = (org.w3c.dom.Element) structureNodeList.item(k);
		    			 namedNodeattr = usableElement.getAttributes();
		    			// if (usableElement.hasAttribute("PropertyStructureBuiltYear") && usableElement.hasAttribute("_AccessoryUnitExistsIndicator") && usableElement.hasAttribute("LivingUnitCount") && namedNodeattr.getLength() == 3) {
		    				 Element structureElement = createdoc.createElement("STRUCTURE");

		    				 createAttribute = createdoc.createAttribute("PropertyStructureBuiltYear");
		    				 createAttribute.setValue(usableElement.getAttribute("PropertyStructureBuiltYear"));
		    				 structureElement.setAttributeNode(createAttribute);
								
							 createAttribute = createdoc.createAttribute("_AccessoryUnitExistsIndicator");
							 createAttribute.setValue(usableElement.getAttribute("_AccessoryUnitExistsIndicator"));
							 structureElement.setAttributeNode(createAttribute);
							 
							 createAttribute = createdoc.createAttribute("LivingUnitCount");
							 createAttribute.setValue(usableElement.getAttribute("LivingUnitCount"));
							 structureElement.setAttributeNode(createAttribute);
							 propertyElement.appendChild(structureElement);
							 rootElement.appendChild(propertyElement);
		    			 //}
		    		 
	    			 }
	    		 }// end of property for 
	    		 
	    		 NodeList partyNodeList = rootValuationElement.getElementsByTagName("PARTIES");
	    		 //System.out.println("partyNodeList" + partyNodeList.getLength());
	    		 for(int j= 0 ; j < partyNodeList.getLength();j++){//property for
	    			 Element partyElement = createdoc.createElement("PARTIES");
	    			 usableElement = (org.w3c.dom.Element) partyNodeList.item(j);
	    			 Element partyUsableElement = (org.w3c.dom.Element) partyNodeList.item(j);
	    			 
	    			 NodeList appraiserNodeList = usableElement.getElementsByTagName("APPRAISER");
	    			 for(int k = 0 ; k < appraiserNodeList.getLength();k++){//appraiser
	    				//valuation for
		    			 usableElement = (org.w3c.dom.Element) appraiserNodeList.item(k);
		    			 namedNodeattr = usableElement.getAttributes();
		    			 //if (usableElement.hasAttribute("_Name") && usableElement.hasAttribute("_CompanyName")  && namedNodeattr.getLength() == 2) {
		    				 Element appraiserElement = createdoc.createElement("APPRAISER");

		    				 createAttribute = createdoc.createAttribute("_CompanyName");
		    				 createAttribute.setValue(usableElement.getAttribute("_CompanyName"));
		    				 appraiserElement.setAttributeNode(createAttribute);
								
							 createAttribute = createdoc.createAttribute("_Name");
							 createAttribute.setValue(usableElement.getAttribute("_Name"));
							 appraiserElement.setAttributeNode(createAttribute);
							 
							 //partyElement.appendChild(structureElement);/
							 
							 
			    			 NodeList appraiserLicenceNodeList = usableElement.getElementsByTagName("APPRAISER_LICENSE");
			    			 for(int l = 0 ; l < appraiserLicenceNodeList.getLength();l++){
			    				//valuation for
				    			 usableElement = (org.w3c.dom.Element) appraiserLicenceNodeList.item(k);
				    			 namedNodeattr = usableElement.getAttributes();
				    			 //if (usableElement.hasAttribute("_Type") && usableElement.hasAttribute("_Identifier")  && namedNodeattr.getLength() == 2) {
				    				 Element appraiserLicenceElement = createdoc.createElement("APPRAISER_LICENSE");

				    				 createAttribute = createdoc.createAttribute("_Type");
				    				 createAttribute.setValue(usableElement.getAttribute("_Type"));
				    				 appraiserLicenceElement.setAttributeNode(createAttribute);
										
									 createAttribute = createdoc.createAttribute("_Identifier");
									 createAttribute.setValue(usableElement.getAttribute("_Identifier"));
									 appraiserLicenceElement.setAttributeNode(createAttribute);
									 appraiserElement.appendChild(appraiserLicenceElement);
									 
									 //partyElement.appendChild(appraiserLicenceElement);/
				    				 
				    			 //}
			    			 }
			    			 partyElement.appendChild(appraiserElement);
		    			 //}
	    			 }//end of appraiser for
	    			 
	    			 NodeList supervisorNodeList = partyUsableElement.getElementsByTagName("SUPERVISOR");
	    			 for(int l= 0 ; l < supervisorNodeList.getLength();l++){//SUPERVISOR for
		    			 Element supervisorElement = createdoc.createElement("SUPERVISOR");
		    			 
		    			 Element supvElement = (org.w3c.dom.Element) supervisorNodeList.item(l);
		    			 NodeList supApprNodeList = supvElement.getElementsByTagName("APPRAISER_LICENSE");
		    			 
		    			 for(int k = 0 ; k < supApprNodeList.getLength();k++){//appraiser
			    				//valuation for
				    			 Element supApprElement = (org.w3c.dom.Element) supApprNodeList.item(k);
				    			 namedNodeattr = supApprElement.getAttributes();
				    			 if (supApprElement.hasAttribute("_Type") && supApprElement.hasAttribute("_Identifier")  && namedNodeattr.getLength() == 2) {
				    				 Element appraiserSupElement = createdoc.createElement("APPRAISER_LICENSE");

				    				 createAttribute = createdoc.createAttribute("_Type");
				    				 createAttribute.setValue(supApprElement.getAttribute("_Type"));
				    				 appraiserSupElement.setAttributeNode(createAttribute);
										
									 createAttribute = createdoc.createAttribute("_Identifier");
									 createAttribute.setValue(supApprElement.getAttribute("_Identifier"));
									 appraiserSupElement.setAttributeNode(createAttribute);
									 supervisorElement.appendChild(appraiserSupElement);
									 }
				    			 }
		    			 partyElement.appendChild(supervisorElement);
		    			 }
	    			 rootElement.appendChild(partyElement);
	    		 }// end of property for 
	    		 
	         }//end of mismo if check
	     }//end of first for
	     createdoc.appendChild(rootElement);
	     parsedStrin = printXmlDocument(createdoc);

	
		
	}catch(Exception ex){
	ex.printStackTrace();
	
	StringWriter sw = new StringWriter();
	ex.printStackTrace(new PrintWriter(sw));
	
	sendEmailToAdmin("Error While Parsing And Creating XML",sw.toString());
	
	}
	
	return parsedStrin;
	}
	
	public static void createPDF(String value,String pathPdf){
		try{
			byte[] decodedValue = Base64.getDecoder().decode(value); // Basic Base64 decoding
			File file = new File(pathPdf+"\\newfile.pdf");;
			FileOutputStream fop = new FileOutputStream(file);
			fop.write(decodedValue);
			fop.flush();
			fop.close();
		}catch(Exception ex){
		ex.printStackTrace();
		
		StringWriter sw = new StringWriter();
		ex.printStackTrace(new PrintWriter(sw));
		
		sendEmailToAdmin("Error While Creating PDF Files",sw.toString());
		
		}
	}
	
	public static void createXML(String value,String pathPdf){
		try{
			byte[] decodedValue = Base64.getDecoder().decode(value); // Basic Base64 decoding
			File file = new File(pathPdf+"\\mismo.xml");;
			FileOutputStream fop = new FileOutputStream(file);
			fop.write(decodedValue);
			fop.flush();
			fop.close();
		}catch(Exception ex){
		ex.printStackTrace();
		}
	}
	
	public String createXMLForRsponse(String mnnetworkId){
		String returnResponse = "";
		try{
			Element rootElement = null;
			 DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			 DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		     Document createdoc = docBuilder.newDocument();
		     
		     rootElement = createdoc.createElement("OrderResponse");
		     
		     Element resultElement = createdoc.createElement("Result");
		     resultElement.appendChild(createdoc.createTextNode("true"));
		     rootElement.appendChild(resultElement);
		     
		     Element errorDescElement = createdoc.createElement("ErrorDescription");
		     errorDescElement.appendChild(createdoc.createTextNode("MN order status history id::"+mnnetworkId+"::--::::MISMO and AppraisalReport added successfully."));
		     rootElement.appendChild(errorDescElement);
		     
		     createdoc.appendChild(rootElement);
		     returnResponse = printXmlDocument(createdoc);
		}catch(Exception ex){
			ex.printStackTrace();
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));
			
			sendEmailToAdmin("Error While Creating XML For Response",sw.toString());
		}
		return returnResponse;
	}
	
	
	public static void postChatter(String urlEndpoint,String caseId, String oauthToken,String pathPdf )
	{

		System.out.println("sending request");
		//String oauthToken = "00D0j0000000QuQ!ARkAQEG1RTmaQFwHNMpqNApzKbr3FpqqOVypkrJt3wI9AyQ39.a2dTgsqLxBwDzVvpIN53HFRtz3FvKfiqBZ4.gi_Kb3xBTN";
		//String urlold = "https://na1.salesforce.com/services/data/v22.0/chatter/feeds/user-profile/me/feed-items";
		//String url = "https://spear--dev3.cs53.my.salesforce.com/services/data/c/40.0/chatter/feeds/record/5000j0000012C4pAAE/feed-items";
		//String urlticket = "https://spear--dev3.cs53.my.salesforce.com/services/data/v35.0/chatter/feed-elements";
		try{
			String textMsg = "Here is the comment";
			File contentFile = new File(pathPdf+"\\newfile.pdf");
			String desc = "Appraisal File Uploaded.";
			String fileName = "Appraisal.pdf";
			final PostMethod postMethod = new PostMethod(urlEndpoint + "/services/data/v23.0/chatter/feeds/record/"+caseId+"/feed-items");
			Part[] parts = {
			new StringPart("desc", desc),
			new StringPart("fileName", fileName),
			new StringPart("text", textMsg),
			new FilePart("feedItemFileUpload", contentFile),
			};
			postMethod.setRequestEntity(new MultipartRequestEntity(parts, 
			postMethod.getParams()));
			postMethod.setRequestHeader("Authorization", "OAuth " + oauthToken);
			postMethod.addRequestHeader("X-PrettyPrint", "1");
			HttpClient client = new HttpClient();
			client.getParams().setSoTimeout(6000);
			client.executeMethod(postMethod);
			//System.out.println(postMethod.getResponseBodyAsString());
			contentFile.delete();
			
		}
		catch (Exception e)
		{
		e.printStackTrace();
		
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		
		sendEmailToAdmin("Error While Posting PDF Into Chatter",sw.toString());
		}
		
	}
	
	public static void sendEmailToAdmin(String errorDesc , String writer){
		try{
			
			final String form1 ="sumit.km@teclever.com"; 
			final String pwd = "sumit906088";
			
			Properties props = new Properties();
			props.put("mail.smtp.auth", "true");
    		props.put("mail.smtp.starttls.enable", "true");
    		props.put("mail.smtp.host", "smtp.gmail.com");
    		props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
    		props.put("mail.debug.auth", "true");
			props.put("mail.smtp.port", "587");
    		props.setProperty("mail.transport.protocol", "smtp");
    		
    		
    		try { 
    			Authenticator auth = new Authenticator() {
    				protected PasswordAuthentication getPasswordAuthentication() {
    					return new PasswordAuthentication(form1, pwd);
    				}
    			};
    			javax.mail.Session session = javax.mail.Session.getInstance(props,auth); 
    			javax.mail.Transport transport = session.getTransport(); 
    			transport.connect(); 
    					Message message = new MimeMessage(session); 
    					message.setFrom(new InternetAddress("sumit.km@teclever.com"));
    					message.setRecipients(Message.RecipientType.TO,InternetAddress.parse("pavlel@vipmtginc.com"));
    					
    					message.setSubject("Heroku/Salesforce Error Log "); 
    					message.setText(errorDesc+writer); 
    					Transport.send(message);
    		}catch(Exception ex){
    			ex.printStackTrace();
    		}
			
			
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

}


