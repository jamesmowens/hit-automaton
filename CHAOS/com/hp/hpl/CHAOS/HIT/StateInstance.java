package com.hp.hpl.CHAOS.HIT;

import java.util.*;

public class StateInstance {
	NonAtomicState state;
	StateAtom name;
	double start;
	double end;
	State activeState;
	Vector<Substitution> substitutions;
	StateInstance parent;
	Vector<StateInstance> children;	
	
	public StateInstance(NonAtomicState s, StateAtom n, double sts, double ets, State a, Vector<Substitution> sub, StateInstance p, Vector<StateInstance> c) {
		this.state = s;
		this.name = n;
		this.start = sts;
		this.end = ets;
		this.activeState = a;
		this.substitutions = sub;
		this.parent = p;
		this.children = c;
	}
	
	public void mergeSubstitutions(Vector<Substitution> sub) {	
		Vector<Substitution> winners = new Vector<Substitution>();
		Vector<Substitution> loosers = new Vector<Substitution>();
		Vector<Substitution> new_ones = new Vector<Substitution>();
		for(Substitution new_sub : sub) {
			if(!new_sub.isPresentIn(this.substitutions)) {
				new_ones.add(new_sub);
			} else {
				Substitution old_sub = new_sub.getSubst4SameSymbol(this.substitutions);
				loosers.add(old_sub);
				Substitution winner = old_sub.resolveConflict(new_sub);
				winners.add(winner);
		}}
		this.substitutions.removeAll(loosers);
		this.substitutions.addAll(winners);
		this.substitutions.addAll(new_ones);
	}
	
	public Vector<StateInstance> getDescendants() {
		Vector<StateInstance> descendants = new Vector<StateInstance>(); 
		for(StateInstance c : this.children) {
			descendants.add(c);
			descendants.addAll(c.getDescendants());
		}
		return descendants;
	}
	
	public void updateLastInAncestors(Event e) {
		Vector<Substitution> sub = new Vector<Substitution>();
		EventIDSubst s = new EventIDSubst(new EventID("last"), e);
		sub.add(s);
		StateInstance p = this.parent;
		while (p != null) {
			p.mergeSubstitutions(sub);
			p = p.parent;
	}}
	
	public String toString() {
		String s = "Process with \n" +
				   "name = " + this.name.toString() + "\n" +
	             //  "time = [ " + this.start + "," + this.end + " ]" + "\n" +
				   "activeState = " + this.activeState.toString() + "\n" + 
				   "substitutions = {";
		for(Substitution sigma : this.substitutions) {
			s = s.concat(sigma.toString());
		}
		s = s.concat("}" + "\n");
		if (this.parent!=null)  
		s = s.concat("parent process = " + this.parent.name.toString() + "\n");
		return s;
		
	}	
}

