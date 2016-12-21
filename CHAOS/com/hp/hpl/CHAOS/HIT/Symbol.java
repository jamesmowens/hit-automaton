package com.hp.hpl.CHAOS.HIT;

public class Symbol {
	public String name;
	
	public Symbol(String n) {
		this.name = n;
	}
	
	public boolean equals(Symbol i) {
		return this.name.equals(i.name);
	}
	
	public String toString() {
		return this.name;
	}
	
}
