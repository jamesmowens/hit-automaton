package bidDatabase;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class BidDatabaseImpl implements BidDatabase {
	
	private ArrayList<Integer> bids = new ArrayList();

	public BidDatabaseImpl(){
	}
	
	@Override
	public void updateInfo(int bid) {
		// TODO Auto-generated method stub
	}
	
	private void makeBids(){
		File fXmlFile = new File("input.xml");
	    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	    Document doc = dBuilder.parse(fXmlFile);
	    
	    NodeList nList = doc.getElementsByTagName("");
	}

	@Override
	public ArrayList<Integer> returnBids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Integer> returnConstrainedBids(int lowerBound, int upperBound) {
		// TODO Auto-generated method stub
		return null;
	}

}
