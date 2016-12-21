package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;
import com.hp.hpl.CHAOS.Expression.DoubleCompExp;
import com.hp.hpl.CHAOS.Expression.DoubleRef2Event;
import com.hp.hpl.CHAOS.Expression.DoubleRef2State;

public class ItemOfferAutomaton extends NonAtomicState {
	
	public ItemOfferAutomaton(StateAtom n, boolean s, boolean e, Vector<Transition> o) {
		super(n,s,e,o);
	}

	public ItemOfferAutomaton(Vector<Transition> in, Vector<Transition> out) {
		
		/** --------------------------------------------------------------------------------------- 
	        ---                                   Transitions                                   --- 
	        --------------------------------------------------------------------------------------- **/
		
		// Ingoing transition A0 -> I0
		Transition A0_to_I0 = in.firstElement();		
		
		// In- and outgoing transition I5_to_I0
		Transition I5_to_I0 = in.lastElement();
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom bid(auctionID(A),itemID(I),price(P*))
		NonGroundAttribute auctionID3 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute itemID3 = new NonGroundAttribute("itemID", new Variable("I", false));
		NonGroundAttribute price3 = new NonGroundAttribute("price", new Variable("P", true));
		Vector<Attribute> attr3 = new Vector<Attribute>();
		attr3.add(auctionID3);
		attr3.add(itemID3);
		attr3.add(price3);
		EventAtom atom3 = new EventAtom(new EventID(""), "bid", attr3);
		
		// DoubleCompExp i.P < this.price
		DoubleCompExp c3 = new DoubleCompExp(10, // <
		   		 						     new DoubleRef2State(new StateID("i"), "P"),
		   		 						     new DoubleRef2Event(new EventID("this"), "price"));
		Vector<DoubleCompExp> constr3 = new Vector<DoubleCompExp>();
		constr3.add(c3);
		
		// Transition I0 -> I1
		Label label3 = new Label(atom3, constr3);
		Transition I0_to_I1 = new Transition(label3, false, false, false, this);	
		
		/* -------------------------------------------------------------------------------------- */

		/* // DoubleCompExp 50 <= now - last.end                                               // now !!!
		DoubleCompExp c4 = new DoubleCompExp(13, // <=
		   		 new DoubleConstant(50),
				 new DoubleArithExp(1, // -
						 			new DoubleConstant(4),                                        
								    new DoubleReference(new Identifier("last"), "end")));
		Vector<DoubleCompExp> constr4 = new Vector<DoubleCompExp>();
		constr4.add(c4);		
		
		// Transition I0 -> I5
		Label label4 = new Label(null, constr4);
		Transition I0_to_I5 = new Transition(label4, false, false, this); */	
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom bid(auctionID(A),itemID(I),price(P*))
		NonGroundAttribute auctionID5 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute itemID5 = new NonGroundAttribute("itemID", new Variable("I", false));
		NonGroundAttribute price5 = new NonGroundAttribute("price", new Variable("P", true));
		Vector<Attribute> attr5 = new Vector<Attribute>();
		attr5.add(auctionID5);
		attr5.add(itemID5);
		attr5.add(price5);
		EventAtom atom5 = new EventAtom(new EventID(""), "bid", attr5);
				
		// DoubleCompExp i.P < this.price
		DoubleCompExp c5 = new DoubleCompExp(10, // <
		   		 						     new DoubleRef2State(new StateID("i"), "P"),
		   		 						     new DoubleRef2Event(new EventID("this"), "price"));
		Vector<DoubleCompExp> constr5 = new Vector<DoubleCompExp>();
		constr5.add(c5);
				
		// Transition I1 -> I1
		Label label5 = new Label(atom5, constr5);
		Transition I1_to_I1 = new Transition(label5, false, false, false, this);
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom hammerBeat(auctionID(A),itemID(I))
		NonGroundAttribute auctionID6 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute itemID6 = new NonGroundAttribute("itemID", new Variable("I", false));
		Vector<Attribute> attr6 = new Vector<Attribute>();
		attr6.add(auctionID6);
		attr6.add(itemID6);
		EventAtom atom6 = new EventAtom(new EventID(""), "hammerBeat", attr6);		
				
		// Transition I1 -> I2
		Label label6 = new Label(atom6, new Vector<DoubleCompExp>());
		Transition I1_to_I2 = new Transition(label6, false, false, false, this);
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom bid(auctionID(A),itemID(I),price(P*))
		NonGroundAttribute auctionID7 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute itemID7 = new NonGroundAttribute("itemID", new Variable("I", false));
		NonGroundAttribute price7 = new NonGroundAttribute("price", new Variable("P", true));
		Vector<Attribute> attr7 = new Vector<Attribute>();
		attr7.add(auctionID7);
		attr7.add(itemID7);
		attr7.add(price7);
		EventAtom atom7 = new EventAtom(new EventID(""), "bid", attr7);
				
		// DoubleCompExp i.P < this.price
		DoubleCompExp c7 = new DoubleCompExp(10, // <
		   		 						     new DoubleRef2State(new StateID("i"), "P"),
		   		 						     new DoubleRef2Event(new EventID("this"), "price"));
		Vector<DoubleCompExp> constr7 = new Vector<DoubleCompExp>();
		constr7.add(c7);
				
		// Transition I2 -> I1
		Label label7 = new Label(atom7, constr7);
		Transition I2_to_I1 = new Transition(label7, false, false, false, this);
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom hammerBeat(auctionID(A),itemID(I))
		NonGroundAttribute auctionID8 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute itemID8 = new NonGroundAttribute("itemID", new Variable("I", false));
		Vector<Attribute> attr8 = new Vector<Attribute>();
		attr8.add(auctionID8);
		attr8.add(itemID8);
		EventAtom atom8 = new EventAtom(new EventID(""), "hammerBeat", attr8);		
				
		// Transition I2 -> I3
		Label label8 = new Label(atom8, new Vector<DoubleCompExp>());
		Transition I2_to_I3 = new Transition(label8, false, false, false, this);
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom bid(auctionID(A),itemID(I),price(P*))
		NonGroundAttribute auctionID9 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute itemID9 = new NonGroundAttribute("itemID", new Variable("I", false));
		NonGroundAttribute price9 = new NonGroundAttribute("price", new Variable("P", true));
		Vector<Attribute> attr9 = new Vector<Attribute>();
		attr9.add(auctionID9);
		attr9.add(itemID9);
		attr9.add(price9);
		EventAtom atom9 = new EventAtom(new EventID(""), "bid", attr9);
				
		// DoubleCompExp i.P < this.price
		DoubleCompExp c9 = new DoubleCompExp(10, // <
				   		 					 new DoubleRef2State(new StateID("i"), "P"),
				   		 					 new DoubleRef2Event(new EventID("this"), "price"));
		Vector<DoubleCompExp> constr9 = new Vector<DoubleCompExp>();
		constr9.add(c9);
				
		// Transition I3 -> I1
		Label label9 = new Label(atom9, constr9);
		Transition I3_to_I1 = new Transition(label9, false, false, false, this);
				
		/* -------------------------------------------------------------------------------------- */
		
		// Atom hammerBeat(auctionID(A),itemID(I))
		NonGroundAttribute auctionID10 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute itemID10 = new NonGroundAttribute("itemID", new Variable("I", false));
		Vector<Attribute> attr10 = new Vector<Attribute>();
		attr10.add(auctionID10);
		attr10.add(itemID10);
		EventAtom atom10 = new EventAtom(new EventID(""), "hammerBeat", attr10);		
		
		// Transition I3 -> I4
		Label label10 = new Label(atom10, new Vector<DoubleCompExp>());
		Transition I3_to_I4 = new Transition(label10, false, false, false, this);
		
		/* -------------------------------------------------------------------------------------- */
		
		// Atom sell(auctionID(A),itemID(I))
		NonGroundAttribute auctionID11 = new NonGroundAttribute("auctionID", new Variable("A", false));
		NonGroundAttribute itemID11 = new NonGroundAttribute("itemID", new Variable("I", false));
		Vector<Attribute> attr11 = new Vector<Attribute>();
		attr11.add(auctionID11);
		attr11.add(itemID11);
		EventAtom atom11 = new EventAtom(new EventID(""), "sell", attr11);
		
		// Transition I4 -> I5
		Label label11 = new Label(atom11, new Vector<DoubleCompExp>());
		Transition I4_to_I5 = new Transition(label11, false, false, false, this);
				
		/* -------------------------------------------------------------------------------------- */
		
		// Outgoing transition I5 -> A1
		Transition I5_to_A1 = out.firstElement();
				
		/** ------------------------------------------------------------------------------------- 
	        ---                                 Atomic states                                 --- 
	        ------------------------------------------------------------------------------------- **/
		
		// State I0		
		StateAtom I0name = new StateAtom(new StateID(""), "I0", new Vector<Attribute>());
		Vector<Transition> I0trans = new Vector<Transition>();
		I0trans.add(I0_to_I1);
		//I0trans.add(I0_to_I5);
		AtomicState I0 = new AtomicState(I0name, true, false, I0trans);
		
		// State I1
		StateAtom I1name = new StateAtom(new StateID(""), "I1", new Vector<Attribute>());
		Vector<Transition> I1trans = new Vector<Transition>();
		I1trans.add(I1_to_I1);
		I1trans.add(I1_to_I2);
		AtomicState I1 = new AtomicState(I1name, false, false, I1trans);
		
		// State I2
		StateAtom I2name = new StateAtom(new StateID(""), "I2", new Vector<Attribute>());
		Vector<Transition> I2trans = new Vector<Transition>();
		I2trans.add(I2_to_I1);
		I2trans.add(I2_to_I3);
		AtomicState I2 = new AtomicState(I2name, false, false, I2trans);
		
		// State I3
		StateAtom I3name = new StateAtom(new StateID(""), "I3", new Vector<Attribute>());
		Vector<Transition> I3trans = new Vector<Transition>();
		I3trans.add(I3_to_I1);
		I3trans.add(I3_to_I4);
		AtomicState I3 = new AtomicState(I3name, false, false, I3trans);
		
		// State I4
		StateAtom I4name = new StateAtom(new StateID(""), "I4", new Vector<Attribute>());
		Vector<Transition> I4trans = new Vector<Transition>();
		I4trans.add(I4_to_I5);
		AtomicState I4 = new AtomicState(I4name, false, false, I4trans);
		
		// State I5
		StateAtom I5name = new StateAtom(new StateID(""), "I5", new Vector<Attribute>());
		Vector<Transition> I5trans = new Vector<Transition>();
		I5trans.add(I5_to_I0);
		I5trans.add(I5_to_A1);
		AtomicState I5 = new AtomicState(I5name, false, true, I5trans);
		
		/** --------------------------------------------------------------------------------------
	        ---                 Set source and target states of the transitions                --- 
	        -------------------------------------------------------------------------------------- **/

		A0_to_I0.target = I0;
		
		I0_to_I1.source = I0;
		I0_to_I1.target = I1;
		
		/* I0_to_I5.source = I0;
		   I0_to_I5.target = I5; */
		
		I1_to_I1.source = I1;
		I1_to_I1.target = I1;
		
		I1_to_I2.source = I1;
		I1_to_I2.target = I2;
		
		I2_to_I1.source = I2;
		I2_to_I1.target = I1;
		
		I2_to_I3.source = I2;
		I2_to_I3.target = I3;
		
		I3_to_I1.source = I3;
		I3_to_I1.target = I1;
		
		I3_to_I4.source = I3;
		I3_to_I4.target = I4;
		
		I4_to_I5.source = I4;
		I4_to_I5.target = I5;
		
		I5_to_I0.source = I5;
		I5_to_I0.target = I0;
		
		I5_to_A1.source = I5;
		
		/** --------------------------------------------------------------------------------------- 
	        ---                                Non-atomic states                                --- 
	        --------------------------------------------------------------------------------------- **/
		
		// State i:ItemOffer(itemID(I))					
		Vector<Attribute> attr14 = new Vector<Attribute>();
		NonGroundAttribute itemID14 = new NonGroundAttribute("itemID", new Variable("I", false));
		attr14.add(itemID14);
		this.name = new StateAtom(new StateID("i"), "ItemOffer", attr14);
		this.start = false;
		this.end = false;
		
		Vector<State> children = new Vector<State>();
		children.add(I0);
		children.add(I1);
		children.add(I2);
		children.add(I3);
		children.add(I4);
		children.add(I5);
		this.children = children;
		
		this.ingoingTransitions = in;
		this.outgoingTransitions = out;
		
		/** --------------------------------------------------------------------------------------
	        ---                           Set parents of the states                            --- 
	        -------------------------------------------------------------------------------------- **/
		
		I0.parent = this;
		I1.parent = this;
		I2.parent = this;
		I3.parent = this;
		I4.parent = this;
		I5.parent = this;
	}
}
