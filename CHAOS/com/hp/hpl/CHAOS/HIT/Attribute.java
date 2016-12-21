package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;

public abstract class Attribute {
	String name;
	
	public abstract boolean equals(GroundAttribute a);
	public abstract boolean equals(NonGroundAttribute a);
	
	public abstract boolean subsumes(GroundAttribute attr, Vector<Substitution> sub);
	
	public abstract Substitution getSubstitutions(GroundAttribute attr);

	public abstract Attribute applySubstitutions(Vector<Substitution> sub);	
	
	public abstract String toString();
}

