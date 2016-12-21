package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;

public class EventIDSubst extends Substitution {
	public Event value;
	
	public EventIDSubst(EventID i, Event e) {
		super(i);
		this.value = e;
	}
	
	public DoubleConstant getConstant(String name) {
		return null;
	}
	
	public Event getEvent(String name) {
		if (name.equals(this.symbol.name)) {
			return this.value;
		}
		return null;
	}
	
	public Vector<StateAtom> getAtoms(String name) {
		return null;
	}
	
	public GroundAttribute substitute(GroundAttribute a) { 	
		return a;
	}
	
	public NonGroundAttribute substitute(NonGroundAttribute a) {  	
		return a;
	}
	
	public Substitution resolveConflict(Substitution s) {
		return s;
	}

	public String toString() {
		return this.symbol.toString() + "->" + this.value.toString() + " ";
	}
}
