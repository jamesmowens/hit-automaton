package connection;

import java.util.ArrayList;
import Query.*;

public class parserTester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Hi");
		ArrayList<DataNode> list = XMLParser.getListDataNodes(StringEscapeUtils.escapeJava("C:\Users\Nicholas Fajardo\Documents\GitHub\hit-automaton\sampleUberData.xml"));
		
	}

}
