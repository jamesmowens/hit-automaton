package com.hp.hpl.CHAOS.HIT;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import com.hp.hpl.CHAOS.Component.CCHAOS;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;
import com.hp.hpl.CHAOS.AnormalDetection.XMLVarParser;
import com.hp.hpl.CHAOS.Expression.DoubleCompExp;
import com.hp.hpl.CHAOS.Queue.StreamQueue;
import com.hp.hpl.CHAOS.StreamData.SchemaElement;
import com.hp.hpl.CHAOS.StreamData.StreamAccessor;
import com.hp.hpl.CHAOS.StreamOperator.SingleInputStreamOperator;

public class Output extends SingleInputStreamOperator{

	private static final long serialVersionUID = 1L;

	public Output(int operatorID, StreamQueue[] input, StreamQueue[] output) {
		super(operatorID, input, output);
	}
	
	int id = 0;
	double range = 0.00;	
	
	@Override
	public void classVariableSetup(String key, String value)
			throws InstantiationException, IllegalAccessException,
			InvocationTargetException, SecurityException,
			ClassNotFoundException {
	
			if (key.equalsIgnoreCase("query")) {	
			XMLVarParser parser = new XMLVarParser(value);
			id = parser.getId();
			range = parser.getRange();
		}	
	}

	@Override
	public int run(int maxDequeueSize) {
		String steps = "<steps>\n";
		String activeStates = "<activeStates>\n";
		boolean flag = false;
		String outputFile = "output.xml";
		String inputFile = "CHAOS/input.xml";
		
		// 1) Create the auction automaton and its configuration
		
		// Atom a:auctionBegin(auctionID(A*))	
		/*NonGroundAttribute auctionID1 = new NonGroundAttribute("auctionID", new Variable("A", true));
		Vector<Attribute> attr1 = new Vector<Attribute>();
		attr1.add(auctionID1);
		EventAtom atom1 = new EventAtom(new EventID("a"), "auctionBegin", attr1);
				
		// Enter transition -> A0
		Label label1 = new Label(atom1, new Vector<DoubleCompExp>());
		Transition to_A0 = new Transition(label1, false, true, true, null);
		
		// State j:Auction(auctionID(A))
		Vector<Transition> in = new Vector<Transition>();
		in.add(to_A0);		
		AuctionAutomaton auction = new AuctionAutomaton(in, new Vector<Transition>());	
		auction.parent = null;*/
		
		NonAtomicState auction = XMLParser.generateMachine(inputFile);
				
		// Configuration
		Vector<StateInstance> configuration = new Vector<StateInstance>();
    	    	
    	// 2) Read the input stream from HIT_stream.txt		
    	StreamQueue inputQueue = getInputQueueArray()[0];
		SchemaElement[] schArray = inputQueue.getSchema();
		
		for(int i = maxDequeueSize; i > 0; i--) {
			byte[] event = inputQueue.dequeue();
			
			if (event == null)
				break;
			
			for (SchemaElement sch : schArray)				
				sch.setTuple(event);	
						
			// Event type
			String type = StreamAccessor.getStringCol(event, schArray, 0);
			
			// Event occurrence time
			double start = StreamAccessor.getDoubleCol(event, schArray, 1);
			double end = StreamAccessor.getDoubleCol(event, schArray, 2);
			
			Vector<GroundAttribute> attributes = new Vector<GroundAttribute>();
			
			// Event attributes
			for(int j=0;;j++) {
				try {
					String attr1Name = StreamAccessor.getStringCol(event, schArray, j*2+3);
					double attr1Value = StreamAccessor.getDoubleCol(event, schArray, j*2+4);
					GroundAttribute attribute1 = new GroundAttribute(attr1Name.trim(), new DoubleConstant(attr1Value));
					if(!attr1Name.trim().equals("nothing")) {
						attributes.add(attribute1);
					}
				}
				catch(ArrayIndexOutOfBoundsException e) {
					break;
				}
			}			
			
			// auctionID
			/*if (type.trim().equals("auctionBegin") || type.trim().equals("auctionEnd")) {
				attributes.add(attribute1);
			} else { 
			// auctionID, itemID, price
			if (type.trim().equals("itemDescription") || type.trim().equals("bid")) {
				attributes.add(attribute1);
				attributes.add(attribute2);
				attributes.add(attribute3);
			} else { 
			// auctionID, itemID or enrollID 
				attributes.add(attribute1);	
				attributes.add(attribute2);				
			}}		*/
			
			// Next incoming event	
			Event e  = new Event(type.trim(), start, end, attributes);
			System.out.println("---------------------------------------------------------------------\n"+ e.toString() + "\n");			

            System.out.print("Hello");

			// Trigger enter transitions  
			for (Transition enter : auction.ingoingTransitions) {				
				if (enter.isEnabled(e)) {
					flag = true;
					//enter.printPair(e);
					steps += CHAOSConnection.getStep(enter);
					
					configuration = enter.fire(configuration, e); 	
			}}		     			
			     			
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
			
			activeStates += "\t<activeState>\n";
			
			for(StateInstance j : configuration) {
				activeStates += j;
				activeStates += "\n";
			}
			activeStates += "\t</activeState>\n";
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
		
		return 0;
	}
}