package com.hp.hpl.CHAOS.HIT;

public class Variable extends Symbol implements AttributeValue {
	boolean flagged;

	public Variable(String n, boolean f) {
		super(n);
		this.flagged = f;
	}
	
	public String toString() {
		String s;
		if (this.flagged) {
			s = "*";
		} else {
			s = "";
		}
		return this.name + s;
	}
}
