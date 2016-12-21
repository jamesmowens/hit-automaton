package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;

public class GroundAttribute extends Attribute {
	DoubleConstant value;
	
	public GroundAttribute(String n, DoubleConstant v) {
		this.name = n;
		this.value = v;
	}
	
	public boolean equals(GroundAttribute attr) {
		return this.name.equals(attr.name) && this.value.equals(attr.value);
	}
	
	public boolean equals(NonGroundAttribute attr) {
		return false;
	}
	
	public boolean subsumes(GroundAttribute attr, Vector<Substitution> sub){
		return this.equals(attr);
	}
	
	public Substitution getSubstitutions(GroundAttribute attr) {
		return null;
	}
	
	public GroundAttribute applySubstitutions(Vector<Substitution> sub) {
		return this;
	}	
	
	public DoubleConstant getConstant(String name) {
		if (name.equals(this.name)) {
			return this.value;
		}
		return null;
	}
	public String toString() {
		return this.name + "(" + this.value.toString() + ")";
	}
}
