package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;
import com.hp.hpl.CHAOS.Expression.DoubleArithExp;
import com.hp.hpl.CHAOS.Expression.DoubleCompExp;
import com.hp.hpl.CHAOS.Expression.DoubleRef2Event;
import com.hp.hpl.CHAOS.Expression.DoubleRef2State;

public class AuctionAutomaton extends NonAtomicState {
	
	public AuctionAutomaton(StateAtom n, boolean s, boolean e, Vector<Transition> o) {
		super(n,s,e,o);
	}
	
	public AuctionAutomaton(Vector<Transition> in, Vector<Transition> out) {
		
		/** --------------------------------------------------------------------------------------- 
		    ---                                   Transitions                                   --- 
		    --------------------------------------------------------------------------------------- **/
		
		// Transition -> A0
		Transition to_A0 = in.firstElement();
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom enrollmentBegin(auctionID(A),enrollID(E*))				
		NonGroundAttribute auctionID = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute enrollID = new NonGroundAttribute("enrollID", new Variable("E", true));
		Vector<Attribute> attr = new Vector<Attribute>();
		attr.add(auctionID);
		attr.add(enrollID);
		EventAtom atom = new EventAtom(new EventID(""), "enrollmentBegin", attr);
		
		// DoubleCompExp this.end - a.end < 20 
		DoubleCompExp c = new DoubleCompExp(10, // <
									   		new DoubleArithExp(1, // -
															   new DoubleRef2Event(new EventID("this"), "end"),
															   new DoubleRef2Event(new EventID("a"), "end")),
											new DoubleConstant(20));
		Vector<DoubleCompExp> constr = new Vector<DoubleCompExp>();
		constr.add(c);
				
		// Transition A0 -> B0
		Label label = new Label(atom, constr);
		Transition A0_to_B0 = new Transition(label, false, true, false, this);	
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom enrollmentBegin(auctionID(A),enrollID(E*)) 
		NonGroundAttribute auctionID1 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute enrollID1 = new NonGroundAttribute("enrollID", new Variable("E", true));
		Vector<Attribute> attr1 = new Vector<Attribute>();
		attr1.add(auctionID1);
		attr1.add(enrollID1);
		EventAtom atom1 = new EventAtom(new EventID(""), "enrollmentBegin", attr1);
		
		// DoubleCompExp this.end - a.end < 20
		DoubleCompExp c1 = new DoubleCompExp(10, // <
		   		new DoubleArithExp(1, // -
								   new DoubleRef2Event(new EventID("this"), "end"),
								   new DoubleRef2Event(new EventID("a"), "end")),
				new DoubleConstant(20));
		Vector<DoubleCompExp> constr1 = new Vector<DoubleCompExp>();
		constr1.add(c1);
		
		// Transition b:BidderEnrollment(enrollID(E)) -> B0
		Label label1 = new Label(atom1, constr1);
		Transition BE_to_B0 = new Transition(label1, false, true, false, this);	
			
		/* -------------------------------------------------------------------------------------- */
		
		// Atom itemDescription(auctionID(A),itemID(I*),price(P*))				
		NonGroundAttribute auctionID2 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute itemID2 = new NonGroundAttribute("itemID", new Variable("I", true));
		NonGroundAttribute price2 = new NonGroundAttribute("price", new Variable("P", true));
		Vector<Attribute> attr2 = new Vector<Attribute>();
		attr2.add(auctionID2);
		attr2.add(itemID2);
		attr2.add(price2);
		EventAtom atom2 = new EventAtom(new EventID(""), "itemDescription", attr2);
		
		// DoubleCompExp 2 <= this.end - a.end
		DoubleCompExp c2 = new DoubleCompExp(13, // <=
									   		 new DoubleConstant(2),
											 new DoubleArithExp(1, // -
															    new DoubleRef2Event(new EventID("this"), "end"),
															    new DoubleRef2Event(new EventID("a"), "end")));
		// b.countFinished >= 2
		DoubleCompExp c3 = new DoubleCompExp(12, // >=
		   								     new DoubleRef2State(new StateID("b"), "countFinished"),
		   								     new DoubleConstant(2));
		
		Vector<DoubleCompExp> constr2 = new Vector<DoubleCompExp>();
		constr2.add(c2);
		constr2.add(c3);
				
		// Transition B3 -> I0
		Label label2 = new Label(atom2, constr2);
		Transition B3_to_I0 = new Transition(label2, true, true, false, this);		
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom itemDescription(auctionID(A),itemID(I*),price(P*))				
		NonGroundAttribute auctionID3 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute itemID3 = new NonGroundAttribute("itemID", new Variable("I", true));
		NonGroundAttribute price3 = new NonGroundAttribute("price", new Variable("P", true));
		Vector<Attribute> attr3 = new Vector<Attribute>();
		attr3.add(auctionID3);
		attr3.add(itemID3);
		attr3.add(price3);
		EventAtom atom3 = new EventAtom(new EventID(""), "itemDescription", attr3);
		
		// Transition I5 -> I0
		Label label3 = new Label(atom3, new Vector<DoubleCompExp>());
		Transition I5_to_I0 = new Transition(label3, true, true, false, this);
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom auctionEnd(auctionID(A))				
		NonGroundAttribute auctionID4 = new NonGroundAttribute("auctionID", new Variable("A", false));
		Vector<Attribute> attr4 = new Vector<Attribute>();
		attr4.add(auctionID4);
		EventAtom atom4 = new EventAtom(new EventID(""), "auctionEnd", attr4);
		
		// DoubleCompExp 2 <= this.end - a.end  
		DoubleCompExp c4 = new DoubleCompExp(13, // <=
											new DoubleConstant(2),
									   		new DoubleArithExp(1, // -
															   new DoubleRef2Event(new EventID("this"), "end"),
															   new DoubleRef2Event(new EventID("a"), "end")));
											
		Vector<DoubleCompExp> constr4 = new Vector<DoubleCompExp>();
		constr4.add(c4);
				
		// Transition A0 -> A1
		Label label4 = new Label(atom4, constr4);
		Transition A0_to_A1 = new Transition(label4, false, false, false, this);	
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom auctionEnd(auctionID(A))				
		NonGroundAttribute auctionID5 = new NonGroundAttribute("auctionID", new Variable("A", false));
		Vector<Attribute> attr5 = new Vector<Attribute>();
		attr5.add(auctionID5);
		EventAtom atom5 = new EventAtom(new EventID(""), "auctionEnd", attr5);
		
		// DoubleCompExp 2 <= this.end - a.end  
		DoubleCompExp c5 = new DoubleCompExp(13, // <=
											new DoubleConstant(2),
									   		new DoubleArithExp(1, // -
															   new DoubleRef2Event(new EventID("this"), "end"),
															   new DoubleRef2Event(new EventID("a"), "end")));
											
		Vector<DoubleCompExp> constr5 = new Vector<DoubleCompExp>();
		constr5.add(c5);
				
		// Transition B3 -> A1
		Label label5 = new Label(atom5, constr5);
		Transition B3_to_A1 = new Transition(label5, true, false, false, this);
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom auctionEnd(auctionID(A)) 		
		NonGroundAttribute auctionID6 = new NonGroundAttribute("auctionID", new Variable("A", false));
		Vector<Attribute> attr6 = new Vector<Attribute>();
		attr6.add(auctionID6);
		EventAtom atom6 = new EventAtom(new EventID(""), "auctionEnd", attr6);
										
		// Transition I5 -> A1
		Label label6 = new Label(atom6, new Vector<DoubleCompExp>());
		Transition I5_to_A1 = new Transition(label6, true, false, false, this);	
				
		/** ------------------------------------------------------------------------------------- 
		    ---                                 Atomic states                                 --- 
		    ------------------------------------------------------------------------------------- **/
				
		// State A0				
		StateAtom A0name = new StateAtom(new StateID(""), "A0", new Vector<Attribute>());		
		Vector<Transition> A0trans = new Vector<Transition>();
		A0trans.add(A0_to_B0);
		A0trans.add(A0_to_A1);
		AtomicState A0 = new AtomicState(A0name, true, false, A0trans);
		
		// State A1		
		StateAtom A1name = new StateAtom(new StateID(""), "A1", new Vector<Attribute>());
		AtomicState A1 = new AtomicState(A1name, false, true, new Vector<Transition>());
		
		/** --------------------------------------------------------------------------------------
		    ---                 Set source and target states of the transitions                --- 
		    -------------------------------------------------------------------------------------- **/
		
		to_A0.source = null;
		to_A0.target = A0;
		
		A0_to_B0.source = A0;
		
		A0_to_A1.source = A0;
		A0_to_A1.target = A1;
		
		B3_to_A1.target = A1;
		
		I5_to_A1.target = A1;
		
		/** --------------------------------------------------------------------------------------- 
		    ---                      Non-atomic states and their interfaces                     --- 
		    --------------------------------------------------------------------------------------- **/
		
		// State b:BidderEnrollment(enrollID(E))	
		Vector<Transition> in1 = new Vector<Transition>();
		in1.add(A0_to_B0);
		in1.add(BE_to_B0);
				
		Vector<Transition> out1 = new Vector<Transition>();
		out1.add(B3_to_I0);
		out1.add(B3_to_A1);	
		out1.add(BE_to_B0);
				
		BidderEnrollmentAutomaton bidderEnrollment = new BidderEnrollmentAutomaton(in1, out1);
		
		// State i:ItemOffer(itemID(I))	
		Vector<Transition> in2= new Vector<Transition>();
		in2.add(B3_to_I0);
		in2.add(I5_to_I0);
		
		Vector<Transition> out2 = new Vector<Transition>();
		out2.add(I5_to_A1);
		out2.add(I5_to_I0);
		
		ItemOfferAutomaton itemOffer = new ItemOfferAutomaton(in2, out2);
		
		// State j:Auction(auctionID(A))			
		NonGroundAttribute auctionID7 = new NonGroundAttribute("auctionID", new Variable("A", false));
		Vector<Attribute> attr7 = new Vector<Attribute>();
		attr7.add(auctionID7);
		this.name = new StateAtom(new StateID("j"), "Auction", attr7);
		this.start = true;
		this.end = true;
			
		Vector<State> children = new Vector<State>();
		children.add(A0);
		children.add(bidderEnrollment);
		children.add(itemOffer);
		children.add(A1);
		this.children = children;	
		
		this.ingoingTransitions = in;
		this.outgoingTransitions = out;
		
		/** --------------------------------------------------------------------------------------
	    ---                              Set parents of the states                             --- 
	    -------------------------------------------------------------------------------------- **/
		
		A0.parent = this;
		bidderEnrollment.parent = this;
		itemOffer.parent = this;
		A1.parent = this;
	}
}
