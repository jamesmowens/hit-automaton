package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;

public class EventAtom extends Atom {
	EventID id;
	
	public EventAtom (EventID i, String t, Vector<Attribute> a) {
		//super(i,t,a);
		this.id = i;
		this.type = t;
		this.attributes = a;		
	}
	
	public boolean subsumes(Event event, Vector<Substitution> sub) {
		if (this.type.equals(event.type)) {
			for (Attribute attr : this.attributes) {
				int i = this.attributes.indexOf(attr);
				//System.out.println(attr);
				if (!attr.applySubstitutions(sub).subsumes(event.attributes.get(i),sub)) {
					return false;
			}}		
			return true;
		} else {
			return false;
		}		
	}
	
	public Vector<Substitution> getVariableSubst(Event e) {
		Vector<Substitution> sub = new Vector<Substitution>();
		for (Attribute qattr : this.attributes) {
			for (GroundAttribute eattr : e.attributes) {
				if (qattr.subsumes(eattr, new Vector<Substitution>())) {
					sub.add(qattr.getSubstitutions(eattr));
		}}}
		return sub;
	}
	
	public Vector<Substitution> getSubstitutions(Event e) {
		Vector<Substitution> sub = new Vector<Substitution>();	
		EventIDSubst s1 = new EventIDSubst(new EventID("last"), e); 
		sub.add(s1);
		if (!this.id.name.equals("")) {
			EventIDSubst s2 = new EventIDSubst(this.id, e);
			sub.add(s2);
		}		
		sub.addAll(this.getVariableSubst(e));
		return sub;
	}
	
	public boolean equals(EventAtom a) {
		if (this.id.name.equals(a.id.name) && this.type.equals(a.type)) {
			for (Attribute attr : this.attributes) {
				int i = this.attributes.indexOf(attr);
				if (!attr.equals(a.attributes.get(i))) {
					return false;
				}				
			}
			return true;
		} else {
			return false;
	}}
	
	public String toString() {
		String s = "";
		if (!this.id.name.equals("")) {
			s = s.concat(this.id.toString() + ":");
		}		
		s = s.concat(this.type);		
		if (!this.attributes.isEmpty()) {
			s = s.concat("( ");
			for (Attribute attr : this.attributes) {
				s = s.concat(attr.toString() + " ");
			}
			s = s.concat(")");
		}
		return s;
	}
}
