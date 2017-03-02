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
		
		ArrayList<String> eventList = new ArrayList<String>();
		
		String inputFile = "input.xml";
		
		try {			
			File inputfile = new File(inputFile);  // TODO: receive list from server
			if(inputfile.exists()) // check if file exists
			{
				Document doc = fileParser(inputFile); // parse into DOM document

				NodeList EventListDoc = doc.getElementsByTagName("event");
				for(int i=0;i<EventListDoc.getLength();i++) {
					Node eventDoc = EventListDoc.item(i);
					String event = getEachEvent(eventDoc);
					if (event != null){
						eventList.add(event);
					} else {
						System.out.print("how?");
					}
				
				}
			}
			else
			{
				System.out					.println("The system cannot find the file specified)");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int maxNumArgs = 0;
		for(String s: eventList) {
			int numArg = countNumArgs(s);
			if(numArg>maxNumArgs) {
				maxNumArgs=numArg;
			}
		}
		
		String data = new String("");
		for(String s: eventList) {
			for(int i=countNumArgs(s);i<maxNumArgs;i++) {
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
		
		EventParser.parseEvents("input.xml");
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
		Node aNode = eventDoc.getChildNodes().item(0);
		if (aNode != null){
			return aNode.getTextContent();
		} else {
			return null;
		}
	}
	
	public static void main(String[] args) {
		System.out.print("Args to connection are: ");
		System.out.println(args); 
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
		System.out.println("getStep() has started");
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
