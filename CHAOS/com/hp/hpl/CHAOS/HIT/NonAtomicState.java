package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;

public class NonAtomicState extends State {
	Vector<Transition> ingoingTransitions;
	Vector<State> children;

	public NonAtomicState(StateAtom n, boolean s, boolean e, Vector<Transition> o) {
		super(n,s,e,o);
	}
	
	public NonAtomicState() { }
	
	public NonAtomicState(Vector<Transition> in, Vector<Transition> out) { 
		this.ingoingTransitions = in; 
		this.outgoingTransitions = out;
	}
}
