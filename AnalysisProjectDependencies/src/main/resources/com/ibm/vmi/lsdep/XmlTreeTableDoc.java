package com.ibm.vmi.lsdep;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.dom.DOMSource;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.w3c.dom.Document;  
import org.w3c.dom.Element;  
import org.w3c.dom.Node;  
import org.w3c.dom.NodeList;

/*
 * <table>
 *   <column>
 *   <item>column1</item>
 *   </column>
 *   <item>
 *     <item>
 *     </item>
 *   </item>
 * </table>
 */

public class XmlTreeTableDoc {
	File mapFile;
	Document document = null;
	Element curElement=null;
	public static final String NODE_ROOT = "table";
	public static final String NODE_COLUMN = "column";
	public static final String NODE_ITEM = "item";
	public static final String NODE_DATA = "data";
	public static final String NODE_PACKAGE = "package";
	public static final String NODE_JARFILE = "jarfile";
	public static final String NODE_REFCLASS = "refclass";
	public static final String NODE_JAVACLASS = "javaclass";
	
	public XmlTreeTableDoc(File mapFile) throws IOException{
		this.mapFile=mapFile;
		if(mapFile.exists()){
			mapFile.delete();
		}
		if(!createMap(mapFile))
			throw new IOException("fail to create xml file!");
		document = InitMapFile(mapFile);
		if(document==null)
			throw new IOException("fail to open xml file!");
	}
	public void sort(){
		sort(document.getDocumentElement());
	}
	public boolean setColumns(List<String> cols){
		Element root = document.getDocumentElement();
		NodeList nodelist = root.getElementsByTagName(NODE_COLUMN);
		for(int i=nodelist.getLength()-1; i>=0; i--){
			root.removeChild(nodelist.item(i));
		}
		Element column = document.createElement(NODE_COLUMN);
		root.appendChild(column);
		for(String col:cols){
			Element ele = document.createElement(NODE_ITEM);
			ele.setAttribute(NODE_DATA, col);
			column.appendChild(ele);
		}
		return true;
	}
	public boolean insert(List<String> items){
		Element root = document.getDocumentElement();
		if(curElement==null){
			curElement = root;
		}
		if(curElement!=null){
			List<Element> curItems = new ArrayList<Element>();
			Element ele = curElement;
			while(ele!=root){
				curItems.add(ele); 
				ele=(Element)ele.getParentNode();
			}
			int size = curItems.size();
			for(int i=0; i<items.size(); i++){
				int index = size-i-1;
				boolean insertFlag=false;
				if(index<0){
					insertFlag = true;
				}
				else{
					String data = curItems.get(index).getAttribute(NODE_DATA);
					if(data.compareTo(items.get(i))!=0){
						insertFlag = true;
						curElement = (Element)curItems.get(index).getParentNode();
					}
				}
				if(insertFlag){//a,b <-- a,b,c; root <--a,b,c
					for(; i<items.size(); i++){
						Element newEle=document.createElement(NODE_ITEM);
						newEle.setAttribute(NODE_DATA, items.get(i));
						curElement.appendChild(newEle);
						curElement = newEle;
					}
					return true;
				}
			}
		}
		return false;
	}
	public boolean flush(){
		if(document==null)
			return false;
		sort();
		StreamResult result=new StreamResult(mapFile);
    	TransformerFactory transformerFactory=TransformerFactory.newInstance();
    	Element root = document.getDocumentElement();
		try {
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource=new DOMSource(root);
            transformer.transform(domSource, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	class ElementComparator implements Comparator<Element>{
        @Override
        public int compare(Element s1, Element s2){
        	String t1=s1.getTagName();
        	String t2=s2.getTagName();
        	if(t1.compareTo(NODE_ITEM)==0 && t2.compareTo(NODE_ITEM)==0){
        		String d1 = s1.getAttribute(NODE_DATA);
        		String d2 = s2.getAttribute(NODE_DATA);
        		return d1.compareTo(d2);
        	}
            return t1.compareTo(t2);
        }
        
    }
	protected void sort(Element element){
		
		if(element==null){
			return;
		}
		NodeList childNodes = element.getChildNodes();
		if(childNodes==null)
			return;
		List<Element> elements = new ArrayList<Element>();
		for(int i=0; i<childNodes.getLength(); i++){
			Element childElement = (Element)childNodes.item(i);
			sort(childElement);
			elements.add(childElement);
		}
		ElementComparator comparator = new ElementComparator();
		Collections.sort(elements,comparator);
		for(Element ele:elements){
			element.removeChild(ele);
		}
		for(Element ele:elements){
			element.appendChild(ele);
		}
		
	}
	protected static boolean createMap(File mapFile){
		SAXTransformerFactory fac = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		TransformerHandler handler;
		try {
			handler = fac.newTransformerHandler();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		Transformer transformer = handler.getTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		OutputStream outStream=null;
		try {
			outStream = new FileOutputStream(mapFile);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		Result resultxml = new StreamResult(outStream);
		handler.setResult(resultxml);
		try {
			AttributesImpl atts = new AttributesImpl();
			handler.startDocument();
			handler.startElement("", "", NODE_ROOT, atts);
			handler.endElement("", "", NODE_ROOT);
			handler.endDocument();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}
	protected static Document InitMapFile(File mapFile){
		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(mapFile);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
        Element root = document.getDocumentElement();
        if(root!=null){
        	if(root.getNodeName().compareTo(NODE_ROOT)!=0)
        		return null;
        }
        return document;
	}
	
}
