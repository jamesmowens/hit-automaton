package com.hp.hpl.CHAOS.HIT;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.util.Vector;

public class EventParser {
	public static Vector<Event> parseEvents(String fileName) {
		Vector<Event> eventList = new Vector<Event>();
		try {
			//Create the xml document reader for 
			File inFile = new File(fileName);
			DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dFactory.newDocumentBuilder(); 
			Document doc = dBuilder.parse(inFile);
			doc.getDocumentElement().normalize();
			
			
			NodeList events = doc.getElementsByTagName("event");
			
			System.out.println("Events length: "+events.getLength()); 
			
			for(int i =0; i<events.getLength();i++) {
				Node eventNode = events.item(i);
				System.out.println("Context: "+eventNode.getTextContent());
			
				String [] properties = eventNode.getTextContent().split(" "); 
				//System.out.println("Properties: " +)
				
				//get the main properties of the event
				if(properties.length>3) {
					String name = properties[0];
					double start = Integer.parseInt(properties[1]);
					double end = Integer.parseInt(properties[2]);
					
					System.out.println("Creating event with name: "+name+" start: "+start+" end: "+end);
					
					Vector<GroundAttribute> attributes = new Vector<GroundAttribute>();
					
					//TODO make this less dangerous 
					for(int j = 3; j<properties.length;j+=2) {
						//construct attribute from string and add it to the list
						String attrName = properties[j];
						double attrVal = Integer.parseInt(properties[j+1]);
						GroundAttribute newAttr = new GroundAttribute(attrName, new DoubleConstant(attrVal)); 
						attributes.add(newAttr);
					}
					
					Event newEvent = new Event(name,start,end,attributes);
					eventList.add(newEvent);
					
				}
				
				
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return eventList; 
	}
	
	public static void main(String [] args) {
		parseEvents("input.xml");
		
	}
	

}
