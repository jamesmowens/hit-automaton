package com.hp.hpl.CHAOS.Expression;

import com.hp.hpl.CHAOS.HIT.Identifier;

public abstract class DoubleReference extends UniDoubleRetExp {
	private static final long serialVersionUID = 1L;
	Identifier id;
	String name;	

	public DoubleReference(DoubleRetExp child) {
		super(null);
		this.id = new Identifier("");
		this.name = "";		
	}
	
	public DoubleReference(Identifier i, String n) {
		super(null);
		this.id = i;
		this.name = n;
	}
	
	public double eval() {
		return 0;
	}

	public String toString() {
		return this.id.toString() + "." + this.name;
	}
}
