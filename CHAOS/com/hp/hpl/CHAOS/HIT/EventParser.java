package com.hp.hpl.CHAOS.HIT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import java.util.Vector;

public class EventParser {
	public static void parseEvents(String fileName) {
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
		 processesEventList(eventList); 
	}
	
	/**
	 * Adapted from output.java, this method fires all the transitions when an events happens
	 * @param events A list of events
	 */
	private static void processesEventList(Vector<Event> events){
		System.out.println("Writing Output.xml");
		String steps = "<steps>\n";
		String activeStates = "<activeStates>\n";
		boolean flag = false;
		String outputFile = "output.xml";
		String inputFile = "CHAOS/input.xml";
		
		NonAtomicState auction = XMLParser.generateMachine(inputFile);
		
		// Configuration
		Vector<StateInstance> configuration = new Vector<StateInstance>();
		for(Event e:events) {
		
			// Trigger enter transitions  
			for (Transition enter : auction.ingoingTransitions) {				
				if (enter.isEnabled(e)) {
					flag = true;
					//enter.printPair(e);
					steps += CHAOSConnection.getStep(enter);
					
					configuration = enter.fire(configuration, e); 	
				}
			}
			
			// Trigger other transitions
			Vector<StateInstance> new_config = new Vector<StateInstance>(); 
			for(StateInstance j : configuration) {
	
				for(Transition t : j.activeState.outgoingTransitions) {	
					if (t.isEnabled(configuration, j, e)){
						flag = true;
						steps += CHAOSConnection.getStep(t);
						//t.printPair(e);
						if (t.container.equals(j.state)) {
							new_config = t.fire(configuration, j, e); 
						} else {
							if (t.container == null) {
								new_config = t.fire(configuration, e);
							}}}}
				if (!new_config.isEmpty()) {
					configuration = new_config;
					break;                      // an event can trigger only 1 transition in only 1 state instance                        
				}
			}
		}
		
		steps += "</steps>\n";
		activeStates += "</activeStates>\n";
		
		String feedback = "<feedback>\n";
		feedback +=steps;
		feedback +=activeStates;
		feedback +="</feedback>\n";
		
		if(flag) {
			try {
				PrintWriter out = new PrintWriter(outputFile);
				out.print(feedback);
				out.flush();
				out.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
			ServerLauncher.finishConnection();
		}		
		
	}
		     			

}
