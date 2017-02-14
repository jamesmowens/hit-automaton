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

/**
 * Retrieving information from feedback from ServerLauncher in XML format.
 */

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
	
	public static ArrayList<Step> getListHighlightObjects () {
		
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
				System.out					.println("The system cannot find the file specified)");
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
				System.out					.println("The system cannot find the file specified)");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

//	/**
//	 * Process the NodeList which contains a list of transition data and return a hashtable which map from the identifier to the real transition object
//	 * @param tranListDoc The NodeList which contains a list of transition data
//	 * @return A hashtable which map from the identifier to the real transition object
//	 */
//	private static Hashtable<String, Transition> generateTranTable(NodeList tranListDoc) {
//		Hashtable<String, Transition> tranTable = new Hashtable<String, Transition>();
//		for(int i=0;i<tranListDoc.getLength();i++) {
//			if(notEmpty(tranListDoc.item(i)) && tranListDoc.item(i).getNodeName().equals("transition"))
//			{
//				addTranToTable(tranListDoc.item(i),tranTable);
//			}
//		}
//		return tranTable;
//	}
//
//	/**
//	 * Extract data for a transition and put it into the hashtable
//	 * @param tranDoc The Node which contains data of a transition
//	 * @param tranTable The hashtable which we will put a transition into
//	 */
//	private static void addTranToTable(Node tranDoc, Hashtable<String, Transition> tranTable) {
//
//		String id = null;
//		String source = null;
//		String target = null;
//		String tranLabel = null;						
//		String enter = null;
//		String instantiating = null;
//		String terminating = null;
//		String container = null;
//
//		//extract data		
//		for(int i=0;i<tranDoc.getChildNodes().getLength();i++) {
//			if(tranDoc.getChildNodes().item(i).getNodeName().equals("identifier")) {
//				id = tranDoc.getChildNodes().item(i).getTextContent();
//			}
//			else if(tranDoc.getChildNodes().item(i).getNodeName().equals("source")) {
//				source = tranDoc.getChildNodes().item(i).getTextContent();
//			} 
//			else if(tranDoc.getChildNodes().item(i).getNodeName().equals("target")) {
//				target = tranDoc.getChildNodes().item(i).getTextContent();
//			}
//			else if(tranDoc.getChildNodes().item(i).getNodeName().equals("label")) {
//				tranLabel = tranDoc.getChildNodes().item(i).getTextContent();
//			}
//			else if(tranDoc.getChildNodes().item(i).getNodeName().equals("enter")) {
//				enter = tranDoc.getChildNodes().item(i).getTextContent();
//			}
//			else if(tranDoc.getChildNodes().item(i).getNodeName().equals("instantiating")) {
//				instantiating = tranDoc.getChildNodes().item(i).getTextContent();
//			}
//			else if(tranDoc.getChildNodes().item(i).getNodeName().equals("terminating")) {
//				terminating = tranDoc.getChildNodes().item(i).getTextContent();
//			}
//			else if(tranDoc.getChildNodes().item(i).getNodeName().equals("container")) {
//				container = tranDoc.getChildNodes().item(i).getTextContent();
//			}
//		}
//
//		//create CHAOS representation from data we have
//		//Label label = ExtractStateTransitionLabel.extractTranLabel(tranLabel);
//		ExtractStateTransitionLabel.addTran(tranLabel);
//		Label label = new Label(new EventAtom(new EventID(""), tranLabel, new Vector<Attribute>()),new Vector<DoubleCompExp>());
//		boolean isTerminating = Boolean.parseBoolean(terminating);
//		boolean isEnter = Boolean.parseBoolean(enter);
//		boolean isInstantiating = Boolean.parseBoolean(instantiating);
//
//		//Note: right now, source, target, and container are not real object. We will change these field later
//		State sourceState = new State(new StateAtom(new StateID(""), source, null), false, false, null);
//		AtomicState targetState = new AtomicState(new StateAtom(new StateID(""), target, null), false, false, null);
//		NonAtomicState containerState = new NonAtomicState(new StateAtom(new StateID(""), container, null), false, false, null);	
//
//		Transition tran = new Transition(label, isTerminating, isInstantiating, isEnter, containerState);
//		tran.source = sourceState;
//		tran.target = targetState;
//
//		tranTable.put(id, tran); // put into the table
//	}
//
//	/**
//	 * Extract Node containing data of a state, and return State object in CHAOS representation
//	 * Moreover, store the node we got into the hashtable which map name of each state to the real object
//	 * @param stateDoc Node which contains data of a state
//	 * @param stateTable Hashtable which we store the State we got from this function into it (for referencing later)
//	 * @param tranTable Hashtable which we use to find real Transition objects (for outGoingTranList,inGoingTranList fields)
//	 * @return
//	 */
//	private static State generateState(Node stateDoc,
//			Hashtable<String, State> stateTable,
//			Hashtable<String, Transition> tranTable) {
//
//		String stateLabel = null;
//		Boolean start = null;
//		Boolean end = null; 
//		Vector<State> childList = new Vector<State>();
//		Vector<Transition> outGoingTranList = new Vector<Transition>();
//		Vector<Transition> inGoingTranList = new Vector<Transition>();
//
//		//extract data
//		for(int i=0;i<stateDoc.getChildNodes().getLength();i++) {
//			if(stateDoc.getChildNodes().item(i).getNodeName().equals("name")) {
//				stateLabel = stateDoc.getChildNodes().item(i).getTextContent();
//			}
//			else if(stateDoc.getChildNodes().item(i).getNodeName().equals("start")) {
//				start = Boolean.valueOf(stateDoc.getChildNodes().item(i).getTextContent());
//			}
//			else if(stateDoc.getChildNodes().item(i).getNodeName().equals("end")) {
//				end = Boolean.valueOf(stateDoc.getChildNodes().item(i).getTextContent());
//			} 
//			else if(stateDoc.getChildNodes().item(i).getNodeName().equals("contains")) {				
//				childList = generateStateList(stateDoc.getChildNodes().item(i).getChildNodes(),stateTable,tranTable);
//			} 
//			else if(stateDoc.getChildNodes().item(i).getNodeName().equals("outgoingTransitions")) {
//				outGoingTranList = generateTranList(stateDoc.getChildNodes().item(i).getChildNodes(),tranTable);
//			} 
//			else if(stateDoc.getChildNodes().item(i).getNodeName().equals("ingoingTransitions")) {
//				inGoingTranList = generateTranList(stateDoc.getChildNodes().item(i).getChildNodes(),tranTable);
//			} 
//		}
//
//		StateAtom atom = ExtractStateTransitionLabel.extractStateLabel(stateLabel);
//		if(childList.size()==0) //AtomicState
//		{
//			AtomicState state = new AtomicState(atom, start, end, outGoingTranList); 				
//			stateTable.put(stateLabel, state);
//			return state;
//		}
//		else //NonAtomicState
//		{
//			NonAtomicState state = new NonAtomicState(atom, start, end, outGoingTranList);
//			state.ingoingTransitions = inGoingTranList;
//			state.children = childList;
//
//			//set parent field in each child
//			for(int i=0;i<childList.size();i++)
//			{
//				childList.elementAt(i).parent = state;
//			}
//
//			stateTable.put(stateLabel, state);
//			return state;
//		}
//	}
//
//	/**
//	 * Process NodeList which contains data of a transition list and produce a Vector which contains Transition in CHAOS representation
//	 * @param tranListDoc NodeList which contains data of a transition list
//	 * @param tranTable Hashtable which we use to find real Transition objects from the id of each transition
//	 * @return Vector of Transition in CHAOS representation
//	 */
//	private static Vector<Transition> generateTranList(
//			NodeList tranListDoc,
//			Hashtable<String, Transition> tranTable) {
//
//		Vector<Transition> outGoingTranList = new Vector<Transition>();		
//		for(int j=0;j<tranListDoc.getLength();j++) // for each transition
//		{
//			if(notEmpty(tranListDoc.item(j)) && tranListDoc.item(j).getNodeName().equals("transition"))
//			{
//				String outGoingTranName = tranListDoc.item(j).getTextContent();
//				if(!outGoingTranName.equals(""))
//					outGoingTranList.add(tranTable.get(outGoingTranName));
//			}
//		}
//		return outGoingTranList;
//	}
//
//	/**
//	 * NodeList which contains data of a state list and produce a Vector which contains State in CHAOS representation
//	 * @param StateListDoc NodeList which contains data of a state list
//	 * @param stateTable Hashtable which we store the State we got from this function into it (for referencing later)
//	 * @param tranTable Hashtable which we use to find real Transition objects from the id of each transition
//	 * @return Vector of State in CHAOS representation
//	 */
//	private static Vector<State> generateStateList(
//			NodeList StateListDoc,
//			Hashtable<java.lang.String, State> stateTable,
//			Hashtable<java.lang.String, com.hp.hpl.CHAOS.HIT.Transition> tranTable) {
//
//		Vector<State> childList = new Vector<State>();		
//		for(int j=0;j<StateListDoc.getLength();j++) // for each state
//		{
//			if(notEmpty(StateListDoc.item(j)) && StateListDoc.item(j).getNodeName().equals("state")) {
//				childList.add(generateState(StateListDoc.item(j), stateTable, tranTable));
//			}
//		}
//		return childList;
//	}
//
//	/**
//	 * Check if a Node is empty
//	 * @param n The Node
//	 * @return True if not empty. Otherwise, return false
//	 */
//	private static boolean notEmpty(Node n) {
//		if(n.getNodeValue()!=null && n.getNodeValue().trim().equals("")) return false;
//		return true;
//	}
//
//	/**
//	 * Create a NonAtomicState from a XML file
//	 * @param filename The input XML file
//	 * @return A NonAtomicState in CHAOS representation
//	 */
//	public static NonAtomicState generateMachine(String filename) {
//		NonAtomicState state = null;
//		try {			
//			File inputfile = new File(filename);
//			if(inputfile.exists()) // check if file exists
//			{
//				Document doc = fileParser(filename); // prase into DOM document
//
//				Hashtable<String, Transition> tranTable = new Hashtable<String, Transition>();
//
//				//extract data of all transitions
//				if(doc.getElementsByTagName("transitions").getLength()!=0)
//				{
//					NodeList tranListDoc = doc.getElementsByTagName("transitions").item(0).getChildNodes();			
//					tranTable = generateTranTable(tranListDoc);
//				}
//
//				//extract data of all states
//				Hashtable<String, State> stateTable = new Hashtable<String, State>();
//				if(doc.getElementsByTagName("states").getLength()!=0)
//				{
//					NodeList stateListDoc = doc.getElementsByTagName("states").item(0).getChildNodes();
//					for(int i=0;i<stateListDoc.getLength();i++) {
//						if(notEmpty(stateListDoc.item(i)) && stateListDoc.item(i).getNodeName().equals("state"))
//						{
//							state = (NonAtomicState) generateState(stateListDoc.item(i), stateTable, tranTable);
//						}
//					}
//				}
//
//				//field up the data for each transition
//				Enumeration<Transition> e = tranTable.elements();
//				while(e.hasMoreElements()) {
//					Transition pt = e.nextElement();
//					pt.label = ExtractStateTransitionLabel.extractTranLabel(pt.label.atom.type);
//					pt.source = stateTable.get(pt.source.name.type);
//					pt.target = (AtomicState) stateTable.get(pt.target.name.type);
//					if(pt.container.name.type==null)
//					{
//						pt.container=null;
//					}
//					else
//					{
//						pt.container = (NonAtomicState) stateTable.get(pt.container.name.type);
//					}
//				}
//			}
//			else
//			{
//				System.out					.println("The system cannot find the file specified)");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return state;
//	}
//
//	public static void main(String[] args) {
//		NonAtomicState s = generateMachine("9.xml");
//
//		/*// Atom a:auctionBegin(auctionID(A*))	
//		NonGroundAttribute auctionID1 = new NonGroundAttribute("auctionID", new Variable("A", true));
//		Vector<Attribute> attr1 = new Vector<Attribute>();
//		attr1.add(auctionID1);
//		EventAtom atom1 = new EventAtom(new EventID("a"), "auctionBegin", attr1);
//
//		// Enter transition -> A0
//		Label label1 = new Label(atom1, new Vector<DoubleCompExp>());
//		Transition to_A0 = new Transition(label1, false, true, true, null);
//
//		// State j:Auction(auctionID(A))
//		Vector<Transition> in = new Vector<Transition>();
//		in.add(to_A0);		
//		AuctionAutomaton auction = new AuctionAutomaton(in, new Vector<Transition>());	
//		auction.parent = null;*/
//
//		PrintStateTransition.printState(s);
//	}
}
