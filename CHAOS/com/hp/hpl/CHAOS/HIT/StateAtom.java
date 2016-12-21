package com.hp.hpl.CHAOS.HIT;

import java.util.Vector;
import com.hp.hpl.CHAOS.Expression.DoubleConstant;

public class StateAtom extends Atom {
	StateID id;
	
	public StateAtom (StateID i, String t, Vector<Attribute> a) {
		//super(i,t,a);
		this.id = i;
		this.type = t;
		this.attributes = a;
	}
	
	public StateAtom skipID() {
		return new StateAtom(new StateID(""), this.type, this.attributes);
	}
	
	public StateIDSubst getSubstitutions(StateAtom a) {
		if (!this.id.name.equals("")) {
			Vector<StateAtom> atoms = new Vector<StateAtom>();
			atoms.add(a);
			return new StateIDSubst(this.id, atoms);
		}		
		return null;
	}
	
	public StateAtom applySubstitutions(Vector<Substitution> sub) {
		Vector<Attribute> new_attributes = new Vector<Attribute>();
		for(Attribute old_attr : this.attributes) {			
			Attribute new_attr = old_attr.applySubstitutions(sub);
			new_attributes.add(new_attr);
		}
		return new StateAtom(this.id, this.type, new_attributes);
	}
	
	public DoubleConstant getConstant(Vector<StateInstance> config, String name) {
		for (StateInstance i : config) {
			if (this.equals(i.name)) {
				// get the value of a time attribute
				if (name.equals("start")) {
					return new DoubleConstant(i.start);
				} else {
				if (name.equals("end")) {
					return new DoubleConstant(i.end);
				} else {
				// get the binding of a variable
				for (Substitution s : i.substitutions) {
					DoubleConstant c = s.getConstant(name);
					if (c!=null) { 
						return c;
		}}}}}}
		return null;		
	}
	
	public boolean isFinished(Vector<StateInstance> config) {
		for (StateInstance i : config) {
			if (this.equals(i.name) && i.activeState.end) {
				return true;
		}}
		return false;
	}
	
	public boolean equals(StateAtom a) {
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
