package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;
import com.hp.hpl.CHAOS.Expression.DoubleCompExp;

public class BidderEnrollmentAutomaton extends NonAtomicState {

	public BidderEnrollmentAutomaton(StateAtom n, boolean s, boolean e, Vector<Transition> o) {
		super(n,s,e,o);
	}

	public BidderEnrollmentAutomaton(Vector<Transition> in, Vector<Transition> out) {
		
		/** --------------------------------------------------------------------------------------- 
	        ---                                   Transitions                                   --- 
	        --------------------------------------------------------------------------------------- **/
		
		// Ingoing transition A0 -> B0 
		Transition A0_to_B0 = in.firstElement();	
		
		// In- and outgoing transition BE -> B0
		Transition BE_to_B0 = in.lastElement();
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom bidderData(auctionID(A),enrollID(E))
		NonGroundAttribute auctionID = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute enrollID = new NonGroundAttribute("enrollID", new Variable("E", false));
		Vector<Attribute> attr = new Vector<Attribute>();
		attr.add(auctionID);
		attr.add(enrollID);
		EventAtom atom = new EventAtom(new EventID(""), "bidderData", attr);
		
		// Transition B0 -> B1
		Label label = new Label(atom, new Vector<DoubleCompExp>());
		Transition B0_to_B1 = new Transition(label, false, false, false, this);	
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom false(auctionID(A),enrollID(E))
		NonGroundAttribute auctionID1 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute enrollID1 = new NonGroundAttribute("enrollID", new Variable("E", false));
		Vector<Attribute> attr1 = new Vector<Attribute>();
		attr1.add(auctionID1);
		attr1.add(enrollID1);
		EventAtom atom1 = new EventAtom(new EventID(""), "false", attr1);
				
		// Transition B1 -> B0
		Label label1 = new Label(atom1, new Vector<DoubleCompExp>());
		Transition B1_to_B0_false = new Transition(label1, false, false, false, this);
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom authData(auctionID(A),enrollID(E))
		NonGroundAttribute auctionID2 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute enrollID2 = new NonGroundAttribute("enrollID", new Variable("E", false));
		Vector<Attribute> attr2 = new Vector<Attribute>();
		attr2.add(auctionID2);
		attr2.add(enrollID2);
		EventAtom atom2 = new EventAtom(new EventID(""), "authData", attr2);		
				
		// Transition B1 -> B0
		Label label2 = new Label(atom2, new Vector<DoubleCompExp>());
		Transition B1_to_B0_authData = new Transition(label2, false, false, false, this);
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom authAttempt(auctionID(A),enrollID(E))
		NonGroundAttribute auctionID3 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute enrollID3 = new NonGroundAttribute("enrollID", new Variable("E", false));
		Vector<Attribute> attr3 = new Vector<Attribute>();
		attr3.add(auctionID3);
		attr3.add(enrollID3);
		EventAtom atom3 = new EventAtom(new EventID(""), "authAttempt", attr3);
				
		// Transition B0 -> B2
		Label label3 = new Label(atom3, new Vector<DoubleCompExp>());
		Transition B0_to_B2 = new Transition(label3, false, false, false, this);
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom false(auctionID(A),enrollID(E))
		NonGroundAttribute auctionID4 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute enrollID4 = new NonGroundAttribute("enrollID", new Variable("E", false));
		Vector<Attribute> attr4 = new Vector<Attribute>();
		attr4.add(auctionID4);
		attr4.add(enrollID4);
		EventAtom atom4 = new EventAtom(new EventID(""), "false", attr4);		
				
		// Transition B2 -> B0
		Label label4 = new Label(atom4, new Vector<DoubleCompExp>());
		Transition B2_to_B0 = new Transition(label4, false, false, false, this);
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom bidderEnrolled(auctionID(A),enrollID(E))
		NonGroundAttribute auctionID5 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute enrollID5 = new NonGroundAttribute("enrollID", new Variable("E", false));
		Vector<Attribute> attr5 = new Vector<Attribute>();
		attr5.add(auctionID5);
		attr5.add(enrollID5);
		EventAtom atom5 = new EventAtom(new EventID(""), "bidderEnrolled", attr5);
				
		// Transition B2 -> B3
		Label label5 = new Label(atom5, new Vector<DoubleCompExp>());
		Transition B2_to_B3 = new Transition(label5, false, false, false, this);
				
		/* -------------------------------------------------------------------------------------- */
		
		// Outgoing transitions B3 -> A1 and B3 -> I0 
		
		Transition B3_to_I0 = out.firstElement();
		Transition B3_to_A1 = out.elementAt(1);			
				
		/** ------------------------------------------------------------------------------------- 
	        ---                                 Atomic states                                 --- 
	        ------------------------------------------------------------------------------------- **/
		
		// State B0		
		StateAtom B0name = new StateAtom(new StateID(""), "B0", new Vector<Attribute>());
		Vector<Transition> B0trans = new Vector<Transition>();
		B0trans.add(B0_to_B1);
		B0trans.add(B0_to_B2);
		AtomicState B0 = new AtomicState(B0name, true, false, B0trans);
		
		// State B1
		StateAtom B1name = new StateAtom(new StateID(""), "B1", new Vector<Attribute>());
		Vector<Transition> B1trans = new Vector<Transition>();
		B1trans.add(B1_to_B0_authData);
		B1trans.add(B1_to_B0_false);
		AtomicState B1 = new AtomicState(B1name, false, false, B1trans);
		
		// State B2
		StateAtom B2name = new StateAtom(new StateID(""), "B2", new Vector<Attribute>());
		Vector<Transition> B2trans = new Vector<Transition>();
		B2trans.add(B2_to_B0);
		B2trans.add(B2_to_B3);
		AtomicState B2 = new AtomicState(B2name, false, false, B2trans);
		
		// State B3
		StateAtom B3name = new StateAtom(new StateID(""), "B3", new Vector<Attribute>());
		Vector<Transition> B3trans = new Vector<Transition>();
		B3trans.add(B3_to_A1);
		B3trans.add(B3_to_I0);
		AtomicState B3 = new AtomicState(B3name, false, true, B3trans);
		
		/** --------------------------------------------------------------------------------------
	        ---                 Set source and target states of the transitions                --- 
	        -------------------------------------------------------------------------------------- **/
		
		A0_to_B0.target = B0;
		
		BE_to_B0.source = this;
		BE_to_B0.target = B0;
		
		B0_to_B1.source = B0;
		B0_to_B1.target = B1;
		
		B0_to_B2.source = B0;
		B0_to_B2.target = B2;
		
		B1_to_B0_authData.source = B1;
		B1_to_B0_authData.target = B0;
		
		B1_to_B0_false.source = B1;
		B1_to_B0_false.target = B0;
		
		B2_to_B0.source = B2;
		B2_to_B0.target = B0;
		
		B2_to_B3.source = B2;
		B2_to_B3.target = B3;
		
		B3_to_A1.source = B3;
		
		B3_to_I0.source = B3;
		
		
		
		/** --------------------------------------------------------------------------------------- 
    	    ---                                Non-atomic states                                --- 
	        --------------------------------------------------------------------------------------- **/
		
		// State b:BidderEnrollment(auctionID(A),enrollID(E))					
		Vector<Attribute> attr6 = new Vector<Attribute>();
		NonGroundAttribute auctionID6 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute enrollID6 = new NonGroundAttribute("enrollID", new Variable("E", false));
		attr6.add(auctionID6);
		attr6.add(enrollID6);
		this.name = new StateAtom(new StateID("b"), "BidderEnrollment", attr6);
		this.start = false;
		this.end = false;
		
		Vector<State> children = new Vector<State>();
		children.add(B0);
		children.add(B1);
		children.add(B2);
		children.add(B3);
		this.children = children;
		
		this.ingoingTransitions = in;
		this.outgoingTransitions = out;
		
		/** --------------------------------------------------------------------------------------
	        ---                              Set parents of the states                             --- 
	        -------------------------------------------------------------------------------------- **/
		
		B0.parent = this;
		B1.parent = this;
		B2.parent = this;
		B3.parent = this;
	}
}

