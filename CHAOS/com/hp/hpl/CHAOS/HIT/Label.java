package com.hp.hpl.CHAOS.HIT;

import java.util.*;

import com.hp.hpl.CHAOS.Expression.DoubleCompExp;

public class Label {
	EventAtom atom;
	Vector<DoubleCompExp> constraints;
	
	public Label(EventAtom a, Vector<DoubleCompExp> c) {
		this.atom = a;
		this.constraints = c;
	}
	
	public boolean isSatisfied(Vector<StateInstance> config, Vector<Substitution> sub, Event e) {	
		if (this.atom.subsumes(e,sub)) {	
			for (DoubleCompExp c : this.constraints) {
				DoubleCompExp x = c.applySubstitutions(config,sub,e);				
				if (!x.eval()) { return false; } 
			}
		} else {
			return false;
		}
		return true;
	}

	public String toString() {
		String s = "";
		if (this.atom != null) {
			s = s.concat(this.atom.toString());
		} 
		for (DoubleCompExp c : this.constraints) {
			s = s.concat(", " + c.toString());
		}
		return s;
	}
}
