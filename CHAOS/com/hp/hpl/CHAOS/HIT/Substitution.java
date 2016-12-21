package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;

public abstract class Substitution {
	public Symbol symbol;
	
	public Substitution(Symbol s) {
		this.symbol = s;
	}

	public abstract DoubleConstant getConstant(String name);
	public abstract Event getEvent(String name);
	public abstract Vector<StateAtom> getAtoms(String name);
	
	public abstract GroundAttribute substitute(GroundAttribute a);
	public abstract Attribute substitute(NonGroundAttribute a);
	
	public abstract Substitution resolveConflict(Substitution s);
	
	public abstract String toString();
	
	public boolean isPresentIn(Vector<Substitution> sub) {
		for (Substitution s : sub) {
			if (this.symbol.equals(s.symbol)) {
				return true;
		}}
		return false;
	}
	
	public Substitution getSubst4SameSymbol(Vector<Substitution> sub) {
		for (Substitution s : sub) {
			if (this.symbol.equals(s.symbol)) {
				return s;
		}}
		return null;
	}
}