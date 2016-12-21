package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;

public class StateIDSubst extends Substitution {
	public Vector<StateAtom> value;
	
	public StateIDSubst(StateID i, Vector<StateAtom> v) {
		super(i);
		this.value = v;
	}
	
	public DoubleConstant getConstant(String name) {
		return null;
	}
	
	public Event getEvent(String name) {
		return null;
	}
	
	public Vector<StateAtom> getAtoms(String name) {		
		if (name.equals(this.symbol.name)) {
			return this.value;
		}
		return null;
	}
	
	public GroundAttribute substitute(GroundAttribute a) { 	
		return a;
	}
	
	public NonGroundAttribute substitute(NonGroundAttribute a) {	
		return a;
	}
	
	public Substitution resolveConflict(Substitution s) {
		Vector<StateAtom> new_value = new Vector<StateAtom>();
		new_value.addAll(this.value);
		new_value.addAll(((StateIDSubst)s).value); // !!!
		return new StateIDSubst(new StateID(this.symbol.name), new_value); 
	}
	
	public String toString() {
		String s = this.symbol.toString() + "->";
		for(Atom a : this.value) {
			s = s.concat(a.toString()) + " ";
		}
		return s;		
	}
}
