package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;

public class NonGroundAttribute extends Attribute {
	Variable value;
	
	public NonGroundAttribute (String n, Variable v) {
		this.name = n;
		this.value = v;
	}
	
	public boolean equals(GroundAttribute attr) {
		return false;
	}
	
	public boolean equals(NonGroundAttribute attr) {
		return this.name.equals(attr.name) && this.value.name.equals(attr.value.name);
	}
	
	public boolean subsumes(GroundAttribute attr, Vector<Substitution> sub){
		return this.name.equals(attr.name) && this.value.flagged;		
	}
	
	public VariableSubst getSubstitutions(GroundAttribute attr) { 
		return new VariableSubst(this.value, attr.value);
	}
	
	public Attribute applySubstitutions(Vector<Substitution> sub) {
		Attribute a = this;
		for (Substitution s : sub) {
			a = s.substitute(this);
			if (!a.equals(this)) {
				break;
		}}			
		return a;
	}	
	
	public String toString() {
		return this.name + "(" + this.value.toString() + ")";
	}	
}
