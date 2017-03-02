package connection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import Query.DataNode;

public class XMLParser {

	/**
	 * Parsing file in XML format into DOM format
	 * (copied from Medhabi's function in package com.hp.hpl.CHAOS.queryplangenerator)
	 * @param filename The name of XML file we want to parse
	 * @return The root of DOM document
	 * @throws SAXException 
	 * @throws IOException
	 */
	private static Document fileParser(String filename) throws SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}

		Document document = null;

		document = builder.parse(filename);

		return document;
	}

	public static ArrayList<Step> getListHighlightObjects() {

		ArrayList<Step> list = new ArrayList<Step>();
		String file = "feedback.xml";

		try {			
			File inputfile = new File(file);  // TODO: receive list from server
			if(inputfile.exists()) // check if file exists
			{
				Document doc = fileParser(file); // parse into DOM document

				NodeList stepListDoc = doc.getElementsByTagName("step");
				for(int i=0;i<stepListDoc.getLength();i++) {
					Node stepDoc = stepListDoc.item(i);
					list.add(getEachStep(stepDoc));
				}
			}
			else
			{
				System.out.println("The system cannot find the file specified");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	private static Step getEachStep(Node stepDoc) {
		String source = null;
		String target = null;
		String label = null;
		for(int i=0;i<stepDoc.getChildNodes().getLength();i++) {
			if(stepDoc.getChildNodes().item(i).getNodeName().equals("source")) {
				source = stepDoc.getChildNodes().item(i).getTextContent();
			}
			else if(stepDoc.getChildNodes().item(i).getNodeName().equals("target")) {
				target = stepDoc.getChildNodes().item(i).getTextContent();
			} if(stepDoc.getChildNodes().item(i).getNodeName().equals("label")) {
				label = stepDoc.getChildNodes().item(i).getTextContent();
			}
		}
		return new Step(source,target,label);
	}

	public static ArrayList<DataNode> getListDataNodes(String docPath){
		ArrayList<DataNode> nodes = new ArrayList();
		//String file = "sampleUberData.xml";
		try{
			File inputfile = new File(docPath);  // TODO: receive list from server
			if(inputfile.exists()) // check if file exists
			{
				Document doc = fileParser(docPath); // parse into DOM document

				NodeList nodeListDoc = doc.getElementsByTagName("node");
				for(int i=0; i < nodeListDoc.getLength(); i++) {
					Node nodeDoc = nodeListDoc.item(i);
					nodes.add(getEachDataPoint(nodeDoc));
					System.out.println("This is data: " + getEachDataPoint(nodeDoc).getCost());
				}
			}
			else
			{
				System.out.println("The system cannot find the file specified");
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return nodes;
	}

	private static DataNode getEachDataPoint(Node dataDoc){
		String cost = "";
		String time = "";
		String lat = "";
		String lon = "";
		for(int i=0;i<dataDoc.getChildNodes().getLength();i++) {
			if(dataDoc.getChildNodes().item(i).getNodeName().equals("cost")){
				cost = dataDoc.getChildNodes().item(i).getTextContent();
			}
			else if(dataDoc.getChildNodes().item(i).getNodeName().equals("latitude")) {
				lat = dataDoc.getChildNodes().item(i).getTextContent();
			} 
			else if(dataDoc.getChildNodes().item(i).getNodeName().equals("longitude")) {
				lon = dataDoc.getChildNodes().item(i).getTextContent();
			}
			else if(dataDoc.getChildNodes().item(i).getNodeName().equals("time")) {
				time = dataDoc.getChildNodes().item(i).getTextContent();
			}
		}
		return new DataNode(cost, lat, lon, time);
	}

	public static void sendEventStreamtoServer() {
		// TODO: send stream to server
	}

	public static ArrayList<String> getActiveStates() {
		ArrayList<String> list = new ArrayList<String>();
		String file = "feedback.xml";

		try {			
			File inputfile = new File(file);  // TODO: receive list from server
			if(inputfile.exists()) // check if file exists
			{
				Document doc = fileParser(file); // parse into DOM document

				NodeList activeStateListDoc = doc.getElementsByTagName("activeState");
				for(int i=0;i<activeStateListDoc.getLength();i++) {
					String activeState = activeStateListDoc.item(i).getTextContent();
					list.add(activeState);
				}
			}
			else
			{
				System.out.println("The system cannot find the file specified");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}
}