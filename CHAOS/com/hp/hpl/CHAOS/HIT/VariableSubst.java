package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;

public class VariableSubst extends Substitution {
	public DoubleConstant value;
	
	public VariableSubst(Variable v, DoubleConstant  c) {
		super(v);
		this.value = c;
	}
	
	public DoubleConstant getConstant(String name) {
		if (name.equals(this.symbol.name)) {
			return this.value;
		}
		return null;
	}
	public Event getEvent(String name) {
		return null;
	}
	
	public Vector<StateAtom> getAtoms(String name) {
		return null;
	}
	
	public GroundAttribute substitute(GroundAttribute a) { 	
		return a;
	}
	
	public Attribute substitute(NonGroundAttribute a) {
		if (!a.value.flagged && this.symbol.equals(a.value)) {			
			return new GroundAttribute(a.name, this.value);	 			
		}  	
		return a;
	}
	
	public Substitution resolveConflict(Substitution s) {
		return s;
	}

	public String toString() {
		return this.symbol.toString() + "->" + this.value.toString() + " ";
	}
}
