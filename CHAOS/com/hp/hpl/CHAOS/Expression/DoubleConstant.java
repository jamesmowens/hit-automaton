package com.hp.hpl.CHAOS.Expression;

import java.util.Vector;

import com.hp.hpl.CHAOS.HIT.AttributeValue;
import com.hp.hpl.CHAOS.HIT.Event;
import com.hp.hpl.CHAOS.HIT.StateInstance;
import com.hp.hpl.CHAOS.HIT.Substitution;

public class DoubleConstant extends UniDoubleRetExp implements AttributeValue {
	private static final long serialVersionUID = 1L;
	public double value;
	
	public DoubleConstant(DoubleRetExp child) {
		super(null);
		this.value = 0.0;
	}

	public DoubleConstant(double value) {
		super(null);
		this.value = value;
	}
	
	public double getValue() {
		return value;
	}
	
	public void setValue(double value) {
		this.value = value;
	}
	
	public double eval() {
		return this.value;
	}
	
	public DoubleConstant applySubstitutions(Vector<StateInstance> config, Vector<Substitution> sub, Event e) {
		return this;
	}
	
	public boolean equals(DoubleConstant c) {
		return this.value == c.value;
	}
		
	public String toString() {
		return "" + this.value;
	}
}
