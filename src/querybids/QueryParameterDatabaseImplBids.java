package querybids;

import query.QueryParameterDatabase;
import java.util.HashMap;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class QueryParameterDatabaseImplBids implements QueryParameterDatabase{
	
	String fileName = "";
	FileWriter writer = new FileWriter(fileName);
	
	HashMap<String, Integer> database = new HashMap<String, Integer>();
	
	public static void main(String[] args){
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("Database");
		doc.appendChild(rootElement);

		//variable elements
		Element contextVars = doc.createElement("context variables");
		rootElement.appendChild(contextVars);

		// set variable element
		Attr var = doc.createAttribute("variable");
		var.setValue("");
		var.setAttributeNode(var);
	}
	
	public QueryParameterDatabaseImplBids(){
		this.fileName = "Bid_Database.txt";
	}

	/**
	 * public access for the return method.
	 * @return the value requested by the key
	 */
	public int returnVal(String key) {
		return fetchVal(key);
	}

	/**
	 * Puts the value in wherever it belongs, a file or an ADT
	 * @param key is the string key to get the thing
	 * @param value is the value that needs to be fetched
	 */
	public void addValue(String key,  int value){
		fw.write("\n" + "Key: " + key + "Value: " + value);
		//database.put(key, value);
	}
	
	public void updateVal(String key, int thing, String action) {
		if(action.equals("add")){
			int newVal = database.get(key) + thing;
			database.remove(key);
			database.put(key, newVal);
		}
		else if(action.equals("subtract")){
			int newVal = database.get(key) - thing;
			database.remove(key);
			database.put(key, newVal);
		}
	}
	
	public void updateVal(String key, String action) {
		if(action.equals("increment")){
			int newVal = database.get(key) + 1;
			database.remove(key);
			database.put(key, newVal);
		}
		else if(action.equals("decrement")){
			int newVal = database.get(key) - 1;
			database.remove(key);
			database.put(key, newVal);
		}
	}
	
	/**
	 * This returns the value from either the file, Hashmap, or whatever database is called to keep the values
	 * @param key
	 * @return value of the thing
	 */
	private int fetchVal(String key){
		return database.get(key);
	}

}
