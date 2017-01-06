package com.hp.hpl.CHAOS.HIT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.CHAOS.Component.CCHAOS;
import com.hp.hpl.CHAOS.Expression.DoubleCompExp;

public class CHAOSConnection {
	
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
	
	public CHAOSConnection() {
		
	}
	
	public static void processData() {
		
		//an array list of strings that contain all the events of the process
		ArrayList<String> eventList = new ArrayList<String>();
		
		String inputFile = "input.xml";
		
		try {			
			File inputfile = new File(inputFile);  // TODO: receive list from server
			if(inputfile.exists()) // check if file exists
			{
				Document doc = fileParser(inputFile); // parse into DOM document

				//this transforms the document made into a list that the java machine can manipulate
				NodeList EventListDoc = doc.getElementsByTagName("event");
				for(int i=0;i<EventListDoc.getLength();i++) {
					//for each node(event) in the NodeList, you fetch the node and then add the node to
					//the arrayList of strings
					Node eventDoc = EventListDoc.item(i);
					eventList.add(getEachEvent(eventDoc));
				}
			}
			else
			{
				System.out.println("The system cannot find the file specified)");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int maxNumArgs = 0;
		for(String s: eventList) {
			//gets the max number of arguments in the list
			int numArg = countNumArgs(s);
			if(numArg>maxNumArgs) {
				maxNumArgs=numArg;
			}
		}
		
		//This is the string that will be printed with all the info.
		String data = new String("");
		for(String s: eventList) {
			for(int i=countNumArgs(s);i<maxNumArgs;i++) {
				//prints the nothing argument for the leftover space
				s+=" nothing 0";
			}
			data+=s;
			data+="\n";
		}
		try {
			PrintWriter out = new PrintWriter("HIT_stream_1.txt");
			out.print(data);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		data = new String("");
		data += "<operator id = \"HIT_config_1.xml\" className=\"StreamSourceOperator\" log =\"true\">\n";
		data += "\t<schema>\n";
		data += "\t\t<schemaElement type=\"String\"/>\n";
		data += "\t\t<schemaElement type=\"Double\"/>\n";
		data += "\t\t<schemaElement type=\"Double\"/>\n";
		for(int i=0;i<maxNumArgs;i++) {
			data += "\t\t<schemaElement type=\"String\"/>\n";
			data += "\t\t<schemaElement type=\"Double\"/>\n";			
		}
		data += "\t</schema>\n";
		data += "\t<classVariables>\n";
		data += "\t\t<InputStreamWrapper name = \"TestFormat\"/>\n";
		data += "\t\t<source name = \"HIT_stream_1.txt\"/>\n";
		data += "\t\t<arrival rate = \"" + eventList.size()  + "\"/>\n";
		data += "\t\t<loop value = \"false\"/>\n";
		data += "\t</classVariables>\n";
		data += "</operator>\n";
		
		try {
			PrintWriter out = new PrintWriter("HIT_config_1.xml");
			out.print(data);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		data = new String("");
		data += "<queryplan>\n";
		data += "\t<operator id =\"1\" className=\"StreamSinkOperatorDemo\" log = \"false\">\n";
		data += "\t<Statistic>\n";
		data += "\t\t<StatisticElement name = \"ElapseTime\"/>\n";
		data += "\t</Statistic>\n";
		data += "\t<classVariables>\n";
		data += "\t\t<output name = \"HIT_Output\"/>\n";
		data += "\t</classVariables>\n";
		data += "\t<children>\n";
		data += "\t\t<child id = \"2\"/>\n";
		data += "\t</children>\n";
		data += "\t</operator>\n";
		data += "\t<operator id=\"2\" className=\"com.hp.hpl.CHAOS.HIT.Output\" log = \"false\">\n";
		data += "\t<Statistic>\n";
		data += "\t\t<StatisticElement name = \"ElapseTime\"/>\n";
		data += "\t</Statistic>\n";
		data += "\t<classVariables>\n";
		data += "\t\t<query name = \"id=1 range=100\"/>\n";
		data += "\t</classVariables>\n";
		data += "\t<children>\n";
		data += "\t\t<child id = \"HIT_config_1.xml\"/>\n";
		data += "\t</children>\n";
		data += "\t</operator>\n";
		data += "</queryplan>\n";

		try {
			PrintWriter out = new PrintWriter("HIT_TEST_1.xml");
			out.print(data);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String s = "run hit_test_1.xml";
		String[] splitString = s.split("\\s+");
		CCHAOS.execute(splitString);
	}

	private static int countNumArgs(String s) {
		int num = 0;
		int i = s.indexOf(" ");
		while(i>=0) {
			num++;
			s = s.substring(i+1);
			i = s.indexOf(" ");
		}
		num++;
		
		num-=3;
		num/=2;
		return num;
	}

	private static String getEachEvent(Node eventDoc) {
		return eventDoc.getChildNodes().item(0).getTextContent();
	}
	
	public static void main(String[] args) {
		CHAOSConnection connection = new CHAOSConnection();
		connection.processData();
	}

	private static String adaptForXML(String label) {
		int i=label.indexOf("<");
		while(i!=-1) {
			label = label.substring(0,i) + "&lt;" + label.substring(i+1);
			i=label.indexOf("<");
		}
		i = label.indexOf(">");
		while(i!=-1) {
			label = label.substring(0,i) + "&gt;" + label.substring(i+1);
			i=label.indexOf(">");
		}
		return label;
	}

	public static String getStep(Transition t) {
		String s = "\t<step>\n";
		s+="\t\t<source>" + adaptForXML(t.getHitVisSourceLabel()) + "</source>\n";
		s+="\t\t<target>" + adaptForXML(t.getHitVisTargetLabel()) + "</target>\n";
		s+="\t\t<label>" + adaptForXML(t.getHitVIsTranLabel()) + "</label>\n";
		s+="\t</step>\n";
		return s;
	}
	
	private static String deleteParenforCons(String label) {
		return label.substring(1,label.length()-1);
	}
}
