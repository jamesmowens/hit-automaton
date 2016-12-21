package com.hp.hpl.CHAOS.Expression;

import java.util.Vector;
import com.hp.hpl.CHAOS.HIT.Event;
import com.hp.hpl.CHAOS.HIT.EventID;
import com.hp.hpl.CHAOS.HIT.StateInstance;
import com.hp.hpl.CHAOS.HIT.Substitution;

public class DoubleRef2Event extends DoubleReference {
	private static final long serialVersionUID = 1L;
	
	public DoubleRef2Event(DoubleRetExp child) {
		super(null);	
	}
	
	public DoubleRef2Event(EventID i, String n) {
		super(i,n);
	}
	
	public DoubleConstant applySubstitutions(Vector<StateInstance> config, Vector<Substitution> sub, Event event) {
		// find the event e referenced by this.id
		Event e = null;
		if (this.id.name.equals("this")) {
			e = event;
		} else {
			for (Substitution s : sub) {
				//System.out.println("s: " + s.toString());
				//System.out.println("event id: " + this.id.name.toString());
				e = s.getEvent(this.id.name);
				if (e!=null) {
					System.out.println("e: " + e.toString());
					break; 
		}}}
		// find the value of the attribute this.name carried by e
		if (e!=null) {
			//System.out.println(e.toString());
			//System.out.println("--------------------------------------------------------------");
			return e.getConstant(this.name);
		}	
		return null;
	}
}
