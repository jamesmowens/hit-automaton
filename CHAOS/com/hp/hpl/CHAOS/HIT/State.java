package com.hp.hpl.CHAOS.HIT;

import java.util.*;

public class State {
	StateAtom name;
	boolean start;
	boolean end;
	Vector<Transition> outgoingTransitions;
	NonAtomicState parent;
	
	public State(StateAtom n, boolean s, boolean e, Vector<Transition> o) {
		this.name = n;		
		this.start = s;
		this.end = e;
		this.outgoingTransitions = o;
	}
	
	public State() { }
	
	public NonAtomicState getRoot() {
		State s = this;
		while (s.parent != null) {
			s = s.parent;
		}
		return (NonAtomicState) s; // !!!
 	}	
	
	public Vector<State> getSuperstates() {
		State s = this;
		Vector<State> result = new Vector<State>();
		while (s != null) {
			result.add(s.parent);
			s = s.parent;
		}
		return result;
	}	
	
	public State getAncestorOrSelf(Vector<State> states) {
		if (states.contains(this)) {
			return this;
		} else {
			Vector<State> superstates = this.getSuperstates();
			for (State s : superstates) {
				if (states.contains(s)) {
					return s;
		}}}
		return null;		
	}
	
	public boolean equals(State s) {
		return this.name.equals(s.name);
	}
	
	public String toString() {
		return this.name.toString();
	}
}

