package connection;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class StreamXMLGenerator {

	public static String generate(ArrayList<String> events) {
		String data = new String("");
		
		data+="<stream>\n";
		for(String event : events) {
			data+="\t<event>";
			data+=event;
			data+="</event>\n";
		}
		data+="</stream>";
		
		return data;
	}

}
