package com.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

/**
 * Root resource (exposed at "update" path)
 */
@Path("/setMercuryNetworkStatus")
public class Update extends HttpServlet{

	// /**
	// * Method handling HTTP GET requests. The returned object will be sent
	// * to the client as "text/plain" media type.
	// *
	// * @return String that will be returned as a text/plain response.
	// */
	// @GET
	// @Produces(MediaType.TEXT_PLAIN)
	// public String getIt() {
	// return "Hello, Heroku!";
	// }
	
	static final String USERNAME = "charmer@soliantconsulting.com.vip.qa";
	static final String PASSWORD = "cjhvip2017LcCwLb5ovFvHIPmbLbpAEOHv";
	static final String AUTHENDPOINT = "https://test.salesforce.com/services/Soap/c/40.0/";

	static EnterpriseConnection connection;


	@POST
	@Produces(MediaType.TEXT_XML)
	//public String parseXML(@QueryParam("xmlRecords") String xmlRecords) throws ParserConfigurationException {
	public String parseXML(String xmlRecords , @Context ServletContext context) throws Exception {
		String as = "";
		//ServletContextEvent kj = new ServletContextEvent();
	  String pathPdf = context.getRealPath("/WEB-INF/Appraisal/");
	  // getServlet().getServletContext().getRealPath("/WEB-INF/mailpdf/" + "appraisal.pdf");
	  System.out.println("pathpdf is :::::" + pathPdf);
	  
	  
		
		System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
		
		//System.out.println("xmlRecords" + xmlRecords);
		String decodedValue = "";
		String mismoPDF = "";
		
		
		//xmlRecords = URLDecoder.decode(xmlRecords).trim();
		
		xmlRecords = xmlRecords.replace("%3C", "<");
		xmlRecords = xmlRecords.replace("%3E", ">");
		xmlRecords = xmlRecords.replace("%2F", "/");
		xmlRecords = xmlRecords.replace("%3D", "=");
		xmlRecords = xmlRecords.replace("%27", "\"");
		xmlRecords = xmlRecords.replace("+", " ");
		xmlRecords = xmlRecords.replace("%3A", ":");
		//xmlRecords = xmlRecords.replaceAll("[^\\x20-\\x7e]", "");
		
		// System.out.println("xmlRecords after is ::" + xmlRecords);
		
		
		
		
		 /*DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		 DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	     Document createDoc = docBuilder.newDocument();*/
	     
		Map<String, String> xmlDataMap = new HashMap<String, String>();
		ConnectorConfig config = new ConnectorConfig();
        config.setUsername(USERNAME);
        config.setPassword(PASSWORD);
        config.setAuthEndpoint(AUTHENDPOINT);

		try {
			
			 //Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

			connection = Connector.newConnection(config);

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlRecords));
			
			//System.out.println("is " + is);
			
			Document doc = db.parse(is);
			
			//Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xmlRecords)));
			
			NodeList loanNodes = doc.getElementsByTagName("LOANNUMBER");
			Element loanElm = (Element) loanNodes.item(0);
			xmlDataMap.put("LOANNUMBER", getCharacterDataFromElement(loanElm));
			
			NodeList stNodes = doc.getElementsByTagName("STATUSNAME");
			Element stElm = (Element) stNodes.item(0);
			xmlDataMap.put("STATUSNAME", getCharacterDataFromElement(stElm));

			NodeList nodes1 = doc.getElementsByTagName("TRACKINGID");
			Element line2 = (Element) nodes1.item(0);
			System.out.println("Tracking Id Is : " + getCharacterDataFromElement(line2));
			xmlDataMap.put("TRACKINGID", getCharacterDataFromElement(line2));

			NodeList statusNode = doc.getElementsByTagName("STATUSID");
			Element statusElement = (Element) statusNode.item(0);
			xmlDataMap.put("STATUSID", getCharacterDataFromElement(statusElement));

			NodeList statusTSNode = doc.getElementsByTagName("STATUSTIMESTAMP");
			Element statusTSElement = (Element) statusTSNode.item(0);
			xmlDataMap.put("STATUSTIMESTAMP", getCharacterDataFromElement(statusTSElement));
			
			NodeList nodes = doc.getElementsByTagName("STATUSCOMMENT");
		    Element line1 = (Element) nodes.item(0);
		    //String pathPdf=System.getProperty("g:\\Appraisal");
		    
			for (int i = 0; i < nodes.getLength(); i++) {
		          Element element = (Element) nodes.item(i);
		          NodeList documentNameNodes = element.getElementsByTagName("DOCUMENT");
		          for(int j = 0 ; j < documentNameNodes.getLength() ; j++){
		        	  Element docElement = (Element) documentNameNodes.item(j);
		        	  Node currentItem = documentNameNodes.item(j);
		        	    String attributeKey = currentItem.getAttributes().getNamedItem("Name").getNodeValue();
		        	   // System.out.println("key is " + attributeKey);
		        	  NodeList contentNodelist = docElement.getElementsByTagName("CONTENT");
		        	  
		        	  for(int k = 0;k < contentNodelist.getLength();k++){
		        		  Element contentElement = (Element) contentNodelist.item(k);
		        		 // System.out.println("Name: " + getCharacterDataFromElement(contentElement));
		        		  if(attributeKey != null && attributeKey.contains("MISMORpt.XML")){
		        			   decodedValue = decodeEncodedValue(getCharacterDataFromElement(contentElement).replace(" ", "+"));
		        			   //xmlDataMap.put("ENCODE", getCharacterDataFromElement(contentElement));
		        		  }
		        		  
		        		  if(attributeKey != null && attributeKey.contains("3705 .pdf")){
		        			  //mismoPDF = decodeEncodedValue(getCharacterDataFromElement(contentElement).replace(" ", "+"));
		        			  createPDF(getCharacterDataFromElement(contentElement).replace(" ", "+"),pathPdf);
		        			   //xmlDataMap.put("ENCODE", getCharacterDataFromElement(contentElement));
		        		  }
		        		  
		        	  }
		          }
		          			       
		     }//end of if
			
			

		//	System.out.println("xmlDataMap is " + xmlDataMap);
			
			// display some current settings
	        /*System.out.println("Auth EndPoint: " + config.getAuthEndpoint());
	        System.out.println("Service EndPoint: "
	                + config.getServiceEndpoint());
	        System.out.println("Username: " + config.getUsername());
	        System.out.println("SessionId: " + config.getSessionId());
	     
	        System.out.println("**********************************");*/
	        

	        com.sforce.soap.SpearAppraisalAPI.SoapConnection soap =    com.sforce.soap.SpearAppraisalAPI.Connector.newConnection("","");
	        soap.setSessionHeader(config.getSessionId());
	        System.out.println("**********************************"+config.getServiceEndpoint());
	        
String endpointurl=	 config.getServiceEndpoint().split("/services")[0]  ;     
	        
	        
	        XStream xStream = new XStream(new DomDriver());
	        xStream.alias("map", java.util.Map.class);
	        String xmlDataString = xStream.toXML(xmlDataMap);
	        
	        //System.out.println("xmlDataString" + xmlDataString);
	        
	        System.out.println("decodedValue is ::::" + decodedValue);
	        
	        String xmlString = createXML(xmlDataMap,decodedValue);
	        
	        System.out.println("xmlString :::::::::" + xmlString);
	        
	        //com.sforce.soap.SpearAppraisalAPI.OrderResponse response = soap.setMercuryNetworkStatus(xmlDataString);///caspers's code
	        com.sforce.soap.SpearAppraisalAPI.OrderResponse response = soap.setMercuryNetworkStatus(xmlString);
	        
	        System.out.println("xmlString :::::::::" + endpointurl);         
	        

	        
	        String caseid=response.getErrorDescription().split("&&")[1].split("&&")[0];
	        postChatter(endpointurl, caseid, config.getSessionId(),pathPdf );
	        
	        return response.getErrorDescription();
	        
		} // end of try
		catch (Exception e) {
			e.printStackTrace();
			//return e.printStackTrace();
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			return sw.toString();
		}

		
		//return xmlDataMap.toString();
	}

	public static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "?";
	}
	
	public static String decodeEncodedValue(String encodedValue){
		String decodesString = "";
		try{
			byte[] decodedValue = Base64.getDecoder().decode(encodedValue); 
			decodesString = new String(decodedValue);
			//System.out.println(decodesString);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	      return decodesString;
		
	}
	
	public String createXML(Map<String, String> xmlDataMap,String decodedString) throws Exception{
		String retString = "";
		try{
			
			 // byte[] encoded = Base64.encodeBase64(decodedString.getBytes()); 
			  
			//<VALUATION PropertyAppraisedValueAmount="170000" AppraisalEffectiveDate="2005-07-22"></VALUATION>
			
			//String  parsedXML = parsinAndCreatinXML(decodedString);
			
			System.out.println("decodedString is " + decodedString);
			
			String hardcoded = " <VALUATION_RESPONSE><VALUATION PropertyAppraisedValueAmount=\"170000\" AppraisalEffectiveDate=\"2005-07-22\"></VALUATION><PROPERTY><STRUCTURE PropertyStructureBuiltYear=\"2\" LivingUnitCount=\"1\" _AccessoryUnitExistsIndicator=\"N\"></STRUCTURE></PROPERTY><PARTIES><APPRAISER Name=\"Test Appraiser\" CompanyName=\"Test Company\"><APPRAISER_LICENSE Type=\"License\" Identifier=\"OK-201\"></APPRAISER_LICENSE></APPRAISER><SUPERVISOR><APPRAISER_LICENSE Type=\"License\" Identifier=\"OK-301\"></APPRAISER_LICENSE></SUPERVISOR></PARTIES></VALUATION_RESPONSE>";
			 String encoded =  Base64.getEncoder().encodeToString(hardcoded.getBytes(StandardCharsets.UTF_8));
		     
			 
			 
			// System.out.println("Base64 Encoded String : " + new String(encoded));
			 
			 DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			 DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		     Document doc = docBuilder.newDocument();
		     
			 Element rootElement = doc.createElement("STATUS");
			 doc.appendChild(rootElement);
			 
			 Element loanNumber = doc.createElement("LOANNUMBER");
			 loanNumber.appendChild(doc.createTextNode(xmlDataMap.get("LOANNUMBER")));
		     rootElement.appendChild(loanNumber);
		     
		     Element statusName = doc.createElement("STATUSNAME");
		     statusName.appendChild(doc.createTextNode(xmlDataMap.get("STATUSNAME")));
		     rootElement.appendChild(statusName);
		     
		     Element trackingId = doc.createElement("TRACKINGID");
		     trackingId.appendChild(doc.createTextNode(xmlDataMap.get("TRACKINGID")));
		     rootElement.appendChild(trackingId);
		     
		     Element statusId = doc.createElement("STATUSID");
		     statusId.appendChild(doc.createTextNode(xmlDataMap.get("STATUSID")));
		     rootElement.appendChild(statusId);
		     
		     Element statusTime = doc.createElement("STATUSTIMESTAMP");
		     statusTime.appendChild(doc.createTextNode(xmlDataMap.get("STATUSTIMESTAMP")));
		     rootElement.appendChild(statusTime);
		     
		     Element statusCmnt = doc.createElement("STATUSCOMMENT");
		     
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
		}
		
		//&gt;&lt;
		
		//>PROPERTY<>
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
		}
	 
	    return xml;
	}
	
	public static String parsinAndCreatinXML(String xmlValue){
		String parsedStrin = "";
		
		try{
		 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	     DocumentBuilder db = dbf.newDocumentBuilder();
	     
	     Element rootElement = null;
	     
	     InputSource is = new InputSource();
	     is.setCharacterStream(new StringReader(xmlValue));
	     Document doc = db.parse(xmlValue);
	     
	     DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		 DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	     Document createdoc = docBuilder.newDocument();
	     
	     Map valuationMap = new HashMap();
			
	     
	     NodeList nodes2 = doc.getElementsByTagName("VALUATION_RESPONSE");
	     for(int k= 0 ; k<nodes2.getLength();k++){
	    	 
	    	 Element element = (Element) nodes2.item(k);
	    	 System.out.println("element" + element.getNodeName());
	    	 Node currentItem = nodes2.item(k);
	    	 
	    	 
	    	 if(element.getNodeName() != null && element.getNodeName().equals("VALUATION_RESPONSE")){
		    	 NamedNodeMap NamedNodeattr = element.getAttributes();
		    	     Node attr = NamedNodeattr.item(0);
		    	     System.out.println(attr.getNodeName() + " = \"" + attr.getNodeValue() + "\"");
		    	 
	     	    String attributeKey1 = currentItem.getAttributes().getNamedItem("MISMOVersionID").getNodeValue();
	     	    System.out.println("key is " + attributeKey1);
	     	    if(attr.getNodeName() != null && attr.getNodeName().equals("MISMOVersionID")){
	     	    	 rootElement = createdoc.createElement(element.getNodeName());
	     			Attr attr1 = createdoc.createAttribute("MISMOVersionID");
					attr1.setValue(attr.getNodeValue());
					rootElement.setAttributeNode(attr1);
					//rootElement.appendChild(valuationResElement);
					
					NodeList nodes3 = doc.getElementsByTagName("VALUATION");
					for(int l= 0 ; l<nodes3.getLength();l++){ //for valuation tag it is workin 
						
						
						Element element1 = (Element) nodes3.item(l);
						
				    	 Node currentItem1 = nodes3.item(l);
				    	 
				    	 NamedNodeattr = element1.getAttributes();
				    	 attr = NamedNodeattr.item(0);
				    	 valuationMap.put(attr.getNodeName(), attr.getNodeValue());
				    	 attr = NamedNodeattr.item(1);
				    	 valuationMap.put(attr.getNodeName(), attr.getNodeValue());
				    	 
				    	 if(valuationMap.size() > 0 &&valuationMap.containsKey("PropertyAppraisedValueAmount") && valuationMap.containsKey("AppraisalEffectiveDate")){
				    		 String attributeKey2 = currentItem1.getAttributes().getNamedItem("PropertyAppraisedValueAmount").getNodeValue();
					    	 String attributeKey3 = currentItem1.getAttributes().getNamedItem("AppraisalEffectiveDate").getNodeValue();
				    	 
					    	 System.out.println(attributeKey2 + "::::" + attributeKey3);
					    	 Element valuationElement = createdoc.createElement("VALUATION");
					    	 
					    	 attr1 = createdoc.createAttribute("PropertyAppraisedValueAmount");
								attr1.setValue(attributeKey2);
								valuationElement.setAttributeNode(attr1);
								
								attr1 = createdoc.createAttribute("AppraisalEffectiveDate");
								attr1.setValue(attributeKey3);
								valuationElement.setAttributeNode(attr1);
								rootElement.appendChild(valuationElement);
				    	 }
					}//end of for valuation
					
					
						NodeList nodes4 = doc.getElementsByTagName("STRUCTURE");
						for(int m=0;m<nodes4.getLength();m++){
							valuationMap.clear();
							Element element1 = (Element)nodes4.item(m);
							System.out.println(element1.getNodeName());
					    	 Node currentItem1 = nodes4.item(m);
					    	 
					    	 NamedNodeattr = element1.getAttributes();
					    	 
					    	 Node attr2 = NamedNodeattr.item(0);
					    	 valuationMap.put(attr2.getNodeName(), attr2.getNodeValue());
					    	 attr2 = NamedNodeattr.item(1);
					    	 valuationMap.put(attr2.getNodeName(), attr2.getNodeValue());
					    	 attr2 = NamedNodeattr.item(2);
					    	 valuationMap.put(attr2.getNodeName(), attr2.getNodeValue());
					    	 
					    	 System.out.println(valuationMap);
					    	 
					    	 if(valuationMap.size() > 0 && valuationMap.containsKey("PropertyStructureBuiltYear") && valuationMap.containsKey("LivingUnitCount") && valuationMap.containsKey("_AccessoryUnitExistsIndicator")){
						    	 Element valuationElement = createdoc.createElement("PROPERTY");
						    	 
						    	 Set keySet = valuationMap.keySet();
						    	 List ls = new ArrayList();
						    	 ls.addAll(keySet);
						    	 
						    	 Element strElement = createdoc.createElement("STRUCTURE");
						    	 attr1 = createdoc.createAttribute((String)ls.get(0));
									attr1.setValue((String)valuationMap.get((String)ls.get(0)));
									strElement.setAttributeNode(attr1);
									
									attr1 = createdoc.createAttribute((String)ls.get(1));
									attr1.setValue((String)valuationMap.get((String)ls.get(1)));
									strElement.setAttributeNode(attr1);
									
									attr1 = createdoc.createAttribute((String)ls.get(2));
									attr1.setValue((String)valuationMap.get((String)ls.get(2)));
									strElement.setAttributeNode(attr1);
									valuationElement.appendChild(strElement);
									rootElement.appendChild(valuationElement);
					    	 }
						}
						
						Element partyElement = createdoc.createElement("PARTIES");
						
						NodeList nodes5 = doc.getElementsByTagName("APPRAISER");
						for(int m=0;m<nodes5.getLength();m++){
							valuationMap.clear();
							Element element1 = (Element)nodes5.item(m);
							System.out.println(element1.getNodeName());
					    	 Node currentItem1 = nodes5.item(m);
					    	 NodeList childList = nodes5.item(m).getChildNodes();
					    	 
					    	 NamedNodeattr = element1.getAttributes();
					    	 
					    	 Node attr2 = NamedNodeattr.item(0);
					    	 valuationMap.put(attr2.getNodeName(), attr2.getNodeValue());
					    	 attr2 = NamedNodeattr.item(1);
					    	 valuationMap.put(attr2.getNodeName(), attr2.getNodeValue());
					    	 
					    	 System.out.println(valuationMap);
					    	 
					    	 if(valuationMap.size() > 0 && valuationMap.containsKey("Name") && valuationMap.containsKey("CompanyName")){
						    	 
						    	 Set keySet = valuationMap.keySet();
						    	 List ls = new ArrayList();
						    	 ls.addAll(keySet);
						    	 
						    	 Element strElement = createdoc.createElement("APPRAISER");
						    	 attr1 = createdoc.createAttribute((String)ls.get(0));
									attr1.setValue((String)valuationMap.get((String)ls.get(0)));
									strElement.setAttributeNode(attr1);
									
									attr1 = createdoc.createAttribute((String)ls.get(1));
									attr1.setValue((String)valuationMap.get((String)ls.get(1)));
									strElement.setAttributeNode(attr1);
									
									partyElement.appendChild(strElement);
									
					    	 }
					    	 
					    	 for (int j = 0; j < childList.getLength(); j++) {
					             Node childNode = childList.item(j);
					             if ("APPRAISER_LICENSE".equals(childNode.getNodeName())) {
					            	 System.out.println("pppppppppppppppppppppppppppppppppppppppppppppppppp");
					                 System.out.println(childList.item(j).getTextContent()
					                         .trim());
					                 Element emnt = (Element)childList.item(j);
					                 
					                 element1.getAttributes();
					                 NamedNodeattr = emnt.getAttributes();
							    	  attr2 = NamedNodeattr.item(0);
							    	 valuationMap.clear();
							    	 valuationMap.put(attr2.getNodeName(), attr2.getNodeValue());
							    	 attr2 = NamedNodeattr.item(1);
							    	 valuationMap.put(attr2.getNodeName(), attr2.getNodeValue());
							    	 System.out.println("valuationMap" + valuationMap);
							    	 
							    	 if(valuationMap.size() > 0 && valuationMap.containsKey("Type") && valuationMap.containsKey("Identifier")){
								    	 Set keySet = valuationMap.keySet();
								    	 List ls = new ArrayList();
								    	 ls.addAll(keySet);
								    	 Element strElement = createdoc.createElement("APPRAISER_LICENSE");
								    	 attr1 = createdoc.createAttribute((String)ls.get(0));
											attr1.setValue((String)valuationMap.get((String)ls.get(0)));
											strElement.setAttributeNode(attr1);
											
											attr1 = createdoc.createAttribute((String)ls.get(1));
											attr1.setValue((String)valuationMap.get((String)ls.get(1)));
											strElement.setAttributeNode(attr1);
											
											partyElement.appendChild(strElement);
											
							    	 }
					             }
					         }
						}
						
						NodeList nodes6 = doc.getElementsByTagName("SUPERVISOR");
						for(int m=0;m<nodes6.getLength();m++){
							valuationMap.clear();
							Element element1 = (Element)nodes6.item(m);
							System.out.println(element1.getNodeName());
					    	 Node currentItem1 = nodes6.item(m);
					    	 Element supervisorElement = createdoc.createElement("SUPERVISOR");
					    	 NodeList childList = nodes6.item(m).getChildNodes();
					    	  
					    	 for (int j = 0; j < childList.getLength(); j++) {
					             Node childNode = childList.item(j);
					             if ("APPRAISER_LICENSE".equals(childNode.getNodeName())) {
					            	 System.out.println("pppppppppppppppppppppppppppppppppppppppppppppppppp");
					                 System.out.println(childList.item(j).getTextContent()
					                         .trim());
					                 Element emnt = (Element)childList.item(j);
					                 
					                 element1.getAttributes();
					                 NamedNodeattr = emnt.getAttributes();
							    	 Node attr2 = NamedNodeattr.item(0);
							    	 valuationMap.clear();
							    	 valuationMap.put(attr2.getNodeName(), attr2.getNodeValue());
							    	 attr2 = NamedNodeattr.item(1);
							    	 valuationMap.put(attr2.getNodeName(), attr2.getNodeValue());
							    	 System.out.println("valuationMap" + valuationMap);
							    	 
							    	 
							    	 if(valuationMap.size() > 0 && valuationMap.containsKey("Type") && valuationMap.containsKey("Identifier")){
								    	 Set keySet = valuationMap.keySet();
								    	 List ls = new ArrayList();
								    	 ls.addAll(keySet);
								    	 Element strElement = createdoc.createElement("APPRAISER_LICENSE");
								    	 attr1 = createdoc.createAttribute((String)ls.get(0));
											attr1.setValue((String)valuationMap.get((String)ls.get(0)));
											strElement.setAttributeNode(attr1);
											
											attr1 = createdoc.createAttribute((String)ls.get(1));
											attr1.setValue((String)valuationMap.get((String)ls.get(1)));
											strElement.setAttributeNode(attr1);
											
											supervisorElement.appendChild(strElement);
							    	 }
					             }
					         }
					    	 partyElement.appendChild(supervisorElement);
						}
						rootElement.appendChild(partyElement);
	     	    }
	    	 }//end of if
	     }//end of for
	     
	     
	     createdoc.appendChild(rootElement);
	     parsedStrin = printXmlDocument(createdoc);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return parsedStrin;
	}
	
	public static void createPDF(String value,String pathPdf){
		try{
			
			byte[] decodedValue = Base64.getDecoder().decode(value);  // Basic Base64 decoding
		      File file = new File(pathPdf+"\\newfile.pdf");;
		      FileOutputStream fop = new FileOutputStream(file);

		      fop.write(decodedValue);
		      fop.flush();
		      fop.close();
			
			
			
			/*
			
			byte[] decodedValue = Base64.getDecoder().decode(value);
			
			final String APP_KEY = "yyh478361o868qc"; // change with yours
	        final String APP_SECRET = "0a7sm9odojdqrmy"; // change with yours
	        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
	        DbxRequestConfig config = new DbxRequestConfig("JavaTutorial/1.0",Locale.getDefault().toString());
	        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
	        String authorizeUrl = webAuth.start();

	        String myToken = "k-4GdIDc6VAAAAAAAAAACUIZ9X2j4p3A_y3159WzMutpsPomoGWLv23O8cK_rijp"; // change with yours
	                                          
	        DbxClient client = new DbxClient(config, myToken);
	        System.out.println("Linked account: "+ client.getAccountInfo().displayName);
	        System.out.println("check1");
	        
	        InputStream stream = new ByteArrayInputStream(decodedValue);

	        try {
	            DbxEntry.File uploadedFile = client.uploadFile("/main/appraisal.pdf",DbxWriteMode.add(), decodedValue.length, stream);
	            System.out.println("Uploaded: " + uploadedFile.toString());
	        } catch (Exception e) {

	            e.printStackTrace();
	        } finally {
	        	
	        }

	        DbxEntry.WithChildren listing = client.getMetadataWithChildren("/main");
	        System.out.println("Files in the root path:");
	        for (DbxEntry child : listing.children) {
	            System.out.println("    " + child.name + ": " + child.toString());
	        }

			

	*/}catch(Exception ex){
		
		ex.printStackTrace();
		
	}
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
	  String desc = "This file is uploaded by xyz user";
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
	  System.out.println(postMethod.getResponseBodyAsString());
	  contentFile.delete();
	}
	catch (Exception e)
	{
	  e.printStackTrace();
	}
	 }
}
